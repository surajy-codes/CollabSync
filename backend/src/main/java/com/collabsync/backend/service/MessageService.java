package com.collabsync.backend.service;

import com.collabsync.backend.entity.Message;
import com.collabsync.backend.entity.Project;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.*;
import com.collabsync.backend.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessage sendMessage(Long projectId, String content, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeam().getId(), userId))
            throw new RuntimeException("You are not a member of this team");

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = Message.builder()
                .project(project)
                .sender(sender)
                .content(content)
                .build();

        messageRepository.save(message);

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(sender.getId())
                .senderName(sender.getName())
                .content(content)
                .projectId(projectId)
                .sentAt(message.getSentAt())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/chat", chatMessage
        );

        return chatMessage;
    }

    public Page<ChatMessage> getChatHistory(Long projectId, int page, int size, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeam().getId(), userId))
            throw new RuntimeException("You are not a member of this team");

        return messageRepository.findAllByProjectIdOrderBySentAtDesc(
                projectId, PageRequest.of(page, size)
        ).map(m -> ChatMessage.builder()
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .content(m.getContent())
                .projectId(projectId)
                .sentAt(m.getSentAt())
                .build());
    }
}