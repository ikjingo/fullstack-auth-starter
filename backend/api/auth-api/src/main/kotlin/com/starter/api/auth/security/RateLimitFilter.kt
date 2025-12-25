package com.starter.api.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.starter.api.auth.service.ratelimit.RateLimitService
import com.starter.core.api.support.error.ErrorType
import com.starter.core.api.support.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 인증 엔드포인트 Rate Limiting 필터
 *
 * 적용 대상 (보안상 민감한 엔드포인트):
 * - POST /api/v1/auth/signin       - 로그인 시도
 * - POST /api/v1/auth/signup       - 회원가입
 * - POST /api/v1/auth/refresh      - 토큰 갱신
 */
@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val RATE_LIMITED_PATHS =
            setOf(
                "/api/v1/auth/signin",
                "/api/v1/auth/signup",
                "/api/v1/auth/refresh",
            )
        private const val HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining"
        private const val HEADER_RATE_LIMIT_LIMIT = "X-RateLimit-Limit"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val path = request.requestURI
        val method = request.method

        // Rate Limit 대상 경로인지 확인 (POST 요청만)
        if (method != "POST" || path !in RATE_LIMITED_PATHS) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)

        // Rate Limit 헤더 추가
        response.setHeader(HEADER_RATE_LIMIT_LIMIT, rateLimitService.getCapacity().toString())
        response.setHeader(HEADER_RATE_LIMIT_REMAINING, rateLimitService.getAvailableTokens(clientIp).toString())

        if (rateLimitService.tryConsume(clientIp)) {
            // 요청 허용
            response.setHeader(HEADER_RATE_LIMIT_REMAINING, rateLimitService.getAvailableTokens(clientIp).toString())
            filterChain.doFilter(request, response)
        } else {
            // Rate Limit 초과
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path)
            sendRateLimitExceededResponse(response)
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        // 프록시 환경 대응
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr
    }

    private fun sendRateLimitExceededResponse(response: HttpServletResponse) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ApiResponse.error<Any>(ErrorType.TOO_MANY_REQUESTS)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
