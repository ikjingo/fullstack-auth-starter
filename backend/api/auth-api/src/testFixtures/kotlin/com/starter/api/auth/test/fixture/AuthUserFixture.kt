package com.starter.api.auth.test.fixture

import com.starter.api.auth.security.AuthUser

/**
 * 테스트용 AuthUser 픽스처
 */
object AuthUserFixture {
    /**
     * 일반 사용자 생성
     */
    fun create(
        userId: Long = 1L,
        email: String = "test@example.com",
        roles: List<String> = listOf("USER"),
    ): AuthUser =
        AuthUser(
            userId = userId,
            email = email,
            roles = roles,
        )

    /**
     * 관리자 사용자 생성
     */
    fun admin(
        userId: Long = 1L,
        email: String = "admin@example.com",
    ): AuthUser =
        AuthUser(
            userId = userId,
            email = email,
            roles = listOf("ADMIN"),
        )

    /**
     * 커스텀 사용자 생성
     */
    fun custom(
        userId: Long,
        email: String,
        roles: List<String>,
    ): AuthUser =
        AuthUser(
            userId = userId,
            email = email,
            roles = roles,
        )
}
