package com.starter.api.auth.event

import java.time.LocalDateTime

/**
 * 인증 관련 도메인 이벤트 기본 클래스
 */
sealed class AuthEvent(
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

/**
 * 사용자 등록 완료 이벤트
 */
data class UserRegisteredEvent(
    val userId: Long,
    val email: String,
    val nickname: String?,
) : AuthEvent()

/**
 * 로그인 성공 이벤트
 */
data class LoginSuccessEvent(
    val userId: Long,
    val email: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
) : AuthEvent()

/**
 * 로그인 실패 이벤트
 */
data class LoginFailureEvent(
    val email: String,
    val reason: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
) : AuthEvent()

/**
 * 비밀번호 변경 완료 이벤트
 */
data class PasswordChangedEvent(
    val userId: Long,
    val email: String,
    val changeType: PasswordChangeType,
) : AuthEvent() {
    enum class PasswordChangeType {
        RESET,
        CHANGE,
        SET,
    }
}

/**
 * 계정 잠금 이벤트
 */
data class AccountLockedEvent(
    val userId: Long,
    val email: String,
    val failedAttempts: Int,
    val lockUntil: LocalDateTime,
) : AuthEvent()

/**
 * 로그아웃 이벤트
 */
data class LogoutEvent(
    val userId: Long,
    val email: String,
) : AuthEvent()

/**
 * 토큰 갱신 이벤트
 */
data class TokenRefreshedEvent(
    val userId: Long,
    val email: String,
) : AuthEvent()
