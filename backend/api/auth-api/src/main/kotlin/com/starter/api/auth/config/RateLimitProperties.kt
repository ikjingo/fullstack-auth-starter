package com.starter.api.auth.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Rate Limiting 설정
 *
 * 환경변수:
 * - RATE_LIMIT_AUTH_CAPACITY: 인증 엔드포인트 최대 요청 수
 * - RATE_LIMIT_AUTH_REFILL_TOKENS: 리필 토큰 수
 * - RATE_LIMIT_AUTH_REFILL_MINUTES: 리필 주기 (분)
 */
@Validated
@ConfigurationProperties(prefix = "rate-limit.auth")
data class RateLimitProperties(
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 1000, message = "Capacity must not exceed 1000")
    val capacity: Long = 10, // 버킷 최대 용량
    @field:Min(value = 1, message = "Refill tokens must be at least 1")
    @field:Max(value = 1000, message = "Refill tokens must not exceed 1000")
    val refillTokens: Long = 10, // 리필 토큰 수
    @field:Min(value = 1, message = "Refill minutes must be at least 1")
    @field:Max(value = 60, message = "Refill minutes must not exceed 60")
    val refillMinutes: Long = 1, // 리필 주기 (분)
)
