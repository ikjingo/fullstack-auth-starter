package com.starter.api.auth.config

import com.starter.api.auth.security.ContentSecurityPolicyFilter
import com.starter.api.auth.security.CustomAccessDeniedHandler
import com.starter.api.auth.security.CustomAuthenticationEntryPoint
import com.starter.api.auth.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.HeaderWriterFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val contentSecurityPolicyFilter: ContentSecurityPolicyFilter,
    private val corsProperties: CorsProperties,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Security Headers
            .headers { headers ->
                headers
                    // X-Frame-Options: DENY - 클릭재킹 방지
                    .frameOptions { it.deny() }
                    // X-Content-Type-Options: nosniff - MIME 스니핑 방지
                    .contentTypeOptions { }
                    // X-XSS-Protection: 0 - 현대 브라우저에서는 비활성화 권장 (CSP 사용)
                    .xssProtection { it.disable() }
                    // Referrer-Policy: strict-origin-when-cross-origin
                    .referrerPolicy { referrer ->
                        referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
                    // Permissions-Policy: 기본 권한 제한
                    .permissionsPolicy { it.policy("geolocation=(), microphone=(), camera=()") }
                // Content-Security-Policy는 ContentSecurityPolicyFilter에서 처리
                // HSTS 설정 (application-prod.yml에서 server.ssl 설정 시 자동 적용)
            }.authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/v1/auth/signup",
                        "/api/v1/auth/signin",
                        "/api/v1/auth/refresh",
                        "/actuator/**",
                        "/docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.GET,
                        "/api/v1/nicknames",
                        "/api/v1/nicknames/services/enabled",
                        "/api/v1/nicknames/rarity-thresholds",
                    ).permitAll()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.POST,
                        "/api/v1/nicknames",
                    ).permitAll()
                    .anyRequest()
                    .authenticated()
            }.exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            // CSP 헤더 필터 추가
            .addFilterAfter(contentSecurityPolicyFilter, HeaderWriterFilter::class.java)
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                // CorsProperties에서 설정값 로드
                corsProperties.allowedOrigins.forEach { origin ->
                    if (origin.contains("*")) {
                        addAllowedOriginPattern(origin)
                    } else {
                        addAllowedOrigin(origin)
                    }
                }
                allowedMethods = corsProperties.allowedMethods
                allowedHeaders = corsProperties.allowedHeaders
                exposedHeaders = corsProperties.exposedHeaders
                allowCredentials = corsProperties.allowCredentials
                maxAge = corsProperties.maxAge
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
