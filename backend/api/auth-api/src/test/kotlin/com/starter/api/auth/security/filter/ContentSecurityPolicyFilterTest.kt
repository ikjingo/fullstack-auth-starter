package com.starter.api.auth.security.filter

import com.starter.api.auth.security.ContentSecurityPolicyFilter
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@DisplayName("ContentSecurityPolicyFilter")
class ContentSecurityPolicyFilterTest {
    private lateinit var filter: ContentSecurityPolicyFilter
    private lateinit var filterChain: FilterChain
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        filter = ContentSecurityPolicyFilter()
        filterChain = mockk(relaxed = true)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
    }

    @Nested
    @DisplayName("CSP 헤더 설정")
    inner class CspHeaderTest {
        @Test
        fun `응답에 Content-Security-Policy 헤더가 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).isNotNull()
        }

        @Test
        fun `default-src가 self로 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("default-src 'self'")
        }

        @Test
        fun `script-src에 Google accounts가 허용되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("script-src 'self' https://accounts.google.com")
        }

        @Test
        fun `style-src에 unsafe-inline이 허용되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("style-src 'self' 'unsafe-inline'")
        }

        @Test
        fun `img-src에 data와 https가 허용되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("img-src 'self' data: https:")
        }

        @Test
        fun `font-src가 self로 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("font-src 'self'")
        }

        @Test
        fun `connect-src에 Google accounts가 허용되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("connect-src 'self' https://accounts.google.com")
        }

        @Test
        fun `frame-ancestors가 none으로 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("frame-ancestors 'none'")
        }

        @Test
        fun `base-uri가 self로 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("base-uri 'self'")
        }

        @Test
        fun `form-action이 self로 설정되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("form-action 'self'")
        }
    }

    @Nested
    @DisplayName("필터 체인 진행")
    inner class FilterChainTest {
        @Test
        fun `필터 처리 후 필터 체인이 진행되어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `모든 요청에 CSP 헤더가 추가되어야 한다`() {
            // Given
            request.method = "GET"
            request.requestURI = "/api/v1/auth/me"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(response.getHeader("Content-Security-Policy")).isNotNull()
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `POST 요청에도 CSP 헤더가 추가되어야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(response.getHeader("Content-Security-Policy")).isNotNull()
            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("CSP 정책 보안")
    inner class CspSecurityTest {
        @Test
        fun `XSS 방지를 위해 unsafe-eval이 허용되지 않아야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).doesNotContain("unsafe-eval")
        }

        @Test
        fun `클릭재킹 방지를 위해 frame-ancestors가 none이어야 한다`() {
            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val cspHeader = response.getHeader("Content-Security-Policy")
            assertThat(cspHeader).contains("frame-ancestors 'none'")
        }
    }
}
