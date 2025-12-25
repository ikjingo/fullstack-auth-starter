package com.starter.core.api.support

/**
 * 검증에 사용되는 공통 상수 정의
 * Request DTO의 @Size 어노테이션에서 일관된 검증값을 사용하기 위함
 */
object ValidationConstants {
    // Password
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 100

    // Nickname
    const val NICKNAME_MIN_LENGTH = 2
    const val NICKNAME_MAX_LENGTH = 50

    // Email
    const val EMAIL_MAX_LENGTH = 100
}
