# Architecture Guide

## Overview

The project follows a clean architecture pattern with clear separation of concerns between layers.

## Backend Architecture

### Multi-Module Structure

```
backend/
├── api/
│   ├── app-api/       ← Entry point (Port 8080)
│   └── auth-api/      ← Authentication business logic
├── core/
│   ├── core-api/      ← Shared API utilities, error handling
│   └── core-domain/   ← Domain models (no dependencies)
└── storage/
    └── db-core/       ← JPA entities, repositories
```

### Module Dependencies

```
app-api
  └── auth-api
        ├── core-api
        ├── core-domain
        └── db-core
              └── core-domain
```

### Layer Responsibilities

| Layer | Module | Responsibility |
|-------|--------|----------------|
| Presentation | auth-api | Controllers, Request/Response DTOs, Validation |
| Business | auth-api | Services, Business logic |
| Infrastructure | auth-api | Security filters, Configuration |
| Persistence | db-core | Entities, Repositories, Migrations |
| Shared | core-api | Error handling, Common utilities |
| Domain | core-domain | Domain models, Enums |

## Authentication Flow

### JWT Token Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Token Types                                 │
├─────────────────────────────────────────────────────────────────┤
│  Access Token                     │  Refresh Token              │
│  ─────────────                    │  ─────────────              │
│  • Short-lived (1 hour)           │  • Long-lived (7 days)      │
│  • Stored in memory (frontend)    │  • Stored in localStorage   │
│  • Contains: userId, email, roles │  • Stored in DB (backend)   │
│  • Used for API authorization     │  • Used to get new tokens   │
└─────────────────────────────────────────────────────────────────┘
```

### Sign-Up Flow

```
Client                    Backend                        Database
  │                          │                              │
  │  POST /auth/signup       │                              │
  │  (email, password,       │                              │
  │   nickname)              │                              │
  │─────────────────────────>│                              │
  │                          │  Check duplicate email       │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  Hash password (BCrypt)      │
  │                          │                              │
  │                          │  Create UserEntity           │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  Generate JWT tokens         │
  │                          │                              │
  │                          │  Save RefreshToken           │
  │                          │─────────────────────────────>│
  │                          │                              │
  │  { user, accessToken,    │                              │
  │    refreshToken }        │                              │
  │<─────────────────────────│                              │
```

### Sign-In Flow

```
Client                    Backend                        Database
  │                          │                              │
  │  POST /auth/signin       │                              │
  │  (email, password)       │                              │
  │─────────────────────────>│                              │
  │                          │  Find user by email          │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  Check account lockout       │
  │                          │                              │
  │                          │  Verify password (BCrypt)    │
  │                          │                              │
  │                          │  [On failure]                │
  │                          │  Record failed attempt       │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  [On success]                │
  │                          │  Reset failed attempts       │
  │                          │  Generate tokens             │
  │                          │  Enforce session limit       │
  │                          │─────────────────────────────>│
  │                          │                              │
  │  { user, tokens }        │                              │
  │<─────────────────────────│                              │
```

### Token Refresh Flow

```
Client                    Backend                        Database
  │                          │                              │
  │  POST /auth/refresh      │                              │
  │  (refreshToken)          │                              │
  │─────────────────────────>│                              │
  │                          │  Validate JWT signature      │
  │                          │                              │
  │                          │  Find RefreshToken in DB     │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  Check not revoked/expired   │
  │                          │                              │
  │                          │  Delete old token            │
  │                          │─────────────────────────────>│
  │                          │                              │
  │                          │  Generate new tokens         │
  │                          │  Save new RefreshToken       │
  │                          │─────────────────────────────>│
  │                          │                              │
  │  { accessToken,          │                              │
  │    refreshToken }        │                              │
  │<─────────────────────────│                              │
```

### Request Authentication Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    Security Filter Chain                        │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Request ─┬─> ContentSecurityPolicyFilter (CSP headers)        │
│           │                                                     │
│           ├─> JwtAuthenticationFilter                          │
│           │     1. Extract Bearer token                         │
│           │     2. Validate signature & expiration              │
│           │     3. Check token blacklist                        │
│           │     4. Set SecurityContext                          │
│           │                                                     │
│           ├─> RateLimitFilter (auth endpoints only)            │
│           │     - Token bucket algorithm (Bucket4j)             │
│           │     - 10 requests/minute per IP                     │
│           │                                                     │
│           ├─> AdminIpFilter (admin endpoints only)             │
│           │     - IP whitelist validation                       │
│           │                                                     │
│           └─> Controller                                        │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

## Frontend Architecture

### Directory Structure

```
src/
├── api/           # API layer
│   ├── client/    # HTTP client, token management
│   └── *.ts       # Domain-specific API functions
│
├── components/    # UI layer
│   ├── ui/        # Base components (shadcn/ui)
│   ├── common/    # Reusable components
│   └── layout/    # Layout components
│
├── pages/         # Page components
│
├── hooks/         # Custom hooks
│   ├── auth/      # Authentication hooks
│   ├── common/    # Shared hooks
│   └── ui/        # UI-specific hooks
│
├── stores/        # State management (Zustand)
│
├── schemas/       # Validation schemas (Zod)
│
├── locales/       # i18n translations
│
└── utils/         # Utility functions
    └── styles/    # Tailwind CSS utilities
