package com.collabsync.backend.dto.team;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TeamMemberResponse {
    private Long userId;
    private String userName;
    private String role;
}