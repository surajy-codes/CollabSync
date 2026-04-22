package com.collabsync.backend.repository;

import com.collabsync.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t JOIN TeamMember tm ON tm.team = t WHERE tm.user.id = :userId")
    List<Team> findAllByUserId(@Param("userId") Long userId);
}