package com.starter.api.auth.docs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.AuthController
import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.AuthUserResponse
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.controller.response.UserResponse
import com.starter.api.auth.service.AuthService
import com.starter.api.auth.test.support.RestDocsTestSupport
import com.starter.core.api.controller.ApiControllerAdvice
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(AuthController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Auth API 문서화")
class AuthControllerDocsTest : RestDocsTestSupport() {
    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `회원가입 API`() {
        // Given
        val request =
            SignUpRequest(
                email = "user@example.com",
                password = "Password123!",
                nickname = "테스트유저",
            )
        val response = createAuthResponse()

        every { authService.signUp(any()) } returns response

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "auth-signup",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("회원가입")
                                    .description("이메일, 비밀번호, 닉네임으로 새 계정을 생성합니다.")
                                    .requestSchema(Schema("SignUpRequest"))
                                    .responseSchema(Schema("AuthResponse"))
                                    .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (8자 이상)"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임 (2-50자)"),
                                    ).responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과 (SUCCESS/ERROR)"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.user").type(JsonFieldType.OBJECT).description("사용자 정보"),
                                        fieldWithPath("data.user.id").type(JsonFieldType.STRING).description("사용자 ID"),
                                        fieldWithPath("data.user.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.user.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("data.user.role").type(JsonFieldType.STRING).description("역할 (USER/ADMIN)"),
                                        fieldWithPath(
                                            "data.user.profileImageUrl",
                                        ).type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                        fieldWithPath("data.user.hasPassword").type(JsonFieldType.BOOLEAN).description("비밀번호 설정 여부"),
                                        fieldWithPath("data.token").type(JsonFieldType.STRING).description("JWT 액세스 토큰"),
                                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰").optional(),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `로그인 API`() {
        // Given
        val request =
            SignInRequest(
                email = "user@example.com",
                password = "Password123!",
            )
        val response = createAuthResponse()

        every { authService.signIn(any()) } returns response

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "auth-signin",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("로그인")
                                    .description("이메일과 비밀번호로 로그인합니다.")
                                    .requestSchema(Schema("SignInRequest"))
                                    .responseSchema(Schema("AuthResponse"))
                                    .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                    ).responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.user").type(JsonFieldType.OBJECT).description("사용자 정보"),
                                        fieldWithPath("data.user.id").type(JsonFieldType.STRING).description("사용자 ID"),
                                        fieldWithPath("data.user.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.user.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("data.user.role").type(JsonFieldType.STRING).description("역할"),
                                        fieldWithPath(
                                            "data.user.profileImageUrl",
                                        ).type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                        fieldWithPath("data.user.hasPassword").type(JsonFieldType.BOOLEAN).description("비밀번호 설정 여부"),
                                        fieldWithPath("data.token").type(JsonFieldType.STRING).description("JWT 액세스 토큰"),
                                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰").optional(),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `토큰 갱신 API`() {
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
            .andDo(
                document(
                    identifier = "auth-refresh",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("토큰 갱신")
                                    .description("리프레시 토큰으로 새 액세스 토큰을 발급받습니다.")
                                    .requestSchema(Schema("RefreshTokenRequest"))
                                    .responseSchema(Schema("TokenResponse"))
                                    .requestFields(
                                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                    ).responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 액세스 토큰"),
                                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("새 리프레시 토큰"),
                                        fieldWithPath("data.tokenType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                        fieldWithPath("data.expiresIn").type(JsonFieldType.NUMBER).description("만료 시간 (초)"),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `내 정보 조회 API`() {
        // Given
        val response =
            UserResponse(
                id = "1",
                email = "user@example.com",
                nickname = "테스트유저",
                role = "USER",
                createdAt = LocalDateTime.now(),
                hasPassword = true,
                profileImageUrl = null,
            )

        every { authService.getMe(testUserId) } returns response

        // When & Then
        mockMvc
            .perform(
                get("/api/v1/auth/me")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "auth-me",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("내 정보 조회")
                                    .description("현재 로그인한 사용자의 정보를 조회합니다.")
                                    .responseSchema(Schema("UserResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("사용자 ID"),
                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("역할"),
                                        fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("가입일"),
                                        fieldWithPath("data.hasPassword").type(JsonFieldType.BOOLEAN).description("비밀번호 설정 여부"),
                                        fieldWithPath(
                                            "data.profileImageUrl",
                                        ).type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `로그아웃 API`() {
        // Given
        every { authService.signOut(testUserId, any()) } returns Unit

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/signout")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "auth-signout",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("로그아웃")
                                    .description("현재 세션에서 로그아웃합니다.")
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터").optional(),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    private fun createAuthResponse(): AuthResponse =
        AuthResponse(
            user =
                AuthUserResponse(
                    id = "1",
                    email = "user@example.com",
                    nickname = "테스트유저",
                    role = "USER",
                    profileImageUrl = null,
                    hasPassword = true,
                ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        )
}
