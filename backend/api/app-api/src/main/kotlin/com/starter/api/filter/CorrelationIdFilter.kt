package com.starter.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {
    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        const val CORRELATION_ID_MDC_KEY = "correlationId"
        const val REQUEST_URI_MDC_KEY = "requestUri"
        const val REQUEST_METHOD_MDC_KEY = "requestMethod"
        const val CLIENT_IP_MDC_KEY = "clientIp"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val correlationId = extractOrGenerateCorrelationId(request)

            MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
            MDC.put(REQUEST_URI_MDC_KEY, request.requestURI)
            MDC.put(REQUEST_METHOD_MDC_KEY, request.method)
            MDC.put(CLIENT_IP_MDC_KEY, getClientIp(request))

            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    private fun extractOrGenerateCorrelationId(request: HttpServletRequest): String =
        request
            .getHeader(CORRELATION_ID_HEADER)
            ?.takeIf { it.isNotBlank() && isValidUuid(it) }
            ?: UUID.randomUUID().toString()

    private fun isValidUuid(value: String): Boolean =
        try {
            UUID.fromString(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr ?: "unknown"
    }
}
