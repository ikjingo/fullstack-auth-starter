package com.starter.api.auth.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.starter.api.auth.security.RateLimitFilter
import com.starter.api.auth.service.ratelimit.RateLimitService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@DisplayName("RateLimitFilter")
class RateLimitFilterTest {
    private lateinit var filter: RateLimitFilter
    private lateinit var rateLimitService: RateLimitService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var filterChain: FilterChain
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        rateLimitService = mockk()
        objectMapper = ObjectMapper()
        filterChain = mockk(relaxed = true)
        filter = RateLimitFilter(rateLimitService, objectMapper)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        every { rateLimitService.getCapacity() } returns 10L
        every { rateLimitService.getAvailableTokens(any()) } returns 9L
    }

    @Nested
    @DisplayName("Rate Limit 대상 경로")
    inner class RateLimitedPathsTest {
        @Test
        fun `POST signin 요청이 Rate Limit 대상이어야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume(any()) }
        }

        @Test
        fun `POST signup 요청이 Rate Limit 대상이어야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signup"
            every { rateLimitService.tryConsume(any()) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume(any()) }
        }

        @Test
        fun `POST refresh 요청이 Rate Limit 대상이어야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/refresh"
            every { rateLimitService.tryConsume(any()) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume(any()) }
        }
    }

    @Nested
    @DisplayName("Rate Limit 비대상 경로")
    inner class NonRateLimitedPathsTest {
        @Test
        fun `GET 요청은 Rate Limit 대상이 아니어야 한다`() {
            // Given
            request.method = "GET"
            request.requestURI = "/api/v1/auth/signin"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 0) { rateLimitService.tryConsume(any()) }
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `Rate Limit 대상이 아닌 POST 경로는 제한하지 않아야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signout"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 0) { rateLimitService.tryConsume(any()) }
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `다른 API 경로는 Rate Limit 대상이 아니어야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/nicknames"

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 0) { rateLimitService.tryConsume(any()) }
        }
    }

    @Nested
    @DisplayName("Rate Limit 허용")
    inner class RateLimitAllowedTest {
        @Test
        fun `제한 내 요청이면 필터 체인을 진행해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `제한 내 요청이면 Rate Limit 헤더를 설정해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("10")
            assertThat(response.getHeader("X-RateLimit-Remaining")).isNotNull()
        }
    }

    @Nested
    @DisplayName("Rate Limit 초과")
    inner class RateLimitExceededTest {
        @Test
        fun `제한 초과 시 429 상태 코드를 반환해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(response.status).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
        }

        @Test
        fun `제한 초과 시 에러 응답을 반환해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val responseBody = response.contentAsString
            assertThat(responseBody).contains("ERROR")
            assertThat(responseBody).contains("E429")
        }

        @Test
        fun `제한 초과 시 필터 체인을 진행하지 않아야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 0) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `제한 초과 시 JSON 응답을 반환해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            every { rateLimitService.tryConsume(any()) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(response.contentType).startsWith("application/json")
            assertThat(response.characterEncoding).isEqualTo("UTF-8")
        }
    }

    @Nested
    @DisplayName("클라이언트 IP 추출")
    inner class ClientIpExtractionTest {
        @Test
        fun `X-Forwarded-For 헤더가 있으면 해당 IP를 사용해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1")
            every { rateLimitService.tryConsume("192.168.1.1") } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume("192.168.1.1") }
        }

        @Test
        fun `X-Real-IP 헤더가 있으면 해당 IP를 사용해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            request.addHeader("X-Real-IP", "192.168.1.2")
            every { rateLimitService.tryConsume("192.168.1.2") } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume("192.168.1.2") }
        }

        @Test
        fun `헤더가 없으면 remoteAddr을 사용해야 한다`() {
            // Given
            request.method = "POST"
            request.requestURI = "/api/v1/auth/signin"
            request.remoteAddr = "127.0.0.1"
            every { rateLimitService.tryConsume("127.0.0.1") } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify { rateLimitService.tryConsume("127.0.0.1") }
        }
    }
}
