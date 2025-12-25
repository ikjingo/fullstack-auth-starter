package com.starter.api.auth.config

import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * CORS 설정 프로퍼티
 * - 환경별로 허용 origin을 설정 가능
 * - 보안상 명시적인 origin만 허용
 */
@Validated
@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    /**
     * 허용된 origin 패턴 목록
     * 예: ["http://localhost:5173", "https://zenless.example.com"]
     */
    @field:NotEmpty
    val allowedOrigins: List<String> = listOf("http://localhost:5173", "http://127.0.0.1:5173"),
    /**
     * 허용된 HTTP 메서드
     */
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
    /**
     * 허용된 헤더 (명시적 목록 - * 사용 금지)
     */
    val allowedHeaders: List<String> =
        listOf(
            "Content-Type",
            "Authorization",
            "Accept",
            "Accept-Language",
            "X-Requested-With",
        ),
    /**
     * 노출할 응답 헤더
     */
    val exposedHeaders: List<String> =
        listOf(
            "X-Request-Id",
            "X-Correlation-ID",
        ),
    /**
     * 자격증명(쿠키, Authorization 헤더) 허용 여부
     * JWT 기반 인증에서는 true 필요
     */
    val allowCredentials: Boolean = true,
    /**
     * Preflight 요청 캐시 시간 (초)
     */
    val maxAge: Long = 3600,
)
