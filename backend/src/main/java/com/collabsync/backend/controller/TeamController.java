package com.collabsync.backend.controller;

import com.collabsync.backend.dto.team.TeamMemberResponse;
import com.collabsync.backend.dto.team.TeamRequest;
import com.collabsync.backend.dto.team.TeamResponse;
import com.collabsync.backend.entity.TeamMember.Role;
import com.collabsync.backend.service.TeamService;
import com.collabsync.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        return jwtUtil.extractUserId(authHeader.substring(7));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request,
                                                   @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(teamService.createTeam(request, getUserId(auth)));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(@PathVariable Long teamId,
                                                               @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(teamService.getMembers(teamId, getUserId(auth)));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getMyTeams(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(teamService.getUserTeams(getUserId(auth)));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long teamId,
                                                @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(teamService.getTeam(teamId, getUserId(auth)));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long teamId,
                                                   @Valid @RequestBody TeamRequest request,
                                                   @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request, getUserId(auth)));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId,
                                           @RequestHeader("Authorization") String auth) {
        teamService.deleteTeam(teamId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/invite")
    public ResponseEntity<Void> invite(@PathVariable Long teamId,
                                       @RequestBody Map<String, String> body,
                                       @RequestHeader("Authorization") String auth) {
        Role role = Role.valueOf(body.getOrDefault("role", "MEMBER").toUpperCase());
        teamService.inviteMember(teamId, body.get("email"), role, getUserId(auth));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{teamId}/members/{userId}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long teamId,
                                           @PathVariable Long userId,
                                           @RequestBody Map<String, String> body,
                                           @RequestHeader("Authorization") String auth) {
        Role role = Role.valueOf(body.get("role").toUpperCase());
        teamService.changeMemberRole(teamId, userId, role, getUserId(auth));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long teamId,
                                             @PathVariable Long userId,
                                             @RequestHeader("Authorization") String auth) {
        teamService.removeMember(teamId, userId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}