```

### State Management Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    State Management                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Server State (React Query)     Client State (Zustand)      │
│  ──────────────────────────     ─────────────────────       │
│  • API responses                • useUserStore              │
│  • Caching                        - user info               │
│  • Background refetching          - auth state              │
│  • Mutation handling                                        │
│                                 • useThemeStore             │
│                                   - dark/light mode         │
│                                                              │
│                                 • useLanguageStore          │
│                                   - ko/en language          │
│                                                              │
│                                 • useToastStore             │
│                                   - notifications           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Token Management

```
┌─────────────────────────────────────────────────────────────┐
│                    Token Storage                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Access Token                    Refresh Token               │
│  ────────────                    ─────────────               │
│  Storage: Memory (variable)      Storage: localStorage       │
│  Reason: XSS protection          Reason: Session persistence │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Auto-Refresh Flow                       │    │
│  │                                                      │    │
│  │  1. API call returns 401                            │    │
│  │  2. tokenManager.refreshAccessToken()               │    │
│  │  3. POST /auth/refresh with refreshToken            │    │
│  │  4. Store new tokens                                │    │
│  │  5. Retry original request                          │    │
│  │  6. If refresh fails → logout                       │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Security Architecture

### Password Validation

Both frontend and backend enforce the same password rules:

| Rule | Regex | Message |
|------|-------|---------|
| Minimum length | `.{8,}` | 8자 이상 |
| Uppercase | `/[A-Z]/` | 대문자 포함 |
| Lowercase | `/[a-z]/` | 소문자 포함 |
| Digit | `/[0-9]/` | 숫자 포함 |
| Special char | `/[!@#$%^&*()...]/` | 특수문자 포함 |

### Account Lockout

```
Configuration:
  - maxFailedAttempts: 5
  - lockDurationMinutes: 15

Flow:
  1. Login attempt fails
  2. Increment failedLoginAttempts
  3. If attempts >= 5:
     - Set lockoutUntil = now + 15 minutes
  4. On successful login:
     - Reset failedLoginAttempts to 0
```

### Rate Limiting

```
Configuration:
  - Capacity: 10 tokens
  - Refill: 10 tokens per minute

Protected endpoints:
  - POST /auth/signin
  - POST /auth/signup
  - POST /auth/refresh

Response headers:
  - X-RateLimit-Limit: 10
  - X-RateLimit-Remaining: <remaining>
```

### Session Management

```
Configuration:
  - maxSessionsPerUser: 5

Session enforcement:
  - On new login: Check session count
  - If count >= max: Revoke oldest session
  - Each session tracks: userAgent, ipAddress, deviceInfo
```

## Error Handling

### Backend Error Types

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | INVALID_REQUEST | Bad request |
| 400 | DUPLICATE_EMAIL | Email already exists |
| 400 | PASSWORD_MISMATCH | Passwords don't match |
| 401 | UNAUTHORIZED | Not authenticated |
| 401 | INVALID_CREDENTIALS | Wrong email/password |
| 401 | INVALID_TOKEN | Invalid JWT |
| 403 | FORBIDDEN | Access denied |
| 404 | NOT_FOUND | Resource not found |
| 404 | USER_NOT_FOUND | User not found |
| 423 | ACCOUNT_LOCKED | Account locked |
| 429 | TOO_MANY_REQUESTS | Rate limited |
| 500 | DEFAULT_ERROR | Server error |

### Frontend Error Handling

```typescript
// Error class with type inference
class ApiError extends Error {
  code: string
  status: number
  data?: unknown
  errorType?: ErrorTypeValue

  isAuthError(): boolean
  isValidationError(): boolean
  isServerError(): boolean
}

// Form validation error handling
if (apiError.isValidationError() && apiError.data) {
  const fieldErrors = apiError.data as Record<string, string>
  Object.entries(fieldErrors).forEach(([field, message]) => {
    form.setError(field, { type: 'server', message })
  })
}
```
