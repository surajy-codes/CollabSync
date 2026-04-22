package com.collabsync.backend.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String status;
    private String priority;
    private Long assigneeId;
    private LocalDate dueDate;
}