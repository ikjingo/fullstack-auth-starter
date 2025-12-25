package com.starter.api.auth.controller.response

import com.starter.storage.db.user.UserEntity

data class AuthResponse(
    val user: AuthUserResponse,
    val token: String,
    val refreshToken: String? = null,
) {
    companion object {
        fun from(
            user: UserEntity,
            token: String,
            refreshToken: String? = null,
        ): AuthResponse =
            AuthResponse(
                user = AuthUserResponse.from(user),
                token = token,
                refreshToken = refreshToken,
            )
    }
}

data class AuthUserResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?,
    val hasPassword: Boolean,
) {
    companion object {
        fun from(user: UserEntity): AuthUserResponse =
            AuthUserResponse(
                id = user.id.toString(),
                email = user.email,
                nickname = user.nickname,
                role = user.role.name,
                profileImageUrl = user.profileImageUrl,
                hasPassword = user.password != null,
            )
    }
}
