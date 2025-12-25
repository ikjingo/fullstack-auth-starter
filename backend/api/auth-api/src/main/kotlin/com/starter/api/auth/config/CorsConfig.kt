package com.starter.api.auth.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * CORS 설정
 * - 보안상 allowedOrigins를 명시적으로 설정
 * - allowedHeaders에 * 사용 금지
 */
@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfig(
    private val corsProperties: CorsProperties,
) {
    @Bean
    fun corsFilter(): CorsFilter {
        val configuration =
            CorsConfiguration().apply {
                // 명시적 origin만 허용 (와일드카드 패턴 사용 시 allowCredentials=false여야 함)
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
        return CorsFilter(source)
    }
}
