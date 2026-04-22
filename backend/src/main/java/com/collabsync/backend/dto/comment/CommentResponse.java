package com.collabsync.backend.dto.comment;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorName;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}