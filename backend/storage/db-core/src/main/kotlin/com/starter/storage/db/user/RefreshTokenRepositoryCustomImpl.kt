package com.starter.storage.db.user

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RefreshTokenRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : RefreshTokenRepositoryCustom {
    private val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }

    private val refreshToken = QRefreshTokenEntity.refreshTokenEntity

    override fun findByToken(token: String): RefreshTokenEntity? =
        queryFactory
            .selectFrom(refreshToken)
            .where(refreshToken.token.eq(token))
            .fetchOne()

    override fun findByUserAndRevokedFalse(user: UserEntity): List<RefreshTokenEntity> =
        queryFactory
            .selectFrom(refreshToken)
            .where(
                refreshToken.user.eq(user),
                refreshToken.revoked.isFalse,
            ).fetch()

    override fun revokeAllByUser(user: UserEntity): Long =
        queryFactory
            .update(refreshToken)
            .set(refreshToken.revoked, true)
            .where(refreshToken.user.eq(user))
            .execute()

    override fun deleteExpiredTokens(): Long =
        queryFactory
            .delete(refreshToken)
            .where(refreshToken.expiresAt.lt(LocalDateTime.now()))
            .execute()

    override fun countActiveSessionsByUser(user: UserEntity): Long =
        queryFactory
            .select(refreshToken.count())
            .from(refreshToken)
            .where(
                refreshToken.user.eq(user),
                refreshToken.revoked.isFalse,
                refreshToken.expiresAt.gt(LocalDateTime.now()),
            ).fetchOne() ?: 0L

    override fun findActiveSessionsByUser(user: UserEntity): List<RefreshTokenEntity> =
        queryFactory
            .selectFrom(refreshToken)
            .where(
                refreshToken.user.eq(user),
                refreshToken.revoked.isFalse,
                refreshToken.expiresAt.gt(LocalDateTime.now()),
            ).orderBy(refreshToken.createdAt.desc())
            .fetch()

    override fun findOldestActiveSessionByUser(user: UserEntity): RefreshTokenEntity? =
        queryFactory
            .selectFrom(refreshToken)
            .where(
                refreshToken.user.eq(user),
                refreshToken.revoked.isFalse,
                refreshToken.expiresAt.gt(LocalDateTime.now()),
            ).orderBy(refreshToken.createdAt.asc())
            .fetchFirst()

    override fun findByIdAndUser(
        sessionId: Long,
        user: UserEntity,
    ): RefreshTokenEntity? =
        queryFactory
            .selectFrom(refreshToken)
            .where(
                refreshToken.id.eq(sessionId),
                refreshToken.user.eq(user),
            ).fetchOne()
}
