package com.collabsync.backend.dto.attachment;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}