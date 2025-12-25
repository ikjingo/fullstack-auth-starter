package com.starter.api.auth.service.auth

import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.MessageResponse
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.api.auth.event.PasswordChangedEvent
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 비밀번호 관리 서비스
 * - 비밀번호 설정 (OAuth 계정)
 * - 비밀번호 변경
 */
@Service
@Transactional(readOnly = true)
class PasswordService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationService: AuthenticationService,
    private val eventPublisher: AuthEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun setPassword(
        userId: Long,
        password: String,
        confirmPassword: String,
    ): AuthResponse {
        if (password != confirmPassword) {
            throw CoreApiException(ErrorType.PASSWORD_MISMATCH)
        }

        val user = authenticationService.findUserByIdOrThrow(userId)

        if (user.password != null) {
            throw CoreApiException(ErrorType.PASSWORD_ALREADY_SET)
        }

        user.password = passwordEncoder.encode(password)
        log.info("Password set for user: ${user.email}")

        // 비밀번호 설정 이벤트 발행
        eventPublisher.publishPasswordChanged(
            userId,
            user.email,
            PasswordChangedEvent.PasswordChangeType.SET,
        )

        val tokenResponse = authenticationService.generateTokens(user)
        val linkedProviders = authenticationService.getLinkedProviderNames(user)
        return AuthResponse.from(user, tokenResponse.accessToken, linkedProviders)
    }

    @Transactional
    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): MessageResponse {
        if (newPassword != confirmPassword) {
            throw CoreApiException(ErrorType.PASSWORD_MISMATCH)
        }

        val user = authenticationService.findUserByIdOrThrow(userId)

        // 비밀번호가 설정되지 않은 경우 (OAuth 전용 계정)
        if (user.password == null) {
            throw CoreApiException(ErrorType.NO_PASSWORD_SET)
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw CoreApiException(ErrorType.INVALID_CURRENT_PASSWORD)
        }

        // 새 비밀번호 설정
        user.password = passwordEncoder.encode(newPassword)
        log.info("Password changed for user: ${user.email}")

        // 기존 리프레시 토큰 모두 무효화
        refreshTokenRepository.revokeAllByUser(user)

        // 비밀번호 변경 이벤트 발행
        eventPublisher.publishPasswordChanged(
            userId,
            user.email,
            PasswordChangedEvent.PasswordChangeType.CHANGE,
        )

        return MessageResponse("비밀번호가 변경되었습니다.")
    }
}
