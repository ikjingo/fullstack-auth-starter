package com.starter.api.auth.docs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.SessionController
import com.starter.api.auth.service.session.SessionResponse
import com.starter.api.auth.service.session.SessionService
import com.starter.api.auth.test.support.RestDocsTestSupport
import com.starter.core.api.controller.ApiControllerAdvice
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SessionController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Session API 문서화")
class SessionControllerDocsTest : RestDocsTestSupport() {
    @MockkBean
    private lateinit var sessionService: SessionService

    @Test
    fun `활성 세션 목록 조회 API`() {
        // Given
        val sessions =
            listOf(
                SessionResponse(
                    id = 1,
                    createdAt = "2024-01-01T10:00:00",
                    expiresAt = "2024-01-08T10:00:00",
                    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0",
                    ipAddress = "192.168.1.100",
                    deviceInfo = "Desktop - Windows",
                ),
                SessionResponse(
                    id = 2,
                    createdAt = "2024-01-02T14:30:00",
                    expiresAt = "2024-01-09T14:30:00",
                    userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0)",
                    ipAddress = "10.0.0.50",
                    deviceInfo = "Mobile - iOS",
                ),
            )

        every { sessionService.getActiveSessions(testUserId) } returns sessions

        // When & Then
        mockMvc
            .perform(
                get("/api/v1/auth/sessions")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isArray)
            .andDo(
                document(
                    identifier = "sessions-get-all",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("세션")
                                    .summary("활성 세션 목록 조회")
                                    .description("현재 사용자의 모든 활성 세션 목록을 조회합니다.")
                                    .responseSchema(Schema("SessionListResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("세션 목록"),
                                        fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("세션 ID"),
                                        fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("세션 생성일시"),
                                        fieldWithPath("data[].expiresAt").type(JsonFieldType.STRING).description("세션 만료일시"),
                                        fieldWithPath("data[].userAgent").type(JsonFieldType.STRING).description("User Agent").optional(),
                                        fieldWithPath("data[].ipAddress").type(JsonFieldType.STRING).description("IP 주소").optional(),
                                        fieldWithPath("data[].deviceInfo").type(JsonFieldType.STRING).description("디바이스 정보").optional(),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `특정 세션 종료 API`() {
        // Given
        val sessionId = 2L
        every { sessionService.revokeSession(testUserId, sessionId) } returns true

        // When & Then
        mockMvc
            .perform(
                delete("/api/v1/auth/sessions/{sessionId}", sessionId)
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").value(true))
            .andDo(
                document(
                    identifier = "sessions-revoke",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("세션")
                                    .summary("특정 세션 종료")
                                    .description("지정된 세션을 강제 종료합니다.")
                                    .pathParameters(
                                        parameterWithName("sessionId").description("종료할 세션 ID"),
                                    ).responseSchema(Schema("BooleanResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("종료 성공 여부"),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }

    @Test
    fun `다른 모든 세션 종료 API`() {
        // Given
        every { sessionService.revokeAllOtherSessions(testUserId, any()) } returns 3

        // When & Then
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/revoke-others")
                    .header("Authorization", "Bearer test-access-token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.revokedCount").value(3))
            .andDo(
                document(
                    identifier = "sessions-revoke-others",
                    snippets =
                        arrayOf(
                            resource(
                                ResourceSnippetParameters
                                    .builder()
                                    .tag("세션")
                                    .summary("다른 모든 세션 종료")
                                    .description("현재 세션을 제외한 모든 다른 세션을 종료합니다.")
                                    .responseSchema(Schema("RevokeOthersResponse"))
                                    .responseFields(
                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 결과"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.revokedCount").type(JsonFieldType.NUMBER).description("종료된 세션 수"),
                                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                                    ).build(),
                            ),
                        ),
                ),
            )
    }
}
