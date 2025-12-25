package com.starter.api.auth.docs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.SocialAuthController
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.AuthUserResponse
import com.starter.api.auth.controller.response.MessageResponse
import com.starter.api.auth.service.AuthService
import com.starter.api.auth.test.support.RestDocsTestSupport
import com.starter.core.api.controller.ApiControllerAdvice
import com.starter.storage.db.user.AuthProvider
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SocialAuthController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Social Auth API 문서화")
class SocialAuthControllerDocsTest : RestDocsTestSupport() {
    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `Google 로그인 API`() {
        // Given
        val idToken = "valid-google-id-token"
        val response = createAuthResponse()

        every { authService.signInWithGoogle(idToken) } returns response

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"idToken": "$idToken"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.token").exists())
            .andDo(
                document(
                    identifier = "social-google-login",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("소셜 인증")
                                    .summary("Google 로그인")
                                    .description(
                                        """
                                        Google ID Token을 사용하여 로그인합니다.

                                        신규 사용자인 경우 자동으로 회원가입이 진행됩니다.
                                        """.trimIndent(),
                                    ).requestSchema(Schema("GoogleLoginRequest"))
                                    .responseSchema(Schema("AuthResponse"))
                                    .requestFields(
                                        fieldWithPath("idToken").type(JsonFieldType.STRING).description("Google ID Token"),
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
                                        fieldWithPath("data.user.linkedSocialAccounts").type(JsonFieldType.ARRAY).description("연동된 소셜 계정"),
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
    fun `Google 계정 연동 API`() {
        // Given
        val idToken = "valid-google-id-token"
        val response = createAuthResponse()

        every { authService.linkGoogleAccount(testUserId, idToken) } returns response

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/social/link/google")
                    .header("Authorization", "Bearer test-access-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"idToken": "$idToken"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "social-link-google",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("소셜 인증")
                                    .summary("Google 계정 연동")
                                    .description("기존 계정에 Google 계정을 연동합니다.")
                                    .requestSchema(Schema("GoogleLoginRequest"))
                                    .responseSchema(Schema("AuthResponse"))
                                    .requestFields(
                                        fieldWithPath("idToken").type(JsonFieldType.STRING).description("Google ID Token"),
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
                                        fieldWithPath("data.user.linkedSocialAccounts").type(JsonFieldType.ARRAY).description("연동된 소셜 계정"),
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
    fun `소셜 계정 연동 해제 API`() {
        // Given
        every { authService.unlinkSocialAccount(testUserId, AuthProvider.GOOGLE) } returns
            MessageResponse("소셜 계정 연동이 해제되었습니다.")

        // When & Then
        mockMvc
            .perform(
                delete("/api/v1/auth/social/unlink/{provider}", "GOOGLE")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.message").exists())
            .andDo(
                document(
                    identifier = "social-unlink",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("소셜 인증")
                                    .summary("소셜 계정 연동 해제")
                                    .description(
                                        """
                                        연동된 소셜 계정을 해제합니다.

                                        **주의**: 비밀번호가 설정되어 있지 않고 연동된 소셜 계정이 하나뿐인 경우 해제할 수 없습니다.
                                        """.trimIndent(),
                                    ).pathParameters(
                                        parameterWithName("provider").description("소셜 제공자 (GOOGLE, KAKAO, NAVER)"),
                                    ).responseSchema(Schema("MessageResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메시지"),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `연동된 소셜 계정 목록 조회 API`() {
        // Given
        every { authService.getLinkedSocialAccounts(testUserId) } returns
            listOf(AuthProvider.GOOGLE, AuthProvider.KAKAO)

        // When & Then
        mockMvc
            .perform(
                get("/api/v1/auth/social/linked")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isArray)
            .andDo(
                document(
                    identifier = "social-linked-list",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("소셜 인증")
                                    .summary("연동된 소셜 계정 목록")
                                    .description("현재 사용자에게 연동된 모든 소셜 계정 목록을 조회합니다.")
                                    .responseSchema(Schema("LinkedSocialAccountsResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("연동된 소셜 제공자 목록 (GOOGLE, KAKAO, NAVER)"),
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
                    id = testUserId.toString(),
                    email = "user@example.com",
                    nickname = "구글유저",
                    role = "USER",
                    profileImageUrl = "https://lh3.googleusercontent.com/a/example",
                    hasPassword = false,
                    linkedSocialAccounts = listOf("GOOGLE"),
                ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            refreshToken = "refresh-token-value",
        )
}
