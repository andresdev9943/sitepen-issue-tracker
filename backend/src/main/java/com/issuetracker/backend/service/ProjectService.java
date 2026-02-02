package com.issuetracker.backend.service;

import com.issuetracker.backend.dto.*;
import com.issuetracker.backend.exception.ForbiddenException;
import com.issuetracker.backend.exception.ResourceNotFoundException;
import com.issuetracker.backend.model.Project;
import com.issuetracker.backend.model.ProjectMember;
import com.issuetracker.backend.model.ProjectRole;
import com.issuetracker.backend.model.User;
import com.issuetracker.backend.repository.ProjectMemberRepository;
import com.issuetracker.backend.repository.ProjectRepository;
import com.issuetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SseService sseService;

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request) {
        User currentUser = getCurrentUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);

        Project savedProject = projectRepository.save(project);
        
        return convertToDTO(savedProject);
    }

    public List<ProjectDTO> getUserProjects() {
        User currentUser = getCurrentUser();
        List<Project> projects = projectRepository.findByUserIdAsMemberOrOwner(currentUser.getId());
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(UUID id) {
        Project project = findProjectById(id);
        checkUserHasAccess(project);
        return convertToDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(UUID id, UpdateProjectRequest request) {
        Project project = findProjectById(id);
        checkUserIsOwner(project);

        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        ProjectDTO projectDTO = convertToDTO(updatedProject);
        
        // Get all member user IDs
        List<ProjectMember> members = projectMemberRepository.findByProjectId(id);
        List<UUID> memberUserIds = members.stream()
            .map(member -> member.getUser().getId())
            .collect(Collectors.toList());
        
        // Broadcast SSE event to all members
        sseService.broadcastProjectEventToMembers(projectDTO, "project.updated", memberUserIds);
        
        return projectDTO;
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProjectById(id);
        checkUserIsOwner(project);
        
        // Get all member user IDs before deletion
        List<ProjectMember> members = projectMemberRepository.findByProjectId(id);
        List<UUID> memberUserIds = members.stream()
            .map(member -> member.getUser().getId())
            .collect(Collectors.toList());
        
        // Broadcast SSE event to all members
        ProjectDTO projectDTO = convertToDTO(project);
        sseService.broadcastProjectEventToMembers(projectDTO, "project.deleted", memberUserIds);
        
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectMemberDTO addMember(UUID projectId, AddMemberRequest request) {
        Project project = findProjectById(projectId);
        checkUserIsOwner(project);

        User userToAdd = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userToAdd.getId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        // Don't add owner as member
        if (project.getOwner().getId().equals(userToAdd.getId())) {
            throw new IllegalArgumentException("Project owner cannot be added as a member");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(userToAdd);
        member.setRole(ProjectRole.MEMBER);

        ProjectMember savedMember = projectMemberRepository.save(member);
        
        // Broadcast SSE event to the added user
        ProjectDTO projectDTO = convertToDTO(project);
        sseService.broadcastProjectEventToUser(userToAdd.getId(), projectDTO, "project.member.added");
        
        return convertMemberToDTO(savedMember);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        Project project = findProjectById(projectId);
        checkUserIsOwner(project);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("User is not a member of this project");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        
        // Broadcast SSE event to the removed user
        ProjectDTO projectDTO = convertToDTO(project);
        sseService.broadcastProjectEventToUser(userId, projectDTO, "project.member.removed");
    }

    public List<ProjectMemberDTO> getProjectMembers(UUID projectId) {
        Project project = findProjectById(projectId);
        checkUserHasAccess(project);

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream().map(this::convertMemberToDTO).collect(Collectors.toList());
    }

    // Helper methods

    private Project findProjectById(UUID id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void checkUserHasAccess(Project project) {
        User currentUser = getCurrentUser();
        
        // Owner always has access
        if (project.getOwner().getId().equals(currentUser.getId())) {
            return;
        }

        // Check if user is a member
        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUser.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    private void checkUserIsOwner(Project project) {
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can perform this action");
        }
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwner(convertUserToDTO(project.getOwner()));
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        dto.setMembers(members.stream().map(this::convertMemberToDTO).collect(Collectors.toList()));
        
        dto.setIssueCount(project.getIssues().size());
        
        return dto;
    }

    private ProjectMemberDTO convertMemberToDTO(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setId(member.getId());
        dto.setUser(convertUserToDTO(member.getUser()));
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
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
