package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Activity log entry")
public class ActivityLogDTO {

    @Schema(description = "Activity ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Issue ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID issueId;

    @Schema(description = "User who performed the action")
    private UserDTO user;

    @Schema(description = "Action performed", example = "Status changed to IN_PROGRESS")
    private String action;

    @Schema(description = "Additional details")
    private String details;

    @Schema(description = "Timestamp")
    private LocalDateTime createdAt;
}
