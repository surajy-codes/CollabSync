package com.collabsync.backend.service;

import com.collabsync.backend.dto.team.TeamMemberResponse;
import com.collabsync.backend.dto.team.TeamRequest;
import com.collabsync.backend.dto.team.TeamResponse;
import com.collabsync.backend.entity.Team;
import com.collabsync.backend.entity.TeamMember;
import com.collabsync.backend.entity.TeamMember.Role;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.TeamMemberRepository;
import com.collabsync.backend.repository.TeamRepository;
import com.collabsync.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamResponse createTeam(TeamRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        teamRepository.save(team);

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(Role.OWNER)
                .build();

        teamMemberRepository.save(member);

        return mapToResponse(team);
    }

    public List<TeamResponse> getUserTeams(Long userId) {
        return teamRepository.findAllByUserId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public TeamResponse getTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamRequest request, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        assertRole(teamId, userId, Role.OWNER);

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        teamRepository.save(team);

        return mapToResponse(team);
    }

    @Transactional
    public void deleteTeam(Long teamId, Long userId) {
        assertRole(teamId, userId, Role.OWNER);
        teamRepository.deleteById(teamId);
    }

    @Transactional
    public void inviteMember(Long teamId, String email, Role role, Long requesterId) {
        assertRole(teamId, requesterId, Role.OWNER);

        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invitee.getId())) {
            throw new RuntimeException("User is already a member of this team");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(invitee)
                .role(role)
                .build();

        teamMemberRepository.save(member);
    }

    @Transactional
    public void changeMemberRole(Long teamId, Long targetUserId, Role newRole, Long requesterId) {
        assertRole(teamId, requesterId, Role.OWNER);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long targetUserId, Long requesterId) {
        assertRole(teamId, requesterId, Role.OWNER);
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, targetUserId);
    }

    private void assertRole(Long teamId, Long userId, Role requiredRole) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this team"));

        if (member.getRole() != requiredRole) {
            throw new RuntimeException("You don't have permission to perform this action");
        }
    }

    public List<TeamMemberResponse> getMembers(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId))
            throw new RuntimeException("Access denied");
        return teamMemberRepository.findAllWithUserByTeamId(teamId)
                .stream().map(m -> TeamMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .userName(m.getUser().getName())
                        .role(m.getRole().name())
                        .build()).toList();
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdBy(team.getCreatedBy().getName())
                .createdAt(team.getCreatedAt())
                .build();
    }
}