package com.starter.api.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Admin API IP 화이트리스트 설정
 *
 * 환경변수:
 * - ADMIN_IP_WHITELIST_ENABLED: IP 화이트리스트 활성화 여부 (기본: false)
 * - ADMIN_IP_WHITELIST_ALLOWED_IPS: 허용된 IP 목록 (쉼표 구분)
 *
 * 예시:
 * ADMIN_IP_WHITELIST_ALLOWED_IPS=127.0.0.1,192.168.1.100,10.0.0.0/8
 */
@Validated
@ConfigurationProperties(prefix = "admin.ip-whitelist")
data class AdminIpProperties(
    val enabled: Boolean = false,
    val allowedIps: List<String> = listOf("127.0.0.1", "::1"),
    val pathPatterns: List<String> = listOf("/api/v1/admin/**"),
)
