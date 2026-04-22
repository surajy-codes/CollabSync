package com.collabsync.backend.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDescriptionRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String projectName;
}