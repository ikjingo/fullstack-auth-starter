package com.starter.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class RequestResponseLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger("HTTP")

    companion object {
        private val SENSITIVE_HEADERS =
            setOf(
                "authorization",
                "cookie",
                "set-cookie",
                "x-api-key",
            )

        private val SENSITIVE_PARAMS =
            setOf(
                "password",
                "token",
                "secret",
                "key",
                "credential",
            )

        private val EXCLUDED_PATHS =
            setOf(
                "/actuator/health",
                "/actuator/prometheus",
                "/favicon.ico",
            )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()

        try {
            logRequest(wrappedRequest)
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logResponse(wrappedRequest, wrappedResponse, duration)
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun shouldSkip(request: HttpServletRequest): Boolean = EXCLUDED_PATHS.any { request.requestURI.startsWith(it) }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val correlationId = MDC.get("correlationId") ?: "N/A"
        val clientIp = MDC.get("clientIp") ?: "unknown"

        val headers =
            buildString {
                val headerNames = request.headerNames
                while (headerNames.hasMoreElements()) {
                    val name = headerNames.nextElement()
                    val value =
                        if (SENSITIVE_HEADERS.contains(name.lowercase())) {
                            "[MASKED]"
                        } else {
                            request.getHeader(name)
                        }
                    append("$name=$value ")
                }
            }.trim()

        val queryString = maskSensitiveParams(request.queryString)

        log.info(
            "REQUEST: {} {} query=[{}] clientIp={} correlationId={} headers=[{}]",
            request.method,
            request.requestURI,
            queryString ?: "",
            clientIp,
            correlationId,
            headers,
        )
    }

    private fun logResponse(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        duration: Long,
    ) {
        val correlationId = MDC.get("correlationId") ?: "N/A"
        val status = response.status
        val contentLength = response.contentSize

        val logLevel =
            when {
                status >= 500 -> "ERROR"
                status >= 400 -> "WARN"
                else -> "INFO"
            }

        val message = "RESPONSE: {} {} status={} duration={}ms size={} correlationId={}"
        val args =
            arrayOf(
                request.method,
                request.requestURI,
                status,
                duration,
                contentLength,
                correlationId,
            )

        when (logLevel) {
            "ERROR" -> log.error(message, *args)
            "WARN" -> log.warn(message, *args)
            else -> log.info(message, *args)
        }
    }

    private fun maskSensitiveParams(queryString: String?): String? {
        if (queryString.isNullOrBlank()) return null

        return queryString.split("&").joinToString("&") { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2 && SENSITIVE_PARAMS.any { parts[0].lowercase().contains(it) }) {
                "${parts[0]}=[MASKED]"
            } else {
                param
            }
        }
    }
}
