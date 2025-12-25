# Backend Development Guide

## Project Structure

```
backend/
├── api/
│   ├── app-api/                    # Main application entry point
│   │   └── src/main/kotlin/
│   │       └── com/starter/api/app/
│   │           ├── AppApiApplication.kt
│   │           └── controller/HealthController.kt
│   │
│   └── auth-api/                   # Authentication module
│       └── src/main/kotlin/
│           └── com/starter/api/auth/
│               ├── config/         # Security, CORS, Properties
│               ├── controller/     # REST Controllers
│               │   ├── AuthController.kt
│               │   ├── PasswordController.kt
│               │   └── SessionController.kt
│               ├── service/        # Business logic
│               ├── security/       # Filters, JWT provider
│               ├── validation/     # Custom validators
│               └── event/          # Domain events
│
├── core/
│   ├── core-api/                   # Common API utilities
│   │   └── src/main/kotlin/
│   │       └── com/starter/core/api/
│   │           └── support/
│   │               ├── response/   # ApiResponse, ApiFailedResponse
│   │               └── error/      # ErrorType, CoreApiException
│   │
│   └── core-domain/                # Domain models
│       └── src/main/kotlin/
│           └── com/starter/core/domain/
│               └── user/           # UserRole, UserStatus enums
│
└── storage/
    └── db-core/                    # Persistence layer
        └── src/main/kotlin/
            └── com/starter/storage/db/
                ├── core/
                │   ├── config/     # JPA configuration
                │   └── entity/     # BaseEntity
                └── user/
                    ├── entity/     # UserEntity, RefreshTokenEntity
                    └── repository/ # JPA repositories
```

## Key Components

### Controllers

#### AuthController (`/api/v1/auth`)

```kotlin
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<AuthResponse>

    @PostMapping("/signin")
    fun signIn(@Valid @RequestBody request: SignInRequest): ApiResponse<AuthResponse>

    @PostMapping("/signout")
    @PreAuthorize("isAuthenticated()")
    fun signOut(@AuthenticationPrincipal user: AuthUser): ApiResponse<MessageResponse>

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<TokenResponse>

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMe(@AuthenticationPrincipal user: AuthUser): ApiResponse<UserResponse>

    @PostMapping("/update-nickname")
    @PreAuthorize("isAuthenticated()")
    fun updateNickname(@Valid @RequestBody request: UpdateNicknameRequest): ApiResponse<AuthResponse>
}
```

#### PasswordController (`/api/v1/auth`)

```kotlin
@RestController
@RequestMapping("/api/v1/auth")
class PasswordController(private val passwordService: PasswordService) {

    @PostMapping("/set-password")
    @PreAuthorize("isAuthenticated()")
    fun setPassword(@Valid @RequestBody request: SetPasswordRequest): ApiResponse<AuthResponse>

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ApiResponse<MessageResponse>
}
```

#### SessionController (`/api/v1/auth/sessions`)

```kotlin
@RestController
@RequestMapping("/api/v1/auth/sessions")
class SessionController(private val sessionService: SessionService) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getActiveSessions(): ApiResponse<List<SessionResponse>>

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    fun revokeSession(@PathVariable sessionId: Long): ApiResponse<MessageResponse>

    @PostMapping("/revoke-others")
    @PreAuthorize("isAuthenticated()")
    fun revokeOtherSessions(): ApiResponse<MessageResponse>
}
```

### Services

#### AuthenticationService

Core authentication logic:

```kotlin
@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService,
    private val loginAttemptService: LoginAttemptService,
    private val sessionService: SessionService
) {
    fun signUp(request: SignUpRequest): AuthResponse
    fun signIn(request: SignInRequest): AuthResponse
    fun refresh(request: RefreshTokenRequest): TokenResponse
    fun signOut(userId: Long, accessToken: String)
    fun getMe(userId: Long): UserResponse
}
```

#### PasswordService

Password management:

```kotlin
@Service
class PasswordService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun setPassword(userId: Long, password: String, confirmPassword: String): AuthResponse
    fun changePassword(userId: Long, currentPassword: String, newPassword: String, confirmPassword: String): MessageResponse
}
```

### Entities

#### UserEntity

```kotlin
@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = true)
    var password: String?,

    @Column(nullable = false, length = 50)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false)
    var failedLoginAttempts: Int = 0,

    @Column
    var lockoutUntil: LocalDateTime? = null
) : BaseEntity()
```

#### RefreshTokenEntity

