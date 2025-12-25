# Auth API Module

인증 및 사용자 관리를 담당하는 API 모듈입니다.

## 주요 기능

- **회원 인증**: 회원가입, 로그인, 로그아웃
- **토큰 관리**: JWT 기반 Access/Refresh Token 발급 및 갱신
- **비밀번호 관리**: 비밀번호 설정 및 변경
- **세션 관리**: 다중 세션 제어, 세션 강제 종료
- **보안**: Rate Limiting, 계정 잠금, IP 기반 접근 제어

## 기술 스택

- Kotlin + Spring Boot 3.5.x
- Spring Security
- JWT (jjwt 0.13.0)
- Bucket4j (Rate Limiting)
- Caffeine Cache

## 프로젝트 구조

```
src/main/kotlin/com/starter/api/auth/
├── AuthApplication.kt          # 메인 애플리케이션
├── config/                     # 설정 클래스
│   ├── SecurityConfig.kt       # Spring Security 설정
│   ├── CacheConfig.kt          # 캐시 설정 (Caffeine)
│   ├── AsyncConfig.kt          # 비동기 처리 설정
│   ├── CorsConfig.kt           # CORS 설정
│   └── *Properties.kt          # 설정 프로퍼티
├── controller/                 # REST 컨트롤러
│   ├── AuthController.kt       # 인증 API
│   ├── PasswordController.kt   # 비밀번호 API
│   ├── SessionController.kt    # 세션 관리 API
│   ├── request/                # 요청 DTO
│   └── response/               # 응답 DTO
├── service/                    # 비즈니스 로직
│   ├── auth/                   # 인증 서비스
│   │   ├── AuthenticationService.kt
│   │   ├── PasswordService.kt
│   │   └── LoginAttemptService.kt
│   ├── token/                  # 토큰 서비스
│   ├── session/                # 세션 서비스
│   ├── ratelimit/              # Rate Limit 서비스
│   ├── audit/                  # 감사 로그 서비스
│   └── metrics/                # 메트릭스 서비스
├── security/                   # 보안 필터
│   ├── JwtAuthenticationFilter.kt
│   ├── RateLimitFilter.kt
│   ├── AdminIpFilter.kt
│   └── jwt/                    # JWT 관련
└── validation/                 # 유효성 검증
```

## API 엔드포인트

### 인증 (`/api/v1/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/signin` | 로그인 |
| POST | `/signout` | 로그아웃 |
| POST | `/refresh` | 토큰 갱신 |
| GET | `/me` | 현재 사용자 정보 |
| PATCH | `/me/nickname` | 닉네임 변경 |

### 비밀번호 (`/api/v1/password`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/set` | 비밀번호 설정 |
| POST | `/change` | 비밀번호 변경 |

### 세션 (`/api/v1/sessions`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 활성 세션 목록 |
| DELETE | `/{sessionId}` | 특정 세션 종료 |
| DELETE | `/others` | 다른 세션 모두 종료 |

## 설정 방법

### application.yml

```yaml
# JWT 설정
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 900000      # 15분
  refresh-token-expiration: 604800000  # 7일

# Rate Limiting
rate-limit:
  enabled: true
  requests-per-minute: 60
  auth-requests-per-minute: 10

# 계정 잠금
account-lockout:
  max-attempts: 5
  lockout-duration-minutes: 30

# 세션 관리
session:
  max-sessions-per-user: 5

# CORS
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:5173
```

## 보안 기능

### JWT 인증
- Access Token (15분) + Refresh Token (7일)
- Token Blacklist로 로그아웃 토큰 무효화
- Caffeine 캐시로 성능 최적화

### Rate Limiting
- Bucket4j 기반 IP별 요청 제한
- 인증 API는 더 엄격한 제한 적용

### 계정 잠금
- 연속 로그인 실패 시 계정 잠금
- 잠금 해제 후 실패 횟수 초기화

## 테스트

```bash
# 전체 테스트 실행
./gradlew :api:auth-api:test

# 커버리지 리포트 생성
./gradlew :api:auth-api:jacocoTestReport

# 커버리지 검증 (70% 이상)
./gradlew :api:auth-api:jacocoTestCoverageVerification
```

### 테스트 구성
- **단위 테스트**: Service, Repository 레이어
- **통합 테스트**: 전체 인증 플로우
- **문서 테스트**: REST Docs 기반 API 문서

## 의존성

```kotlin
// build.gradle.kts
dependencies {
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Rate Limiting
    implementation("com.bucket4j:bucket4j_jdk17-core:8.15.0")

    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}
```

## 관련 문서

- [API 문서 (Swagger UI)](http://localhost:8080/swagger-ui.html)
- [OpenAPI Spec](http://localhost:8080/v3/api-docs)
