package com.starter.storage.db.user

/**
 * SocialAccount QueryDSL Custom Repository Interface
 */
interface SocialAccountRepositoryCustom {
    fun findByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): SocialAccountEntity?

    fun findByProviderId(providerId: String): SocialAccountEntity?

    fun findAllByUser(user: UserEntity): List<SocialAccountEntity>

    fun findByUserAndProvider(
        user: UserEntity,
        provider: AuthProvider,
    ): SocialAccountEntity?

    fun existsByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): Boolean

    fun existsByUserAndProvider(
        user: UserEntity,
        provider: AuthProvider,
    ): Boolean
}
