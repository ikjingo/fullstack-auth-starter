package com.starter.api.auth.controller

import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.controller.request.ForgotPasswordRequest
import com.starter.api.auth.controller.response.MessageResponse
import com.starter.api.auth.service.AuthService
import com.starter.api.auth.test.support.ControllerTestSupport
import com.starter.core.api.controller.ApiControllerAdvice
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PasswordController::class)
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PasswordController")
class PasswordControllerTest : ControllerTestSupport() {
    @MockkBean
    private lateinit var authService: AuthService

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    inner class ForgotPasswordTest {
        @Test
        fun `비밀번호 재설정 인증코드를 발송해야 한다`() {
            // Given
            val request = ForgotPasswordRequest(email = "test@example.com")
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
                .andExpect(jsonPath("$.data.message").value("인증번호가 이메일로 발송되었습니다."))
        }
    }
}
