# Fullstack Auth Starter - Project Overview

## Introduction

Fullstack Auth Starter is a production-ready authentication template project built with modern technologies. It provides a solid foundation for building secure web applications with email/password authentication.

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 1.9+ | Primary language |
| Spring Boot | 3.5.8 | Application framework |
| Java | 21 | Runtime |
| PostgreSQL | 16 | Database |
| JWT (jjwt) | 0.13.0 | Token authentication |
| Bucket4j | - | Rate limiting |
| Flyway | - | Database migration |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.2.0 | UI framework |
| TypeScript | 5.9.3 | Type safety |
| Vite | 7.2.4 | Build tool |
| Tailwind CSS | 4.1.17 | Styling |
| Zustand | 5.0.9 | State management |
| React Query | 5.90.11 | Server state |
| React Hook Form | 7.67.0 | Form handling |
| Zod | 4.1.13 | Validation |

## Core Features

### Authentication
- Email/password registration and login
- JWT-based authentication (Access + Refresh tokens)
- BCrypt password hashing
- Password strength validation (uppercase, lowercase, digit, special char)

### Security
- Rate limiting on authentication endpoints
- Account lockout after failed login attempts
- Token blacklisting on logout
- CORS configuration
- Content Security Policy headers
- Admin IP whitelist (optional)

### Session Management
- Multi-session support (up to 5 concurrent sessions)
- Session revocation (single or all other sessions)
- Device/browser info tracking
- Remember Me functionality

### User Management
- Nickname management
- Password set/change
- User profile retrieval

## Intentionally Excluded Features

This template focuses on core authentication only. The following features are intentionally excluded:

- OAuth/Social login (Google, etc.)
- Two-Factor Authentication (2FA)
- Email-based password reset
- Email verification

## Project Structure

```
fullstack-auth-starter/
├── backend/                    # Kotlin Spring Boot multi-module API
│   ├── api/
│   │   ├── app-api/           # Main application (integrates all APIs)
│   │   └── auth-api/          # Authentication API module
│   ├── core/
│   │   ├── core-api/          # Common API utilities
│   │   └── core-domain/       # Domain models
│   └── storage/
│       └── db-core/           # JPA entities & repositories
│
├── frontend/                   # React TypeScript SPA
│   └── src/
│       ├── api/               # API client & integration
│       ├── components/        # UI components
│       ├── pages/             # Page components
│       ├── hooks/             # Custom hooks
│       ├── stores/            # Zustand stores
│       ├── schemas/           # Zod validation schemas
│       └── locales/           # i18n translations
│
├── docker-compose.yml         # Full stack deployment
├── docker-compose.dev.yml     # Development database only
└── CLAUDE.md                  # AI assistant guidelines
```

## Database Schema

### Tables

**users**
- `id` (PK) - Auto-generated ID
- `email` (UNIQUE) - User email address
- `password` - BCrypt hashed password (nullable)
- `nickname` - Display name
- `role` - USER or ADMIN
- `status` - ACTIVE, INACTIVE, or SUSPENDED
- `failedLoginAttempts` - For account lockout
- `lockoutUntil` - Lockout expiration timestamp

**refresh_tokens**
- `id` (PK) - Token ID
- `user_id` (FK) - Reference to users
- `token` (UNIQUE) - JWT refresh token
- `expiresAt` - Token expiration
- `revoked` - Whether token is revoked
- `userAgent`, `ipAddress`, `deviceInfo` - Session metadata

**token_blacklist**
- `id` (PK) - Entry ID
- `token` (UNIQUE) - Blacklisted JWT
- `expiresAt` - When entry can be cleaned up

## Quick Start

### Prerequisites
- Docker & Docker Compose
- JDK 21+
- Node.js 18+

### Running the Project

```bash
# Start PostgreSQL database
docker compose -f docker-compose.dev.yml up -d

# Start backend (terminal 1)
cd backend
./gradlew :api:app-api:bootRun

# Start frontend (terminal 2)
cd frontend
npm install
npm run dev
```

### Access Points
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Documentation Index

- [Architecture Guide](./architecture.md)
- [Backend Development Guide](./backend-guide.md)
- [Frontend Development Guide](./frontend-guide.md)
- [API Reference](./api-reference.md)
- [Development Setup](./development-setup.md)
