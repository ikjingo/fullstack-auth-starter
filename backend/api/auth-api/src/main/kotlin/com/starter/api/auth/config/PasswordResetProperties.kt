package com.starter.api.auth.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * 비밀번호 재설정 관련 설정
 *
 * 환경변수:
 * - PASSWORD_RESET_CODE_EXPIRATION_MINUTES: 인증코드 만료 시간 (기본: 10분)
 */
@Validated
@ConfigurationProperties(prefix = "password-reset")
data class PasswordResetProperties(
    /** 인증코드 만료 시간 (분) */
    @field:Min(value = 1, message = "Code expiration must be at least 1 minute")
    @field:Max(value = 60, message = "Code expiration must not exceed 60 minutes")
    val codeExpirationMinutes: Long = 10,
)
