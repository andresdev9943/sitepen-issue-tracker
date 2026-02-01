package com.issuetracker.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Add member to project request")
public class AddMemberRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email of user to add", example = "user@example.com")
    private String email;
}
