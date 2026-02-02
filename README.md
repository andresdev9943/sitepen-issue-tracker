# Issue Tracker

A full-stack issue tracking application with Angular frontend and Spring Boot backend.

## 🚀 Quick Start

**Want to start immediately?** See [QUICK_START.md](QUICK_START.md) for a fast setup guide!

```bash
docker-compose up --build
# Then visit http://localhost:4200
# Login: admin1@issue-tracker.com / password123
```

## Project Structure

```
issue-tracker/
├── frontend/          # Angular 19 application
│   ├── src/
│   ├── package.json
│   ├── angular.json
│   └── README.md
│
└── backend/           # Spring Boot 3.5.0 application (Java 17)
    ├── src/
    ├── pom.xml
    └── README.md
```

## Getting Started

### Prerequisites

- **Node.js** v20.18+ and npm (for frontend)
- **JDK 17** or higher (for backend)
- **Maven** 3.6+ (or use included Maven Wrapper)

### Frontend Setup

The frontend is built with Angular 19, TypeScript, ESLint, and Prettier.

```bash
cd frontend
npm install
npm start
```

The Angular app will be available at `http://localhost:4200`

For detailed frontend documentation, see [frontend/README.md](frontend/README.md)

### Backend Setup

The backend is built with Spring Boot 3.5.0 and Java 17.

**Note:** You need to install JDK 17 first if not already installed.

```bash
cd backend
./mvnw spring-boot:run
```

The Spring Boot API will be available at `http://localhost:8080`

For detailed backend documentation, see [backend/README.md](backend/README.md)

## Docker Setup (Recommended for Development)

The easiest way to run the entire stack (PostgreSQL + Backend + Frontend) is using Docker Compose.

### Prerequisites
- Docker (version 20.10+)
- Docker Compose (version 2.0+)

### Quick Start with Docker

```bash
# Build and start all services
docker-compose up --build

# Or use the Makefile
make up
```

This will start:
- **PostgreSQL** database on port 5432
- **Backend** API on port 8080
- **Frontend** app on port 4200

**Test User Credentials:**
- Email: `admin1@issue-tracker.com` / Password: `password123`
- Email: `admin2@issue-tracker.com` / Password: `password123`

The database is automatically seeded with sample projects, issues, and comments. See [SEED_DATA.md](SEED_DATA.md) for complete details.

### Docker Commands

```bash
# Start services
make up              # Start all services (foreground)
make up-d            # Start all services (background)

# View logs
make logs            # All services
make logs-backend    # Backend only
make logs-frontend   # Frontend only

# Stop services
make down            # Stop all services

# Database access
make db-connect      # Connect to PostgreSQL

# Clean up (⚠️ deletes data)
make clean           # Remove all containers and volumes
```

For detailed Docker documentation, see [DOCKER_SETUP.md](DOCKER_SETUP.md)

## Quick Commands

### Frontend

| Command | Description |
|---------|-------------|
| `npm start` | Start dev server (port 4200) |
| `npm run build` | Build for production |
| `npm run lint` | Check code quality |
| `npm run lint:fix` | Auto-fix linting issues |
| `npm run format` | Format code with Prettier |
| `npm test` | Run unit tests |

### Backend

| Command | Description |
|---------|-------------|
| `./mvnw spring-boot:run` | Start Spring Boot app (port 8080) |
| `./mvnw clean package` | Build JAR file |
| `./mvnw test` | Run tests |
| `./mvnw clean` | Clean build artifacts |

## Architecture

### Frontend (Angular 19)
- **Framework:** Angular 19 with TypeScript
- **Styling:** SCSS
- **Routing:** Angular Router
- **Code Quality:** ESLint + Prettier
- **Testing:** Jasmine + Karma

### Backend (Spring Boot 3.5.0)
- **Language:** Java 17
- **Framework:** Spring Boot 3.5.0
- **Database:** H2 (in-memory, development)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Key Dependencies:**
  - Spring Web (REST API)
  - Spring Data JPA (Database access)
  - Spring Validation (Input validation)
  - Spring Actuator (Monitoring)
  - Lombok (Code generation)
  - H2 Database (Development database)

## API Endpoints

Once both applications are running:

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api
- **API Health:** http://localhost:8080/api/health
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Docs:** http://localhost:8080/v3/api-docs
- **H2 Console:** http://localhost:8080/h2-console
- **Actuator:** http://localhost:8080/actuator

## Development Workflow

1. **Start Backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Start Frontend:**
   ```bash
   cd frontend
   npm start
   ```

3. **Access Application:**
   - Open browser to `http://localhost:4200`
   - Frontend will proxy API calls to backend on port 8080

## Configuration

### CORS Configuration
The backend is pre-configured to allow CORS requests from `http://localhost:4200` (Angular dev server).

### Database
- **Docker (Recommended):** PostgreSQL in Docker container
- **Local Development:** PostgreSQL on localhost or H2 in-memory database
- **Production:** Configure in `backend/src/main/resources/application-prod.properties`

### Profiles

Backend supports multiple profiles:
- **Default:** Basic configuration
- **Dev:** Development settings with local PostgreSQL (`application-dev.properties`)
- **Docker:** Docker environment with containerized PostgreSQL (`application-docker.properties`)
- **Prod:** Production settings (`application-prod.properties`)

Run with profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Project Features

### Current Features
✅ Angular 19 frontend with TypeScript
✅ Spring Boot 3.5.0 backend with Java 17
✅ ESLint + Prettier configured
✅ CORS enabled for frontend-backend communication
✅ H2 database for development
✅ Global exception handling
✅ Spring Actuator for monitoring
✅ Maven Wrapper included
✅ Environment-specific configurations
✅ Swagger/OpenAPI documentation

### Ready to Implement
- User authentication & authorization
- Issue CRUD operations
- Issue status management
- User management
- Comments system
- File attachments
- Search & filtering
- Notifications

## Next Steps

1. **Install JDK 17** if not already installed
2. **Run backend:** `cd backend && ./mvnw spring-boot:run`
3. **Run frontend:** `cd frontend && npm start`
4. **Start building features!**

## Troubleshooting

### JDK 17 Not Found
Make sure JDK 17 is installed and JAVA_HOME is set correctly:
```bash
export JAVA_HOME=/path/to/jdk-17
```

### Port Already in Use
- Frontend: Change port in `angular.json` or use `ng serve --port 4201`
- Backend: Change port in `application.properties`: `server.port=8081`

### Node Version Issues
Angular 19 works with Node.js v20.18+. Angular 20 requires Node.js v20.19+ or v22.12+.

## License

This project is for educational/development purposes.

## Contributing

1. Create feature branches
2. Follow code style (ESLint/Prettier for frontend)
3. Write tests for new features
4. Submit pull requests

---

**Tech Stack:** Angular 19 | Spring Boot 3.5 | TypeScript | Java 17 | H2 Database | Maven | npm