```kotlin
@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(nullable = false, unique = true, length = 500)
    val token: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    var revoked: Boolean = false,

    @Column(length = 500)
    val userAgent: String? = null,

    @Column(length = 50)
    val ipAddress: String? = null,

    @Column(length = 100)
    val deviceInfo: String? = null
) : BaseEntity()
```

### Security Configuration

#### SecurityConfig

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/auth/signup",
                    "/api/v1/auth/signin",
                    "/api/v1/auth/refresh",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

### Custom Validators

#### StrongPasswordValidator

```kotlin
@Constraint(validatedBy = [StrongPasswordValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class StrongPassword(
    val message: String = "Password does not meet requirements",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class StrongPasswordValidator : ConstraintValidator<StrongPassword, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        val violations = mutableListOf<String>()

        if (value.length < 8)
            violations.add("비밀번호는 8자 이상이어야 합니다")
        if (!value.any { it.isUpperCase() })
            violations.add("대문자를 1개 이상 포함해야 합니다")
        if (!value.any { it.isLowerCase() })
            violations.add("소문자를 1개 이상 포함해야 합니다")
        if (!value.any { it.isDigit() })
            violations.add("숫자를 1개 이상 포함해야 합니다")
        if (!value.any { it in "!@#\$%^&*()_+-=[]{}|;':\",./<>?`~\\" })
            violations.add("특수문자를 1개 이상 포함해야 합니다")

        return violations.isEmpty()
    }
}
```

## Configuration

### Application Properties

```yaml
# application.yml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/zenless
    username: zenless
    password: zenless123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

jwt:
  secret: ${JWT_SECRET}  # Min 32 characters
  access-token-expiration: 3600000    # 1 hour
  refresh-token-expiration: 604800000 # 7 days
  issuer: zenless

rate-limit:
  auth:
    capacity: 10
    refill-tokens: 10
    refill-minutes: 1

security:
  account-lockout:
    enabled: true
    max-failed-attempts: 5
    lock-duration-minutes: 15

session:
  max-sessions-per-user: 5

cors:
  allowed-origins: http://localhost:5173,http://127.0.0.1:5173
  allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
```

## Database Migrations (Flyway)

Location: `storage/db-core/src/main/resources/db/migration/`

```sql
-- V1__initial_schema.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    lockout_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent VARCHAR(500),
    ip_address VARCHAR(50),
    device_info VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Testing

### Test Dependencies

```kotlin
// build.gradle.kts
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.mockk:mockk:1.13.16")
testImplementation("com.ninja-squad:springmockk:4.0.2")
```

### Unit Test Example

```kotlin
@ExtendWith(MockKExtension::class)
class AuthenticationServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    private lateinit var authenticationService: AuthenticationService

    @Test
    fun `signUp should create user and return tokens`() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "Test1234!",
            nickname = "TestUser"
        )
        every { userRepository.existsByEmail(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { userRepository.save(any()) } returns mockUser()

        // when
        val result = authenticationService.signUp(request)

        // then
        assertThat(result.user.email).isEqualTo(request.email)
        verify { userRepository.save(any()) }
    }
}
```

## Running the Backend

### Development Mode

```bash
cd backend

# With environment variable
JWT_SECRET=your-32-character-secret-key-here ./gradlew :api:app-api:bootRun

# Or create .env file
echo "JWT_SECRET=your-32-character-secret-key-here" > .env
./gradlew :api:app-api:bootRun
```

### Build

```bash
./gradlew build           # Full build with tests
./gradlew compileKotlin   # Compile only
./gradlew test            # Run tests
```

### API Documentation

After starting the server:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Error Handling

### ApiResponse Format

```kotlin
// Success response
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T,
    val message: String? = null
)

// Error response
data class ApiFailedResponse(
    val success: Boolean = false,
    val code: String,
    val message: String,
    val data: Any? = null
)
```

### Exception Handling

```kotlin
@RestControllerAdvice
class ApiControllerAdvice {

    @ExceptionHandler(CoreApiException::class)
    fun handleCoreApiException(e: CoreApiException): ResponseEntity<ApiFailedResponse> {
        return ResponseEntity
            .status(e.errorType.status)
            .body(ApiFailedResponse(
                code = e.errorType.code.name,
                message = e.errorType.message,
                data = e.data
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiFailedResponse> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity
            .badRequest()
            .body(ApiFailedResponse(
                code = "INVALID_REQUEST",
                message = "Validation failed",
                data = errors
            ))
    }
}
```
