package com.issuetracker.backend.repository;

import com.issuetracker.backend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}
