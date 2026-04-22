package com.collabsync.backend.repository;

import com.collabsync.backend.entity.TeamMember;
import com.collabsync.backend.entity.TeamMember.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
    List<TeamMember> findAllByTeamId(Long teamId);
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    void deleteByTeamIdAndUserId(Long teamId, Long userId);
}