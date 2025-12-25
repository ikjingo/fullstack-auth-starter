package com.starter.storage.db.user

import com.starter.storage.db.core.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_id"]),
    ],
)
class SocialAccountEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: AuthProvider,
    @Column(name = "provider_id", nullable = false, length = 100)
    val providerId: String,
    @Column(nullable = true, length = 500)
    var profileImageUrl: String? = null,
) : BaseEntity()
