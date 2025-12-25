package com.starter.api.auth.controller

import com.starter.api.auth.security.AuthUser
import com.starter.api.auth.service.session.SessionResponse
import com.starter.api.auth.service.session.SessionService
import com.starter.core.api.support.HttpConstants.AUTHORIZATION_HEADER
import com.starter.core.api.support.HttpConstants.BEARER_PREFIX
import com.starter.core.api.support.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 세션 관리 API
 */
@RestController
@RequestMapping("/api/v1/auth/sessions")
class SessionController(
    private val sessionService: SessionService,
) {
    /**
     * 활성 세션 목록 조회
     */
    @GetMapping
    fun getActiveSessions(
        @AuthenticationPrincipal authUser: AuthUser,
    ): ApiResponse<List<SessionResponse>> {
        val sessions = sessionService.getActiveSessions(authUser.userId)
        return ApiResponse.success(sessions)
    }

    /**
     * 특정 세션 강제 종료
     */
    @DeleteMapping("/{sessionId}")
    fun revokeSession(
        @AuthenticationPrincipal authUser: AuthUser,
        @PathVariable sessionId: Long,
    ): ApiResponse<Boolean> {
        val result = sessionService.revokeSession(authUser.userId, sessionId)
        return ApiResponse.success(result)
    }

    /**
     * 현재 세션 제외 모든 세션 종료
     */
    @PostMapping("/revoke-others")
    fun revokeAllOtherSessions(
        @AuthenticationPrincipal authUser: AuthUser,
        request: HttpServletRequest,
    ): ApiResponse<RevokeOthersResponse> {
        val token = extractToken(request)
        val revokedCount = sessionService.revokeAllOtherSessions(authUser.userId, token ?: "")
        return ApiResponse.success(RevokeOthersResponse(revokedCount))
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}

data class RevokeOthersResponse(
    val revokedCount: Int,
)
