package com.collabsync.backend.dto.task;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assignee;
    private Long assigneeId;
    private String createdBy;
    private LocalDate dueDate;
    private List<SubtaskResponse> subtasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}