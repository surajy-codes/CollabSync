package com.collabsync.backend.dto.project;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String teamName;
    private String createdBy;
    private LocalDateTime createdAt;
}