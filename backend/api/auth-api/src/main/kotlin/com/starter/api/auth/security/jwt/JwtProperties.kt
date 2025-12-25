package com.starter.api.auth.security.jwt

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * JWT 설정 속성
 *
 * 환경변수:
 * - JWT_SECRET: JWT 서명 키 (최소 32자, HS256 알고리즘)
 * - JWT_ACCESS_EXPIRATION: Access Token 만료 시간 (ms)
 * - JWT_REFRESH_EXPIRATION: Refresh Token 만료 시간 (ms)
 * - JWT_ISSUER: 토큰 발급자
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    @field:NotBlank(message = "JWT secret must not be blank")
    @field:Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits) for HS256")
    val secret: String,
    @field:Min(value = 60000, message = "Access token expiration must be at least 1 minute")
    val accessTokenExpiration: Long = 3600000, // 1시간 (ms)
    @field:Min(value = 3600000, message = "Refresh token expiration must be at least 1 hour")
    val refreshTokenExpiration: Long = 604800000, // 7일 (ms)
    @field:NotBlank(message = "JWT issuer must not be blank")
    val issuer: String = "zenless",
)
