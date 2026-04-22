package com.collabsync.backend.dto.task;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubtaskResponse {
    private Long id;
    private String title;
    private boolean completed;
}