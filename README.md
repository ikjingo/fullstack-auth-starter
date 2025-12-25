# Fullstack Auth Starter

A production-ready fullstack authentication starter template with Kotlin + Spring Boot backend and React + TypeScript frontend.

## Features

- JWT-based authentication (Access Token + Refresh Token)
- User registration and login
- Password change functionality
- Session management with "Remember Me" option
- Multi-language support (Korean/English)
- Dark/Light theme toggle
- PostgreSQL database
- Docker Compose for easy deployment

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

### Frontend
- React 19
- TypeScript 5
- Vite 7
- Tailwind CSS
- shadcn/ui
- Zustand (State management)
- TanStack Query (Server state)
- React Hook Form + Zod (Form validation)

### Infrastructure
- Docker & Docker Compose
- Nginx (Production)
- PostgreSQL 16 (Alpine)

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
│   └── storage/                # Storage modules
│       └── db-core/            # JPA entities and repositories
├── frontend/                   # React TypeScript SPA
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml          # Full stack (PostgreSQL + Backend + Frontend)
└── docker-compose.dev.yml      # PostgreSQL only for local development
```

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for local frontend development)
- JDK 21+ (for local backend development)

### Using Docker (Recommended)

```bash
# Start PostgreSQL only (for local development)
docker compose -f docker-compose.dev.yml up -d

# Start full stack
docker compose up -d

# Stop all services
docker compose down
```

### Local Development

#### Backend

```bash
cd backend
./gradlew :api:app-api:bootRun    # Run application (port 8080)
./gradlew test                    # Run tests
./gradlew build                   # Build project
```

#### Frontend

```bash
cd frontend
npm install                       # Install dependencies
npm run dev                       # Run development server
npm run build                     # Build for production
```

## Database

### Connection Info (Default)
- Host: localhost
- Port: 5432
- Database: auth_starter
- Username: starter
- Password: starter123

### JDBC URL
```
jdbc:postgresql://localhost:5432/auth_starter
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/me` | Get current user info |
| PUT | `/api/auth/password` | Change password |

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

## Development

### Commit Convention

- Use Korean for commit messages
- Format: `[type] [area] description`
- Types: `[feature]`, `[fix]`, `[docs]`, `[refactor]`, `[test]`, `[chore]`
- Areas: `[BE]`, `[FE]`, `[INFRA]`, `[FULL]`

Example:
```
[feature] [BE] 회원가입 API 구현
[fix] [FE] 로그인 폼 유효성 검사 수정
```

## License

MIT License - See [LICENSE](LICENSE) for details.
