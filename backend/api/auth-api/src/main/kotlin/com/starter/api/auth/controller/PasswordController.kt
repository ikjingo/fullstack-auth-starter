package com.starter.api.auth.controller

import com.starter.api.auth.controller.request.ChangePasswordRequest
import com.starter.api.auth.controller.request.SetPasswordRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.MessageResponse
import com.starter.api.auth.security.AuthUser
import com.starter.api.auth.service.AuthService
import com.starter.core.api.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 비밀번호 관리 컨트롤러
 *
 * 비밀번호 설정 및 변경 기능을 담당합니다.
 */
@RestController
@RequestMapping("/api/v1/auth")
class PasswordController(
    private val authService: AuthService,
) {
    @PostMapping("/set-password")
    fun setPassword(
        @AuthenticationPrincipal authUser: AuthUser,
        @Valid @RequestBody request: SetPasswordRequest,
    ): ApiResponse<AuthResponse> {
        val result =
            authService.setPassword(
                authUser.userId,
                request.password,
                request.confirmPassword,
            )
        return ApiResponse.success(result)
    }

    @PostMapping("/change-password")
    fun changePassword(
        @AuthenticationPrincipal authUser: AuthUser,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ApiResponse<MessageResponse> {
        val result =
            authService.changePassword(
                authUser.userId,
                request.currentPassword,
                request.newPassword,
                request.confirmPassword,
            )
        return ApiResponse.success(result)
    }
}
