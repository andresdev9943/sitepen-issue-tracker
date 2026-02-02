# Issue Tracker

A full-stack issue tracking application built with Angular and Spring Boot for managing software development projects, issues, and team collaboration.

---

## 1. About the App

Issue Tracker is a comprehensive project management and issue tracking system designed for software development teams. The application enables teams to:

- **Manage Projects**: Create and organize multiple projects with team members and role-based access control
- **Track Issues**: Create, assign, and monitor issues with customizable statuses (OPEN, IN_PROGRESS, IN_REVIEW, DONE, CLOSED) and priorities (LOW, MEDIUM, HIGH, CRITICAL)
- **Collaborate**: Add comments, track activity logs, and communicate with team members in real-time
- **Monitor Progress**: View project dashboards, filter issues, and track team productivity
- **Real-time Updates**: Receive instant notifications through Server-Sent Events (SSE) when issues are updated

The application follows modern full-stack architecture with a RESTful API backend and a responsive Angular frontend, providing a seamless user experience for issue management across devices.

---

## 2. Used Tech-Stacks

### Frontend

- **Framework**: Angular 19
- **Language**: TypeScript 5.7+
- **Styling**: SCSS
- **HTTP Client**: Angular HttpClient with RxJS
- **Routing**: Angular Router
- **State Management**: RxJS BehaviorSubject patterns
- **Code Quality**: ESLint + Prettier
- **Testing**: Jasmine + Karma
- **Build Tool**: Angular CLI with esbuild

### Backend

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17
- **Database**: PostgreSQL 16 (production), H2 (development)
- **ORM**: Spring Data JPA / Hibernate
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security 6
- **API Documentation**: SpringDoc OpenAPI 3 / Swagger
- **Build Tool**: Maven 3.6+
- **Additional Libraries**:
  - Spring Validation (input validation)
  - Spring Actuator (monitoring & health checks)

### DevOps & Infrastructure

- **Containerization**: Docker & Docker Compose
- **Database**: PostgreSQL 16 (Docker container)
- **Environment Management**: Spring Profiles (dev, docker, prod)

---

## 3. Database Schema Design

The application uses a PostgreSQL relational database with 6 core tables:

### `users`

Stores user account information and authentication credentials.

| Column        | Type         | Constraints             | Description                |
| ------------- | ------------ | ----------------------- | -------------------------- |
| id            | UUID         | PRIMARY KEY             | Unique user identifier     |
| email         | VARCHAR(255) | NOT NULL, UNIQUE        | User email (login)         |
| password_hash | VARCHAR(255) | NOT NULL                | Bcrypt hashed password     |
| full_name     | VARCHAR(255) | NOT NULL                | User's full name           |
| created_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW() | Account creation timestamp |
| updated_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW() | Last update timestamp      |

---

### `projects`

Stores project information and metadata.

| Column      | Type         | Constraints                      | Description                |
| ----------- | ------------ | -------------------------------- | -------------------------- |
| id          | UUID         | PRIMARY KEY                      | Unique project identifier  |
| name        | VARCHAR(255) | NOT NULL                         | Project name               |
| description | TEXT         | NULL                             | Project description        |
| owner_id    | UUID         | NOT NULL, FOREIGN KEY (users.id) | Project owner              |
| created_at  | TIMESTAMP    | NOT NULL, DEFAULT NOW()          | Project creation timestamp |
| updated_at  | TIMESTAMP    | NOT NULL, DEFAULT NOW()          | Last update timestamp      |

---

### `project_members`

Junction table for many-to-many relationship between users and projects with role information.

| Column     | Type        | Constraints                         | Description                   |
| ---------- | ----------- | ----------------------------------- | ----------------------------- |
| id         | UUID        | PRIMARY KEY                         | Unique membership identifier  |
| project_id | UUID        | NOT NULL, FOREIGN KEY (projects.id) | Reference to project          |
| user_id    | UUID        | NOT NULL, FOREIGN KEY (users.id)    | Reference to user             |
| role       | VARCHAR(50) | NOT NULL, ENUM                      | User role in project          |
| joined_at  | TIMESTAMP   | NOT NULL, DEFAULT NOW()             | Membership creation timestamp |

---

### `issues`

Stores issue/task information within projects.

| Column      | Type         | Constraints                         | Description                |
| ----------- | ------------ | ----------------------------------- | -------------------------- |
| id          | UUID         | PRIMARY KEY                         | Unique issue identifier    |
| project_id  | UUID         | NOT NULL, FOREIGN KEY (projects.id) | Parent project             |
| title       | VARCHAR(255) | NOT NULL                            | Issue title                |
| description | TEXT         | NULL                                | Detailed issue description |
| status      | VARCHAR(50)  | NOT NULL, ENUM, DEFAULT 'OPEN'      | Current issue status       |
| priority    | VARCHAR(50)  | NOT NULL, ENUM, DEFAULT 'MEDIUM'    | Issue priority level       |
| assignee_id | UUID         | NULL, FOREIGN KEY (users.id)        | Assigned user (optional)   |
| created_by  | UUID         | NOT NULL, FOREIGN KEY (users.id)    | Issue creator              |
| created_at  | TIMESTAMP    | NOT NULL, DEFAULT NOW()             | Issue creation timestamp   |
| updated_at  | TIMESTAMP    | NOT NULL, DEFAULT NOW()             | Last update timestamp      |

