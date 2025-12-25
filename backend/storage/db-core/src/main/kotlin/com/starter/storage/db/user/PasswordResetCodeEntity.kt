package com.starter.storage.db.user

import com.starter.storage.db.core.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "password_reset_codes")
class PasswordResetCodeEntity(
    @Column(nullable = false, length = 100)
    val email: String,
    @Column(nullable = false, length = 6)
    val code: String,
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    @Column(nullable = false)
    var used: Boolean = false,
    @Column(nullable = false)
    var verified: Boolean = false,
) : BaseEntity() {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun isValid(): Boolean = !used && !isExpired()

    fun markAsUsed() {
        this.used = true
    }

    fun markAsVerified() {
        this.verified = true
    }
}
