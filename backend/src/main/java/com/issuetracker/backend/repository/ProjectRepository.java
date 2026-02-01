package com.issuetracker.backend.repository;

import com.issuetracker.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Find projects where user is owner
    List<Project> findByOwnerId(Long ownerId);
    
    // Find projects where user is a member (including owner)
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members pm " +
           "WHERE p.owner.id = :userId OR pm.user.id = :userId")
    List<Project> findByUserIdAsMemberOrOwner(@Param("userId") Long userId);
}
