package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findAllByProjectIdOrderBySentAtDesc(Long projectId, Pageable pageable);
}