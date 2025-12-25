package com.starter.api.auth.docs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.PasswordController
import com.starter.api.auth.controller.request.ForgotPasswordRequest
import com.starter.api.auth.controller.response.MessageResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PasswordController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Password API 문서화")
class PasswordControllerDocsTest : RestDocsTestSupport() {
    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `비밀번호 찾기 API`() {
        // Given
        val request = ForgotPasswordRequest(email = "user@example.com")
        val response = MessageResponse("인증번호가 이메일로 발송되었습니다.")

        every { authService.forgotPassword(any()) } returns response

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andDo(
                document(
                    identifier = "auth-forgot-password",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("인증")
                                    .summary("비밀번호 찾기")
                                    .description("이메일로 비밀번호 재설정 인증번호를 발송합니다.")
                                    .requestSchema(Schema("ForgotPasswordRequest"))
                                    .responseSchema(Schema("MessageResponse"))
                                    .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                                    ).responseFields(
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
}
