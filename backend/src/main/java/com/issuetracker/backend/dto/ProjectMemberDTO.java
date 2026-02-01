package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project member information")
public class ProjectMemberDTO {

    @Schema(description = "Member ID", example = "1")
    private Long id;

    @Schema(description = "User information")
    private UserDTO user;

    @Schema(description = "Member role", example = "MEMBER")
    private ProjectRole role;

    @Schema(description = "When the user joined the project")
    private LocalDateTime joinedAt;
}
