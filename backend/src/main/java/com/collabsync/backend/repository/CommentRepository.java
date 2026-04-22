package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId, Pageable pageable);
}