---

### `comments`

Stores user comments on issues.

| Column     | Type      | Constraints                       | Description                |
| ---------- | --------- | --------------------------------- | -------------------------- |
| id         | UUID      | PRIMARY KEY                       | Unique comment identifier  |
| issue_id   | UUID      | NOT NULL, FOREIGN KEY (issues.id) | Parent issue               |
| user_id    | UUID      | NOT NULL, FOREIGN KEY (users.id)  | Comment author             |
| content    | TEXT      | NOT NULL                          | Comment content            |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW()           | Comment creation timestamp |

---

### `activity_log`

Audit trail for tracking all actions performed on issues.

| Column     | Type         | Constraints                       | Description                |
| ---------- | ------------ | --------------------------------- | -------------------------- |
| id         | UUID         | PRIMARY KEY                       | Unique activity identifier |
| issue_id   | UUID         | NOT NULL, FOREIGN KEY (issues.id) | Related issue              |
| user_id    | UUID         | NOT NULL, FOREIGN KEY (users.id)  | User who performed action  |
| action     | VARCHAR(255) | NOT NULL                          | Action type/description    |
| details    | TEXT         | NULL                              | Additional action details  |
| created_at | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | Action timestamp           |

---

## 4. User Scenarios

### Scenario 1: Project Manager Creating a New Project

Sarah, a project manager, logs into the Issue Tracker to start a new software project. She:

1. Creates a project named "Mobile App Redesign"
2. Adds team members (developers, designers, QA engineers) with appropriate roles
3. Sets project description and metadata
4. Begins creating initial issues for sprint planning

### Scenario 2: Developer Working on Issues

John, a backend developer:

1. Logs in and views his assigned issues on the project dashboard
2. Selects a HIGH priority issue: "Implement user authentication API"
3. Changes status from OPEN to IN_PROGRESS
4. Adds comments with progress updates and code references
5. Marks issue as IN_REVIEW when complete
6. Team members receive real-time notifications of the update

### Scenario 3: Team Collaboration

The development team:

1. Reviews issues in the IN_REVIEW status during daily standup
2. QA engineer tests the feature and adds comments with test results
3. Team discusses blocking issues with CRITICAL priority
4. Project manager reassigns issues based on workload
5. All activity is logged with timestamps and user information

### Scenario 4: Filtering and Search

Lisa, a tech lead:

1. Filters issues by status (IN_PROGRESS) to see active work
2. Filters by priority (CRITICAL, HIGH) to identify urgent tasks
3. Views issues assigned to specific team members
4. Exports or reviews activity logs for sprint retrospective

---

## 5. How to Start

### Option A: Docker Compose (Recommended - Fastest Setup)

**Prerequisites:**

- Docker (20.10+)
- Docker Compose (2.0+)

**Quick Start:**

```bash
# Clone the repository
git clone https://github.com/andresdev9943/sitepen-issue-tracker.git
cd issue-tracker

# Copy environment file
cp .env.example .env

# Start all services (PostgreSQL + Backend + Frontend)
docker-compose up --build

# Access the application
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**Default Login Credentials:**

- Email: `admin1@issue-tracker.com`
- Password: `password123`

- Email: `admin2@issue-tracker.com`
- Password: `password123`

The database is automatically seeded with sample projects, issues, and comments.

### Option B: Local Development Setup

**Prerequisites:**

- Node.js v20.18+
- JDK 17+
- Maven 3.6+ (or use included Maven Wrapper)
- PostgreSQL 12+ (or use H2 in-memory database)

**Backend Setup:**

```bash
# Navigate to backend directory
cd backend

# Run with H2 in-memory database
./mvnw spring-boot:run

# Or run with local PostgreSQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend will start at `http://localhost:8080`

**Frontend Setup:**

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend will start at `http://localhost:4200`

### Useful Commands

```bash
# Docker commands
docker-compose up -d          # Start in background
docker-compose logs -f        # View logs
docker-compose down           # Stop services
docker-compose down -v        # Stop and remove volumes
```

---

## 6. Potential Improvements

**1. User Management and Security**

- Implement Multi-tenancy support
- OAuth2 Integration
- SSO with SAML

**2. Project Management and Issue tracking**

- Burndown charts for sprints
- Linear-like view for Issue view
- Rich-Text editor for project, issue and comment editing
- File attachment to the editors

**3. AI integration**

- Setup AI-Agent bot for summarizing the issue and project
- Integrate the LLMs and attach it to the editors for AI-powered content refinement

---
