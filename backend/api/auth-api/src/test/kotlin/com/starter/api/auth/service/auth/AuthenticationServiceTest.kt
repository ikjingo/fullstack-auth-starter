package com.starter.api.auth.service.auth

import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.api.auth.security.jwt.JwtProperties
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.audit.AuditLogService
import com.starter.api.auth.service.metrics.AuthMetricsService
import com.starter.api.auth.service.session.SessionService
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenEntity
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserRole
import com.starter.storage.db.user.UserStatus
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("AuthenticationService 테스트")
class AuthenticationServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var tokenBlacklistService: TokenBlacklistService
    private lateinit var loginAttemptService: LoginAttemptService
    private lateinit var auditLogService: AuditLogService
    private lateinit var authMetricsService: AuthMetricsService
    private lateinit var sessionService: SessionService
    private lateinit var eventPublisher: AuthEventPublisher
    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        jwtTokenProvider = mockk(relaxed = true)
        jwtProperties =
            JwtProperties(
                secret = "test-secret-key-for-jwt-token-generation-minimum-32-chars",
                accessTokenExpiration = 3600000L,
                refreshTokenExpiration = 604800000L,
                issuer = "test",
            )
        passwordEncoder = mockk(relaxed = true)
        tokenBlacklistService = mockk(relaxed = true)
        loginAttemptService = mockk(relaxed = true)
        auditLogService = mockk(relaxed = true)
        authMetricsService = mockk(relaxed = true)
        sessionService = mockk(relaxed = true)
        eventPublisher = mockk(relaxed = true)

        authenticationService =
            AuthenticationService(
                userRepository = userRepository,
                refreshTokenRepository = refreshTokenRepository,
                jwtTokenProvider = jwtTokenProvider,
                jwtProperties = jwtProperties,
                passwordEncoder = passwordEncoder,
                tokenBlacklistService = tokenBlacklistService,
                loginAttemptService = loginAttemptService,
                auditLogService = auditLogService,
                authMetricsService = authMetricsService,
                sessionService = sessionService,
                eventPublisher = eventPublisher,
            )
    }

    private fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
        password: String? = "encodedPassword",
        nickname: String = "테스트유저",
        role: UserRole = UserRole.USER,
        status: UserStatus = UserStatus.ACTIVE,
    ): UserEntity =
        UserEntity(
            email = email,
            password = password,
            nickname = nickname,
            role = role,
            status = status,
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }

    @Nested
    @DisplayName("signUp")
    inner class SignUpTest {
        @Test
        fun `회원가입이 성공해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "new@example.com",
                    password = "Password123!",
                    nickname = "새사용자",
                )
            val savedUser = createUser(email = request.email, nickname = request.nickname)

            every { userRepository.existsByEmail(request.email) } returns false
            every { passwordEncoder.encode(request.password) } returns "encodedPassword"
            every { userRepository.save(any()) } returns savedUser
            every { jwtTokenProvider.createAccessToken(any(), any(), any()) } returns "access-token"
            every { jwtTokenProvider.createRefreshToken(any(), any()) } returns "refresh-token"
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // When
            val result = authenticationService.signUp(request)

            // Then
            assertThat(result.user.email).isEqualTo(request.email)
            assertThat(result.user.nickname).isEqualTo(request.nickname)
            assertThat(result.token).isEqualTo("access-token")
            assertThat(result.refreshToken).isEqualTo("refresh-token")
            verify { userRepository.save(any()) }
            verify { auditLogService.logSignUp(any(), any()) }
            verify { authMetricsService.recordSignUp() }
        }

        @Test
        fun `중복 이메일로 회원가입 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "existing@example.com",
                    password = "Password123!",
                    nickname = "새사용자",
                )

            every { userRepository.existsByEmail(request.email) } returns true

            // When & Then
            assertThatThrownBy { authenticationService.signUp(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_EMAIL)
        }
    }

    @Nested
    @DisplayName("signIn")
    inner class SignInTest {
        @Test
        fun `로그인이 성공해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = "Password123!",
                )
            val user = createUser()

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { passwordEncoder.matches(request.password, user.password) } returns true
            every { jwtTokenProvider.createAccessToken(any(), any(), any()) } returns "access-token"
            every { jwtTokenProvider.createRefreshToken(any(), any()) } returns "refresh-token"
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // When
            val result = authenticationService.signIn(request)

            // Then
            assertThat(result.user.email).isEqualTo(user.email)
            assertThat(result.token).isEqualTo("access-token")
            verify { loginAttemptService.checkAccountLocked(user) }
            verify { loginAttemptService.resetFailedAttempts(user) }
            verify { auditLogService.logLoginSuccess(user.id, user.email) }
            verify { authMetricsService.recordLoginSuccess() }
        }

        @Test
        fun `존재하지 않는 사용자로 로그인 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "nonexistent@example.com",
                    password = "Password123!",
                )

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns null

            // When & Then
            assertThatThrownBy { authenticationService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }

        @Test
        fun `비밀번호 불일치 시 예외가 발생하고 실패가 기록되어야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = "WrongPassword!",
                )
            val user = createUser()

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { passwordEncoder.matches(request.password, user.password) } returns false

            // When & Then
            assertThatThrownBy { authenticationService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)

            verify { loginAttemptService.recordFailedAttempt(user) }
            verify { auditLogService.logLoginFailure(request.email, "invalid_password") }
            verify { authMetricsService.recordLoginFailure("invalid_password") }
        }

        @Test
        fun `비밀번호가 설정되지 않은 계정으로 로그인 시도 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "nopassword@example.com",
                    password = "Password123!",
                )
            val userWithoutPassword = createUser(password = null)

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns userWithoutPassword

            // When & Then
            assertThatThrownBy { authenticationService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }
    }

    @Nested
    @DisplayName("refresh")
    inner class RefreshTest {
        @Test
        fun `토큰 갱신이 성공해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "valid-refresh-token")
            val user = createUser()
            val refreshTokenEntity =
                RefreshTokenEntity(
                    user = user,
                    token = request.refreshToken,
                    expiresAt = LocalDateTime.now().plusDays(7),
                )

            every { jwtTokenProvider.validateToken(request.refreshToken) } returns true
            every { jwtTokenProvider.isRefreshToken(request.refreshToken) } returns true
            every { refreshTokenRepository.findByToken(request.refreshToken) } returns refreshTokenEntity
            justRun { refreshTokenRepository.delete(refreshTokenEntity) }
            justRun { refreshTokenRepository.flush() }
            every { jwtTokenProvider.createAccessToken(any(), any(), any()) } returns "new-access-token"
            every { jwtTokenProvider.createRefreshToken(any(), any()) } returns "new-refresh-token"
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // When
            val result = authenticationService.refresh(request)

            // Then
            assertThat(result.accessToken).isEqualTo("new-access-token")
            assertThat(result.refreshToken).isEqualTo("new-refresh-token")
            verify { refreshTokenRepository.delete(refreshTokenEntity) }
            verify { auditLogService.logTokenRefresh(user.id) }
            verify { authMetricsService.recordTokenRefresh() }
        }

        @Test
        fun `유효하지 않은 토큰으로 갱신 시도 시 예외가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "invalid-token")

            every { jwtTokenProvider.validateToken(request.refreshToken) } returns false

            // When & Then
            assertThatThrownBy { authenticationService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `Access Token으로 갱신 시도 시 예외가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "access-token")

            every { jwtTokenProvider.validateToken(request.refreshToken) } returns true
            every { jwtTokenProvider.isRefreshToken(request.refreshToken) } returns false

            // When & Then
            assertThatThrownBy { authenticationService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `DB에 없는 리프레시 토큰으로 갱신 시도 시 예외가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "unknown-token")

            every { jwtTokenProvider.validateToken(request.refreshToken) } returns true
            every { jwtTokenProvider.isRefreshToken(request.refreshToken) } returns true
            every { refreshTokenRepository.findByToken(request.refreshToken) } returns null

            // When & Then
            assertThatThrownBy { authenticationService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `이미 취소된 리프레시 토큰으로 갱신 시도 시 예외가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "revoked-token")
            val user = createUser()
            val revokedToken =
                RefreshTokenEntity(
                    user = user,
                    token = request.refreshToken,
                    expiresAt = LocalDateTime.now().plusDays(7),
                    revoked = true,
                )

            every { jwtTokenProvider.validateToken(request.refreshToken) } returns true
            every { jwtTokenProvider.isRefreshToken(request.refreshToken) } returns true
            every { refreshTokenRepository.findByToken(request.refreshToken) } returns revokedToken

            // When & Then
            assertThatThrownBy { authenticationService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }
    }

    @Nested
    @DisplayName("signOut")
    inner class SignOutTest {
        @Test
        fun `로그아웃이 성공해야 한다`() {
            // Given
            val userId = 1L
            val accessToken = "valid-access-token"
            val user = createUser(id = userId)
            val expiresAt = LocalDateTime.now().plusHours(1)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { jwtTokenProvider.validateToken(accessToken) } returns true
            every { jwtTokenProvider.getExpiration(accessToken) } returns expiresAt

            // When
            authenticationService.signOut(userId, accessToken)

            // Then
            verify { refreshTokenRepository.revokeAllByUser(user) }
            verify { tokenBlacklistService.blacklistToken(accessToken, expiresAt) }
            verify { auditLogService.logLogout(userId) }
            verify { authMetricsService.recordLogout() }
        }

        @Test
        fun `Access Token 없이 로그아웃 시 토큰 블랙리스트에 추가하지 않아야 한다`() {
            // Given
            val userId = 1L
            val user = createUser(id = userId)

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When
            authenticationService.signOut(userId, null)

            // Then
            verify { refreshTokenRepository.revokeAllByUser(user) }
            verify(exactly = 0) { tokenBlacklistService.blacklistToken(any(), any()) }
            verify { auditLogService.logLogout(userId) }
        }
    }

    @Nested
    @DisplayName("getMe")
    inner class GetMeTest {
        @Test
        fun `사용자 정보를 조회해야 한다`() {
            // Given
            val userId = 1L
            val user = createUser(id = userId)

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When
            val result = authenticationService.getMe(userId)

            // Then
            assertThat(result.id).isEqualTo(userId.toString())
            assertThat(result.email).isEqualTo(user.email)
            assertThat(result.nickname).isEqualTo(user.nickname)
        }

        @Test
        fun `존재하지 않는 사용자 조회 시 예외가 발생해야 한다`() {
            // Given
            val userId = 999L

            every { userRepository.findById(userId) } returns Optional.empty()

            // When & Then
            assertThatThrownBy { authenticationService.getMe(userId) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("updateNickname")
    inner class UpdateNicknameTest {
        @Test
        fun `닉네임 업데이트가 성공해야 한다`() {
            // Given
            val userId = 1L
            val newNickname = "새닉네임"
            val user = createUser(id = userId)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { jwtTokenProvider.createAccessToken(any(), any(), any()) } returns "new-access-token"
            every { jwtTokenProvider.createRefreshToken(any(), any()) } returns "new-refresh-token"
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // When
            val result = authenticationService.updateNickname(userId, newNickname)

            // Then
            assertThat(result.user.nickname).isEqualTo(newNickname)
            assertThat(user.nickname).isEqualTo(newNickname)
        }

        @Test
        fun `존재하지 않는 사용자의 닉네임 업데이트 시 예외가 발생해야 한다`() {
            // Given
            val userId = 999L
            val newNickname = "새닉네임"

            every { userRepository.findById(userId) } returns Optional.empty()

            // When & Then
            assertThatThrownBy { authenticationService.updateNickname(userId, newNickname) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("generateTokens")
    inner class GenerateTokensTest {
        @Test
        fun `토큰 생성이 성공해야 한다`() {
            // Given
            val user = createUser()

            every { jwtTokenProvider.createAccessToken(user.id, user.email, listOf("USER")) } returns "access-token"
            every { jwtTokenProvider.createRefreshToken(user.id, user.email) } returns "refresh-token"
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // When
            val result = authenticationService.generateTokens(user)

            // Then
            assertThat(result.accessToken).isEqualTo("access-token")
            assertThat(result.refreshToken).isEqualTo("refresh-token")
            assertThat(result.expiresIn).isEqualTo(3600L)
            verify { refreshTokenRepository.save(any()) }
            verify { sessionService.enforceSessionLimit(user) }
        }

        @Test
        fun `디바이스 정보가 포함된 토큰을 생성해야 한다`() {
            // Given
            val user = createUser()
            val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
            val ipAddress = "192.168.1.1"
            val tokenSlot = slot<RefreshTokenEntity>()

            every { jwtTokenProvider.createAccessToken(any(), any(), any()) } returns "access-token"
            every { jwtTokenProvider.createRefreshToken(any(), any()) } returns "refresh-token"
            every { refreshTokenRepository.save(capture(tokenSlot)) } answers { tokenSlot.captured }

            // When
            authenticationService.generateTokens(user, userAgent, ipAddress)

            // Then
            assertThat(tokenSlot.captured.userAgent).isEqualTo(userAgent)
            assertThat(tokenSlot.captured.ipAddress).isEqualTo(ipAddress)
            assertThat(tokenSlot.captured.deviceInfo).isEqualTo("Mac")
        }
    }
}
