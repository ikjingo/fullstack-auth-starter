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
            linkedProviders: List<String> = emptyList(),
            refreshToken: String? = null,
        ): AuthResponse =
            AuthResponse(
                user = AuthUserResponse.from(user, linkedProviders),
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
    val linkedSocialAccounts: List<String>,
) {
    companion object {
        fun from(
            user: UserEntity,
            linkedProviders: List<String> = emptyList(),
        ): AuthUserResponse =
            AuthUserResponse(
                id = user.id.toString(),
                email = user.email,
                nickname = user.nickname,
                role = user.role.name,
                profileImageUrl = user.profileImageUrl,
                hasPassword = user.password != null,
                linkedSocialAccounts = linkedProviders,
            )
    }
}
