package com.issuetracker.backend.controller;

import com.issuetracker.backend.dto.*;
import com.issuetracker.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @Operation(
        summary = "Create new project",
        description = "Creates a new project with the authenticated user as owner"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ProjectDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectDTO project = projectService.createProject(request);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
        summary = "Get user's projects",
        description = "Returns all projects where the user is owner or member"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Projects retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<ProjectDTO>> getUserProjects() {
        List<ProjectDTO> projects = projectService.getUserProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get project by ID",
        description = "Returns project details if user has access"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to this project"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable UUID id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update project",
        description = "Updates project details. Only owner can update."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project updated successfully",
            content = @Content(schema = @Schema(implementation = ProjectDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Only owner can update"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectDTO project = projectService.updateProject(id, request);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete project",
        description = "Deletes a project. Only owner can delete."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Only owner can delete"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @Operation(
        summary = "Add member to project",
        description = "Adds a user as a member to the project. Only owner can add members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Member added successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input or user already member"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Only owner can add members"),
        @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    public ResponseEntity<ProjectMemberDTO> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        ProjectMemberDTO member = projectService.addMember(id, request);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(
        summary = "Remove member from project",
        description = "Removes a user from the project. Only owner can remove members."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Member removed successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Only owner can remove members"),
        @ApiResponse(responseCode = "404", description = "Project or member not found")
    })
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(
        summary = "Get project members",
        description = "Returns all members of a project"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Members retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "No access to this project"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<ProjectMemberDTO>> getProjectMembers(@PathVariable UUID id) {
        List<ProjectMemberDTO> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(members);
    }
}
