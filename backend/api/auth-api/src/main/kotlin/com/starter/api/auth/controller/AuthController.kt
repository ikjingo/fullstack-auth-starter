package com.starter.api.auth.controller

import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.request.UpdateNicknameRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.controller.response.UserResponse
import com.starter.api.auth.security.AuthUser
import com.starter.api.auth.service.AuthService
import com.starter.core.api.support.HttpConstants.AUTHORIZATION_HEADER
import com.starter.core.api.support.HttpConstants.BEARER_PREFIX
import com.starter.core.api.support.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 핵심 인증 컨트롤러
 *
 * 회원가입, 로그인, 로그아웃, 토큰 갱신, 사용자 정보 조회를 담당합니다.
 *
 * 관련 컨트롤러:
 * - PasswordController: 비밀번호 관리
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ApiResponse<AuthResponse> {
        val result = authService.signUp(request)
        return ApiResponse.success(result)
    }

    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
    ): ApiResponse<AuthResponse> {
        val result = authService.signIn(request)
        return ApiResponse.success(result)
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ApiResponse<TokenResponse> {
        val result = authService.refresh(request)
        return ApiResponse.success(result)
    }

    @PostMapping("/signout")
    fun signOut(
        @AuthenticationPrincipal authUser: AuthUser,
        request: HttpServletRequest,
    ): ApiResponse<Any> {
        val token = extractToken(request)
        authService.signOut(authUser.userId, token)
        return ApiResponse.success()
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal authUser: AuthUser,
    ): ApiResponse<UserResponse> {
        val result = authService.getMe(authUser.userId)
        return ApiResponse.success(result)
    }

    @PostMapping("/update-nickname")
    fun updateNickname(
        @AuthenticationPrincipal authUser: AuthUser,
        @Valid @RequestBody request: UpdateNicknameRequest,
    ): ApiResponse<AuthResponse> {
        val result = authService.updateNickname(authUser.userId, request.nickname)
        return ApiResponse.success(result)
    }
}
