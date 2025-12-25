package com.starter.api.auth.controller.request

import com.starter.api.auth.validation.StrongPassword
import com.starter.core.api.support.ValidationConstants.NICKNAME_MAX_LENGTH
import com.starter.core.api.support.ValidationConstants.NICKNAME_MIN_LENGTH
import com.starter.core.api.support.ValidationConstants.PASSWORD_MAX_LENGTH
import com.starter.core.api.support.ValidationConstants.PASSWORD_MIN_LENGTH
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @field:StrongPassword
    val password: String,
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    val nickname: String,
)
