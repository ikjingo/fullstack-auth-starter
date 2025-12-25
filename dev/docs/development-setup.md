# Development Setup Guide

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Docker | Latest | Database container |
| Docker Compose | Latest | Container orchestration |
| JDK | 21+ | Backend runtime |
| Node.js | 18+ | Frontend runtime |
| npm | 9+ | Package manager |

### Recommended Tools

| Tool | Purpose |
|------|---------|
| IntelliJ IDEA | Kotlin/Spring development |
| VS Code | Frontend development |
| Postman | API testing |
| pgAdmin | Database management |

---

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd fullstack-auth-starter
```

### 2. Start PostgreSQL

```bash
docker compose -f docker-compose.dev.yml up -d
```

Verify database is running:
```bash
docker compose -f docker-compose.dev.yml ps
```

### 3. Configure Backend Environment

Create `.env` file in the `backend` directory:

```bash
cd backend
cp .env.example .env
```

Edit `.env` and set JWT_SECRET (minimum 32 characters):
```env
JWT_SECRET=your-very-long-secret-key-at-least-32-chars
```

### 4. Start Backend

```bash
cd backend
./gradlew :api:app-api:bootRun
```

Backend will start on http://localhost:8080

### 5. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend will start on http://localhost:5173

---

## Access Points

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:5173 | React application |
| Backend API | http://localhost:8080 | Spring Boot API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | OpenAPI spec |

---

## Database Configuration

### Connection Details

| Property | Value |
|----------|-------|
| Host | localhost |
| Port | 5432 |
| Database | zenless |
| Username | zenless |
| Password | zenless123 |

### JDBC URL
```
jdbc:postgresql://localhost:5432/zenless
```

### Direct Connection

```bash
# Using Docker
docker compose -f docker-compose.dev.yml exec postgres psql -U zenless -d zenless

# Using psql directly
psql -h localhost -p 5432 -U zenless -d zenless
```

---

## Backend Development

### Gradle Commands

```bash
cd backend

# Run application
./gradlew :api:app-api:bootRun

# Build project
./gradlew build

# Compile only (fast check)
./gradlew compileKotlin

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| JWT_SECRET | Yes | - | JWT signing key (min 32 chars) |

### Run with Environment Variable

```bash
JWT_SECRET=your-32-character-secret-key-here ./gradlew :api:app-api:bootRun
```

### Application Profiles

```bash
# Development (default)
./gradlew :api:app-api:bootRun

# Test profile
./gradlew :api:app-api:bootRun --args='--spring.profiles.active=test'

# Production profile
./gradlew :api:app-api:bootRun --args='--spring.profiles.active=prod'
```

---

## Frontend Development

### npm Commands

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter
npm run lint

# Run tests
npm run test
```

### Development Server

The development server runs on port 5173 with:
- Hot Module Replacement (HMR)
- API proxy to http://localhost:8080

### Build Output

Production build output is in `dist/` directory.

---

## Docker Compose

### Development Only (Database)

```bash
# Start PostgreSQL
docker compose -f docker-compose.dev.yml up -d

# Stop PostgreSQL
docker compose -f docker-compose.dev.yml down

# View logs
docker compose -f docker-compose.dev.yml logs -f

# Reset database
docker compose -f docker-compose.dev.yml down -v
docker compose -f docker-compose.dev.yml up -d
```

### Full Stack

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# Rebuild images
docker compose build

# View logs
docker compose logs -f
```

---

## Database Migrations

Database schema is managed by Flyway.

### Migration Location
```
backend/storage/db-core/src/main/resources/db/migration/
```

### Migration Files
```
V1__initial_schema.sql
V2__add_version_column.sql
V3__add_performance_indexes.sql
V4__add_two_factor_fields.sql  (empty)
V5__add_session_info_fields.sql
V6__add_composite_indexes.sql
```

### Run Migrations Manually

Migrations run automatically on application startup.

To run manually:
```bash
cd backend
./gradlew :storage:db-core:flywayMigrate
```

---

## IDE Setup

### IntelliJ IDEA (Backend)

1. Open `backend` folder as Gradle project
2. Enable annotation processing:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"
3. Set JDK 21 as project SDK
4. Import Gradle project

### VS Code (Frontend)

Recommended extensions:
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- TypeScript Vue Plugin (Volar)

Settings:
```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "typescript.preferences.importModuleSpecifier": "relative"
}
```

---

## Troubleshooting

### Backend Won't Start

**Error: JWT secret must be at least 32 characters**
```bash
# Set JWT_SECRET environment variable
export JWT_SECRET=your-very-long-secret-key-at-least-32-chars
./gradlew :api:app-api:bootRun
```

**Error: Connection refused to database**
```bash
# Check if PostgreSQL is running
docker compose -f docker-compose.dev.yml ps

# Start PostgreSQL if needed
docker compose -f docker-compose.dev.yml up -d
```

**Error: Port 8080 already in use**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Frontend Won't Start

**Error: Port 5173 already in use**
```bash
# Find process using port
lsof -i :5173

# Or use different port
npm run dev -- --port 3000
```

**Error: Module not found**
```bash
# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Database Issues

**Reset database completely**
```bash
docker compose -f docker-compose.dev.yml down -v
docker compose -f docker-compose.dev.yml up -d
```

**Check database connection**
```bash
docker compose -f docker-compose.dev.yml exec postgres pg_isready
```

---

## Testing Accounts

After setting up the project, you can create test accounts via the registration page:

| Email | Password | Notes |
|-------|----------|-------|
| test@example.com | Test1234! | Standard test user |
| admin@example.com | Admin1234! | Admin test user |

---

## Project Structure Reference

```
fullstack-auth-starter/
├── backend/
│   ├── api/
│   │   ├── app-api/          # Main application
│   │   └── auth-api/         # Auth module
│   ├── core/
│   │   ├── core-api/         # Common utilities
│   │   └── core-domain/      # Domain models
│   ├── storage/
│   │   └── db-core/          # Database layer
│   ├── build.gradle.kts
│   ├── gradlew
│   └── .env                  # Environment variables
│
├── frontend/
│   ├── src/                  # Source code
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
│
├── docker-compose.yml        # Full stack
├── docker-compose.dev.yml    # PostgreSQL only
└── CLAUDE.md                 # AI guidelines
```
