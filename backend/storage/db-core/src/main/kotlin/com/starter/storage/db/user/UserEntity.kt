package com.starter.storage.db.user

import com.starter.storage.db.core.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true, length = 100)
    val email: String,
    @Column(nullable = true)
    var password: String? = null,
    @Column(nullable = false, length = 50)
    var nickname: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = UserRole.USER,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE,
    @Column(nullable = true, length = 500)
    var profileImageUrl: String? = null,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val socialAccounts: MutableList<SocialAccountEntity> = mutableListOf(),
    // 계정 잠금 관련 필드
    @Column(nullable = false)
    var failedLoginAttempts: Int = 0,
    @Column(nullable = true)
    var lockoutUntil: LocalDateTime? = null,
    // 2FA (Two-Factor Authentication) 관련 필드
    @Column(nullable = false)
    var twoFactorEnabled: Boolean = false,
    @Column(nullable = true, length = 64)
    var twoFactorSecret: String? = null,
) : BaseEntity() {
    fun hasSocialAccount(provider: AuthProvider): Boolean = socialAccounts.any { it.provider == provider }

    fun addSocialAccount(socialAccount: SocialAccountEntity) {
        socialAccounts.add(socialAccount)
    }

    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }

    fun updateNickname(newNickname: String) {
        this.nickname = newNickname
    }

    fun deactivate() {
        this.status = UserStatus.INACTIVE
    }

    fun suspend() {
        this.status = UserStatus.SUSPENDED
    }

    fun activate() {
        this.status = UserStatus.ACTIVE
    }
}
