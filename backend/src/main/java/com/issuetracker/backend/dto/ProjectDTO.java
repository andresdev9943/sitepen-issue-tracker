package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project information")
public class ProjectDTO {

    @Schema(description = "Project ID", example = "1")
    private Long id;

    @Schema(description = "Project name", example = "My Awesome Project")
    private String name;

    @Schema(description = "Project description")
    private String description;

    @Schema(description = "Project owner")
    private UserDTO owner;

    @Schema(description = "Project creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Project members")
    private List<ProjectMemberDTO> members;

    @Schema(description = "Total issue count")
    private Integer issueCount;
}
