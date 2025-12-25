# Core API Module

API 모듈들이 공유하는 공통 기능을 제공하는 핵심 모듈입니다.

## 주요 기능

- **공통 응답 형식**: 표준화된 API 응답 구조
- **에러 처리**: 중앙화된 예외 처리 및 에러 응답
- **유효성 검증 상수**: 공통 유효성 검증 규칙

## 프로젝트 구조

```
src/main/kotlin/com/zenless/core/api/
├── controller/
│   ├── BaseController.kt          # 기본 컨트롤러
│   └── ApiControllerAdvice.kt     # 전역 예외 처리
└── support/
    ├── ValidationConstants.kt     # 유효성 검증 상수
    ├── response/
    │   ├── ApiResponse.kt         # 공통 API 응답
    │   └── ResultType.kt          # 응답 결과 타입
    └── error/
        ├── CoreApiException.kt    # 커스텀 예외
        ├── ErrorCode.kt           # 에러 코드
        ├── ErrorMessage.kt        # 에러 메시지 DTO
        └── ErrorType.kt           # 에러 타입 정의
```

## API 응답 형식

### 성공 응답

```json
{
  "result": "SUCCESS",
  "data": { ... },
  "error": null
}
```

### 에러 응답

```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E400",
    "message": "잘못된 요청입니다.",
    "data": { ... }
  }
}
```

## 에러 타입

### HTTP 상태 코드별 분류

| 상태 코드 | 에러 타입 | 설명 |
|----------|----------|------|
| 400 | INVALID_REQUEST | 잘못된 요청 |
| 400 | DUPLICATE_EMAIL | 중복 이메일 |
| 401 | UNAUTHORIZED | 인증 필요 |
| 401 | INVALID_CREDENTIALS | 잘못된 인증 정보 |
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 403 | FORBIDDEN | 접근 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 404 | USER_NOT_FOUND | 사용자 없음 |
| 423 | ACCOUNT_LOCKED | 계정 잠금 |
| 429 | TOO_MANY_REQUESTS | 요청 제한 초과 |
| 500 | DEFAULT_ERROR | 서버 오류 |

### 비밀번호 관련

| 에러 타입 | 설명 |
|----------|------|
| INVALID_RESET_CODE | 인증번호 오류/만료 |
| CODE_NOT_VERIFIED | 인증번호 미확인 |
| PASSWORD_MISMATCH | 비밀번호 불일치 |
| PASSWORD_ALREADY_SET | 이미 비밀번호 설정됨 |
| NO_PASSWORD_SET | 비밀번호 미설정 |

## 사용 예시

### 성공 응답 반환

```kotlin
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> {
    val user = userService.getUser(id)
    return ApiResponse.success(user)
}
```

### 에러 발생

```kotlin
throw CoreApiException(ErrorType.USER_NOT_FOUND)
```

## 의존성

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
```

## 사용 모듈

이 모듈을 의존하는 모듈:
- auth-api
- nickname-api
- admin-api
- app-api
