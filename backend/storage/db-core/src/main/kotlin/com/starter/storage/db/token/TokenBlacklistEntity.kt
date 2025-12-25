package com.starter.storage.db.token

import com.starter.storage.db.core.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 블랙리스트에 등록된 토큰 엔티티
 * 로그아웃 시 토큰을 블랙리스트에 추가하여 재사용 방지
 */
@Entity
@Table(
    name = "token_blacklist",
    indexes = [
        Index(name = "idx_token_blacklist_token", columnList = "token"),
        Index(name = "idx_token_blacklist_expires_at", columnList = "expiresAt"),
    ],
)
class TokenBlacklistEntity(
    @Column(nullable = false, unique = true, length = 512)
    val token: String,
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity() {
    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
