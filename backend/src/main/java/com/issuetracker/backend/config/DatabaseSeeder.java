package com.issuetracker.backend.config;

import com.issuetracker.backend.model.*;
import com.issuetracker.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class DatabaseSeeder {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Bean
    public CommandLineRunner seedData(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            IssueRepository issueRepository,
            CommentRepository commentRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        return args -> {
            // Check if seeding is enabled
            if (!seedEnabled) {
                log.info("Database seeding is disabled via configuration.");
                return;
            }

            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Database already seeded. Skipping seed data creation.");
                return;
            }

            log.info("Starting database seeding...");

            // Create Users
            User admin1 = createUser(
                    "admin1@issue-tracker.com",
                    "Admin User1",
                    "password123"
            );
            User admin2 = createUser(
                    "admin2@issue-tracker.com",
                    "Admin User2",
                    "password123"
            );

            admin1 = userRepository.save(admin1);
            admin2 = userRepository.save(admin2);
            log.info("Created 2 users: {} and {}", admin1.getEmail(), admin2.getEmail());

            // Create Projects
            Project project1 = createProject(
                    "Issue Tracker Application",
                    "A comprehensive issue tracking system built with Angular and Spring Boot. " +
                    "This project aims to provide a modern, user-friendly interface for managing project issues, " +
                    "tracking progress, and collaborating with team members.",
                    admin1
            );

            Project project2 = createProject(
                    "E-Commerce Platform",
                    "An online shopping platform with product catalog, shopping cart, and payment integration. " +
                    "Features include user authentication, order management, and real-time inventory tracking.",
                    admin1
            );

            Project project3 = createProject(
                    "Mobile Banking App",
                    "A secure mobile banking application for iOS and Android. Provides features like " +
                    "account management, money transfers, bill payments, and transaction history.",
                    admin2
            );

            project1 = projectRepository.save(project1);
            project2 = projectRepository.save(project2);
            project3 = projectRepository.save(project3);
            log.info("Created 3 projects");

            // Add project members
            ProjectMember member1 = createProjectMember(project1, admin2, ProjectRole.MEMBER);
            ProjectMember member2 = createProjectMember(project2, admin2, ProjectRole.MEMBER);
            ProjectMember member3 = createProjectMember(project3, admin1, ProjectRole.MEMBER);

            projectMemberRepository.saveAll(List.of(member1, member2, member3));
            log.info("Added project members");

            // Create Issues for Project 1 (Issue Tracker)
            List<Issue> project1Issues = new ArrayList<>();

            project1Issues.add(createIssue(
                    project1,
                    "Implement user authentication system",
                    "Create a complete authentication system with JWT tokens, login, logout, and registration. " +
                    "Should include password validation, email verification, and secure session management.",
                    IssueStatus.IN_PROGRESS,
                    IssuePriority.CRITICAL,
                    admin1,
                    admin1
            ));

            project1Issues.add(createIssue(
                    project1,
                    "Design dashboard UI/UX",
                    "Create wireframes and mockups for the main dashboard. Include charts for issue statistics, " +
                    "recent activity feed, and quick action buttons. Should be responsive and modern.",
                    IssueStatus.OPEN,
                    IssuePriority.HIGH,
                    admin2,
                    admin1
            ));

            project1Issues.add(createIssue(
                    project1,
                    "Add real-time notifications",
                    "Implement WebSocket-based real-time notifications for issue updates, comments, and assignments. " +
                    "Notifications should appear as toast messages and be stored in a notification center.",
                    IssueStatus.OPEN,
                    IssuePriority.MEDIUM,
                    admin2,
                    admin2
            ));

            project1Issues.add(createIssue(
                    project1,
                    "Fix issue pagination bug",
                    "The pagination on the issues list page is showing incorrect page numbers when filtering is applied. " +
                    "The total count doesn't update properly after applying filters.",
                    IssueStatus.IN_PROGRESS,
                    IssuePriority.HIGH,
                    admin1,
                    admin2
            ));

            project1Issues.add(createIssue(
                    project1,
                    "Optimize database queries",
                    "Several database queries are causing performance issues. Need to add proper indexes and optimize " +
                    "N+1 query problems in the issue and comment endpoints.",
                    IssueStatus.CLOSED,
                    IssuePriority.MEDIUM,
                    admin1,
                    admin1
            ));

            // Create Issues for Project 2 (E-Commerce)
            List<Issue> project2Issues = new ArrayList<>();

            project2Issues.add(createIssue(
                    project2,
                    "Implement shopping cart functionality",
                    "Create a shopping cart that allows users to add, remove, and update product quantities. " +
                    "Cart should persist across sessions and calculate totals with taxes.",
                    IssueStatus.IN_PROGRESS,
                    IssuePriority.CRITICAL,
                    admin1,
                    admin1
            ));

            project2Issues.add(createIssue(
                    project2,
                    "Add product search with filters",
                    "Implement full-text search for products with filters for category, price range, brand, and ratings. " +
                    "Search should be fast and support autocomplete suggestions.",
                    IssueStatus.OPEN,
                    IssuePriority.HIGH,
                    admin2,
                    admin1
            ));

            project2Issues.add(createIssue(
                    project2,
                    "Integrate payment gateway",
                    "Integrate Stripe payment gateway for credit card processing. Should support multiple currencies " +
                    "and handle payment failures gracefully.",
                    IssueStatus.OPEN,
                    IssuePriority.CRITICAL,
                    null,
                    admin2
            ));

            project2Issues.add(createIssue(
                    project2,
                    "Product images not loading on mobile",
                    "Product detail page images are failing to load on mobile devices. Desktop works fine. " +
                    "Appears to be related to image size or format.",
                    IssueStatus.CLOSED,
                    IssuePriority.HIGH,
                    admin1,
                    admin2
            ));

            // Create Issues for Project 3 (Mobile Banking)
            List<Issue> project3Issues = new ArrayList<>();

            project3Issues.add(createIssue(
                    project3,
                    "Implement biometric authentication",
                    "Add fingerprint and Face ID authentication for iOS and Touch ID for Android. " +
                    "Should fall back to PIN if biometric is unavailable.",
                    IssueStatus.IN_PROGRESS,
                    IssuePriority.CRITICAL,
                    admin2,
                    admin2
            ));

            project3Issues.add(createIssue(
                    project3,
                    "Add transaction history export",
                    "Allow users to export their transaction history as PDF or CSV. Should support date range " +
                    "filtering and include all transaction details.",
                    IssueStatus.OPEN,
                    IssuePriority.LOW,
                    admin1,
                    admin2
            ));

            project3Issues.add(createIssue(
                    project3,
                    "Security vulnerability in API endpoints",
                    "Some API endpoints are not properly validating user permissions, allowing users to access " +
                    "other users' account information. Critical security issue that needs immediate attention.",
                    IssueStatus.IN_PROGRESS,
                    IssuePriority.CRITICAL,
                    admin2,
                    admin1
            ));

            project3Issues.add(createIssue(
                    project3,
                    "App crashes on iOS 15",
                    "The app is consistently crashing on iOS 15 devices when opening the transaction history screen. " +
                    "Stack trace shows a memory management issue.",
                    IssueStatus.CLOSED,
                    IssuePriority.HIGH,
                    admin1,
                    admin1
            ));

            // Save all issues
            List<Issue> allIssues = new ArrayList<>();
            allIssues.addAll(project1Issues);
            allIssues.addAll(project2Issues);
            allIssues.addAll(project3Issues);
            issueRepository.saveAll(allIssues);
            log.info("Created {} issues across all projects", allIssues.size());

            // Create Comments
            List<Comment> comments = new ArrayList<>();

            // Comments for Project 1 issues
            comments.add(createComment(
                    project1Issues.get(0),
                    admin2,
                    "I can help with the frontend part of this. Should we use JWT tokens or session-based auth?"
            ));
            comments.add(createComment(
                    project1Issues.get(0),
                    admin1,
                    "Let's go with JWT tokens for stateless authentication. It will work better with our microservices architecture."
            ));
            comments.add(createComment(
                    project1Issues.get(0),
                    admin2,
                    "Sounds good! I'll start working on the login and registration forms today."
            ));

            comments.add(createComment(
                    project1Issues.get(1),
                    admin1,
                    "Great work on the initial mockups! Can we add a dark mode option as well?"
            ));
            comments.add(createComment(
                    project1Issues.get(1),
                    admin2,
                    "Absolutely! I'll create both light and dark theme versions. Should be ready by end of week."
            ));

            comments.add(createComment(
                    project1Issues.get(3),
                    admin1,
                    "Found the issue - we're not updating the page count after applying filters. Working on a fix now."
            ));

            comments.add(createComment(
                    project1Issues.get(4),
                    admin1,
                    "Fixed! Added database indexes and implemented eager loading for related entities. Performance improved by 60%."
            ));

            // Comments for Project 2 issues
            comments.add(createComment(
                    project2Issues.get(0),
                    admin1,
                    "Shopping cart backend is ready. Need to integrate with the frontend now."
            ));
            comments.add(createComment(
                    project2Issues.get(0),
                    admin2,
                    "I'll handle the frontend integration. Should have it done by tomorrow."
            ));

            comments.add(createComment(
                    project2Issues.get(1),
                    admin2,
                    "What search engine should we use? Elasticsearch or simple SQL full-text search?"
            ));
            comments.add(createComment(
                    project2Issues.get(1),
                    admin1,
                    "For now, let's start with PostgreSQL full-text search. We can migrate to Elasticsearch later if needed."
            ));

            comments.add(createComment(
                    project2Issues.get(2),
                    admin2,
                    "This is a high priority. We need payment processing before launch. I'll start the Stripe integration today."
            ));

            comments.add(createComment(
                    project2Issues.get(3),
                    admin2,
                    "Issue was caused by unoptimized image sizes. Implemented lazy loading and image compression. Fixed!"
            ));

            // Comments for Project 3 issues
            comments.add(createComment(
                    project3Issues.get(0),
                    admin2,
                    "Biometric authentication is working on iOS. Testing on Android devices now."
            ));
            comments.add(createComment(
                    project3Issues.get(0),
                    admin1,
                    "Excellent! Make sure to handle the case where user denies biometric permissions."
            ));

            comments.add(createComment(
                    project3Issues.get(1),
                    admin1,
                    "Should we include pending transactions in the export as well?"
            ));
            comments.add(createComment(
                    project3Issues.get(1),
                    admin2,
                    "Yes, let's add a checkbox to include/exclude pending transactions."
            ));

            comments.add(createComment(
                    project3Issues.get(2),
                    admin1,
                    "This is critical! We need to fix this ASAP before it's exploited."
            ));
            comments.add(createComment(
                    project3Issues.get(2),
                    admin2,
                    "Agreed. I'm implementing proper permission checks on all endpoints. Will have a fix ready in a few hours."
            ));

            comments.add(createComment(
                    project3Issues.get(3),
                    admin1,
                    "Issue was caused by a memory leak in the chart rendering library. Updated to the latest version and it's fixed!"
            ));

            commentRepository.saveAll(comments);
            log.info("Created {} comments", comments.size());

            log.info("Database seeding completed successfully!");
            log.info("========================================");
            log.info("Test User Credentials:");
            log.info("User 1: admin1@issue-tracker.com / password123");
            log.info("User 2: admin2@issue-tracker.com / password123");
            log.info("========================================");
        };
    }

    private User createUser(String email, String fullName, String password) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        return user;
    }

    private Project createProject(String name, String description, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwner(owner);
        return project;
    }

    private ProjectMember createProjectMember(Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    private Issue createIssue(
            Project project,
            String title,
            String description,
            IssueStatus status,
            IssuePriority priority,
            User assignee,
            User createdBy
    ) {
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setStatus(status);
        issue.setPriority(priority);
        issue.setAssignee(assignee);
        issue.setCreatedBy(createdBy);
        return issue;
    }

    private Comment createComment(Issue issue, User user, String content) {
        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setContent(content);
        return comment;
    }
}
