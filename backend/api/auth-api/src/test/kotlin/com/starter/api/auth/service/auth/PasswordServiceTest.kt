package com.starter.api.auth.service.auth

import com.starter.api.auth.config.PasswordResetProperties
import com.starter.api.auth.controller.request.ForgotPasswordRequest
import com.starter.api.auth.controller.request.ResetPasswordRequest
import com.starter.api.auth.controller.request.VerifyCodeRequest
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.PasswordResetCodeEntity
import com.starter.storage.db.user.PasswordResetCodeRepository
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserRole
import com.starter.storage.db.user.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@DisplayName("PasswordService 테스트")
class PasswordServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordResetCodeRepository: PasswordResetCodeRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationService: AuthenticationService
    private lateinit var passwordResetProperties: PasswordResetProperties
    private lateinit var eventPublisher: AuthEventPublisher
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordResetCodeRepository = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        authenticationService = mockk(relaxed = true)
        passwordResetProperties = PasswordResetProperties(codeExpirationMinutes = 10)
        eventPublisher = mockk(relaxed = true)

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
    }

    private fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
        password: String? = "encodedPassword",
        nickname: String = "테스트유저",
    ): UserEntity =
        UserEntity(
            email = email,
            password = password,
            nickname = nickname,
            role = UserRole.USER,
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }

    private fun createResetCode(
        id: Long = 1L,
        email: String = "test@example.com",
        code: String = "123456",
        expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(10),
        used: Boolean = false,
        verified: Boolean = false,
    ): PasswordResetCodeEntity =
        PasswordResetCodeEntity(
            email = email,
            code = code,
            expiresAt = expiresAt,
            used = used,
            verified = verified,
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }

    @Nested
    @DisplayName("forgotPassword")
    inner class ForgotPasswordTest {
        @Test
        fun `비밀번호 찾기 요청이 성공해야 한다`() {
            // Given
            val request = ForgotPasswordRequest(email = "test@example.com")
            val user = createUser()

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { passwordResetCodeRepository.save(any()) } answers { firstArg() }

            // When
            val result = passwordService.forgotPassword(request)

            // Then
            assertThat(result.message).contains("인증번호")
            verify { passwordResetCodeRepository.invalidateAllByEmail(request.email) }
            verify { passwordResetCodeRepository.save(any()) }
        }

        @Test
        fun `존재하지 않는 이메일도 동일한 응답을 반환해야 한다 (보안)`() {
            // Given
            val request = ForgotPasswordRequest(email = "nonexistent@example.com")

            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns null

            // When
            val result = passwordService.forgotPassword(request)

            // Then
            assertThat(result.message).contains("인증번호")
            // 인증코드 저장은 호출되지 않아야 함
            verify(exactly = 0) { passwordResetCodeRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("verifyCode")
    inner class VerifyCodeTest {
        @Test
        fun `인증코드 검증이 성공해야 한다`() {
            // Given
            val request = VerifyCodeRequest(email = "test@example.com", code = "123456")
            val resetCode = createResetCode()

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(request.email, request.code)
            } returns resetCode

            // When
            val result = passwordService.verifyCode(request)

            // Then
            assertThat(result.message).contains("인증")
            assertThat(resetCode.verified).isTrue()
        }

        @Test
        fun `잘못된 인증코드로 검증 시 예외가 발생해야 한다`() {
            // Given
            val request = VerifyCodeRequest(email = "test@example.com", code = "wrong-code")

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(request.email, request.code)
            } returns null

            // When & Then
            assertThatThrownBy { passwordService.verifyCode(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_RESET_CODE)
        }

        @Test
        fun `만료된 인증코드로 검증 시 예외가 발생해야 한다`() {
            // Given
            val request = VerifyCodeRequest(email = "test@example.com", code = "123456")
            val expiredCode = createResetCode(expiresAt = LocalDateTime.now().minusMinutes(1))

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(request.email, request.code)
            } returns expiredCode

            // When & Then
            assertThatThrownBy { passwordService.verifyCode(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_RESET_CODE)
        }
    }

    @Nested
    @DisplayName("resetPassword")
    inner class ResetPasswordTest {
        @Test
        fun `비밀번호 재설정이 성공해야 한다`() {
            // Given
            val request =
                ResetPasswordRequest(
                    email = "test@example.com",
                    code = "123456",
                    newPassword = "NewPassword123!",
                )
            val user = createUser()
            val resetCode = createResetCode(verified = true)

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(request.email, request.code)
            } returns resetCode
            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns user
            every { passwordEncoder.encode(request.newPassword) } returns "newEncodedPassword"

            // When
            val result = passwordService.resetPassword(request)

            // Then
            assertThat(result.message).contains("비밀번호")
            assertThat(user.password).isEqualTo("newEncodedPassword")
            assertThat(resetCode.used).isTrue()
            verify { refreshTokenRepository.revokeAllByUser(user) }
        }

        @Test
        fun `검증되지 않은 코드로 재설정 시도 시 예외가 발생해야 한다`() {
            // Given
            val request =
                ResetPasswordRequest(
                    email = "test@example.com",
                    code = "123456",
                    newPassword = "NewPassword123!",
                )

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(request.email, request.code)
            } returns null

            // When & Then
            assertThatThrownBy { passwordService.resetPassword(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.CODE_NOT_VERIFIED)
        }

        @Test
        fun `만료된 코드로 재설정 시도 시 예외가 발생해야 한다`() {
            // Given
            val request =
                ResetPasswordRequest(
                    email = "test@example.com",
                    code = "123456",
                    newPassword = "NewPassword123!",
                )
            val expiredCode = createResetCode(verified = true, expiresAt = LocalDateTime.now().minusMinutes(1))

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(request.email, request.code)
            } returns expiredCode

            // When & Then
            assertThatThrownBy { passwordService.resetPassword(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_RESET_CODE)
        }

        @Test
        fun `존재하지 않는 사용자에 대해 재설정 시도 시 예외가 발생해야 한다`() {
            // Given
            val request =
                ResetPasswordRequest(
                    email = "test@example.com",
                    code = "123456",
                    newPassword = "NewPassword123!",
                )
            val resetCode = createResetCode(verified = true)

            every {
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(request.email, request.code)
            } returns resetCode
            every { userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE) } returns null

            // When & Then
            assertThatThrownBy { passwordService.resetPassword(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("setPassword")
    inner class SetPasswordTest {
        @Test
        fun `OAuth 계정에 비밀번호 설정이 성공해야 한다`() {
            // Given
            val userId = 1L
            val password = "NewPassword123!"
            val confirmPassword = "NewPassword123!"
            val user = createUser(id = userId, password = null) // OAuth 계정
            val tokenResponse =
                TokenResponse(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    expiresIn = 3600L,
                )

            every { authenticationService.findUserByIdOrThrow(userId) } returns user
            every { passwordEncoder.encode(password) } returns "encodedNewPassword"
            every { authenticationService.generateTokens(user) } returns tokenResponse
            every { authenticationService.getLinkedProviderNames(user) } returns listOf("GOOGLE")

            // When
            val result = passwordService.setPassword(userId, password, confirmPassword)

            // Then
            assertThat(result.user.email).isEqualTo(user.email)
            assertThat(user.password).isEqualTo("encodedNewPassword")
        }

        @Test
        fun `비밀번호가 일치하지 않으면 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val password = "NewPassword123!"
            val confirmPassword = "DifferentPassword!"

            // When & Then
            assertThatThrownBy { passwordService.setPassword(userId, password, confirmPassword) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_MISMATCH)
        }

        @Test
        fun `이미 비밀번호가 설정된 계정에 비밀번호 설정 시 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val password = "NewPassword123!"
            val confirmPassword = "NewPassword123!"
            val user = createUser(id = userId, password = "existingPassword")

            every { authenticationService.findUserByIdOrThrow(userId) } returns user

            // When & Then
            assertThatThrownBy { passwordService.setPassword(userId, password, confirmPassword) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_ALREADY_SET)
        }
    }

    @Nested
    @DisplayName("changePassword")
    inner class ChangePasswordTest {
        @Test
        fun `비밀번호 변경이 성공해야 한다`() {
            // Given
            val userId = 1L
            val currentPassword = "CurrentPassword123!"
            val newPassword = "NewPassword123!"
            val confirmPassword = "NewPassword123!"
            val user = createUser(id = userId)

            every { authenticationService.findUserByIdOrThrow(userId) } returns user
            every { passwordEncoder.matches(currentPassword, user.password) } returns true
            every { passwordEncoder.encode(newPassword) } returns "encodedNewPassword"

            // When
            val result = passwordService.changePassword(userId, currentPassword, newPassword, confirmPassword)

            // Then
            assertThat(result.message).contains("비밀번호")
            assertThat(user.password).isEqualTo("encodedNewPassword")
            verify { refreshTokenRepository.revokeAllByUser(user) }
        }

        @Test
        fun `새 비밀번호가 일치하지 않으면 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val currentPassword = "CurrentPassword123!"
            val newPassword = "NewPassword123!"
            val confirmPassword = "DifferentPassword!"

            // When & Then
            assertThatThrownBy {
                passwordService.changePassword(userId, currentPassword, newPassword, confirmPassword)
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_MISMATCH)
        }

        @Test
        fun `비밀번호가 없는 OAuth 계정에서 변경 시도 시 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val currentPassword = "CurrentPassword123!"
            val newPassword = "NewPassword123!"
            val confirmPassword = "NewPassword123!"
            val user = createUser(id = userId, password = null)

            every { authenticationService.findUserByIdOrThrow(userId) } returns user

            // When & Then
            assertThatThrownBy {
                passwordService.changePassword(userId, currentPassword, newPassword, confirmPassword)
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NO_PASSWORD_SET)
        }

        @Test
        fun `현재 비밀번호가 틀리면 예외가 발생해야 한다`() {
            // Given
            val userId = 1L
            val currentPassword = "WrongPassword!"
            val newPassword = "NewPassword123!"
            val confirmPassword = "NewPassword123!"
            val user = createUser(id = userId)

            every { authenticationService.findUserByIdOrThrow(userId) } returns user
            every { passwordEncoder.matches(currentPassword, user.password) } returns false

            // When & Then
            assertThatThrownBy {
                passwordService.changePassword(userId, currentPassword, newPassword, confirmPassword)
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CURRENT_PASSWORD)
        }
    }
}
