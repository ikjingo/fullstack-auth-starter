# Backend Test Patterns Reference

이 문서는 Fullstack Auth Starter 프로젝트의 백엔드 테스트 작성에 대한 상세 가이드입니다.

## 목차

1. [프로젝트 구조 이해](#프로젝트-구조-이해)
2. [AuthService 테스트 예시](#authservice-테스트-예시)
3. [AuthController 테스트 예시](#authcontroller-테스트-예시)
4. [Repository 테스트 예시](#repository-테스트-예시)
5. [Mocking 패턴](#mocking-패턴)
6. [테스트 데이터 생성](#테스트-데이터-생성)
7. [통합 테스트](#통합-테스트)

---

## 프로젝트 구조 이해

### 모듈 구조

```
backend/
├── api/
│   └── auth-api/                    # 인증 API
│       └── src/main/kotlin/com/starter/api/auth/
│           ├── controller/          # REST Controllers
│           │   ├── AuthController.kt
│           │   └── request/         # Request DTOs
│           └── service/             # Business Logic
│               └── AuthService.kt
├── core/
│   └── core-api/                    # 핵심 API 컴포넌트
│       └── src/main/kotlin/com/starter/core/api/
│           └── support/
│               ├── response/        # ApiResponse 래퍼
│               └── error/           # 에러 처리
└── storage/
    └── db-core/                     # JPA 엔티티 및 리포지토리
        └── src/main/kotlin/com/starter/storage/db/
            └── user/
                ├── UserEntity.kt
                ├── UserRepository.kt
                └── RefreshTokenEntity.kt
```

### ApiResponse 래퍼 구조

모든 API 응답은 `ApiResponse` 래퍼로 감싸집니다:

```kotlin
data class ApiResponse<T>(
    val result: ResultType,  // SUCCESS or ERROR
    val data: T?,
    val error: ErrorMessage?
)
```

---

## AuthService 테스트 예시

### 파일 위치
`backend/api/auth-api/src/test/kotlin/com/starter/api/auth/service/AuthServiceTest.kt`

### 전체 테스트 코드

```kotlin
package com.starter.api.auth.service

import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.storage.db.user.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `새로운 사용자를 등록할 수 있어야 한다`() {
        // Given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저"
        )

        every { userRepository.findByEmail("test@example.com") } returns null
        every { passwordEncoder.encode("password123") } returns "encodedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = authService.signUp(request)

        // Then
        assertThat(result.email).isEqualTo("test@example.com")
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `이미 존재하는 이메일로 가입 시 예외가 발생해야 한다`() {
        // Given
        val request = SignUpRequest(
            email = "existing@example.com",
            password = "password123",
            nickname = "테스트유저"
        )

        val existingUser = UserEntity(
            email = "existing@example.com",
            password = "encodedPassword",
            nickname = "기존유저"
        )

        every { userRepository.findByEmail("existing@example.com") } returns existingUser

        // When & Then
        assertThrows<DuplicateEmailException> {
            authService.signUp(request)
        }
    }

    @Test
    fun `올바른 자격 증명으로 로그인할 수 있어야 한다`() {
        // Given
        val request = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = UserEntity(
            email = "test@example.com",
            password = "encodedPassword",
            nickname = "테스트유저"
        ).apply { id = 1L }

        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("password123", "encodedPassword") } returns true
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        // When
        val result = authService.signIn(request)

        // Then
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
    }

    @Test
    fun `잘못된 비밀번호로 로그인 시 예외가 발생해야 한다`() {
        // Given
        val request = SignInRequest(
            email = "test@example.com",
            password = "wrongPassword"
        )

        val user = UserEntity(
            email = "test@example.com",
            password = "encodedPassword",
            nickname = "테스트유저"
        )

        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("wrongPassword", "encodedPassword") } returns false

        // When & Then
        assertThrows<InvalidCredentialsException> {
            authService.signIn(request)
        }
    }
}
```

---

## AuthController 테스트 예시

### 파일 위치
`backend/api/auth-api/src/test/kotlin/com/starter/api/auth/controller/AuthControllerTest.kt`

### 전체 테스트 코드

```kotlin
package com.starter.api.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.service.AuthService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `회원가입 성공 시 사용자 정보를 반환해야 한다`() {
        // Given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저"
        )

        val response = UserResponse(
            id = 1,
            email = "test@example.com",
            nickname = "테스트유저",
            role = "USER"
        )

        every { authService.signUp(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    fun `로그인 성공 시 토큰을 반환해야 한다`() {
        // Given
        val request = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )

        val response = AuthResponse(
            accessToken = "accessToken123",
            refreshToken = "refreshToken123",
            user = UserResponse(
                id = 1,
                email = "test@example.com",
                nickname = "테스트유저",
                role = "USER"
            )
        )

        every { authService.signIn(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.accessToken").value("accessToken123"))
    }

    @Test
    fun `유효하지 않은 요청 시 400 에러를 반환해야 한다`() {
        // Given
        val invalidRequest = mapOf(
            "email" to "invalid-email",
            "password" to "short"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result").value("ERROR"))
    }
}
```

---

## Repository 테스트 예시

### 파일 위치
`backend/storage/db-core/src/test/kotlin/com/starter/storage/db/user/UserRepositoryTest.kt`

### 전체 테스트 코드

```kotlin
package com.starter.storage.db.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `이메일로 사용자를 조회할 수 있어야 한다`() {
        // Given
        val user = UserEntity(
            email = "test@example.com",
            password = "encodedPassword",
            nickname = "테스트유저"
        )
        entityManager.persistAndFlush(user)

        // When
        val found = userRepository.findByEmail("test@example.com")

        // Then
        assertThat(found).isNotNull
        assertThat(found?.email).isEqualTo("test@example.com")
        assertThat(found?.nickname).isEqualTo("테스트유저")
    }

    @Test
    fun `존재하지 않는 이메일 조회 시 null을 반환해야 한다`() {
        // When
        val found = userRepository.findByEmail("nonexistent@example.com")

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `사용자 상태로 필터링할 수 있어야 한다`() {
        // Given
        val activeUser = UserEntity(
            email = "active@example.com",
            password = "password",
            nickname = "활성유저",
            status = UserStatus.ACTIVE
        )
        val inactiveUser = UserEntity(
            email = "inactive@example.com",
            password = "password",
            nickname = "비활성유저",
            status = UserStatus.INACTIVE
        )
        entityManager.persistAndFlush(activeUser)
        entityManager.persistAndFlush(inactiveUser)

        // When
        val result = userRepository.findByStatus(UserStatus.ACTIVE)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("active@example.com")
    }
}
```

---

## Mocking 패턴

### MockK 기본 사용법

```kotlin
// 단순 반환값 설정
every { repository.findById(1) } returns Optional.of(entity)

// 인자 매칭
every { repository.findByName(any()) } returns listOf()
every { repository.findByAge(eq(20)) } returns listOf()

// 예외 던지기
every { service.doSomething() } throws RuntimeException("Error")

// 호출 검증
verify(exactly = 1) { repository.save(any()) }
verify { repository.findById(any()) wasNot Called }

// 순서 검증
verifyOrder {
    repository.findById(1)
    repository.save(any())
}
```

### Capture를 이용한 인자 검증

```kotlin
val slot = slot<UserEntity>()
every { repository.save(capture(slot)) } answers { slot.captured }

service.createUser(request)

assertThat(slot.captured.email).isEqualTo("test@example.com")
assertThat(slot.captured.nickname).isNotBlank()
```

---

## 테스트 데이터 생성

### 팩토리 함수 패턴

```kotlin
object TestDataFactory {

    fun createUserEntity(
        id: Long = 1L,
        email: String = "test@example.com",
        password: String = "encodedPassword",
        nickname: String = "테스트유저",
        role: UserRole = UserRole.USER,
        status: UserStatus = UserStatus.ACTIVE
    ): UserEntity {
        return UserEntity(
            email = email,
            password = password,
            nickname = nickname,
            role = role,
            status = status
        ).apply { this.id = id }
    }

    fun createSignUpRequest(
        email: String = "test@example.com",
        password: String = "password123",
        nickname: String = "테스트유저"
    ): SignUpRequest {
        return SignUpRequest(
            email = email,
            password = password,
            nickname = nickname
        )
    }

    fun createSignInRequest(
        email: String = "test@example.com",
        password: String = "password123"
    ): SignInRequest {
        return SignInRequest(
            email = email,
            password = password
        )
    }
}
```

---

## 통합 테스트

### @SpringBootTest 전체 통합 테스트

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `회원가입 후 로그인 통합 테스트`() {
        // Given - 회원가입
        val signUpRequest = SignUpRequest(
            email = "integration@example.com",
            password = "password123",
            nickname = "통합테스트유저"
        )

        // When - 회원가입
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest))
        )
            .andExpect(status().isOk)

        // Then - 사용자 확인
        val saved = userRepository.findByEmail("integration@example.com")
        assertThat(saved).isNotNull

        // Given - 로그인
        val signInRequest = SignInRequest(
            email = "integration@example.com",
            password = "password123"
        )

        // When & Then - 로그인
        mockMvc.perform(
            post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").exists())
    }
}
```

---

## 테스트 설정 파일

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
```

### Testcontainers 사용 시

```kotlin
@Testcontainers
@SpringBootTest
class PostgresIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `실제 PostgreSQL에서 테스트`() {
        // ...
    }
}
```
