package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create issue request")
public class CreateIssueRequest {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Project ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @NotBlank(message = "Title is required")
    @Schema(description = "Issue title", example = "Bug in login page")
    private String title;

    @Schema(description = "Issue description", example = "Users cannot login with valid credentials")
    private String description;

    @Schema(description = "Issue priority", example = "HIGH", defaultValue = "MEDIUM")
    private IssuePriority priority;

    @Schema(description = "Assignee user ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assigneeId;
}
