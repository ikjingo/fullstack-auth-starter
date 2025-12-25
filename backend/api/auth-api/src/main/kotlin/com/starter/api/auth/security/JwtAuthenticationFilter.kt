package com.starter.api.auth.security

import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.core.api.support.HttpConstants.AUTHORIZATION_HEADER
import com.starter.core.api.support.HttpConstants.BEARER_PREFIX
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenBlacklistService: TokenBlacklistService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null &&
            jwtTokenProvider.validateToken(token) &&
            jwtTokenProvider.isAccessToken(token) &&
            !tokenBlacklistService.isBlacklisted(token)
        ) {
            val userId = jwtTokenProvider.getUserId(token)
            val email = jwtTokenProvider.getEmail(token)
            val roles = jwtTokenProvider.getRoles(token)

            val authUser = AuthUser(userId, email, roles)
            val authentication = UsernamePasswordAuthenticationToken(authUser, null, authUser.authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length)
        }
        return null
    }
}
