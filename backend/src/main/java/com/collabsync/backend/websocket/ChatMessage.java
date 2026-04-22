package com.collabsync.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long senderId;
    private String senderName;
    private String content;
    private Long projectId;
    private LocalDateTime sentAt;
}