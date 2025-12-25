package com.starter.api.auth.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        const val CACHE_TOKEN_BLACKLIST = "tokenBlacklist"
        const val CACHE_USERS = "users"
        const val CACHE_USERS_BY_EMAIL = "usersByEmail"
        const val CACHE_SOCIAL_ACCOUNTS = "socialAccounts"
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()

        // 기본 캐시 설정 (15분 TTL, 최대 10,000개)
        cacheManager.setCaffeine(
            Caffeine
                .newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats(),
        )

        // 캐시별 커스텀 설정
        cacheManager.registerCustomCache(
            CACHE_TOKEN_BLACKLIST,
            Caffeine
                .newBuilder()
                .maximumSize(50_000)
                .expireAfterWrite(Duration.ofMinutes(15)) // Access Token TTL과 동일
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            CACHE_USERS,
            Caffeine
                .newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            CACHE_USERS_BY_EMAIL,
            Caffeine
                .newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            CACHE_SOCIAL_ACCOUNTS,
            Caffeine
                .newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofHours(1))
                .recordStats()
                .build(),
        )

        return cacheManager
    }
}
