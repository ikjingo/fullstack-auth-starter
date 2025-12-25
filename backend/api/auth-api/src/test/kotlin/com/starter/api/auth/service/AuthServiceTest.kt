package com.starter.api.auth.service

import com.starter.api.auth.config.AccountLockoutProperties
import com.starter.api.auth.config.PasswordResetProperties
import com.starter.api.auth.controller.request.ForgotPasswordRequest
import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.request.VerifyCodeRequest
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.api.auth.security.jwt.JwtProperties
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.audit.AuditLogService
import com.starter.api.auth.service.auth.AuthenticationService
import com.starter.api.auth.service.auth.LoginAttemptService
import com.starter.api.auth.service.auth.PasswordService
import com.starter.api.auth.service.auth.SocialAuthService
import com.starter.api.auth.service.metrics.AuthMetricsService
import com.starter.api.auth.service.session.SessionService
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.AuthProvider
import com.starter.storage.db.user.PasswordResetCodeEntity
import com.starter.storage.db.user.PasswordResetCodeRepository
import com.starter.storage.db.user.RefreshTokenEntity
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.SocialAccountEntity
import com.starter.storage.db.user.SocialAccountRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserRole
import com.starter.storage.db.user.UserStatus
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("AuthService")
class AuthServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var socialAccountRepository: SocialAccountRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var passwordResetCodeRepository: PasswordResetCodeRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var googleOAuthService: GoogleOAuthService
    private lateinit var passwordResetProperties: PasswordResetProperties
    private lateinit var tokenBlacklistService: TokenBlacklistService
    private lateinit var loginAttemptService: LoginAttemptService
    private lateinit var auditLogService: AuditLogService
    private lateinit var authMetricsService: AuthMetricsService
    private lateinit var sessionService: SessionService
    private lateinit var eventPublisher: AuthEventPublisher

    // Sub-services
    private lateinit var authenticationService: AuthenticationService
    private lateinit var socialAuthService: SocialAuthService
    private lateinit var passwordService: PasswordService

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        socialAccountRepository = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        passwordResetCodeRepository = mockk(relaxed = true)
        jwtProperties =
            JwtProperties(
                secret = "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256",
                accessTokenExpiration = 3600000L,
                refreshTokenExpiration = 604800000L,
                issuer = "test-issuer",
            )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
        passwordEncoder = BCryptPasswordEncoder()
        googleOAuthService = mockk(relaxed = true)
        passwordResetProperties = PasswordResetProperties(codeExpirationMinutes = 10)
        tokenBlacklistService = mockk(relaxed = true)
        auditLogService = mockk(relaxed = true)
        authMetricsService = mockk(relaxed = true)
        sessionService = mockk(relaxed = true)
        eventPublisher = mockk(relaxed = true)

        // LoginAttemptService with real implementation for testing
        val lockoutProperties =
            AccountLockoutProperties(
                maxFailedAttempts = 5,
                lockDurationMinutes = 15L,
                enabled = true,
            )
        loginAttemptService = LoginAttemptService(userRepository, lockoutProperties, eventPublisher)

        // Default mock behavior for save methods to return the argument
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { passwordResetCodeRepository.save(any()) } answers { firstArg() }
        every { userRepository.save(any()) } answers { firstArg() }

        // Create sub-services
        authenticationService =
            AuthenticationService(
                userRepository = userRepository,
                socialAccountRepository = socialAccountRepository,
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

        socialAuthService =
            SocialAuthService(
                socialAccountRepository = socialAccountRepository,
                googleOAuthService = googleOAuthService,
                authenticationService = authenticationService,
                eventPublisher = eventPublisher,
            )

        passwordService =
            PasswordService(
                userRepository = userRepository,
                passwordResetCodeRepository = passwordResetCodeRepository,
                refreshTokenRepository = refreshTokenRepository,
                passwordEncoder = passwordEncoder,
                authenticationService = authenticationService,
                passwordResetProperties = passwordResetProperties,
                eventPublisher = eventPublisher,
            )

        // Create AuthService Facade
        authService =
            AuthService(
                authenticationService = authenticationService,
                socialAuthService = socialAuthService,
                passwordService = passwordService,
            )
    }

    @Nested
    @DisplayName("signUp 메서드")
    inner class SignUpTest {
        @Test
        fun `회원가입을 성공적으로 수행해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "test@example.com",
                    password = "Password123!",
                    nickname = "테스트유저",
                )
            val savedUser = createUserEntity(1L, request.email, request.nickname)

            every { userRepository.existsByEmail(request.email) } returns false
            every { userRepository.save(any()) } returns savedUser

            // When
            val result = authService.signUp(request)

            // Then
            assertThat(result.user.email).isEqualTo(request.email)
            assertThat(result.user.nickname).isEqualTo(request.nickname)
            assertThat(result.token).isNotBlank()
            verify(exactly = 1) { userRepository.save(any()) }
            verify(exactly = 1) { refreshTokenRepository.save(any()) }
        }

        @Test
        fun `중복 이메일로 회원가입 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "duplicate@example.com",
                    password = "Password123!",
                    nickname = "테스트유저",
                )

            every { userRepository.existsByEmail(request.email) } returns true

            // When & Then
            assertThatThrownBy { authService.signUp(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_EMAIL)
        }
    }

    @Nested
    @DisplayName("signIn 메서드")
    inner class SignInTest {
        @Test
        fun `정상적으로 로그인해야 한다`() {
            // Given
            val plainPassword = "password123!"
            val encodedPassword = passwordEncoder.encode(plainPassword)
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = plainPassword,
                )
            val user = createUserEntity(1L, request.email, "테스트유저", encodedPassword)

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { socialAccountRepository.findAllByUser(user) } returns emptyList()
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(any()) } answers { firstArg() }

            // When
            val result = authService.signIn(request)

            // Then
            assertThat(result.user.email).isEqualTo(request.email)
            assertThat(result.token).isNotBlank()
        }

        @Test
        fun `존재하지 않는 이메일로 로그인 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "notfound@example.com",
                    password = "Password123!",
                )

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns null

            // When & Then
            assertThatThrownBy { authService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }

        @Test
        fun `잘못된 비밀번호로 로그인 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = "wrongpassword",
                )
            val user = createUserEntity(1L, request.email, "테스트유저", passwordEncoder.encode("correctpassword"))

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(any()) } answers { firstArg() }

            // When & Then
            assertThatThrownBy { authService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }

        @Test
        fun `비밀번호 없는 OAuth 계정으로 로그인 시 예외가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "oauth@example.com",
                    password = "anypassword",
                )
            val user = createUserEntity(1L, request.email, "테스트유저", password = null)

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user

            // When & Then
            assertThatThrownBy { authService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.OAUTH_ACCOUNT_NO_PASSWORD)
        }
    }

    @Nested
    @DisplayName("refresh 메서드")
    inner class RefreshTest {
        @Test
        fun `유효한 리프레시 토큰으로 새 토큰을 발급해야 한다`() {
            // Given
            val user = createUserEntity(1L, "test@example.com", "테스트유저")
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val refreshTokenEntity =
                RefreshTokenEntity(
                    user = user,
                    token = refreshToken,
                    expiresAt = LocalDateTime.now().plusDays(7),
                    revoked = false,
                )

            every { refreshTokenRepository.findByToken(refreshToken) } returns refreshTokenEntity

            // When
            val result = authService.refresh(request)

            // Then
            assertThat(result.accessToken).isNotBlank()
            assertThat(result.refreshToken).isNotBlank()
            verify(exactly = 1) { refreshTokenRepository.save(any()) }
        }

        @Test
        fun `유효하지 않은 토큰으로 갱신 시 예외가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "invalid-token")

            // When & Then
            assertThatThrownBy { authService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `액세스 토큰으로 갱신 시 예외가 발생해야 한다`() {
            // Given
            val accessToken = jwtTokenProvider.createAccessToken(1L, "test@example.com", listOf("USER"))
            val request = RefreshTokenRequest(refreshToken = accessToken)

            // When & Then
            assertThatThrownBy { authService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `만료된 리프레시 토큰으로 갱신 시 예외가 발생해야 한다`() {
            // Given
            val user = createUserEntity(1L, "test@example.com", "테스트유저")
            val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val expiredRefreshTokenEntity =
                RefreshTokenEntity(
                    user = user,
                    token = refreshToken,
                    expiresAt = LocalDateTime.now().minusDays(1), // 만료됨
                    revoked = false,
                )

            every { refreshTokenRepository.findByToken(refreshToken) } returns expiredRefreshTokenEntity

            // When & Then
            assertThatThrownBy { authService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }

        @Test
        fun `토큰 갱신 시 기존 토큰이 삭제되어야 한다 (Token Rotation)`() {
            // Given
            val user = createUserEntity(1L, "test@example.com", "테스트유저")
            val oldRefreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
            val request = RefreshTokenRequest(refreshToken = oldRefreshToken)
            val refreshTokenEntity =
                RefreshTokenEntity(
                    user = user,
                    token = oldRefreshToken,
                    expiresAt = LocalDateTime.now().plusDays(7),
                    revoked = false,
                )

            every { refreshTokenRepository.findByToken(oldRefreshToken) } returns refreshTokenEntity
            every { refreshTokenRepository.delete(refreshTokenEntity) } just Runs
            every { refreshTokenRepository.flush() } just Runs

            // When
            val result = authService.refresh(request)

            // Then
            // 1. 기존 토큰이 삭제됨 (Token Rotation의 핵심)
            verify(exactly = 1) { refreshTokenRepository.delete(refreshTokenEntity) }
            // 2. 새 토큰이 발급됨
            assertThat(result.refreshToken).isNotBlank()
            // 3. 새 토큰이 저장됨
            verify(exactly = 1) { refreshTokenRepository.save(any()) }
        }

        @Test
        fun `이미 폐기된 토큰으로 갱신 시 예외가 발생해야 한다`() {
            // Given
            val user = createUserEntity(1L, "test@example.com", "테스트유저")
            val revokedRefreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
            val request = RefreshTokenRequest(refreshToken = revokedRefreshToken)
            val revokedTokenEntity =
                RefreshTokenEntity(
                    user = user,
                    token = revokedRefreshToken,
                    expiresAt = LocalDateTime.now().plusDays(7),
                    revoked = true, // 이미 폐기됨
                )

            every { refreshTokenRepository.findByToken(revokedRefreshToken) } returns revokedTokenEntity

            // When & Then
            assertThatThrownBy { authService.refresh(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }
    }

    @Nested
    @DisplayName("getMe 메서드")
    inner class GetMeTest {
        @Test
        fun `사용자 정보를 반환해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저")

            every { userRepository.findByIdWithSocialAccounts(userId) } returns user

            // When
            val result = authService.getMe(userId)

            // Then
            assertThat(result.email).isEqualTo(user.email)
            assertThat(result.nickname).isEqualTo(user.nickname)
        }

        @Test
        fun `존재하지 않는 사용자 ID로 조회 시 예외가 발생해야 한다`() {
            // Given
            val userId = 999L

            every { userRepository.findByIdWithSocialAccounts(userId) } returns null

            // When & Then
            assertThatThrownBy { authService.getMe(userId) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("forgotPassword 메서드")
    inner class ForgotPasswordTest {
        @Test
        fun `존재하는 이메일에 대해 인증코드를 생성해야 한다`() {
            // Given
            val request = ForgotPasswordRequest(email = "test@example.com")
            val user = createUserEntity(1L, request.email, "테스트유저")

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user

            // When
            val result = authService.forgotPassword(request)

            // Then
            assertThat(result.message).contains("인증번호가 이메일로 발송되었습니다")
            verify(exactly = 1) { passwordResetCodeRepository.invalidateAllByEmail(request.email) }
            verify(exactly = 1) { passwordResetCodeRepository.save(any()) }
        }

        @Test
        fun `존재하지 않는 이메일에도 동일한 메시지를 반환해야 한다`() {
            // Given
            val request = ForgotPasswordRequest(email = "notfound@example.com")

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns null

            // When
            val result = authService.forgotPassword(request)

            // Then
            assertThat(result.message).contains("인증번호가 이메일로 발송되었습니다")
            verify(exactly = 0) { passwordResetCodeRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("verifyCode 메서드")
    inner class VerifyCodeTest {
        @Test
        fun `유효한 인증코드를 검증해야 한다`() {
            // Given
            val request =
                VerifyCodeRequest(
                    email = "test@example.com",
                    code = "123456",
                )
            val resetCode =
                PasswordResetCodeEntity(
                    email = request.email,
                    code = request.code,
                    expiresAt = LocalDateTime.now().plusMinutes(10),
                )

            every { passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(request.email, request.code) } returns resetCode

            // When
            val result = authService.verifyCode(request)

            // Then
            assertThat(result.message).contains("인증이 완료되었습니다")
        }

        @Test
        fun `존재하지 않는 인증코드로 검증 시 예외가 발생해야 한다`() {
            // Given
            val request =
                VerifyCodeRequest(
                    email = "test@example.com",
                    code = "wrongcode",
                )

            every { passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(request.email, request.code) } returns null

            // When & Then
            assertThatThrownBy { authService.verifyCode(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_RESET_CODE)
        }
    }

    @Nested
    @DisplayName("setPassword 메서드")
    inner class SetPasswordTest {
        @Test
        fun `비밀번호를 설정해야 한다`() {
            // Given
            val userId = 1L
            val password = "NewPassword123!"
            val user = createUserEntity(userId, "test@example.com", "테스트유저", password = null)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { socialAccountRepository.findAllByUser(user) } returns emptyList()

            // When
            val result = authService.setPassword(userId, password, password)

            // Then
            assertThat(result.user.hasPassword).isTrue()
            assertThat(result.token).isNotBlank()
        }

        @Test
        fun `비밀번호 불일치 시 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", password = null)

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When & Then
            assertThatThrownBy { authService.setPassword(userId, "password1", "password2") }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_MISMATCH)
        }

        @Test
        fun `이미 비밀번호가 설정된 경우 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", passwordEncoder.encode("existingpassword"))

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When & Then
            assertThatThrownBy { authService.setPassword(userId, "newpassword", "newpassword") }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_ALREADY_SET)
        }
    }

    @Nested
    @DisplayName("changePassword 메서드")
    inner class ChangePasswordTest {
        @Test
        fun `비밀번호를 변경해야 한다`() {
            // Given
            val userId = 1L
            val currentPassword = "CurrentPassword123!"
            val newPassword = "NewPassword123!"
            val user = createUserEntity(userId, "test@example.com", "테스트유저", passwordEncoder.encode(currentPassword))

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When
            val result = authService.changePassword(userId, currentPassword, newPassword, newPassword)

            // Then
            assertThat(result.message).contains("비밀번호가 변경되었습니다")
            verify(exactly = 1) { refreshTokenRepository.revokeAllByUser(user) }
        }

        @Test
        fun `현재 비밀번호가 틀리면 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", passwordEncoder.encode("correctpassword"))

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When & Then
            assertThatThrownBy { authService.changePassword(userId, "wrongpassword", "newpassword", "newpassword") }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CURRENT_PASSWORD)
        }

        @Test
        fun `비밀번호가 설정되지 않은 계정에서 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", password = null)

            every { userRepository.findById(userId) } returns Optional.of(user)

            // When & Then
            assertThatThrownBy { authService.changePassword(userId, "anypassword", "newpassword", "newpassword") }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NO_PASSWORD_SET)
        }
    }

    @Nested
    @DisplayName("unlinkSocialAccount 메서드")
    inner class UnlinkSocialAccountTest {
        @Test
        fun `소셜 계정 연동을 해제해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", passwordEncoder.encode("password"))
            val socialAccount =
                SocialAccountEntity(
                    user = user,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { socialAccountRepository.findAllByUser(user) } returns listOf(socialAccount)
            every { socialAccountRepository.findByUserAndProvider(user, AuthProvider.GOOGLE) } returns socialAccount

            // When
            val result = authService.unlinkSocialAccount(userId, AuthProvider.GOOGLE)

            // Then
            assertThat(result.message).contains("연동이 해제되었습니다")
            verify(exactly = 1) { socialAccountRepository.delete(socialAccount) }
        }

        @Test
        fun `마지막 로그인 방법 해제 시 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val user = createUserEntity(userId, "test@example.com", "테스트유저", password = null)
            val socialAccount =
                SocialAccountEntity(
                    user = user,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { socialAccountRepository.findAllByUser(user) } returns listOf(socialAccount)

            // When & Then
            assertThatThrownBy { authService.unlinkSocialAccount(userId, AuthProvider.GOOGLE) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.CANNOT_UNLINK_ONLY_LOGIN_METHOD)
        }
    }

    private fun createUserEntity(
        id: Long,
        email: String,
        nickname: String,
        password: String? = "encodedpassword",
    ): UserEntity {
        val entity =
            UserEntity(
                email = email,
                password = password,
                nickname = nickname,
                role = UserRole.USER,
                status = UserStatus.ACTIVE,
            )
        val idField = entity.javaClass.superclass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(entity, id)
        return entity
    }
}
