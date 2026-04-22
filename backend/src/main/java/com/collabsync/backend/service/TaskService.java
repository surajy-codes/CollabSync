package com.collabsync.backend.service;

import com.collabsync.backend.dto.task.*;
import com.collabsync.backend.entity.*;
import com.collabsync.backend.entity.Task.Priority;
import com.collabsync.backend.entity.Task.Status;
import com.collabsync.backend.repository.*;
import com.collabsync.backend.websocket.TaskUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        assertMember(project.getTeam().getId(), userId);

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId()).orElse(null)
                : null;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(parseStatus(request.getStatus(), Status.TODO))
                .priority(parsePriority(request.getPriority(), Priority.MEDIUM))
                .project(project)
                .createdBy(creator)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .build();
        if (assignee != null) {
            notificationService.send(assignee,
                    "You have been assigned a task: " + task.getTitle(),
                    Notification.Type.TASK_ASSIGNED, task.getId());
        }

        TaskResponse response = mapToResponse(taskRepository.save(task));

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/tasks",
                TaskUpdateMessage.builder()
                        .eventType("TASK_CREATED")
                        .task(response)
                        .projectId(projectId)
                        .build()
        );

        if (assignee != null) {
            notificationService.send(assignee,
                    "You have been assigned a task: " + task.getTitle(),
                    Notification.Type.TASK_ASSIGNED, task.getId());
        }

        return response;
    }

    public Page<TaskResponse> getTasks(Long projectId, String status, String priority,
                                       Long assigneeId, int page, int size, String sortBy, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        assertMember(project.getTeam().getId(), userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        Status statusEnum = status != null ? Status.valueOf(status.toUpperCase()) : null;
        Priority priorityEnum = priority != null ? Priority.valueOf(priority.toUpperCase()) : null;

        return taskRepository.findAllByProjectIdWithFilters(
                projectId, statusEnum, priorityEnum, assigneeId, pageable
        ).map(this::mapToResponse);
    }

    public TaskResponse getTask(Long taskId, Long userId) {
        Task task = findTask(taskId);
        assertMember(task.getProject().getTeam().getId(), userId);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request, Long userId) {
        Task task = findTask(taskId);
        assertMember(task.getProject().getTeam().getId(), userId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getStatus() != null)
            task.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
        if (request.getPriority() != null)
            task.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());
        if (request.getAssigneeId() != null)
            task.setAssignee(userRepository.findById(request.getAssigneeId()).orElse(null));

        if (task.getAssignee() != null) {
            notificationService.send(task.getAssignee(),
                    "Task updated: " + task.getTitle(),
                    Notification.Type.TASK_UPDATED, task.getId());
        }

        TaskResponse response = mapToResponse(taskRepository.save(task));

        messagingTemplate.convertAndSend(
                "/topic/projects/" + task.getProject().getId() + "/tasks",
                TaskUpdateMessage.builder()
                        .eventType("TASK_UPDATED")
                        .task(response)
                        .projectId(task.getProject().getId())
                        .build()
        );

        if (task.getAssignee() != null) {
            notificationService.send(task.getAssignee(),
                    "Task updated: " + task.getTitle(),
                    Notification.Type.TASK_UPDATED, task.getId());
        }

        return response;
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = findTask(taskId);
        Long projectId = task.getProject().getId();
        assertMember(task.getProject().getTeam().getId(), userId);
        taskRepository.deleteById(taskId);
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/tasks",
                TaskUpdateMessage.builder()
                        .eventType("TASK_DELETED")
                        .task(taskId)
                        .projectId(projectId)
                        .build()
        );
    }

    @Transactional
    public SubtaskResponse createSubtask(Long taskId, SubtaskRequest request, Long userId) {
        Task task = findTask(taskId);
        assertMember(task.getProject().getTeam().getId(), userId);

        Subtask subtask = Subtask.builder()
                .title(request.getTitle())
                .completed(false)
                .task(task)
                .build();

        return mapSubtask(subtaskRepository.save(subtask));
    }

    @Transactional
    public SubtaskResponse updateSubtask(Long subtaskId, SubtaskRequest request, Long userId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));
        assertMember(subtask.getTask().getProject().getTeam().getId(), userId);

        subtask.setTitle(request.getTitle());
        subtask.setCompleted(request.isCompleted());
        return mapSubtask(subtaskRepository.save(subtask));
    }

    @Transactional
    public void deleteSubtask(Long subtaskId, Long userId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found"));
        assertMember(subtask.getTask().getProject().getTeam().getId(), userId);
        subtaskRepository.deleteById(subtaskId);
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private void assertMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId))
            throw new RuntimeException("You are not a member of this team");
    }

    private Status parseStatus(String s, Status def) {
        try { return s != null ? Status.valueOf(s.toUpperCase()) : def; }
        catch (Exception e) { return def; }
    }

    private Priority parsePriority(String p, Priority def) {
        try { return p != null ? Priority.valueOf(p.toUpperCase()) : def; }
        catch (Exception e) { return def; }
    }

    private TaskResponse mapToResponse(Task task) {
        List<SubtaskResponse> subtasks = subtaskRepository.findAllByTaskId(task.getId())
                .stream().map(this::mapSubtask).toList();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .assignee(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .createdBy(task.getCreatedBy().getName())
                .dueDate(task.getDueDate())
                .subtasks(subtasks)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private SubtaskResponse mapSubtask(Subtask s) {
        return SubtaskResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .completed(s.isCompleted())
                .build();
    }
}