package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create comment request")
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Schema(description = "Comment content", example = "This issue needs more investigation")
    private String content;
}
