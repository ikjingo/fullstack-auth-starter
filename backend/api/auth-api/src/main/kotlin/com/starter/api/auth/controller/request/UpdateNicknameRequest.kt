package com.starter.api.auth.controller.request

import com.starter.core.api.support.ValidationConstants.NICKNAME_MAX_LENGTH
import com.starter.core.api.support.ValidationConstants.NICKNAME_MIN_LENGTH
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateNicknameRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    val nickname: String,
)
