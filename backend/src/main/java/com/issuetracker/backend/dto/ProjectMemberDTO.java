package com.issuetracker.backend.dto;

import com.issuetracker.backend.model.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project member information")
public class ProjectMemberDTO {

    @Schema(description = "Member ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User information")
    private UserDTO user;

    @Schema(description = "Member role", example = "MEMBER")
    private ProjectRole role;

    @Schema(description = "When the user joined the project")
    private LocalDateTime joinedAt;
}
