package com.collabsync.backend.service;

import com.collabsync.backend.entity.Notification;
import com.collabsync.backend.entity.Notification.Type;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(User user, String message, Type type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);
    }

    public Page<Map<String, Object>> getNotifications(Long userId, int page, int size) {
        return notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(n -> Map.of(
                        "id", n.getId(),
                        "message", n.getMessage(),
                        "type", n.getType(),
                        "referenceId", n.getReferenceId() != null ? n.getReferenceId() : "",
                        "read", n.isRead(),
                        "createdAt", n.getCreatedAt()
                ));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}