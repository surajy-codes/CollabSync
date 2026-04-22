package com.collabsync.backend.controller;

import com.collabsync.backend.service.NotificationService;
import com.collabsync.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String auth) {
        return jwtUtil.extractUserId(auth.substring(7));
    }

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(notificationService.getNotifications(getUserId(auth), page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(getUserId(auth))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
                                           @RequestHeader("Authorization") String auth) {
        notificationService.markAsRead(id, getUserId(auth));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("Authorization") String auth) {
        notificationService.markAllAsRead(getUserId(auth));
        return ResponseEntity.ok().build();
    }
}