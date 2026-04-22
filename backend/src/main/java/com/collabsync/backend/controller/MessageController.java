package com.collabsync.backend.controller;

import com.collabsync.backend.service.MessageService;
import com.collabsync.backend.util.JwtUtil;
import com.collabsync.backend.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String auth) {
        return jwtUtil.extractUserId(auth.substring(7));
    }

    @GetMapping("/projects/{projectId}/messages")
    public ResponseEntity<Page<ChatMessage>> getChatHistory(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(messageService.getChatHistory(projectId, page, size, getUserId(auth)));
    }
}