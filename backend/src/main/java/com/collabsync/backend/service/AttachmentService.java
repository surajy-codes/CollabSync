package com.collabsync.backend.service;

import com.collabsync.backend.dto.attachment.AttachmentResponse;
import com.collabsync.backend.entity.Attachment;
import com.collabsync.backend.entity.Task;
import com.collabsync.backend.entity.User;
import com.collabsync.backend.repository.*;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MinioClient minioClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Transactional
    public AttachmentResponse upload(Long taskId, MultipartFile file, Long userId) throws Exception {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertMember(task.getProject().getTeam().getId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String storagePath = "tasks/" + taskId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(storagePath)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        Attachment attachment = Attachment.builder()
                .task(task)
                .uploadedBy(user)
                .fileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        return mapToResponse(attachmentRepository.save(attachment));
    }

    public List<AttachmentResponse> getAttachments(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertMember(task.getProject().getTeam().getId(), userId);

        return attachmentRepository.findAllByTaskId(taskId)
                .stream().map(this::mapToResponse).toList();
    }

    public String getDownloadUrl(Long attachmentId, Long userId) throws Exception {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        assertMember(attachment.getTask().getProject().getTeam().getId(), userId);

        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(attachment.getStoragePath())
                .method(Method.GET)
                .expiry(1, TimeUnit.HOURS)
                .build());
    }

    @Transactional
    public void delete(Long attachmentId, Long userId) throws Exception {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        boolean isUploader = attachment.getUploadedBy().getId().equals(userId);
        boolean isOwner = teamMemberRepository.findByTeamIdAndUserId(
                        attachment.getTask().getProject().getTeam().getId(), userId)
                .map(m -> m.getRole().name().equals("OWNER"))
                .orElse(false);

        if (!isUploader && !isOwner)
            throw new RuntimeException("You don't have permission to delete this file");

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(attachment.getStoragePath())
                .build());

        attachmentRepository.deleteById(attachmentId);
    }

    private void assertMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId))
            throw new RuntimeException("You are not a member of this team");
    }

    private AttachmentResponse mapToResponse(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .mimeType(a.getMimeType())
                .fileSize(a.getFileSize())
                .uploadedBy(a.getUploadedBy().getName())
                .uploadedAt(a.getUploadedAt())
                .build();
    }
}