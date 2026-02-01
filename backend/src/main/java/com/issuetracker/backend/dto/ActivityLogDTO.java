package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Activity log entry")
public class ActivityLogDTO {

    @Schema(description = "Activity ID", example = "1")
    private Long id;

    @Schema(description = "Issue ID", example = "1")
    private Long issueId;

    @Schema(description = "User who performed the action")
    private UserDTO user;

    @Schema(description = "Action performed", example = "Status changed to IN_PROGRESS")
    private String action;

    @Schema(description = "Additional details")
    private String details;

    @Schema(description = "Timestamp")
    private LocalDateTime createdAt;
}
