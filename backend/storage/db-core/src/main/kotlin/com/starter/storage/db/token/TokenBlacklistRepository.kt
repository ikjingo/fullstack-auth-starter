package com.starter.storage.db.token

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TokenBlacklistRepository : JpaRepository<TokenBlacklistEntity, Long> {
    /**
     * 토큰이 블랙리스트에 존재하는지 확인
     */
    fun existsByToken(token: String): Boolean

    /**
     * 만료된 토큰 삭제 (스케줄러에서 사용)
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklistEntity t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(
        @Param("now") now: LocalDateTime,
    ): Int
}
