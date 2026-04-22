package com.collabsync.backend.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiPriorityRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
}