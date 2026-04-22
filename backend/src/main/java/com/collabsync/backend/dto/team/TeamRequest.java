package com.collabsync.backend.dto.team;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    private String description;
}