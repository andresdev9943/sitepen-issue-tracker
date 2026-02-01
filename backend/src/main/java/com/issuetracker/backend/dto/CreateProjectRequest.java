package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create project request")
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Schema(description = "Project name", example = "My Awesome Project")
    private String name;

    @Schema(description = "Project description", example = "A project to track bugs and features")
    private String description;
}
