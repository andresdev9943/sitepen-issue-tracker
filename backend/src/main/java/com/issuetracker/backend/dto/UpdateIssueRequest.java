package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update issue request")
public class UpdateIssueRequest {

    @Schema(description = "Issue title", example = "Updated bug title")
    private String title;

    @Schema(description = "Issue description", example = "Updated description")
    private String description;

    @Schema(description = "Issue status", example = "IN_PROGRESS")
    private IssueStatus status;

    @Schema(description = "Issue priority", example = "HIGH")
    private IssuePriority priority;

    @Schema(description = "Assignee user ID (null to unassign)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assigneeId;
}
