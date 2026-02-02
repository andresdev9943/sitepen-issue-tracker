package com.issuetracker.backend.controller;

import com.issuetracker.backend.dto.*;
import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import com.issuetracker.backend.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues", description = "Issue management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @PostMapping
    @Operation(
        summary = "Create new issue",
        description = "Creates a new issue in a project. User must have access to the project."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Issue created successfully",
            content = @Content(schema = @Schema(implementation = IssueDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<IssueDTO> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        IssueDTO issue = issueService.createIssue(request);
        return new ResponseEntity<>(issue, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
        summary = "Get issues with filters and pagination",
        description = "Returns paginated list of issues with optional filters for status, priority, assignee, and text search on title"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Issues retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project")
    })
    public ResponseEntity<Page<IssueDTO>> getIssues(
            @Parameter(description = "Project ID filter") 
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "Status filter") 
            @RequestParam(required = false) IssueStatus status,
            
            @Parameter(description = "Priority filter") 
            @RequestParam(required = false) IssuePriority priority,
            
            @Parameter(description = "Assignee ID filter") 
            @RequestParam(required = false) UUID assigneeId,
            
            @Parameter(description = "Text search on title") 
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field (e.g., createdAt, priority, status)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)") 
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<IssueDTO> issues = issueService.getIssues(
            projectId, status, priority, assigneeId, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get issue by ID",
        description = "Returns issue details if user has access to the project"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Issue retrieved successfully",
            content = @Content(schema = @Schema(implementation = IssueDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<IssueDTO> getIssueById(@PathVariable UUID id) {
        IssueDTO issue = issueService.getIssueById(id);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update issue",
        description = "Updates issue details. User must have access to the project."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Issue updated successfully",
            content = @Content(schema = @Schema(implementation = IssueDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<IssueDTO> updateIssue(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIssueRequest request) {
        IssueDTO issue = issueService.updateIssue(id, request);
        return ResponseEntity.ok(issue);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete issue",
        description = "Deletes an issue. Only project owner can delete."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Issue deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Only project owner can delete"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<Void> deleteIssue(@PathVariable UUID id) {
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @Operation(
        summary = "Add comment to issue",
        description = "Adds a comment to an issue. User must have access to the project."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment added successfully",
            content = @Content(schema = @Schema(implementation = CommentDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDTO comment = issueService.addComment(id, request);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/comments")
    @Operation(
        summary = "Get issue comments",
        description = "Returns all comments for an issue, ordered by creation time"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<List<CommentDTO>> getIssueComments(@PathVariable UUID id) {
        List<CommentDTO> comments = issueService.getIssueComments(id);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/activity")
    @Operation(
        summary = "Get issue activity log",
        description = "Returns all activity log entries for an issue, ordered by most recent first"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Activity log retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<List<ActivityLogDTO>> getIssueActivity(@PathVariable UUID id) {
        List<ActivityLogDTO> activities = issueService.getIssueActivity(id);
        return ResponseEntity.ok(activities);
    }

    @PutMapping("/{id}/comments/{commentId}")
    @Operation(
        summary = "Update comment",
        description = "Updates a comment. Only the comment author can edit their own comments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment updated successfully",
            content = @Content(schema = @Schema(implementation = CommentDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "You can only edit your own comments"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentDTO comment = issueService.updateComment(id, commentId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @Operation(
        summary = "Delete comment",
        description = "Deletes a comment. Only the comment author can delete their own comments."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "You can only delete your own comments"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId) {
        issueService.deleteComment(id, commentId);
        return ResponseEntity.noContent().build();
    }
}
