package com.starter.storage.db.user

/**
 * RefreshToken QueryDSL Custom Repository Interface
 */
interface RefreshTokenRepositoryCustom {
    fun findByToken(token: String): RefreshTokenEntity?

    fun findByUserAndRevokedFalse(user: UserEntity): List<RefreshTokenEntity>

    fun revokeAllByUser(user: UserEntity): Long

    fun deleteExpiredTokens(): Long

    /**
     * 사용자의 활성 세션 수 조회
     */
    fun countActiveSessionsByUser(user: UserEntity): Long

    /**
     * 사용자의 활성 세션 목록 조회 (생성일 기준 정렬)
     */
    fun findActiveSessionsByUser(user: UserEntity): List<RefreshTokenEntity>

    /**
     * 가장 오래된 활성 세션 조회
     */
    fun findOldestActiveSessionByUser(user: UserEntity): RefreshTokenEntity?

    /**
     * 특정 세션 ID로 조회
     */
    fun findByIdAndUser(
        sessionId: Long,
        user: UserEntity,
    ): RefreshTokenEntity?
}
