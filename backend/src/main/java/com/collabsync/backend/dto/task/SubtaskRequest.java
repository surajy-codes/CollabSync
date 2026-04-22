package com.collabsync.backend.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubtaskRequest {
    @NotBlank
    private String title;
    private boolean completed;
}