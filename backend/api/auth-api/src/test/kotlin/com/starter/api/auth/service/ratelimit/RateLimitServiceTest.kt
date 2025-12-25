package com.starter.api.auth.service.ratelimit

import com.starter.api.auth.config.RateLimitProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

@DisplayName("RateLimitService 테스트")
class RateLimitServiceTest {
    private lateinit var rateLimitProperties: RateLimitProperties
    private lateinit var rateLimitService: RateLimitService

    @BeforeEach
    fun setUp() {
        rateLimitProperties =
            RateLimitProperties(
                capacity = 5L,
                refillTokens = 5L,
                refillMinutes = 1L,
            )
        rateLimitService = RateLimitService(rateLimitProperties)
    }

    @Nested
    @DisplayName("tryConsume")
    inner class TryConsumeTest {
        @Test
        fun `첫 번째 요청은 허용되어야 한다`() {
            // Given
            val ip = "192.168.1.1"

            // When
            val result = rateLimitService.tryConsume(ip)

            // Then
            assertThat(result).isTrue()
        }

        @Test
        fun `용량 내 요청은 모두 허용되어야 한다`() {
            // Given
            val ip = "192.168.1.2"

            // When & Then
            for (i in 1..5) {
                val result = rateLimitService.tryConsume(ip)
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `용량 초과 요청은 차단되어야 한다`() {
            // Given
            val ip = "192.168.1.3"

            // 용량만큼 소비
            repeat(5) {
                rateLimitService.tryConsume(ip)
            }

            // When
            val result = rateLimitService.tryConsume(ip)

            // Then
            assertThat(result).isFalse()
        }

        @Test
        fun `다른 IP는 독립적인 버킷을 가져야 한다`() {
            // Given
            val ip1 = "192.168.1.100"
            val ip2 = "192.168.1.101"

            // IP1의 용량 모두 소비
            repeat(5) {
                rateLimitService.tryConsume(ip1)
            }

            // When - IP2로 요청
            val result = rateLimitService.tryConsume(ip2)

            // Then
            assertThat(result).isTrue()
        }
    }

    @Nested
    @DisplayName("getAvailableTokens")
    inner class GetAvailableTokensTest {
        @Test
        fun `새로운 IP는 최대 용량을 반환해야 한다`() {
            // Given
            val ip = "10.0.0.1"

            // When
            val tokens = rateLimitService.getAvailableTokens(ip)

            // Then
            assertThat(tokens).isEqualTo(5L)
        }

        @Test
        fun `요청 후 남은 토큰 수를 반환해야 한다`() {
            // Given
            val ip = "10.0.0.2"
            rateLimitService.tryConsume(ip)
            rateLimitService.tryConsume(ip)

            // When
            val tokens = rateLimitService.getAvailableTokens(ip)

            // Then
            assertThat(tokens).isEqualTo(3L)
        }

        @Test
        fun `모든 토큰 소비 후 0을 반환해야 한다`() {
            // Given
            val ip = "10.0.0.3"
            repeat(5) {
                rateLimitService.tryConsume(ip)
            }

            // When
            val tokens = rateLimitService.getAvailableTokens(ip)

            // Then
            assertThat(tokens).isEqualTo(0L)
        }
    }

    @Nested
    @DisplayName("getCapacity")
    inner class GetCapacityTest {
        @Test
        fun `설정된 용량을 반환해야 한다`() {
            // When
            val capacity = rateLimitService.getCapacity()

            // Then
            assertThat(capacity).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("cleanup")
    inner class CleanupTest {
        @Test
        fun `가득 찬 버킷을 정리해야 한다`() {
            // Given
            val ip1 = "172.16.0.1"
            val ip2 = "172.16.0.2"

            // IP1: 토큰 일부 소비
            rateLimitService.tryConsume(ip1)

            // IP2: 토큰 사용 안함 (버킷 생성만)
            rateLimitService.getAvailableTokens(ip2)

            // When
            rateLimitService.cleanup()

            // Then
            // IP1은 토큰이 부족하므로 유지됨
            assertThat(rateLimitService.getAvailableTokens(ip1)).isEqualTo(4L)
            // IP2는 가득 차 있었으므로 정리됨 (새 조회 시 최대 용량 반환)
            assertThat(rateLimitService.getAvailableTokens(ip2)).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    inner class ConcurrencyTest {
        @Test
        fun `동시 요청에서 Rate Limit이 정확히 적용되어야 한다`() {
            // Given
            val ip = "192.168.100.1"
            val successCount = AtomicInteger(0)
            val threads = mutableListOf<Thread>()

            // When - 10개 쓰레드에서 동시에 요청
            repeat(10) {
                val thread =
                    Thread {
                        if (rateLimitService.tryConsume(ip)) {
                            successCount.incrementAndGet()
                        }
                    }
                threads.add(thread)
            }

            threads.forEach { thread -> thread.start() }
            threads.forEach { thread -> thread.join() }

            // Then - 최대 용량(5)만큼만 성공해야 함
            assertThat(successCount.get()).isEqualTo(5)
        }
    }
}
