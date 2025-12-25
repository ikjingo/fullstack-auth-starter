# Backend Development Guidelines

백엔드(Kotlin/Spring Boot) 개발 가이드라인 스킬. 사용자가 "백엔드 개발", "API 구현", "컨트롤러", "서비스" 등의 요청을 할 때 사용합니다. (project)

## 디렉토리 구조

```
backend/src/main/kotlin/com/zenless/backend/
├── config/         # 설정 클래스
├── controller/     # REST 컨트롤러
├── service/        # 비즈니스 로직
├── repository/     # JPA 리포지토리
├── entity/         # JPA 엔티티
├── dto/            # 데이터 전송 객체
├── exception/      # 커스텀 예외
└── util/           # 유틸리티 클래스
```

## 핵심 기술 스택

- **Kotlin 1.9+**
- **Spring Boot 3.5.8**
- **Spring Web MVC**
- **Spring Data JPA**
- **PostgreSQL 16**
- **Spring Validation**

## 계층 구조 패턴

### Controller
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(id))
    }
}
```

### Service
```kotlin
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    fun getUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }
        return UserResponse.from(user)
    }

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        // ...
    }
}
```

### Repository
```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
```

## DTO 패턴

```kotlin
// Request DTO
data class CreateUserRequest(
    @field:NotBlank
    val email: String,

    @field:Size(min = 8)
    val password: String
)

// Response DTO
data class UserResponse(
    val id: Long,
    val email: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id!!,
            email = user.email,
            createdAt = user.createdAt
        )
    }
}
```

## Entity 패턴

```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

## 예외 처리

```kotlin
// 커스텀 예외
class UserNotFoundException(id: Long) :
    RuntimeException("User not found: $id")

// 글로벌 예외 핸들러
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(e.message ?: "Not found"))
    }
}
```

## 유효성 검증

```kotlin
@PostMapping
fun createUser(
    @Valid @RequestBody request: CreateUserRequest
): ResponseEntity<UserResponse> {
    // @Valid가 자동으로 검증
}
```

## 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|-----|------|------|
| 클래스 | PascalCase | `UserController`, `AuthService` |
| 함수 | camelCase | `getUser`, `createOrder` |
| 상수 | UPPER_SNAKE | `MAX_RETRY_COUNT` |
| 패키지 | lowercase | `com.zenless.backend` |

## API 응답 형식

### 성공 응답
```json
{
  "id": 1,
  "email": "user@example.com",
  "createdAt": "2024-01-01T00:00:00"
}
```

### 에러 응답
```json
{
  "error": "User not found: 1",
  "timestamp": "2024-01-01T00:00:00"
}
```

## 주의사항

1. **@Transactional** - 읽기 전용은 `readOnly = true`
2. **N+1 문제** - `@EntityGraph` 또는 fetch join 사용
3. **비밀번호** - BCrypt로 암호화
4. **입력 검증** - DTO에서 @Valid 사용
5. **로깅** - 민감 정보 로깅 금지

## 빌드 & 테스트

```bash
./gradlew bootRun      # 실행
./gradlew test         # 테스트
./gradlew compileKotlin # 컴파일 체크
```
