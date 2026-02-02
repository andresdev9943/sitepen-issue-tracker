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
@Schema(description = "Comment information")
public class CommentDTO {

    @Schema(description = "Comment ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Issue ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID issueId;

    @Schema(description = "Comment author")
    private UserDTO user;

    @Schema(description = "Comment content")
    private String content;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
