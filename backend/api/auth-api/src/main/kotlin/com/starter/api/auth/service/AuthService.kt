package com.starter.api.auth.service

import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.MessageResponse
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.controller.response.UserResponse
import com.starter.api.auth.service.auth.AuthenticationService
import com.starter.api.auth.service.auth.PasswordService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 Facade 서비스
 *
 * 하위 서비스 구성:
 * - AuthenticationService: 회원가입/로그인, 토큰 관리, 사용자 정보
 * - PasswordService: 비밀번호 변경
 */
@Service
@Transactional(readOnly = true)
class AuthService(
    private val authenticationService: AuthenticationService,
    private val passwordService: PasswordService,
) {
    // ========== Authentication ==========

    @Transactional
    fun signUp(request: SignUpRequest): AuthResponse = authenticationService.signUp(request)

    @Transactional
    fun signIn(request: SignInRequest): AuthResponse = authenticationService.signIn(request)

    @Transactional
    fun refresh(request: RefreshTokenRequest): TokenResponse = authenticationService.refresh(request)

    @Transactional
    fun signOut(
        userId: Long,
        accessToken: String?,
    ) = authenticationService.signOut(userId, accessToken)

    fun getMe(userId: Long): UserResponse = authenticationService.getMe(userId)

    @Transactional
    fun updateNickname(
        userId: Long,
        nickname: String,
    ): AuthResponse = authenticationService.updateNickname(userId, nickname)

    // ========== Password Management ==========

    @Transactional
    fun setPassword(
        userId: Long,
        password: String,
        confirmPassword: String,
    ): AuthResponse = passwordService.setPassword(userId, password, confirmPassword)

    @Transactional
    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): MessageResponse = passwordService.changePassword(userId, currentPassword, newPassword, confirmPassword)
}
