package com.starter.api.auth.test.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.security.AuthUser
import com.starter.api.auth.security.jwt.JwtProperties
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.ratelimit.RateLimitService
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.api.auth.test.fixture.AuthUserFixture
import com.starter.core.api.controller.ApiControllerAdvice
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc

/**
 * 컨트롤러 테스트를 위한 기본 지원 클래스
 *
 * @WebMvcTest와 함께 사용하며, JWT Mock과 SecurityContext 설정을 제공합니다.
 * RestDocs가 필요 없는 일반 컨트롤러 테스트에 사용합니다.
 */
@Import(ApiControllerAdvice::class)
@AutoConfigureMockMvc(addFilters = false)
abstract class ControllerTestSupport {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockkBean
    protected lateinit var jwtTokenProvider: JwtTokenProvider

    @MockkBean
    protected lateinit var jwtProperties: JwtProperties

    @MockkBean
    protected lateinit var tokenBlacklistService: TokenBlacklistService

    @MockkBean
    protected lateinit var rateLimitService: RateLimitService

    protected val testUserId = 1L
    protected val authUser: AuthUser = AuthUserFixture.create(userId = testUserId)

    @BeforeEach
    fun setUpSecurityContext() {
        val authentication = UsernamePasswordAuthenticationToken(authUser, null, authUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }
}
