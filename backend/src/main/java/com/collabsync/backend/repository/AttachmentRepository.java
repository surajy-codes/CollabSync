package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByTaskId(Long taskId);
}