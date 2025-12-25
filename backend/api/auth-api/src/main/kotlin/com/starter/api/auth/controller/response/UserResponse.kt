package com.starter.api.auth.controller.response

import com.starter.storage.db.user.UserEntity
import java.time.LocalDateTime

data class UserResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val role: String,
    val createdAt: LocalDateTime,
    val hasPassword: Boolean,
    val profileImageUrl: String?,
) {
    companion object {
        fun from(user: UserEntity): UserResponse =
            UserResponse(
                id = user.id.toString(),
                email = user.email,
                nickname = user.nickname,
                role = user.role.name,
                createdAt = user.createdAt,
                hasPassword = user.password != null,
                profileImageUrl = user.profileImageUrl,
            )
    }
}
