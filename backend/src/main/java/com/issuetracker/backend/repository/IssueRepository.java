package com.issuetracker.backend.repository;

import com.issuetracker.backend.model.Issue;
import com.issuetracker.backend.model.IssuePriority;
import com.issuetracker.backend.model.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    
    // Basic queries
    List<Issue> findByProjectId(UUID projectId);
    
    Page<Issue> findByProjectId(UUID projectId, Pageable pageable);
    
    // Filtered queries with pagination  
    @Query("SELECT i FROM Issue i WHERE " +
           "(:projectId IS NULL OR i.project.id = :projectId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:assigneeId IS NULL OR i.assignee.id = :assigneeId)")
    Page<Issue> findByFilters(
        @Param("projectId") UUID projectId,
        @Param("status") IssueStatus status,
        @Param("priority") IssuePriority priority,
        @Param("assigneeId") UUID assigneeId,
        @Param("search") String search,
        Pageable pageable
    );
    
    // Count issues by project
    long countByProjectId(UUID projectId);
}
