package com.collabsync.backend.service;

import com.collabsync.backend.dto.project.ProjectRequest;
import com.collabsync.backend.dto.project.ProjectResponse;
import com.collabsync.backend.entity.Project;
import com.collabsync.backend.entity.TeamMember;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.ProjectRepository;
import com.collabsync.backend.repository.TeamMemberRepository;
import com.collabsync.backend.repository.TeamRepository;
import com.collabsync.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(Long teamId, ProjectRequest request, Long userId) {
        assertMember(teamId, userId);

        var team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .team(team)
                .createdBy(user)
                .status(Project.Status.ACTIVE)
                .build();

        return mapToResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> getTeamProjects(Long teamId, Long userId) {
        assertMember(teamId, userId);
        return projectRepository.findAllByTeamId(teamId)
                .stream().map(this::mapToResponse).toList();
    }

    public ProjectResponse getProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertMember(project.getTeam().getId(), userId);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertMember(project.getTeam().getId(), userId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return mapToResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertOwner(project.getTeam().getId(), userId);
        projectRepository.deleteById(projectId);
    }

    @Transactional
    public ProjectResponse toggleArchive(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertOwner(project.getTeam().getId(), userId);

        project.setStatus(project.getStatus() == Project.Status.ACTIVE
                ? Project.Status.ARCHIVED
                : Project.Status.ACTIVE);

        return mapToResponse(projectRepository.save(project));
    }

    private void assertMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("You are not a member of this team");
        }
    }

    private void assertOwner(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this team"));
        if (member.getRole() != TeamMember.Role.OWNER) {
            throw new RuntimeException("Only the owner can perform this action");
        }
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .teamName(project.getTeam().getName())
                .createdBy(project.getCreatedBy().getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}