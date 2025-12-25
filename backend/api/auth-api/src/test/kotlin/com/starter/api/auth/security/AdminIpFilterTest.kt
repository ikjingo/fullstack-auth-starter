package com.starter.api.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.starter.api.auth.config.AdminIpProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter

@DisplayName("AdminIpFilter 테스트")
class AdminIpFilterTest {
    private lateinit var adminIpProperties: AdminIpProperties
    private lateinit var objectMapper: ObjectMapper
    private lateinit var filter: AdminIpFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain
    private lateinit var responseWriter: StringWriter

    private lateinit var printWriter: PrintWriter

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        filterChain = mockk(relaxed = true)
        responseWriter = StringWriter()
        printWriter = PrintWriter(responseWriter, true) // auto-flush enabled

        every { response.writer } returns printWriter
        // OncePerRequestFilter에서 사용하는 getAttribute가 null을 반환하도록 설정
        every { request.getAttribute(any()) } returns null
    }

    @Nested
    @DisplayName("Admin 경로 필터링")
    inner class AdminPathFiltering {
        @Test
        fun `허용된 IP에서 Admin API 접근 시 통과해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("192.168.1.100"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "192.168.1.100"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
            verify(exactly = 0) { response.status = HttpServletResponse.SC_FORBIDDEN }
        }

        @Test
        fun `허용되지 않은 IP에서 Admin API 접근 시 차단해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("192.168.1.100"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "10.0.0.1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then - 필터 체인이 호출되지 않으면 요청이 차단된 것
            verify(exactly = 0) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `Admin 경로가 아닌 경우 필터를 통과해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("192.168.1.100"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/auth/signin"
            every { request.remoteAddr } returns "10.0.0.1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("CIDR 표기법 지원")
    inner class CidrSupport {
        @Test
        fun `CIDR 범위 내 IP는 허용되어야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("10.0.0.0/8"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "10.255.255.255"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `CIDR 범위 외 IP는 차단되어야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("10.0.0.0/8"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "192.168.1.1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then - 필터 체인이 호출되지 않으면 요청이 차단된 것
            verify(exactly = 0) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `192_168_1_0_24 CIDR 범위 테스트`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("192.168.1.0/24"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "192.168.1.50"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("X-Forwarded-For 헤더 지원")
    inner class XForwardedForSupport {
        @Test
        fun `X-Forwarded-For 헤더의 첫 번째 IP를 사용해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("203.0.113.50"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "10.0.0.1"
            every { request.getHeader("X-Forwarded-For") } returns "203.0.113.50, 70.41.3.18, 150.172.238.178"
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `X-Real-IP 헤더를 사용해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("203.0.113.60"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "10.0.0.1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns "203.0.113.60"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("여러 IP 허용")
    inner class MultipleIpsAllowed {
        @Test
        fun `여러 허용된 IP 중 하나와 일치하면 통과해야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("192.168.1.100", "10.0.0.0/8", "172.16.0.1"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "172.16.0.1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("IPv6 지원")
    inner class IPv6Support {
        @Test
        fun `IPv6 localhost는 허용되어야 한다`() {
            // Given
            adminIpProperties =
                AdminIpProperties(
                    enabled = true,
                    allowedIps = listOf("::1"),
                    pathPatterns = listOf("/api/v1/admin/**"),
                )
            filter = AdminIpFilter(adminIpProperties, objectMapper)

            every { request.requestURI } returns "/api/v1/admin/score-rules"
            every { request.remoteAddr } returns "::1"
            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Real-IP") } returns null

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }
}
