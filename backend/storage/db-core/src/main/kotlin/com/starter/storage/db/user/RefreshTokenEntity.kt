package com.starter.storage.db.user

import com.starter.storage.db.core.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,
    @Column(nullable = false, unique = true, length = 500)
    val token: String,
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    @Column(nullable = false)
    var revoked: Boolean = false,
    // 세션 정보 (디바이스/클라이언트 식별)
    @Column(nullable = true, length = 500)
    val userAgent: String? = null,
    @Column(nullable = true, length = 50)
    val ipAddress: String? = null,
    @Column(nullable = true, length = 100)
    val deviceInfo: String? = null,
) : BaseEntity() {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun isValid(): Boolean = !revoked && !isExpired()

    fun revoke() {
        this.revoked = true
    }
}
