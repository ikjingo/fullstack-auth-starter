# Fullstack Auth Starter - Project Overview

**Last Updated**: 2025-12-25

## Project Description

Zenless 프로젝트에서 분리된 인증 전용 스타터 템플릿입니다.
핵심 인증 기능만 포함하여 다른 프로젝트의 기반으로 사용할 수 있습니다.

**GitHub Repository**: https://github.com/ikjingo/fullstack-auth-starter

## Tech Stack

### Backend
- Kotlin + Spring Boot 3.5.8 (Java 21)
- Spring Security + JWT (jjwt 0.13.0)
- Spring Data JPA + PostgreSQL 16
- Bucket4j (Rate Limiting)
- Caffeine Cache

### Frontend
- React 19 + TypeScript 5 + Vite 7
- Tailwind CSS + shadcn/ui
- Zustand (전역 상태) + TanStack Query (서버 상태)
- React Hook Form + Zod

### Infrastructure
- Docker & Docker Compose
- PostgreSQL 16 (Alpine)

## Package Structure

- **Backend**: `com.starter.api.auth`, `com.starter.core`, `com.starter.storage`
- **Frontend**: 기존 Zenless 구조 유지

## Included Features

- 회원가입 (이메일/비밀번호)
- 로그인/로그아웃
- JWT 인증 (Access + Refresh Token)
- 세션 관리 (다중 세션 제어)
- Remember Me 기능
- 비밀번호 변경/설정
- Rate Limiting
- 계정 잠금 (연속 로그인 실패 시)

## Excluded Features (Intentionally Removed)

- Google OAuth 소셜 로그인
- 2FA (Two-Factor Authentication)
- 이메일 기반 비밀번호 재설정

## API Endpoints

### 인증 (`/api/v1/auth`)
- `POST /signup` - 회원가입
- `POST /signin` - 로그인
- `POST /signout` - 로그아웃
- `POST /refresh` - 토큰 갱신
- `GET /me` - 현재 사용자 정보
- `PATCH /me/nickname` - 닉네임 변경

### 비밀번호 (`/api/v1/password`)
- `POST /set` - 비밀번호 설정
- `POST /change` - 비밀번호 변경

### 세션 (`/api/v1/sessions`)
- `GET /` - 활성 세션 목록
- `DELETE /{sessionId}` - 특정 세션 종료
- `DELETE /others` - 다른 세션 모두 종료

## Development Commands

```bash
# PostgreSQL 시작
docker compose -f docker-compose.dev.yml up -d

# Backend 실행
cd backend && ./gradlew :api:app-api:bootRun

# Frontend 실행
cd frontend && npm run dev

# 빌드
cd backend && ./gradlew build
cd frontend && npm run build
```

## Key Decisions

1. **패키지명 변경**: `com.zenless` → `com.starter`
2. **인증 방식**: JWT 기반 (Access + Refresh Token)
3. **소셜 로그인 제외**: 핵심 인증만 포함하여 단순화
4. **2FA 제외**: 복잡도 감소, 필요시 별도 추가 가능
5. **비밀번호 재설정 제외**: 이메일 서비스 의존성 제거
