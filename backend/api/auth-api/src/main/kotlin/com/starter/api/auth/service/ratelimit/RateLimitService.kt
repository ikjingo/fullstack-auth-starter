package com.starter.api.auth.service.ratelimit

import com.starter.api.auth.config.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * IP 기반 Rate Limiting 서비스
 *
 * 인증 관련 엔드포인트에 대한 요청 제한을 관리합니다.
 * 무차별 대입 공격 및 자격증명 스터핑 공격 방지 목적.
 */
@Service
class RateLimitService(
    private val rateLimitProperties: RateLimitProperties,
) {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    /**
     * IP 주소에 대한 요청 허용 여부 확인
     * @return true: 요청 허용, false: 요청 차단 (Rate Limit 초과)
     */
    fun tryConsume(ip: String): Boolean {
        val bucket = buckets.computeIfAbsent(ip) { createBucket() }
        return bucket.tryConsume(1)
    }

    /**
     * IP 주소에 대한 남은 토큰 수 조회
     */
    fun getAvailableTokens(ip: String): Long {
        val bucket = buckets[ip] ?: return rateLimitProperties.capacity
        return bucket.availableTokens
    }

    /**
     * 설정된 Rate Limit 용량 조회
     */
    fun getCapacity(): Long = rateLimitProperties.capacity

    /**
     * 오래된 버킷 정리 (메모리 관리)
     * 주기적으로 호출하여 사용하지 않는 IP의 버킷 제거
     */
    fun cleanup() {
        // 버킷이 가득 찬 IP는 정리 (오랫동안 요청하지 않은 것으로 간주)
        buckets.entries.removeIf { it.value.availableTokens >= rateLimitProperties.capacity }
    }

    private fun createBucket(): Bucket {
        val refill =
            Refill.greedy(
                rateLimitProperties.refillTokens,
                Duration.ofMinutes(rateLimitProperties.refillMinutes),
            )
        val limit = Bandwidth.classic(rateLimitProperties.capacity, refill)
        return Bucket
            .builder()
            .addLimit(limit)
            .build()
    }
}
