package com.starter.api.auth.service.integration

import com.starter.api.auth.controller.request.ForgotPasswordRequest
import com.starter.api.auth.controller.request.ResetPasswordRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.request.VerifyCodeRequest
import com.starter.api.auth.service.auth.AuthenticationService
import com.starter.api.auth.service.auth.PasswordService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.token.TokenBlacklistRepository
import com.starter.storage.db.user.PasswordResetCodeRepository
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.SocialAccountRepository
import com.starter.storage.db.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "password-reset.code-expiration-minutes=5",
    ],
)
@DisplayName("비밀번호 관리 플로우 통합 테스트")
class PasswordFlowIntegrationTest {
    @Autowired
    private lateinit var passwordService: PasswordService

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordResetCodeRepository: PasswordResetCodeRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var socialAccountRepository: SocialAccountRepository

    @Autowired
    private lateinit var tokenBlacklistRepository: TokenBlacklistRepository

    @BeforeEach
    fun setUp() {
        cleanUp()
    }

    @AfterEach
    fun tearDown() {
        cleanUp()
    }

    private fun cleanUp() {
        tokenBlacklistRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        passwordResetCodeRepository.deleteAll()
        socialAccountRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Nested
    @DisplayName("비밀번호 재설정 플로우")
    inner class PasswordResetFlowTest {
        @Test
        fun `비밀번호 찾기 요청이 성공한다`() {
            // Given - 사용자 생성
            val signUpRequest =
                SignUpRequest(
                    email = "reset@example.com",
                    password = "OldPassword123!",
                    nickname = "재설정유저",
                )
            authenticationService.signUp(signUpRequest)

            // When - 비밀번호 찾기 요청
            val result =
                passwordService.forgotPassword(
                    ForgotPasswordRequest(email = "reset@example.com"),
                )

            // Then
            assertThat(result.message).contains("인증번호")

            // DB 확인 - 인증코드 생성됨
            val codes = passwordResetCodeRepository.findAll()
            assertThat(codes).hasSize(1)
            assertThat(codes[0].email).isEqualTo("reset@example.com")
            assertThat(codes[0].code).hasSize(6)
            assertThat(codes[0].used).isFalse()
        }

        @Test
        fun `존재하지 않는 이메일로도 비밀번호 찾기가 동일한 응답을 반환한다`() {
            // When - 존재하지 않는 이메일로 요청 (보안을 위해 동일한 응답 반환)
            val result =
                passwordService.forgotPassword(
                    ForgotPasswordRequest(email = "nonexistent@example.com"),
                )

            // Then - 동일한 메시지 반환
            assertThat(result.message).contains("인증번호")

            // DB 확인 - 인증코드 생성되지 않음
            val codes = passwordResetCodeRepository.findAll()
            assertThat(codes).isEmpty()
        }

        @Test
        fun `비밀번호 재설정 전체 플로우가 성공한다`() {
            // Given - 사용자 생성
            val signUpRequest =
                SignUpRequest(
                    email = "fullflow@example.com",
                    password = "OldPassword123!",
                    nickname = "전체플로우유저",
                )
            authenticationService.signUp(signUpRequest)

            // Step 1: 비밀번호 찾기 요청
            passwordService.forgotPassword(
                ForgotPasswordRequest(email = "fullflow@example.com"),
            )

            // 생성된 인증코드 조회
            val resetCode = passwordResetCodeRepository.findAll().first()
            val code = resetCode.code

            // Step 2: 인증코드 검증
            val verifyResult =
                passwordService.verifyCode(
                    VerifyCodeRequest(
                        email = "fullflow@example.com",
                        code = code,
                    ),
                )
            assertThat(verifyResult.message).contains("인증")

            // Step 3: 새 비밀번호 설정
            val resetResult =
                passwordService.resetPassword(
                    ResetPasswordRequest(
                        email = "fullflow@example.com",
                        code = code,
                        newPassword = "NewPassword456!",
                    ),
                )
            assertThat(resetResult.message).contains("비밀번호")

            Thread.sleep(1100)

            // Step 4: 새 비밀번호로 로그인 성공
            val loginResult =
                authenticationService.signIn(
                    SignInRequest(
                        email = "fullflow@example.com",
                        password = "NewPassword456!",
                    ),
                )
            assertThat(loginResult.user.email).isEqualTo("fullflow@example.com")
        }

        @Test
        fun `이전 비밀번호로는 로그인할 수 없다`() {
            // Given - 사용자 생성 및 비밀번호 재설정
            val signUpRequest =
                SignUpRequest(
                    email = "oldpw@example.com",
                    password = "OldPassword123!",
                    nickname = "이전비밀번호유저",
                )
            authenticationService.signUp(signUpRequest)

            passwordService.forgotPassword(
                ForgotPasswordRequest(email = "oldpw@example.com"),
            )

            val code = passwordResetCodeRepository.findAll().first().code

            passwordService.verifyCode(
                VerifyCodeRequest(email = "oldpw@example.com", code = code),
            )

            passwordService.resetPassword(
                ResetPasswordRequest(
                    email = "oldpw@example.com",
                    code = code,
                    newPassword = "NewPassword456!",
                ),
            )

            // When & Then - 이전 비밀번호로 로그인 시도
            assertThatThrownBy {
                authenticationService.signIn(
                    SignInRequest(
                        email = "oldpw@example.com",
                        password = "OldPassword123!",
                    ),
                )
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }

        @Test
        fun `잘못된 인증코드로는 검증에 실패한다`() {
            // Given - 사용자 생성 및 비밀번호 찾기 요청
            val signUpRequest =
                SignUpRequest(
                    email = "wrongcode@example.com",
                    password = "Password123!",
                    nickname = "잘못된코드유저",
                )
            authenticationService.signUp(signUpRequest)

            passwordService.forgotPassword(
                ForgotPasswordRequest(email = "wrongcode@example.com"),
            )

            // When & Then - 잘못된 코드로 검증 시도
            assertThatThrownBy {
                passwordService.verifyCode(
                    VerifyCodeRequest(
                        email = "wrongcode@example.com",
                        code = "000000",
                    ),
                )
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_RESET_CODE)
        }

        @Test
        fun `검증되지 않은 코드로는 비밀번호 재설정에 실패한다`() {
            // Given - 사용자 생성 및 비밀번호 찾기 요청 (검증 단계 건너뜀)
            val signUpRequest =
                SignUpRequest(
                    email = "notverified@example.com",
                    password = "Password123!",
                    nickname = "미검증유저",
                )
            authenticationService.signUp(signUpRequest)

            passwordService.forgotPassword(
                ForgotPasswordRequest(email = "notverified@example.com"),
            )

            val code = passwordResetCodeRepository.findAll().first().code

            // When & Then - 검증 없이 바로 재설정 시도
            assertThatThrownBy {
                passwordService.resetPassword(
                    ResetPasswordRequest(
                        email = "notverified@example.com",
                        code = code,
                        newPassword = "NewPassword456!",
                    ),
                )
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.CODE_NOT_VERIFIED)
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 플로우")
    inner class ChangePasswordFlowTest {
        @Test
        fun `비밀번호 변경이 성공한다`() {
            // Given - 사용자 생성
            val signUpRequest =
                SignUpRequest(
                    email = "change@example.com",
                    password = "OldPassword123!",
                    nickname = "변경유저",
                )
            authenticationService.signUp(signUpRequest)
            val userId = userRepository.findByEmail("change@example.com")!!.id

            // When - 비밀번호 변경
            val result =
                passwordService.changePassword(
                    userId = userId,
                    currentPassword = "OldPassword123!",
                    newPassword = "NewPassword456!",
                    confirmPassword = "NewPassword456!",
                )

            // Then
            assertThat(result.message).contains("비밀번호")

            Thread.sleep(1100)

            // 새 비밀번호로 로그인 성공
            val loginResult =
                authenticationService.signIn(
                    SignInRequest(
                        email = "change@example.com",
                        password = "NewPassword456!",
                    ),
                )
            assertThat(loginResult.user.email).isEqualTo("change@example.com")
        }

        @Test
        fun `잘못된 현재 비밀번호로 변경 시도하면 실패한다`() {
            // Given
            val signUpRequest =
                SignUpRequest(
                    email = "wrongcurrent@example.com",
                    password = "Password123!",
                    nickname = "잘못된현재비밀번호유저",
                )
            authenticationService.signUp(signUpRequest)
            val userId = userRepository.findByEmail("wrongcurrent@example.com")!!.id

            // When & Then
            assertThatThrownBy {
                passwordService.changePassword(
                    userId = userId,
                    currentPassword = "WrongPassword123!",
                    newPassword = "NewPassword456!",
                    confirmPassword = "NewPassword456!",
                )
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CURRENT_PASSWORD)
        }

        @Test
        fun `새 비밀번호와 확인 비밀번호가 일치하지 않으면 실패한다`() {
            // Given
            val signUpRequest =
                SignUpRequest(
                    email = "mismatch@example.com",
                    password = "Password123!",
                    nickname = "불일치유저",
                )
            authenticationService.signUp(signUpRequest)
            val userId = userRepository.findByEmail("mismatch@example.com")!!.id

            // When & Then
            assertThatThrownBy {
                passwordService.changePassword(
                    userId = userId,
                    currentPassword = "Password123!",
                    newPassword = "NewPassword456!",
                    confirmPassword = "DifferentPassword789!",
                )
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSWORD_MISMATCH)
        }
    }
}
