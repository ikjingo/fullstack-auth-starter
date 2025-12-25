package com.starter.storage.db.user

/**
 * PasswordResetCode QueryDSL Custom Repository Interface
 */
interface PasswordResetCodeRepositoryCustom {
    fun findByEmailAndCodeAndUsedFalse(
        email: String,
        code: String,
    ): PasswordResetCodeEntity?

    fun findByEmailAndCodeAndVerifiedTrueAndUsedFalse(
        email: String,
        code: String,
    ): PasswordResetCodeEntity?

    fun invalidateAllByEmail(email: String): Long
}
