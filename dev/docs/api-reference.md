# API Reference

Base URL: `/api/v1`

## Authentication Endpoints

### Sign Up

Create a new user account.

**Endpoint:** `POST /auth/signup`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "nickname": "UserNick"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| email | Required, valid email format, unique |
| password | Required, 8+ chars, uppercase, lowercase, digit, special char |
| nickname | Required, 2-12 characters |

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "UserNick",
      "role": "USER",
      "hasPassword": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600000
  }
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 400 | DUPLICATE_EMAIL | Email already exists |
| 400 | INVALID_REQUEST | Validation failed |

---

### Sign In

Authenticate user and get tokens.

**Endpoint:** `POST /auth/signin`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "UserNick",
      "role": "USER",
      "hasPassword": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600000
  }
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 401 | INVALID_CREDENTIALS | Wrong email or password |
| 423 | ACCOUNT_LOCKED | Account locked due to failed attempts |
| 429 | TOO_MANY_REQUESTS | Rate limit exceeded |

---

### Sign Out

Logout and invalidate tokens.

**Endpoint:** `POST /auth/signout`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "message": "Successfully logged out"
  }
}
```

---

### Refresh Token

Get new access token using refresh token.

**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600000
  }
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 401 | INVALID_TOKEN | Token expired or revoked |

---

### Get Current User

Get authenticated user's information.

**Endpoint:** `GET /auth/me`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "UserNick",
    "role": "USER",
    "createdAt": "2024-01-01T00:00:00Z",
    "hasPassword": true
  }
}
```

---

### Update Nickname

Update user's nickname.

**Endpoint:** `POST /auth/update-nickname`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
  "nickname": "NewNick"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "NewNick",
      "role": "USER",
      "hasPassword": true
    },
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

---

## Password Endpoints

### Set Password

Set password for account without password (e.g., OAuth users).

**Endpoint:** `POST /auth/set-password`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
  "password": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": { ... },
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 400 | PASSWORD_ALREADY_SET | Password already exists |
| 400 | PASSWORD_MISMATCH | Passwords don't match |

---

### Change Password

Change existing password.

**Endpoint:** `POST /auth/change-password`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "message": "Password changed successfully"
  }
}
```

**Error Responses:**
| Status | Code | Description |
|--------|------|-------------|
| 400 | NO_PASSWORD_SET | No password to change |
| 400 | INVALID_CURRENT_PASSWORD | Current password wrong |
| 400 | PASSWORD_MISMATCH | Passwords don't match |

---

## Session Endpoints

### Get Active Sessions

List all active sessions for current user.

**Endpoint:** `GET /auth/sessions`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "createdAt": "2024-01-01T00:00:00Z",
      "expiresAt": "2024-01-08T00:00:00Z",
      "userAgent": "Mozilla/5.0...",
      "ipAddress": "192.168.1.1",
      "deviceInfo": "Chrome on Windows"
    }
  ]
}
```

---

### Revoke Session

Revoke a specific session.

**Endpoint:** `DELETE /auth/sessions/{sessionId}`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "message": "Session revoked successfully"
  }
}
```

---

### Revoke Other Sessions

Revoke all sessions except current.

**Endpoint:** `POST /auth/sessions/revoke-others`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "message": "3 sessions revoked successfully"
  }
}
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "Human readable message",
  "data": null
}
```

### Validation Error with Field Details

```json
{
  "success": false,
  "code": "INVALID_REQUEST",
  "message": "Validation failed",
  "data": {
    "email": "유효한 이메일을 입력해주세요",
    "password": "대문자를 1개 이상 포함해야 합니다"
  }
}
```

---

## Error Codes Reference

### Authentication Errors (4xx)

| Code | Status | Description |
|------|--------|-------------|
| INVALID_REQUEST | 400 | Bad request / validation failed |
| DUPLICATE_EMAIL | 400 | Email already registered |
| PASSWORD_MISMATCH | 400 | Passwords don't match |
| PASSWORD_ALREADY_SET | 400 | Password already exists |
| NO_PASSWORD_SET | 400 | No password to change |
| INVALID_CURRENT_PASSWORD | 400 | Current password is wrong |
| UNAUTHORIZED | 401 | Not authenticated |
| INVALID_CREDENTIALS | 401 | Wrong email/password |
| INVALID_TOKEN | 401 | Token expired/invalid |
| FORBIDDEN | 403 | Access denied |
| NOT_FOUND | 404 | Resource not found |
| USER_NOT_FOUND | 404 | User not found |
| ACCOUNT_LOCKED | 423 | Account locked |
| TOO_MANY_REQUESTS | 429 | Rate limit exceeded |

### Server Errors (5xx)

| Code | Status | Description |
|------|--------|-------------|
| DEFAULT_ERROR | 500 | Internal server error |

---

## Rate Limiting

Authentication endpoints are rate limited:

| Limit | Value |
|-------|-------|
| Capacity | 10 requests |
| Refill | 10 tokens/minute |

**Rate Limit Headers:**
```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 5
```

**Rate Limited Response (429):**
```json
{
  "success": false,
  "code": "TOO_MANY_REQUESTS",
  "message": "Too many requests. Please try again later."
}
```

---

## Account Lockout

Account gets locked after 5 failed login attempts:

| Setting | Value |
|---------|-------|
| Max Attempts | 5 |
| Lock Duration | 15 minutes |

**Locked Account Response (423):**
```json
{
  "success": false,
  "code": "ACCOUNT_LOCKED",
  "message": "Account is locked. Please try again later."
}
```
