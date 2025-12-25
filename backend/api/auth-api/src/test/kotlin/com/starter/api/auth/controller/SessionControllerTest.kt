package com.starter.api.auth.controller

import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.service.session.SessionResponse
import com.starter.api.auth.service.session.SessionService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SessionController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SessionController")
class SessionControllerTest : ControllerTestSupport() {
    @MockkBean
    private lateinit var sessionService: SessionService

    @Nested
    @DisplayName("GET /api/v1/auth/sessions")
    inner class GetActiveSessionsTest {
        @Test
        fun `활성 세션 목록을 반환해야 한다`() {
            // Given
            val sessions =
                listOf(
                    createSessionResponse(1L, "Chrome on Windows"),
                    createSessionResponse(2L, "Safari on macOS"),
                )

            every { sessionService.getActiveSessions(testUserId) } returns sessions

            // When & Then
            mockMvc
                .perform(get("/api/v1/auth/sessions"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userAgent").value("Chrome on Windows"))
        }

        @Test
        fun `활성 세션이 없으면 빈 목록을 반환해야 한다`() {
            // Given
            every { sessionService.getActiveSessions(testUserId) } returns emptyList()

            // When & Then
            mockMvc
                .perform(get("/api/v1/auth/sessions"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(0))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/auth/sessions/{sessionId}")
    inner class RevokeSessionTest {
        @Test
        fun `특정 세션을 종료해야 한다`() {
            // Given
            val sessionId = 2L
            every { sessionService.revokeSession(testUserId, sessionId) } returns true

            // When & Then
            mockMvc
                .perform(delete("/api/v1/auth/sessions/$sessionId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true))
        }

        @Test
        fun `존재하지 않는 세션 종료 시 false를 반환해야 한다`() {
            // Given
            val sessionId = 999L
            every { sessionService.revokeSession(testUserId, sessionId) } returns false

            // When & Then
            mockMvc
                .perform(delete("/api/v1/auth/sessions/$sessionId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(false))
        }

        @Test
        fun `다른 사용자의 세션 종료 시 에러가 발생해야 한다`() {
            // Given
            val sessionId = 2L
            every { sessionService.revokeSession(testUserId, sessionId) } throws
                CoreApiException(ErrorType.FORBIDDEN)

            // When & Then
            mockMvc
                .perform(delete("/api/v1/auth/sessions/$sessionId"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.result").value("ERROR"))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/sessions/revoke-others")
    inner class RevokeAllOtherSessionsTest {
        @Test
        fun `현재 세션 제외 모든 세션을 종료해야 한다`() {
            // Given
            every { sessionService.revokeAllOtherSessions(testUserId, any()) } returns 3

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/sessions/revoke-others")
                        .header("Authorization", "Bearer test-token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.revokedCount").value(3))
        }

        @Test
        fun `다른 세션이 없으면 0을 반환해야 한다`() {
            // Given
            every { sessionService.revokeAllOtherSessions(testUserId, any()) } returns 0

            // When & Then
            mockMvc
                .perform(
                    post("/api/v1/auth/sessions/revoke-others")
                        .header("Authorization", "Bearer test-token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.revokedCount").value(0))
        }
    }

    private fun createSessionResponse(
        id: Long,
        userAgent: String,
    ): SessionResponse =
        SessionResponse(
            id = id,
            createdAt = "2024-01-01T00:00:00",
            expiresAt = "2024-01-08T00:00:00",
            userAgent = userAgent,
            ipAddress = "192.168.1.1",
            deviceInfo = "Desktop",
        )
}
