package com.starter.api.auth.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * 계정 잠금 설정
 *
 * 환경변수:
 * - ACCOUNT_LOCKOUT_MAX_FAILED_ATTEMPTS: 최대 로그인 실패 횟수 (기본: 5)
 * - ACCOUNT_LOCKOUT_DURATION_MINUTES: 잠금 지속 시간 (기본: 15분)
 * - ACCOUNT_LOCKOUT_ENABLED: 활성화 여부 (기본: true)
 */
@Validated
@ConfigurationProperties(prefix = "security.account-lockout")
data class AccountLockoutProperties(
    /**
     * 계정 잠금까지 허용되는 최대 로그인 실패 횟수
     */
    @field:Min(value = 1, message = "Max failed attempts must be at least 1")
    @field:Max(value = 20, message = "Max failed attempts must not exceed 20")
    val maxFailedAttempts: Int = 5,
    /**
     * 계정 잠금 지속 시간 (분)
     */
    @field:Min(value = 1, message = "Lock duration must be at least 1 minute")
    @field:Max(value = 1440, message = "Lock duration must not exceed 24 hours (1440 minutes)")
    val lockDurationMinutes: Long = 15,
    /**
     * 계정 잠금 기능 활성화 여부
     */
    val enabled: Boolean = true,
)
