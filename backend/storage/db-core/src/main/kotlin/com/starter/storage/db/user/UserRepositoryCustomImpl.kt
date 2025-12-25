package com.starter.storage.db.user

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : UserRepositoryCustom {
    private val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }

    private val user = QUserEntity.userEntity

    override fun findByEmail(email: String): UserEntity? =
        queryFactory
            .selectFrom(user)
            .where(user.email.eq(email))
            .fetchOne()

    override fun existsByEmail(email: String): Boolean {
        val count =
            queryFactory
                .select(user.count())
                .from(user)
                .where(user.email.eq(email))
                .fetchOne() ?: 0L
        return count > 0
    }

    override fun findByEmailAndStatus(
        email: String,
        status: UserStatus,
    ): UserEntity? =
        queryFactory
            .selectFrom(user)
            .where(
                user.email.eq(email),
                user.status.eq(status),
            ).fetchOne()

    override fun findByIdWithSocialAccounts(userId: Long): UserEntity? =
        queryFactory
            .selectFrom(user)
            .leftJoin(user.socialAccounts)
            .fetchJoin()
            .where(user.id.eq(userId))
            .fetchOne()
}
