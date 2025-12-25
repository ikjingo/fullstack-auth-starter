package com.starter.storage.db.user

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class PasswordResetCodeRepositoryCustomImpl(
    private val entityManager: EntityManager,
) : PasswordResetCodeRepositoryCustom {
    private val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }

    private val passwordResetCode = QPasswordResetCodeEntity.passwordResetCodeEntity

    override fun findByEmailAndCodeAndUsedFalse(
        email: String,
        code: String,
    ): PasswordResetCodeEntity? =
        queryFactory
            .selectFrom(passwordResetCode)
            .where(
                passwordResetCode.email.eq(email),
                passwordResetCode.code.eq(code),
                passwordResetCode.used.isFalse,
            ).fetchOne()

    override fun findByEmailAndCodeAndVerifiedTrueAndUsedFalse(
        email: String,
        code: String,
    ): PasswordResetCodeEntity? =
        queryFactory
            .selectFrom(passwordResetCode)
            .where(
                passwordResetCode.email.eq(email),
                passwordResetCode.code.eq(code),
                passwordResetCode.verified.isTrue,
                passwordResetCode.used.isFalse,
            ).fetchOne()

    override fun invalidateAllByEmail(email: String): Long =
        queryFactory
            .update(passwordResetCode)
            .set(passwordResetCode.used, true)
            .where(
                passwordResetCode.email.eq(email),
                passwordResetCode.used.isFalse,
            ).execute()
}
