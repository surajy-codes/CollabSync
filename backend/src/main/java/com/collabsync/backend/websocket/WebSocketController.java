package com.collabsync.backend.websocket;

import com.collabsync.backend.service.MessageService;
import com.collabsync.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    @MessageMapping("/projects/{projectId}/chat")
    public void sendMessage(@DestinationVariable Long projectId,
                            @Payload ChatMessage message,
                            SimpMessageHeaderAccessor headerAccessor) {
        String token = (String) headerAccessor.getSessionAttributes().get("token");
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Unauthorized: no token in session");
        }
        Long userId = jwtUtil.extractUserId(token);
        messageService.sendMessage(projectId, message.getContent(), userId);
    }
}