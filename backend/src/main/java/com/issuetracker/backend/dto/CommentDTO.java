package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comment information")
public class CommentDTO {

    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @Schema(description = "Issue ID", example = "1")
    private Long issueId;

    @Schema(description = "Comment author")
    private UserDTO user;

    @Schema(description = "Comment content")
    private String content;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
