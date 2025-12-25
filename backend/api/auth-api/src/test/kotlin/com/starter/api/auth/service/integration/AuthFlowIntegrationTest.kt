package com.starter.api.auth.service.integration

import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.service.auth.AuthenticationService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.token.TokenBlacklistRepository
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
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
        "security.account-lockout.enabled=true",
        "security.account-lockout.max-failed-attempts=5",
        "security.account-lockout.lock-duration-minutes=15",
    ],
)
@DisplayName("인증 플로우 통합 테스트")
class AuthFlowIntegrationTest {
    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var tokenBlacklistRepository: TokenBlacklistRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

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
        userRepository.deleteAll()
    }

    /**
     * 테스트용 사용자를 DB에 직접 생성 (토큰 생성 없이)
     */
    private fun createUserDirectly(
        email: String = "test@example.com",
        password: String = "Password123!",
        nickname: String = "테스트유저",
    ): UserEntity =
        userRepository.save(
            UserEntity(
                email = email,
                password = passwordEncoder.encode(password),
                nickname = nickname,
            ),
        )

    @Nested
    @DisplayName("회원가입 테스트")
    inner class SignUpTest {
        @Test
        fun `회원가입이 성공한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "newuser@example.com",
                    password = "Password123!",
                    nickname = "새로운유저",
                )

            // When
            val result = authenticationService.signUp(request)

            // Then
            assertThat(result.user.email).isEqualTo("newuser@example.com")
            assertThat(result.user.nickname).isEqualTo("새로운유저")
            assertThat(result.token).isNotBlank()
            assertThat(result.refreshToken).isNotBlank()

            // Verify user saved in DB
            val savedUser = userRepository.findByEmail("newuser@example.com")
            assertThat(savedUser).isNotNull
            assertThat(savedUser?.status).isEqualTo(UserStatus.ACTIVE)
        }

        @Test
        fun `중복 이메일로 회원가입하면 실패한다`() {
            // Given - 기존 사용자 직접 생성
            createUserDirectly(email = "existing@example.com")

            val request =
                SignUpRequest(
                    email = "existing@example.com",
                    password = "Password456!",
                    nickname = "새유저",
                )

            // When & Then
            assertThatThrownBy { authenticationService.signUp(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_EMAIL)
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    inner class SignInTest {
        @Test
        fun `로그인이 성공한다`() {
            // Given - 사용자 직접 생성
            createUserDirectly(
                email = "login@example.com",
                password = "Password123!",
            )

            val request =
                SignInRequest(
                    email = "login@example.com",
                    password = "Password123!",
                )

            // When
            val result = authenticationService.signIn(request)

            // Then
            assertThat(result.user.email).isEqualTo("login@example.com")
            assertThat(result.token).isNotBlank()
            assertThat(result.refreshToken).isNotBlank()
        }

        @Test
        fun `잘못된 비밀번호로 로그인하면 실패한다`() {
            // Given
            createUserDirectly(
                email = "wrongpw@example.com",
                password = "Password123!",
            )

            val request =
                SignInRequest(
                    email = "wrongpw@example.com",
                    password = "WrongPassword123!",
                )

            // When & Then
            assertThatThrownBy { authenticationService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }

        @Test
        fun `존재하지 않는 사용자로 로그인하면 실패한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "nonexistent@example.com",
                    password = "Password123!",
                )

            // When & Then
            assertThatThrownBy { authenticationService.signIn(request) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS)
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    inner class TokenRefreshTest {
        @Test
        fun `토큰 갱신이 성공한다`() {
            // Given - 사용자 생성 후 로그인하여 토큰 획득
            createUserDirectly(
                email = "refresh@example.com",
                password = "Password123!",
            )

            val loginResult =
                authenticationService.signIn(
                    SignInRequest(
                        email = "refresh@example.com",
                        password = "Password123!",
                    ),
                )

            // JWT 토큰의 iat(issued at) 클레임이 초 단위이므로 다른 토큰을 얻으려면 대기 필요
            Thread.sleep(1100)

            // When - 토큰 갱신
            val refreshResult =
                authenticationService.refresh(
                    RefreshTokenRequest(loginResult.refreshToken!!),
                )

            // Then
            assertThat(refreshResult.accessToken).isNotBlank()
            assertThat(refreshResult.refreshToken).isNotBlank()
            assertThat(refreshResult.accessToken).isNotEqualTo(loginResult.token)
        }

        @Test
        fun `잘못된 리프레시 토큰으로 갱신하면 실패한다`() {
            // When & Then
            assertThatThrownBy {
                authenticationService.refresh(RefreshTokenRequest("invalid-token"))
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    inner class SignOutTest {
        @Test
        fun `로그아웃이 성공하고 리프레시 토큰이 무효화된다`() {
            // Given - 사용자 생성 후 로그인
            createUserDirectly(
                email = "logout@example.com",
                password = "Password123!",
            )

            val loginResult =
                authenticationService.signIn(
                    SignInRequest(
                        email = "logout@example.com",
                        password = "Password123!",
                    ),
                )

            val user = userRepository.findByEmailAndStatus("logout@example.com", UserStatus.ACTIVE)!!

            // When - 로그아웃
            authenticationService.signOut(user.id, loginResult.token)

            // Then - 리프레시 토큰 무효화 확인
            assertThatThrownBy {
                authenticationService.refresh(RefreshTokenRequest(loginResult.refreshToken!!))
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN)
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    inner class GetMeTest {
        @Test
        fun `사용자 정보를 조회할 수 있다`() {
            // Given
            val user =
                createUserDirectly(
                    email = "getme@example.com",
                    nickname = "조회테스트",
                )

            // When
            val result = authenticationService.getMe(user.id)

            // Then
            assertThat(result.email).isEqualTo("getme@example.com")
            assertThat(result.nickname).isEqualTo("조회테스트")
        }
    }

    @Nested
    @DisplayName("닉네임 변경 테스트")
    inner class UpdateNicknameTest {
        @Test
        fun `닉네임을 변경할 수 있다`() {
            // Given
            val user =
                createUserDirectly(
                    email = "nickname@example.com",
                    nickname = "원래닉네임",
                )

            // When
            val result = authenticationService.updateNickname(user.id, "새닉네임")

            // Then
            assertThat(result.user.nickname).isEqualTo("새닉네임")

            // DB 확인
            val updatedUser = userRepository.findById(user.id).orElseThrow()
            assertThat(updatedUser.nickname).isEqualTo("새닉네임")
        }
    }

    @Nested
    @DisplayName("계정 잠금 테스트")
    inner class AccountLockoutTest {
        @Test
        fun `연속 로그인 실패 후 계정이 잠긴다`() {
            // Given
            createUserDirectly(
                email = "lockout@example.com",
                password = "Password123!",
            )

            val wrongRequest =
                SignInRequest(
                    email = "lockout@example.com",
                    password = "WrongPassword!",
                )

            // When - 5번 연속 로그인 실패
            repeat(5) {
                try {
                    authenticationService.signIn(wrongRequest)
                } catch (e: CoreApiException) {
                    // 예외 무시 - 실패 기록용
                }
            }

            // Then - 6번째 시도는 계정 잠금 에러
            assertThatThrownBy { authenticationService.signIn(wrongRequest) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_LOCKED)
        }

        @Test
        fun `올바른 비밀번호로 로그인하면 실패 카운터가 초기화된다`() {
            // Given
            createUserDirectly(
                email = "reset@example.com",
                password = "Password123!",
            )

            val wrongRequest =
                SignInRequest(
                    email = "reset@example.com",
                    password = "WrongPassword!",
                )

            // 2번 실패
            repeat(2) {
                try {
                    authenticationService.signIn(wrongRequest)
                } catch (e: CoreApiException) {
                    // 무시
                }
            }

            // When - 올바른 비밀번호로 로그인 성공
            val correctRequest =
                SignInRequest(
                    email = "reset@example.com",
                    password = "Password123!",
                )
            authenticationService.signIn(correctRequest)

            // Then - 다시 5번 실패해야 잠김 (이전 실패 카운터 초기화됨)
            repeat(5) {
                try {
                    authenticationService.signIn(wrongRequest)
                } catch (e: CoreApiException) {
                    // 무시
                }
            }

            // 6번째 시도에서 잠김
            assertThatThrownBy { authenticationService.signIn(wrongRequest) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_LOCKED)
        }
    }
}
