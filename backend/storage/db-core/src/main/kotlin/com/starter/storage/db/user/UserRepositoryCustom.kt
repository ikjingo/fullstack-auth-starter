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

    /**
     * 사용자 ID로 조회 (소셜 계정 함께 로드 - Fetch Join)
     * N+1 문제 방지를 위해 소셜 계정도 함께 조회해야 할 때 사용
     */
    fun findByIdWithSocialAccounts(userId: Long): UserEntity?
}
