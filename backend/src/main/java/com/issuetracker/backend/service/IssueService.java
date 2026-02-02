package com.issuetracker.backend.service;

import com.issuetracker.backend.dto.*;
import com.issuetracker.backend.exception.ForbiddenException;
import com.issuetracker.backend.exception.ResourceNotFoundException;
import com.issuetracker.backend.model.*;
import com.issuetracker.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private SseService sseService;

    @Transactional
    public IssueDTO createIssue(CreateIssueRequest request) {
        User currentUser = getCurrentUser();
        
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
        
        // Check if user has access to the project
        checkUserHasProjectAccess(project);

        Issue issue = new Issue();
        issue.setProject(project);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(IssueStatus.OPEN);
        issue.setPriority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM);
        issue.setCreatedBy(currentUser);

        // Set assignee if provided
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            
            // Check if assignee has access to the project
            checkUserHasProjectAccessById(project, assignee.getId());
            
            issue.setAssignee(assignee);
        }

        Issue savedIssue = issueRepository.save(issue);

        // Log activity
        logActivity(savedIssue, currentUser, "Issue created", 
            "Created with status: " + savedIssue.getStatus() + ", priority: " + savedIssue.getPriority());

        IssueDTO issueDTO = convertToDTO(savedIssue);
        
        // Broadcast SSE event
        sseService.broadcastIssueUpdate(issueDTO, "issue-created");

        return issueDTO;
    }

    public Page<IssueDTO> getIssues(
            UUID projectId,
            IssueStatus status,
            IssuePriority priority,
            UUID assigneeId,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validate project access if projectId is specified
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            checkUserHasProjectAccess(project);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get filtered results (without text search in query)
        Page<Issue> issues = issueRepository.findByFilters(
            projectId, status, priority, assigneeId, null, pageable);

        // Apply text search filtering in memory if needed
        if (search != null && !search.isBlank()) {
            final String searchLower = search.toLowerCase();
            List<Issue> filtered = issues.getContent().stream()
                .filter(i -> i.getTitle().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
            
            // Note: This is a simplified approach. For production, consider using 
            // Specification API or full-text search for better performance
            return new PageImpl<>(
                filtered.stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                filtered.size()
            );
        }

        return issues.map(this::convertToDTO);
    }

    public IssueDTO getIssueById(UUID id) {
        Issue issue = findIssueById(id);
        checkUserHasProjectAccess(issue.getProject());
        return convertToDTO(issue);
    }

    @Transactional
    public IssueDTO updateIssue(UUID id, UpdateIssueRequest request) {
        Issue issue = findIssueById(id);
        checkUserHasProjectAccess(issue.getProject());
        
        User currentUser = getCurrentUser();
        StringBuilder activityDetails = new StringBuilder();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            if (!issue.getTitle().equals(request.getTitle())) {
                activityDetails.append("Title changed from '").append(issue.getTitle())
                    .append("' to '").append(request.getTitle()).append("'. ");
            }
            issue.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            if (!request.getDescription().equals(issue.getDescription())) {
                activityDetails.append("Description updated. ");
            }
            issue.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            if (!issue.getStatus().equals(request.getStatus())) {
                activityDetails.append("Status changed from ").append(issue.getStatus())
                    .append(" to ").append(request.getStatus()).append(". ");
            }
            issue.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            if (!issue.getPriority().equals(request.getPriority())) {
                activityDetails.append("Priority changed from ").append(issue.getPriority())
                    .append(" to ").append(request.getPriority()).append(". ");
            }
            issue.setPriority(request.getPriority());
        }

        if (request.getAssigneeId() != null) {
            User newAssignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            
            checkUserHasProjectAccessById(issue.getProject(), newAssignee.getId());
            
            String oldAssignee = issue.getAssignee() != null ? issue.getAssignee().getFullName() : "Unassigned";
            if (issue.getAssignee() == null || !issue.getAssignee().getId().equals(newAssignee.getId())) {
                activityDetails.append("Assignee changed from ").append(oldAssignee)
                    .append(" to ").append(newAssignee.getFullName()).append(". ");
            }
            issue.setAssignee(newAssignee);
        }

        Issue updatedIssue = issueRepository.save(issue);

        // Log activity if there were changes
        if (activityDetails.length() > 0) {
            logActivity(updatedIssue, currentUser, "Issue updated", activityDetails.toString().trim());
        }

        IssueDTO issueDTO = convertToDTO(updatedIssue);
        
        // Broadcast SSE event if there were changes
        if (activityDetails.length() > 0) {
            sseService.broadcastIssueUpdate(issueDTO, "issue-updated");
        }

        return issueDTO;
    }

    @Transactional
    public void deleteIssue(UUID id) {
        Issue issue = findIssueById(id);
        Project project = issue.getProject();
        
        // Only project owner can delete issues
        checkUserIsProjectOwner(project);
        
        issueRepository.delete(issue);
    }

    @Transactional
    public CommentDTO addComment(UUID issueId, CreateCommentRequest request) {
        Issue issue = findIssueById(issueId);
        checkUserHasProjectAccess(issue.getProject());
        
        User currentUser = getCurrentUser();

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());

        Comment savedComment = commentRepository.save(comment);

        // Log activity
        logActivity(issue, currentUser, "Comment added", 
            "Comment: " + (request.getContent().length() > 50 
                ? request.getContent().substring(0, 50) + "..." 
                : request.getContent()));

        // Broadcast SSE event for issue update (comment count changed)
        IssueDTO issueDTO = convertToDTO(issue);
        sseService.broadcastIssueUpdate(issueDTO, "issue-commented");

        return convertCommentToDTO(savedComment);
    }

    public List<CommentDTO> getIssueComments(UUID issueId) {
        Issue issue = findIssueById(issueId);
        checkUserHasProjectAccess(issue.getProject());

        List<Comment> comments = commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
        return comments.stream().map(this::convertCommentToDTO).collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getIssueActivity(UUID issueId) {
        Issue issue = findIssueById(issueId);
        checkUserHasProjectAccess(issue.getProject());

        List<ActivityLog> activities = activityLogRepository.findByIssueIdOrderByCreatedAtDesc(issueId);
        return activities.stream().map(this::convertActivityToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO updateComment(UUID issueId, UUID commentId, UpdateCommentRequest request) {
        Issue issue = findIssueById(issueId);
        checkUserHasProjectAccess(issue.getProject());
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // Verify comment belongs to the issue
        if (!comment.getIssue().getId().equals(issueId)) {
            throw new ForbiddenException("Comment does not belong to this issue");
        }
        
        // Only comment author can update
        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }
        
        String oldContent = comment.getContent();
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        
        // Log activity
        logActivity(issue, currentUser, "Comment edited", 
            "Comment edited (ID: " + commentId + ")");
        
        // Broadcast SSE event
        IssueDTO issueDTO = convertToDTO(issue);
        sseService.broadcastIssueUpdate(issueDTO, "comment-updated");
        
        return convertCommentToDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(UUID issueId, UUID commentId) {
        Issue issue = findIssueById(issueId);
        checkUserHasProjectAccess(issue.getProject());
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // Verify comment belongs to the issue
        if (!comment.getIssue().getId().equals(issueId)) {
            throw new ForbiddenException("Comment does not belong to this issue");
        }
        
        // Only comment author can delete
        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
        
        // Log activity
        logActivity(issue, currentUser, "Comment deleted", 
            "Comment deleted (ID: " + commentId + ")");
        
        // Broadcast SSE event
        IssueDTO issueDTO = convertToDTO(issue);
        sseService.broadcastIssueUpdate(issueDTO, "comment-deleted");
    }

    // Helper methods

    private Issue findIssueById(UUID id) {
        return issueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void checkUserHasProjectAccess(Project project) {
        User currentUser = getCurrentUser();
        checkUserHasProjectAccessById(project, currentUser.getId());
    }

    private void checkUserHasProjectAccessById(Project project, UUID userId) {
        // Owner always has access
        if (project.getOwner().getId().equals(userId)) {
            return;
        }

        // Check if user is a member
        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId)) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    private void checkUserIsProjectOwner(Project project) {
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can perform this action");
        }
    }

    private void logActivity(Issue issue, User user, String action, String details) {
        ActivityLog log = new ActivityLog();
        log.setIssue(issue);
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        activityLogRepository.save(log);
    }

    private IssueDTO convertToDTO(Issue issue) {
        IssueDTO dto = new IssueDTO();
        dto.setId(issue.getId());
        dto.setProjectId(issue.getProject().getId());
        dto.setProjectName(issue.getProject().getName());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setStatus(issue.getStatus());
        dto.setPriority(issue.getPriority());
        dto.setAssignee(issue.getAssignee() != null ? convertUserToDTO(issue.getAssignee()) : null);
        dto.setReporter(convertUserToDTO(issue.getCreatedBy()));
        dto.setProjectOwnerId(issue.getProject().getOwner().getId());
        dto.setCreatedAt(issue.getCreatedAt());
        dto.setUpdatedAt(issue.getUpdatedAt());
        dto.setCommentCount((int) commentRepository.countByIssueId(issue.getId()));
        return dto;
    }

    private CommentDTO convertCommentToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setIssueId(comment.getIssue().getId());
        dto.setUser(convertUserToDTO(comment.getUser()));
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    private ActivityLogDTO convertActivityToDTO(ActivityLog activity) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.setId(activity.getId());
        dto.setIssueId(activity.getIssue().getId());
        dto.setUser(convertUserToDTO(activity.getUser()));
        dto.setAction(activity.getAction());
        dto.setDetails(activity.getDetails());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
