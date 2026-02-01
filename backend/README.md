# Backend - Issue Tracker

This is a Spring Boot 3.5.0 application built with Java 17, providing the backend API for the Issue Tracker application.

## Prerequisites

Before running this application, ensure you have the following installed:

- **JDK 17** or higher ([Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java17) or [OpenJDK](https://adoptium.net/))
- **Maven 3.6+** (or use the included Maven Wrapper)

### Verify Java Installation

```bash
java -version
```

You should see output indicating Java 17 or higher.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/issuetracker/backend/
│   │   │       └── BackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/issuetracker/backend/
│               └── BackendApplicationTests.java
├── pom.xml
└── README.md
```

## Dependencies

This project includes the following Spring Boot starters and dependencies:

- **Spring Web** - RESTful web services
- **Spring Data JPA** - Database access with JPA/Hibernate
- **Spring Validation** - Bean validation
- **Spring Actuator** - Production-ready monitoring and management
- **Spring DevTools** - Development time features (auto-restart, live reload)
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **Spring Boot Test** - Testing support

## Running the Application

### Using Maven Wrapper (Recommended)

The project includes Maven Wrapper, so you don't need to install Maven separately.

**On macOS/Linux:**
```bash
./mvnw spring-boot:run
```

**On Windows:**
```cmd
mvnw.cmd spring-boot:run
```

### Using Installed Maven

If you have Maven installed:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Building the Application

### Build JAR file

```bash
./mvnw clean package
```

This creates an executable JAR file in the `target/` directory.

### Run the JAR

```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Testing

### Run all tests

```bash
./mvnw test
```

### Run tests with coverage

```bash
./mvnw test jacoco:report
```

## Database Configuration

The application is configured to use H2 in-memory database by default. You can access the H2 console at:

```
http://localhost:8080/h2-console
```

**Default H2 Connection Settings:**
- JDBC URL: `jdbc:h2:mem:issuetracker`
- Username: `sa`
- Password: (leave empty)

## API Documentation

This project includes **Swagger/OpenAPI** documentation for easy API exploration and testing.

### Access Swagger UI

Once the application is running, you can access the interactive API documentation at:

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

The Swagger UI allows you to:
- 📖 Browse all available API endpoints
- 🧪 Test endpoints directly from the browser
- 📝 View request/response schemas
- 🔍 See detailed parameter information

### OpenAPI Specification

You can also access the raw OpenAPI specification in JSON format:

**OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

This can be imported into tools like Postman, Insomnia, or used to generate client SDKs.

## Actuator Endpoints

Spring Boot Actuator provides several useful endpoints for monitoring:

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`

## Development

### Hot Reload

Spring DevTools enables automatic restart when files change. Simply make changes to your Java files, and the application will automatically restart.

### Project Configuration

Main configuration file: `src/main/resources/application.properties`

You can create environment-specific configuration files:
- `application-dev.properties` - Development
- `application-prod.properties` - Production

Run with specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Common Commands

| Command | Description |
|---------|-------------|
| `./mvnw clean` | Clean build artifacts |
| `./mvnw compile` | Compile the project |
| `./mvnw test` | Run unit tests |
| `./mvnw package` | Create JAR file |
| `./mvnw spring-boot:run` | Run the application |
| `./mvnw dependency:tree` | View dependency tree |

## API Documentation

Once you implement your REST endpoints, consider adding:
- **Swagger/OpenAPI** for API documentation
- **Spring REST Docs** for test-driven documentation

## Next Steps

1. Install JDK 17 if not already installed
2. Run `./mvnw spring-boot:run` to start the application
3. Access `http://localhost:8080` in your browser
4. Begin implementing your Issue Tracker domain models and REST controllers

## Troubleshooting

### Java Version Issues

If you encounter "Unsupported class file major version" errors, ensure you're using JDK 17:

```bash
export JAVA_HOME=/path/to/jdk-17
```

### Port Already in Use

If port 8080 is already in use, you can change it in `application.properties`:

```properties
server.port=8081
```

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Project Lombok](https://projectlombok.org/)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
