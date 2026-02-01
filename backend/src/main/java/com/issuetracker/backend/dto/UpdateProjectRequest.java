package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update project request")
public class UpdateProjectRequest {

    @Schema(description = "Project name", example = "Updated Project Name")
    private String name;

    @Schema(description = "Project description", example = "Updated description")
    private String description;
}
