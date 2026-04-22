package com.collabsync.backend.service;

import com.collabsync.backend.dto.comment.CommentRequest;
import com.collabsync.backend.dto.comment.CommentResponse;
import com.collabsync.backend.entity.Comment;
import com.collabsync.backend.entity.Task;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertMember(task.getProject().getTeam().getId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .user(user)
                .build();

        return mapToResponse(commentRepository.save(comment));
    }

    public Page<CommentResponse> getComments(Long taskId, int page, int size, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertMember(task.getProject().getTeam().getId(), userId);

        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(
                taskId, PageRequest.of(page, size)
        ).map(this::mapToResponse);
    }

    @Transactional
    public CommentResponse editComment(Long commentId, CommentRequest request, Long userId) {
        Comment comment = findComment(commentId);
        assertAuthor(comment, userId);

        comment.setContent(request.getContent());
        return mapToResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findComment(commentId);

        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isOwner = teamMemberRepository.findByTeamIdAndUserId(
                        comment.getTask().getProject().getTeam().getId(), userId)
                .map(m -> m.getRole().name().equals("OWNER"))
                .orElse(false);

        if (!isAuthor && !isOwner) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        commentRepository.deleteById(commentId);
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    private void assertMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId))
            throw new RuntimeException("You are not a member of this team");
    }

    private void assertAuthor(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId))
            throw new RuntimeException("You can only edit your own comments");
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName())
                .authorId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}