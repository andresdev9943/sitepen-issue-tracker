package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issue information")
public class IssueDTO {

    @Schema(description = "Issue ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

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
    private Long projectOwnerId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Number of comments")
    private Integer commentCount;
}
