package com.starter.api.auth.security.filter

import com.starter.api.auth.security.JwtAuthenticationFilter
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.token.TokenBlacklistService
import io.mockk.every
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
import org.springframework.security.core.context.SecurityContextHolder

@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {
    private lateinit var filter: JwtAuthenticationFilter
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var tokenBlacklistService: TokenBlacklistService
    private lateinit var filterChain: FilterChain
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        tokenBlacklistService = mockk()
        filterChain = mockk(relaxed = true)
        filter = JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("인증 성공")
    inner class AuthenticationSuccessTest {
        @Test
        fun `유효한 토큰으로 인증이 성공해야 한다`() {
            // Given
            val token = "valid-access-token"
            request.addHeader("Authorization", "Bearer $token")

            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.isAccessToken(token) } returns true
            every { tokenBlacklistService.isBlacklisted(token) } returns false
            every { jwtTokenProvider.getUserId(token) } returns 1L
            every { jwtTokenProvider.getEmail(token) } returns "test@example.com"
            every { jwtTokenProvider.getRoles(token) } returns listOf("USER")

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertThat(authentication).isNotNull
            assertThat(authentication.isAuthenticated).isTrue()

            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `인증 성공 시 AuthUser가 SecurityContext에 설정되어야 한다`() {
            // Given
            val token = "valid-access-token"
            request.addHeader("Authorization", "Bearer $token")

            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.isAccessToken(token) } returns true
            every { tokenBlacklistService.isBlacklisted(token) } returns false
            every { jwtTokenProvider.getUserId(token) } returns 123L
            every { jwtTokenProvider.getEmail(token) } returns "user@example.com"
            every { jwtTokenProvider.getRoles(token) } returns listOf("USER", "ADMIN")

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authUser = SecurityContextHolder.getContext().authentication.principal
            assertThat(authUser).isNotNull
        }
    }

    @Nested
    @DisplayName("인증 실패 - 토큰 없음")
    inner class NoTokenTest {
        @Test
        fun `Authorization 헤더가 없으면 인증을 건너뛰고 필터 체인을 진행해야 한다`() {
            // Given - no Authorization header

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `Authorization 헤더가 Bearer로 시작하지 않으면 인증을 건너뛰어야 한다`() {
            // Given
            request.addHeader("Authorization", "Basic some-token")

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `빈 Bearer 토큰이면 인증을 건너뛰어야 한다`() {
            // Given
            request.addHeader("Authorization", "Bearer ")

            every { jwtTokenProvider.validateToken("") } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("인증 실패 - 유효하지 않은 토큰")
    inner class InvalidTokenTest {
        @Test
        fun `토큰 검증에 실패하면 인증을 건너뛰어야 한다`() {
            // Given
            val token = "invalid-token"
            request.addHeader("Authorization", "Bearer $token")

            every { jwtTokenProvider.validateToken(token) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        fun `Access Token이 아니면 인증을 건너뛰어야 한다`() {
            // Given
            val token = "refresh-token"
            request.addHeader("Authorization", "Bearer $token")

            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.isAccessToken(token) } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("인증 실패 - 블랙리스트 토큰")
    inner class BlacklistedTokenTest {
        @Test
        fun `블랙리스트에 있는 토큰이면 인증을 건너뛰어야 한다`() {
            // Given
            val token = "blacklisted-token"
            request.addHeader("Authorization", "Bearer $token")

            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.isAccessToken(token) } returns true
            every { tokenBlacklistService.isBlacklisted(token) } returns true

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            assertThat(SecurityContextHolder.getContext().authentication).isNull()
            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("필터 체인 진행")
    inner class FilterChainTest {
        @Test
        fun `인증 성공 여부와 관계없이 필터 체인이 항상 진행되어야 한다`() {
            // Given - no token

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }

        @Test
        fun `유효하지 않은 토큰이어도 필터 체인이 진행되어야 한다`() {
            // Given
            request.addHeader("Authorization", "Bearer invalid")
            every { jwtTokenProvider.validateToken("invalid") } returns false

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }
}
