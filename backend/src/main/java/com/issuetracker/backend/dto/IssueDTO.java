package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issue information")
public class IssueDTO {

    @Schema(description = "Issue ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Project name", example = "My Project")
    private String projectName;

    @Schema(description = "Issue title", example = "Bug in login page")
    private String title;

    @Schema(description = "Issue description")
    private String description;

    @Schema(description = "Issue status", example = "OPEN")
    private IssueStatus status;

    @Schema(description = "Issue priority", example = "HIGH")
    private IssuePriority priority;

    @Schema(description = "Assigned user")
    private UserDTO assignee;

    @Schema(description = "Reporter (creator) of the issue")
    private UserDTO reporter;

    @Schema(description = "Project owner ID for permission checks")
    private UUID projectOwnerId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Number of comments")
    private Integer commentCount;
}
