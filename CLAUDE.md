# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

Fullstack Auth Starter is a production-ready authentication starter template with:
- **Backend**: Kotlin + Spring Boot 3.5.8 (Java 21)
- **Frontend**: React 19 + TypeScript + Vite
- **Database**: PostgreSQL 16

## Project Structure

```
fullstack-auth-starter/
├── backend/                    # Kotlin Spring Boot multi-module API
│   ├── api/                    # API modules
│   │   ├── app-api/            # Main application (integrates all APIs)
│   │   └── auth-api/           # Authentication API
│   ├── core/                   # Core modules
│   │   ├── core-api/           # Common API utilities
│   │   └── core-domain/        # Domain models
│   ├── storage/                # Storage modules
│   │   └── db-core/            # JPA entities and repositories
│   ├── build.gradle.kts
│   └── gradlew
├── frontend/                   # React TypeScript SPA
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml          # Full stack (PostgreSQL + Backend + Frontend)
└── docker-compose.dev.yml      # PostgreSQL only for local development
```

> **Important**: When running the backend, use the `app-api` module. This module integrates all API modules.

## Development Commands

### Docker (Recommended)
```bash
# Start PostgreSQL only (for local development)
docker compose -f docker-compose.dev.yml up -d

# Start full stack
docker compose up -d

# Stop all services
docker compose down

# Rebuild and start
docker compose up -d --build
```

### Backend (Kotlin + Spring Boot)
```bash
cd backend
./gradlew :api:app-api:bootRun    # Run full application (port 8080)
./gradlew test                    # Run all tests
./gradlew build                   # Build project
./gradlew compileKotlin           # Compile check only
```

### Frontend (React + TypeScript)
```bash
cd frontend
npm run dev            # Run development server (Vite)
npm run build          # Build for production
npm run lint           # Run ESLint
npm run preview        # Preview production build
```

## Database

### Connection Info
- **Host**: localhost
- **Port**: 5432
- **Database**: auth_starter
- **Username**: starter
- **Password**: starter123

### JDBC URL
```
jdbc:postgresql://localhost:5432/auth_starter
```

## Tech Stack

### Backend
- Kotlin 1.9+
- Spring Boot 3.5.8
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL 16
- Flyway (Database migrations)
- JWT (JSON Web Token)
- Spring Validation
- Spring Actuator

### Frontend
- React 19
- TypeScript 5
- Vite 7
- Tailwind CSS
- shadcn/ui
- Zustand (State management)
- TanStack Query (Server state)
- React Hook Form + Zod (Validation)
- lucide-react (Icons)
- ESLint
- Nginx (Production)

### Infrastructure
- Docker & Docker Compose
- PostgreSQL 16 (Alpine)
- Eclipse Temurin JDK 21
- Node.js 20 (Alpine)

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/me` | Get current user info |
| PUT | `/api/auth/password` | Change password |
| POST | `/api/auth/logout` | User logout |

### Sessions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sessions` | Get active sessions |
| DELETE | `/api/sessions/{id}` | Terminate specific session |
| DELETE | `/api/sessions` | Terminate all sessions |

## Conventions

### Git Commit Messages
- Write commit messages in Korean
- Format: `[type] [area] description`
- Types:
  - `[feature]` - New feature
  - `[fix]` - Bug fix
  - `[docs]` - Documentation changes
  - `[refactor]` - Code refactoring
  - `[test]` - Test additions/modifications
  - `[chore]` - Build, configuration, and other changes
- Areas:
  - `[BE]` - Backend (Kotlin/Spring Boot)
  - `[FE]` - Frontend (React/TypeScript)
  - `[INFRA]` - Infrastructure (Docker, Gradle, npm, etc.)
  - `[FULL]` - Fullstack (Both frontend and backend)

### Pull Request
- PR titles follow the same format as commit messages: `[type] description`
- PR body structure:
  ```markdown
  ## Summary
  - Summary of changes (bullet points)

  ## Test plan
  - [ ] Test checklist items

  🤖 Generated with [Claude Code](https://claude.com/claude-code)
  ```
- Use squash merge for PRs

## Environment Variables

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/auth_starter` | Database URL |
| `DATABASE_USERNAME` | `starter` | Database username |
| `DATABASE_PASSWORD` | `starter123` | Database password |
| `SERVER_PORT` | `8080` | Server port |
| `JWT_SECRET` | (required) | JWT signing secret |
| `JWT_EXPIRATION` | `3600000` | Access token expiration (ms) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token expiration (ms) |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | `/api` | API base URL |
| `VITE_APP_NAME` | `Auth Starter` | Application name |

## Architecture

### Backend Multi-Module Structure

```
backend/
├── api/
│   ├── app-api/         # Main entry point, integrates all APIs
│   └── auth-api/        # Authentication domain (controllers, services, security)
├── core/
│   ├── core-api/        # Common API utilities (exceptions, responses)
│   └── core-domain/     # Domain models (if needed)
└── storage/
    └── db-core/         # JPA entities, repositories, migrations
```

### Frontend Structure

```
frontend/src/
├── api/           # API client (axios instances, endpoints)
├── components/    # Reusable components (ui, common, layout)
├── hooks/         # Custom hooks (auth, etc.)
├── pages/         # Page components (PascalCase)
├── schemas/       # Zod schemas for validation
├── stores/        # Zustand stores
├── locales/       # i18n translations
└── lib/           # Utilities
```

## Security Features

- JWT-based authentication (Access Token + Refresh Token)
- Password hashing with BCrypt
- Account lockout after failed login attempts
- Rate limiting on sensitive endpoints
- Token blacklisting on logout
- Session management (multiple device support)
