package com.starter.api.auth.controller

import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.AuthUserResponse
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.controller.response.UserResponse
import com.starter.api.auth.service.AuthService
import com.starter.api.auth.test.support.ControllerTestSupport
import com.starter.core.api.controller.ApiControllerAdvice
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(AuthController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthControllerTest : ControllerTestSupport() {
    @MockkBean
    private lateinit var authService: AuthService

    @Nested
    @DisplayName("POST /api/v1/auth/signup")
    inner class SignUpTest {
        @Test
        fun `회원가입을 성공해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "test@example.com",
                    password = "Password123!",
                    nickname = "테스트유저",
                )
            val response = createAuthResponse(1L, "test@example.com", "테스트유저", "test-token")

            every { authService.signUp(any()) } returns response

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.token").value("test-token"))
        }

        @Test
        fun `중복 이메일로 회원가입 시 에러가 발생해야 한다`() {
            // Given
            val request =
                SignUpRequest(
                    email = "duplicate@example.com",
                    password = "Password123!",
                    nickname = "테스트유저",
                )

            every { authService.signUp(any()) } throws CoreApiException(ErrorType.DUPLICATE_EMAIL)

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }

        @Test
        fun `유효하지 않은 이메일 형식으로 회원가입 시 에러가 발생해야 한다`() {
            // Given
            val request =
                mapOf(
                    "email" to "invalid-email",
                    "password" to "password123!",
                    "nickname" to "테스트유저",
                )

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }

        @Test
        fun `비밀번호가 8자 미만이면 에러가 발생해야 한다`() {
            // Given
            val request =
                mapOf(
                    "email" to "test@example.com",
                    "password" to "short",
                    "nickname" to "테스트유저",
                )

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/signin")
    inner class SignInTest {
        @Test
        fun `로그인을 성공해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = "Password123!",
                )
            val response = createAuthResponse(1L, "test@example.com", "테스트유저", "test-token")

            every { authService.signIn(any()) } returns response

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.token").value("test-token"))
        }

        @Test
        fun `잘못된 자격 증명으로 로그인 시 에러가 발생해야 한다`() {
            // Given
            val request =
                SignInRequest(
                    email = "test@example.com",
                    password = "wrongpassword",
                )

            every { authService.signIn(any()) } throws CoreApiException(ErrorType.INVALID_CREDENTIALS)

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    inner class RefreshTest {
        @Test
        fun `토큰을 갱신해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "valid-refresh-token")
            val response =
                TokenResponse(
                    accessToken = "new-access-token",
                    refreshToken = "new-refresh-token",
                    expiresIn = 3600,
                )

            every { authService.refresh(any()) } returns response

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
        }

        @Test
        fun `유효하지 않은 토큰으로 갱신 시 에러가 발생해야 한다`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "invalid-token")

            every { authService.refresh(any()) } throws CoreApiException(ErrorType.INVALID_TOKEN)

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    inner class GetMeTest {
        @Test
        fun `사용자 정보를 반환해야 한다`() {
            // Given
            val response =
                UserResponse(
                    id = "1",
                    email = "test@example.com",
                    nickname = "테스트유저",
                    role = "USER",
                    createdAt = LocalDateTime.now(),
                    hasPassword = true,
                    profileImageUrl = null,
                )

            every { authService.getMe(testUserId) } returns response

            // When & Then
            mockMvc
                .perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/signout")
    inner class SignOutTest {
        @Test
        fun `로그아웃을 성공해야 한다`() {
            // Given
            every { authService.signOut(testUserId, any()) } returns Unit

            // When & Then
            mockMvc
                .perform(post("/api/v1/auth/signout"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
        }
    }

    private fun createAuthResponse(
        id: Long,
        email: String,
        nickname: String,
        token: String,
    ): AuthResponse =
        AuthResponse(
            user =
                AuthUserResponse(
                    id = id.toString(),
                    email = email,
                    nickname = nickname,
                    role = "USER",
                    profileImageUrl = null,
                    hasPassword = true,
                ),
            token = token,
        )
}
