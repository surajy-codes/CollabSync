package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Task;
import com.collabsync.backend.entity.Task.Priority;
import com.collabsync.backend.entity.Task.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT t FROM Task t WHERE t.project.id = :projectId
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
    """)
    Page<Task> findAllByProjectIdWithFilters(
            @Param("projectId") Long projectId,
            @Param("status") Status status,
            @Param("priority") Priority priority,
            @Param("assigneeId") Long assigneeId,
            Pageable pageable
    );
}