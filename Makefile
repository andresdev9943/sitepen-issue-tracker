.PHONY: help build up down restart logs clean ps db-connect backend-shell frontend-shell

# Default target
help:
	@echo "Issue Tracker - Docker Commands"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  build          Build all Docker images"
	@echo "  up             Start all services"
	@echo "  up-d           Start all services in detached mode"
	@echo "  down           Stop all services"
	@echo "  restart        Restart all services"
	@echo "  logs           View logs from all services"
	@echo "  logs-backend   View backend logs"
	@echo "  logs-frontend  View frontend logs"
	@echo "  logs-db        View database logs"
	@echo "  ps             Show running containers"
	@echo "  clean          Stop services and remove volumes (⚠️  deletes data)"
	@echo "  db-connect     Connect to PostgreSQL database"
	@echo "  backend-shell  Open shell in backend container"
	@echo "  frontend-shell Open shell in frontend container"
	@echo "  rebuild-backend Rebuild and restart backend"
	@echo "  rebuild-frontend Rebuild and restart frontend"

# Build all images
build:
	docker-compose build

# Start all services
up:
	docker-compose up

# Start all services in detached mode
up-d:
	docker-compose up -d

# Stop all services
down:
	docker-compose down

# Restart all services
restart:
	docker-compose restart

# View logs
logs:
	docker-compose logs -f

logs-backend:
	docker-compose logs -f backend

logs-frontend:
	docker-compose logs -f frontend

logs-db:
	docker-compose logs -f postgres

# Show container status
ps:
	docker-compose ps

# Clean up everything including volumes
clean:
	@echo "⚠️  WARNING: This will delete all database data!"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v; \
	fi

# Connect to database
db-connect:
	docker exec -it issue-tracker-postgres psql -U vostro -d issue_tracker

# Access backend shell
backend-shell:
	docker exec -it issue-tracker-backend sh

# Access frontend shell
frontend-shell:
	docker exec -it issue-tracker-frontend sh

# Rebuild specific services
rebuild-backend:
	docker-compose up --build -d backend

rebuild-frontend:
	docker-compose up --build -d frontend
