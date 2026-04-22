package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findAllByTaskId(Long taskId);
}