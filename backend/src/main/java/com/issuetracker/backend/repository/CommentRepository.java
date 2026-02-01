package com.issuetracker.backend.repository;

import com.issuetracker.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
    
    long countByIssueId(Long issueId);
}
