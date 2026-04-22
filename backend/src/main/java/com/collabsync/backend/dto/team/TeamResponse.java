package com.collabsync.backend.dto.team;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}