package com.collabsync.backend.controller;

import com.collabsync.backend.dto.project.ProjectRequest;
import com.collabsync.backend.dto.project.ProjectResponse;
import com.collabsync.backend.service.ProjectService;
import com.collabsync.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        return jwtUtil.extractUserId(authHeader.substring(7));
    }

    @PostMapping("/api/v1/teams/{teamId}/projects")
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long teamId,
                                                         @Valid @RequestBody ProjectRequest request,
                                                         @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(projectService.createProject(teamId, request, getUserId(auth)));
    }

    @GetMapping("/api/v1/teams/{teamId}/projects")
    public ResponseEntity<List<ProjectResponse>> getTeamProjects(@PathVariable Long teamId,
                                                                 @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(projectService.getTeamProjects(teamId, getUserId(auth)));
    }

    @GetMapping("/api/v1/projects/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long projectId,
                                                      @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(projectService.getProject(projectId, getUserId(auth)));
    }

    @PutMapping("/api/v1/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId,
                                                         @Valid @RequestBody ProjectRequest request,
                                                         @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request, getUserId(auth)));
    }

    @DeleteMapping("/api/v1/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId,
                                              @RequestHeader("Authorization") String auth) {
        projectService.deleteProject(projectId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/projects/{projectId}/archive")
    public ResponseEntity<ProjectResponse> toggleArchive(@PathVariable Long projectId,
                                                         @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(projectService.toggleArchive(projectId, getUserId(auth)));
    }
}