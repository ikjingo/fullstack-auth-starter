package com.starter.api.auth.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.cache.caffeine.CaffeineCacheManager

@DisplayName("CacheConfig")
class CacheConfigTest {
    private lateinit var cacheConfig: CacheConfig

    @BeforeEach
    fun setUp() {
        cacheConfig = CacheConfig()
    }

    @Nested
    @DisplayName("cacheManager 메서드")
    inner class CacheManagerTest {
        @Test
        fun `CaffeineCacheManager를 반환해야 한다`() {
            // When
            val cacheManager = cacheConfig.cacheManager()

            // Then
            assertThat(cacheManager).isInstanceOf(CaffeineCacheManager::class.java)
        }

        @Test
        fun `tokenBlacklist 캐시가 등록되어야 한다`() {
            // When
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_TOKEN_BLACKLIST)

            // Then
            assertThat(cache).isNotNull
            assertThat(cache?.name).isEqualTo(CacheConfig.CACHE_TOKEN_BLACKLIST)
        }

        @Test
        fun `users 캐시가 등록되어야 한다`() {
            // When
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_USERS)

            // Then
            assertThat(cache).isNotNull
            assertThat(cache?.name).isEqualTo(CacheConfig.CACHE_USERS)
        }

        @Test
        fun `usersByEmail 캐시가 등록되어야 한다`() {
            // When
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_USERS_BY_EMAIL)

            // Then
            assertThat(cache).isNotNull
            assertThat(cache?.name).isEqualTo(CacheConfig.CACHE_USERS_BY_EMAIL)
        }

        @Test
        fun `모든 필수 캐시가 등록되어야 한다`() {
            // Given
            val requiredCaches =
                listOf(
                    CacheConfig.CACHE_TOKEN_BLACKLIST,
                    CacheConfig.CACHE_USERS,
                    CacheConfig.CACHE_USERS_BY_EMAIL,
                )

            // When
            val cacheManager = cacheConfig.cacheManager()

            // Then
            requiredCaches.forEach { cacheName ->
                val cache = cacheManager.getCache(cacheName)
                assertThat(cache)
                    .withFailMessage("캐시 '$cacheName'가 등록되지 않았습니다")
                    .isNotNull
            }
        }

        @Test
        fun `캐시에 값을 저장하고 조회할 수 있어야 한다`() {
            // Given
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_USERS)!!
            val key = "user:1"
            val value = "test-user-data"

            // When
            cache.put(key, value)
            val result = cache.get(key, String::class.java)

            // Then
            assertThat(result).isEqualTo(value)
        }

        @Test
        fun `캐시에서 값을 제거할 수 있어야 한다`() {
            // Given
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_USERS)!!
            val key = "user:1"
            val value = "test-user-data"
            cache.put(key, value)

            // When
            cache.evict(key)
            val result = cache.get(key, String::class.java)

            // Then
            assertThat(result).isNull()
        }

        @Test
        fun `캐시를 초기화할 수 있어야 한다`() {
            // Given
            val cacheManager = cacheConfig.cacheManager()
            val cache = cacheManager.getCache(CacheConfig.CACHE_USERS)!!
            cache.put("key1", "value1")
            cache.put("key2", "value2")

            // When
            cache.clear()

            // Then
            assertThat(cache.get("key1", String::class.java)).isNull()
            assertThat(cache.get("key2", String::class.java)).isNull()
        }
    }

    @Nested
    @DisplayName("캐시 상수")
    inner class CacheConstantsTest {
        @Test
        fun `CACHE_TOKEN_BLACKLIST 상수가 올바른 값을 가져야 한다`() {
            assertThat(CacheConfig.CACHE_TOKEN_BLACKLIST).isEqualTo("tokenBlacklist")
        }

        @Test
        fun `CACHE_USERS 상수가 올바른 값을 가져야 한다`() {
            assertThat(CacheConfig.CACHE_USERS).isEqualTo("users")
        }

        @Test
        fun `CACHE_USERS_BY_EMAIL 상수가 올바른 값을 가져야 한다`() {
            assertThat(CacheConfig.CACHE_USERS_BY_EMAIL).isEqualTo("usersByEmail")
        }
    }
}
