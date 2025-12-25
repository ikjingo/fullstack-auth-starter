# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

Fullstack Auth Starter is a monorepo authentication template project with:
- **Backend**: Kotlin + Spring Boot 3.5.8 (Java 21)
- **Frontend**: React 19 + TypeScript + Vite
- **Database**: PostgreSQL 16

This project provides core authentication features only (no OAuth, no 2FA).

## Project Structure

```
fullstack-auth-starter/
├── backend/                    # Kotlin Spring Boot 멀티모듈 API
│   ├── api/                    # API 모듈
│   │   ├── app-api/            # 메인 애플리케이션 (모든 API 통합)
│   │   ├── auth-api/           # 인증 API
│   │   ├── nickname-api/       # 닉네임 API
│   │   └── admin-api/          # 관리자 API
│   ├── core/                   # 핵심 모듈
│   │   ├── core-api/           # 공통 API 유틸리티
│   │   └── core-domain/        # 도메인 모델
│   ├── storage/                # 저장소 모듈
│   │   └── db-core/            # JPA 엔티티 및 리포지토리
│   ├── build.gradle.kts
│   └── gradlew
├── frontend/                   # React TypeScript SPA
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   └── nginx.conf
├── docker-compose.yml          # Full stack (postgres + backend + frontend)
├── docker-compose.dev.yml      # PostgreSQL only for local dev
└── dev/                        # 개발 문서
```

> **중요**: 백엔드 실행 시 `app-api` 모듈을 사용합니다. 이 모듈이 모든 API 모듈을 통합합니다.

## Included Features

- 회원가입 (이메일/비밀번호)
- 로그인/로그아웃
- JWT 인증 (Access + Refresh Token)
- 세션 관리 (다중 세션 제어, Remember Me)
- 비밀번호 변경/설정
- Rate Limiting
- 계정 잠금 (연속 로그인 실패 시)

## Excluded Features (Intentionally Removed)

- ❌ Google OAuth 소셜 로그인
- ❌ 2FA (Two-Factor Authentication)
- ❌ 이메일 기반 비밀번호 재설정

## Development Commands

### Docker (Recommended)
```bash
# Start PostgreSQL only (for local development)
docker compose -f docker-compose.dev.yml up -d

# Start full stack
docker compose up -d

# Stop all services
docker compose down
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
```

## Database

### Connection Info
- **Host**: localhost
- **Port**: 5432
- **Database**: zenless
- **Username**: zenless
- **Password**: zenless123

## Tech Stack

### Backend
- Kotlin 1.9+
- Spring Boot 3.5.8
- Spring Data JPA + PostgreSQL 16
- JWT (jjwt 0.13.0)
- Bucket4j (Rate Limiting)
- Caffeine Cache

### Frontend
- React 19 + TypeScript 5 + Vite 7
- Tailwind CSS + shadcn/ui
- Zustand + TanStack Query
- React Hook Form + Zod

## Package Structure

- **Backend**: `com.starter.api.auth`, `com.starter.core`, `com.starter.storage`

## Conventions

### Git Commit Messages
- 형식: `[타입] [영역] 설명`
- 타입: `[feature]`, `[fix]`, `[docs]`, `[refactor]`, `[test]`, `[chore]`
- 영역: `[BE]`, `[FE]`, `[INFRA]`, `[FULL]`

### Pull Request
- PR 제목: `[타입] [영역] 설명`
- Squash merge 사용

## API Endpoints

### 인증 (`/api/v1/auth`)
- `POST /signup` - 회원가입
- `POST /signin` - 로그인
- `POST /signout` - 로그아웃
- `POST /refresh` - 토큰 갱신
- `GET /me` - 현재 사용자 정보

### 비밀번호 (`/api/v1/password`)
- `POST /set` - 비밀번호 설정
- `POST /change` - 비밀번호 변경

### 세션 (`/api/v1/sessions`)
- `GET /` - 활성 세션 목록
- `DELETE /{sessionId}` - 특정 세션 종료
- `DELETE /others` - 다른 세션 모두 종료
