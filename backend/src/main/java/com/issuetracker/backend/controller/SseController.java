package com.issuetracker.backend.controller;

import com.issuetracker.backend.exception.ForbiddenException;
import com.issuetracker.backend.exception.ResourceNotFoundException;
import com.issuetracker.backend.model.Project;
import com.issuetracker.backend.model.User;
import com.issuetracker.backend.repository.ProjectMemberRepository;
import com.issuetracker.backend.repository.ProjectRepository;
import com.issuetracker.backend.repository.UserRepository;
import com.issuetracker.backend.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
@Tag(name = "Server-Sent Events", description = "Real-time issue update streaming endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SseController {

    @Autowired
    private SseService sseService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/issues", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Subscribe to issue updates",
        description = "Creates a Server-Sent Events connection to receive real-time updates for issues. " +
                      "Optionally filter by project ID. The connection will remain open and send events " +
                      "when issues are created, updated, or commented on."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "SSE connection established successfully",
            content = @Content(mediaType = "text/event-stream")
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to project"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public SseEmitter subscribeToIssues(
            @Parameter(description = "Project ID to filter updates (optional, null for all projects user has access to)")
            @RequestParam(required = false) UUID projectId) {
        
        // Validate project access if projectId is specified
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            checkUserHasProjectAccess(project);
        }

        return sseService.createEmitter(projectId);
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get SSE connection statistics",
        description = "Returns the number of active SSE connections"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Map<String, Object>> getStats(
            @Parameter(description = "Project ID to get stats for (optional)")
            @RequestParam(required = false) UUID projectId) {
        
        Map<String, Object> stats = new HashMap<>();
        
        if (projectId != null) {
            stats.put("projectId", projectId);
            stats.put("activeConnections", sseService.getProjectConnectionCount(projectId));
        } else {
            stats.put("totalActiveConnections", sseService.getActiveConnectionCount());
        }
        
        return ResponseEntity.ok(stats);
    }

    // Helper methods

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void checkUserHasProjectAccess(Project project) {
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
}
