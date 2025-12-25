package com.starter.storage.db.user

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class SocialAccountRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : SocialAccountRepositoryCustom {
    private val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }

    private val socialAccount = QSocialAccountEntity.socialAccountEntity

    override fun findByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): SocialAccountEntity? =
        queryFactory
            .selectFrom(socialAccount)
            .where(
                socialAccount.provider.eq(provider),
                socialAccount.providerId.eq(providerId),
            ).fetchOne()

    override fun findByProviderId(providerId: String): SocialAccountEntity? =
        queryFactory
            .selectFrom(socialAccount)
            .where(socialAccount.providerId.eq(providerId))
            .fetchOne()

    override fun findAllByUser(user: UserEntity): List<SocialAccountEntity> =
        queryFactory
            .selectFrom(socialAccount)
            .where(socialAccount.user.eq(user))
            .fetch()

    override fun findByUserAndProvider(
        user: UserEntity,
        provider: AuthProvider,
    ): SocialAccountEntity? =
        queryFactory
            .selectFrom(socialAccount)
            .where(
                socialAccount.user.eq(user),
                socialAccount.provider.eq(provider),
            ).fetchOne()

    override fun existsByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): Boolean {
        val count =
            queryFactory
                .select(socialAccount.count())
                .from(socialAccount)
                .where(
                    socialAccount.provider.eq(provider),
                    socialAccount.providerId.eq(providerId),
                ).fetchOne() ?: 0L
        return count > 0
    }

    override fun existsByUserAndProvider(
        user: UserEntity,
        provider: AuthProvider,
    ): Boolean {
        val count =
            queryFactory
                .select(socialAccount.count())
                .from(socialAccount)
                .where(
                    socialAccount.user.eq(user),
                    socialAccount.provider.eq(provider),
                ).fetchOne() ?: 0L
        return count > 0
    }
}
