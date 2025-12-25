package com.starter.api.auth.controller.request

import com.starter.api.auth.validation.StrongPassword
import com.starter.core.api.support.ValidationConstants.PASSWORD_MAX_LENGTH
import com.starter.core.api.support.ValidationConstants.PASSWORD_MIN_LENGTH
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,
    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @field:StrongPassword
    val newPassword: String,
    @field:NotBlank(message = "비밀번호 확인은 필수입니다")
    val confirmPassword: String,
)
