package com.starter.api.auth.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * 세션 관리 설정
 */
@Validated
@ConfigurationProperties(prefix = "session")
data class SessionProperties(
    /**
     * 사용자당 최대 동시 세션 수
     */
    @field:Min(1)
    @field:Max(20)
    val maxSessionsPerUser: Int = 5,
)
