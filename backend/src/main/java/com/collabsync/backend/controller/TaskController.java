package com.collabsync.backend.controller;

import com.collabsync.backend.dto.task.*;
import com.collabsync.backend.service.TaskService;
import com.collabsync.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String auth) {
        return jwtUtil.extractUserId(auth.substring(7));
    }

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId,
                                                   @Valid @RequestBody TaskRequest request,
                                                   @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.createTask(projectId, request, getUserId(auth)));
    }

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.getTasks(
                projectId, status, priority, assigneeId, page, size, sortBy, getUserId(auth)));
    }

    @GetMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId,
                                                @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.getTask(taskId, getUserId(auth)));
    }

    @PutMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId,
                                                   @Valid @RequestBody TaskRequest request,
                                                   @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, getUserId(auth)));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId,
                                           @RequestHeader("Authorization") String auth) {
        taskService.deleteTask(taskId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/tasks/{taskId}/subtasks")
    public ResponseEntity<SubtaskResponse> createSubtask(@PathVariable Long taskId,
                                                         @Valid @RequestBody SubtaskRequest request,
                                                         @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.createSubtask(taskId, request, getUserId(auth)));
    }

    @PutMapping("/api/v1/tasks/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<SubtaskResponse> updateSubtask(@PathVariable Long taskId,
                                                         @PathVariable Long subtaskId,
                                                         @Valid @RequestBody SubtaskRequest request,
                                                         @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(taskService.updateSubtask(subtaskId, request, getUserId(auth)));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long taskId,
                                              @PathVariable Long subtaskId,
                                              @RequestHeader("Authorization") String auth) {
        taskService.deleteSubtask(subtaskId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}