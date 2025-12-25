package com.starter.storage.db.user

/**
 * User QueryDSL Custom Repository Interface
 */
interface UserRepositoryCustom {
    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun findByEmailAndStatus(
        email: String,
        status: UserStatus,
    ): UserEntity?
}
