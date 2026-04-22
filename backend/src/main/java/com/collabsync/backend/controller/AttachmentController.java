package com.collabsync.backend.controller;

import com.collabsync.backend.dto.attachment.AttachmentResponse;
import com.collabsync.backend.service.AttachmentService;
import com.collabsync.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final JwtUtil jwtUtil;

    private Long getUserId(String auth) {
        return jwtUtil.extractUserId(auth.substring(7));
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentResponse> upload(@PathVariable Long taskId,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestHeader("Authorization") String auth) throws Exception {
        return ResponseEntity.ok(attachmentService.upload(taskId, file, getUserId(auth)));
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long taskId,
                                                                   @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(attachmentService.getAttachments(taskId, getUserId(auth)));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long attachmentId,
                                                              @RequestHeader("Authorization") String auth) throws Exception {
        String url = attachmentService.getDownloadUrl(attachmentId, getUserId(auth));
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId,
                                       @RequestHeader("Authorization") String auth) throws Exception {
        attachmentService.delete(attachmentId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}