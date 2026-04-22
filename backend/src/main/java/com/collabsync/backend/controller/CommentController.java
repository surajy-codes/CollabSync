package com.collabsync.backend.controller;

import com.collabsync.backend.dto.comment.CommentRequest;
import com.collabsync.backend.dto.comment.CommentResponse;
import com.collabsync.backend.service.CommentService;
import com.collabsync.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String auth) {
        return jwtUtil.extractUserId(auth.substring(7));
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId,
                                                      @Valid @RequestBody CommentRequest request,
                                                      @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(commentService.addComment(taskId, request, getUserId(auth)));
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(commentService.getComments(taskId, page, size, getUserId(auth)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId,
                                                       @Valid @RequestBody CommentRequest request,
                                                       @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(commentService.editComment(commentId, request, getUserId(auth)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestHeader("Authorization") String auth) {
        commentService.deleteComment(commentId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}