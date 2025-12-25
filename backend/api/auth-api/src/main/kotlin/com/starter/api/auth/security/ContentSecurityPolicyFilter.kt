package com.starter.api.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Content Security Policy (CSP) 헤더 추가 필터
 *
 * XSS 및 데이터 삽입 공격 방지를 위한 보안 헤더
 */
@Component
class ContentSecurityPolicyFilter : OncePerRequestFilter() {
    companion object {
        private val CSP_POLICY =
            listOf(
                "default-src 'self'",
                "script-src 'self'",
                "style-src 'self' 'unsafe-inline'",
                "img-src 'self' data: https:",
                "font-src 'self'",
                "connect-src 'self'",
                "frame-ancestors 'none'",
                "base-uri 'self'",
                "form-action 'self'",
            ).joinToString("; ")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        response.setHeader("Content-Security-Policy", CSP_POLICY)
        filterChain.doFilter(request, response)
    }
}
