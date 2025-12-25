package com.starter.api.auth.service.token

import com.starter.api.auth.config.CacheConfig
import com.starter.storage.db.token.TokenBlacklistEntity
import com.starter.storage.db.token.TokenBlacklistRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 토큰 블랙리스트 관리 서비스
 * - 로그아웃 시 토큰을 블랙리스트에 추가
 * - 인증 시 블랙리스트 확인
 * - 만료된 토큰 정기 삭제
 */
@Service
@Transactional(readOnly = true)
class TokenBlacklistService(
    private val tokenBlacklistRepository: TokenBlacklistRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 토큰을 블랙리스트에 추가
     * 캐시에도 즉시 추가하여 DB 조회 없이 블랙리스트 확인 가능
     */
    @Transactional
    @CachePut(cacheNames = [CacheConfig.CACHE_TOKEN_BLACKLIST], key = "#token")
    fun blacklistToken(
        token: String,
        expiresAt: LocalDateTime,
    ): Boolean {
        // 이미 블랙리스트에 있는지 확인
        if (tokenBlacklistRepository.existsByToken(token)) {
            return true
        }

        val blacklistEntity =
            TokenBlacklistEntity(
                token = token,
                expiresAt = expiresAt,
            )
        tokenBlacklistRepository.save(blacklistEntity)
        log.debug("Token added to blacklist, expires at: {}", expiresAt)
        return true
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 캐시에서 먼저 확인하여 DB 조회 최소화
     */
    @Cacheable(cacheNames = [CacheConfig.CACHE_TOKEN_BLACKLIST], key = "#token")
    fun isBlacklisted(token: String): Boolean = tokenBlacklistRepository.existsByToken(token)

    /**
     * 만료된 토큰 정기 삭제 (매시간 실행)
     */
    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    @Transactional
    fun cleanupExpiredTokens() {
        val deletedCount = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now())
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired tokens from blacklist", deletedCount)
        }
    }
}
