package com.starter.api.auth.service.auth

import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserRole
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

@DisplayName("PasswordService 테스트")
class PasswordServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationService: AuthenticationService
    private lateinit var eventPublisher: AuthEventPublisher
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        authenticationService = mockk(relaxed = true)
        eventPublisher = mockk(relaxed = true)

        passwordService =
            PasswordService(
                userRepository = userRepository,
                refreshTokenRepository = refreshTokenRepository,
                passwordEncoder = passwordEncoder,
                authenticationService = authenticationService,
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
