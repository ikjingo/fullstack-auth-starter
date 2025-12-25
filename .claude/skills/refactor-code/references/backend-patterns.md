# 백엔드 리팩토링 패턴 (Kotlin/Spring Boot)

## 서비스 레이어 패턴

### Before (Bad)
```kotlin
@RestController
class UserController(
    private val userRepository: UserRepository
) {
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): User {
        // 컨트롤러에서 비즈니스 로직 처리 - BAD
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found") }
        return user
    }
}
```

### After (Good)
```kotlin
@RestController
class UserController(
    private val userService: UserService
) {
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return userService.getUser(id)
    }
}

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }
        return UserResponse.from(user)
    }
}
```

### 핵심 원칙
- 컨트롤러: 요청/응답 처리, 입력 유효성 검증만
- 서비스: 비즈니스 로직, 트랜잭션 관리
- 리포지토리: 데이터 접근만

---

## DTO 변환 패턴

### Response DTO
```kotlin
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id!!,
            email = user.email,
            nickname = user.nickname,
            createdAt = user.createdAt,
        )

        fun from(users: List<User>) = users.map { from(it) }
    }
}
```

### Request DTO
```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "이메일을 입력하세요")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "닉네임을 입력하세요")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    val nickname: String,
) {
    fun toEntity() = User(
        email = email,
        nickname = nickname,
    )
}
```

### 핵심 원칙
- Entity를 직접 반환하지 않음
- `companion object { fun from() }` 패턴으로 변환
- Request DTO에 Validation 어노테이션 사용
- `toEntity()` 메서드로 Entity 생성

---

## 예외 처리 표준화

### 도메인 예외 정의
```kotlin
// 기본 도메인 예외
sealed class DomainException(message: String) : RuntimeException(message)

// 구체적인 예외들
class UserNotFoundException(id: Long) : DomainException("사용자를 찾을 수 없습니다: $id")
class DuplicateEmailException(email: String) : DomainException("이미 사용 중인 이메일입니다: $email")
class InvalidPasswordException : DomainException("비밀번호가 일치하지 않습니다")
class UnauthorizedException : DomainException("인증이 필요합니다")
class ForbiddenException : DomainException("권한이 없습니다")
```

### GlobalExceptionHandler
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is UserNotFoundException -> HttpStatus.NOT_FOUND
            is DuplicateEmailException -> HttpStatus.CONFLICT
            is UnauthorizedException -> HttpStatus.UNAUTHORIZED
            is ForbiddenException -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status)
            .body(ErrorResponse(ex.message ?: "알 수 없는 오류"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest()
            .body(ErrorResponse(message))
    }
}

data class ErrorResponse(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
```

### 핵심 원칙
- `sealed class` 활용으로 타입 안전성 확보
- `when` 표현식으로 HTTP 상태 코드 매핑
- 사용자 친화적인 한글 메시지

---

## Repository 쿼리 최적화

### Projection 사용
```kotlin
// 필요한 필드만 조회
interface UserProjection {
    val id: Long
    val nickname: String
}

interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u.id as id, u.nickname as nickname FROM User u WHERE u.email = :email")
    fun findIdAndNicknameByEmail(email: String): UserProjection?
}
```

### N+1 문제 방지
```kotlin
interface UserRepository : JpaRepository<User, Long> {
    // FetchType.LAZY 관계 한 번에 조회
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    fun findByIdWithRoles(id: Long): User?

    // 컬렉션 관계도 JOIN FETCH
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.posts WHERE u.id = :id")
    fun findByIdWithPosts(id: Long): User?
}
```

### Batch Size 설정
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

### 핵심 원칙
- 필요한 필드만 조회 (Projection)
- 연관 엔티티는 JOIN FETCH
- 컬렉션 조회 시 DISTINCT 사용
- Batch Size 설정으로 N+1 완화

---

## 트랜잭션 관리

### 읽기 전용 트랜잭션
```kotlin
@Service
@Transactional(readOnly = true)  // 클래스 레벨: 읽기 전용 기본값
class UserService(
    private val userRepository: UserRepository
) {
    fun getUser(id: Long): UserResponse {
        // readOnly = true 적용됨
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }
        return UserResponse.from(user)
    }

    @Transactional  // 쓰기 작업은 오버라이드
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException(id) }
        user.update(request.nickname)
        return UserResponse.from(user)
    }
}
```

### 핵심 원칙
- 클래스 레벨: `@Transactional(readOnly = true)`
- 쓰기 메서드만 `@Transactional` 오버라이드
- 읽기 전용은 성능 최적화됨

---

## 엔티티 설계

### BaseEntity
```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
}
```

### Entity 업데이트 패턴
```kotlin
@Entity
class User(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var nickname: String,  // 변경 가능 필드는 var
) : BaseEntity() {

    // 업데이트 메서드로 캡슐화
    fun update(nickname: String) {
        this.nickname = nickname
    }

    // 비즈니스 로직도 엔티티 내부에
    fun isEmailVerified(): Boolean {
        return email.contains("@verified.")
    }
}
```

### 핵심 원칙
- `BaseEntity`로 공통 필드 관리
- 변경 가능 필드만 `var`, 나머지는 `val`
- Setter 대신 명시적 업데이트 메서드
- 비즈니스 로직은 엔티티 내부에
