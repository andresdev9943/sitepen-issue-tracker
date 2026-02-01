package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create issue request")
public class CreateIssueRequest {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @NotBlank(message = "Title is required")
    @Schema(description = "Issue title", example = "Bug in login page")
    private String title;

    @Schema(description = "Issue description", example = "Users cannot login with valid credentials")
    private String description;

    @Schema(description = "Issue priority", example = "HIGH", defaultValue = "MEDIUM")
    private IssuePriority priority;

    @Schema(description = "Assignee user ID", example = "2")
    private Long assigneeId;
}
