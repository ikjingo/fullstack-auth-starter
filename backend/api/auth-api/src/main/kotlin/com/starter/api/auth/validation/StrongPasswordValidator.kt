package com.starter.api.auth.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * StrongPassword 어노테이션의 검증 로직
 *
 * 비밀번호 복잡성 요구사항:
 * - 최소 1개의 대문자 (A-Z)
 * - 최소 1개의 소문자 (a-z)
 * - 최소 1개의 숫자 (0-9)
 * - 최소 1개의 특수문자
 */
class StrongPasswordValidator : ConstraintValidator<StrongPassword, String> {
    companion object {
        private val UPPERCASE_PATTERN = Regex("[A-Z]")
        private val LOWERCASE_PATTERN = Regex("[a-z]")
        private val DIGIT_PATTERN = Regex("[0-9]")
        private val SPECIAL_CHAR_PATTERN = Regex("[!@#\$%^&*()_+\\-=\\[\\]{}|;':\",./<>?`~\\\\]")
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value.isNullOrBlank()) {
            // @NotBlank 어노테이션이 별도로 처리
            return true
        }

        val violations = mutableListOf<String>()

        if (!UPPERCASE_PATTERN.containsMatchIn(value)) {
            violations.add("대문자를 1개 이상 포함해야 합니다")
        }
        if (!LOWERCASE_PATTERN.containsMatchIn(value)) {
            violations.add("소문자를 1개 이상 포함해야 합니다")
        }
        if (!DIGIT_PATTERN.containsMatchIn(value)) {
            violations.add("숫자를 1개 이상 포함해야 합니다")
        }
        if (!SPECIAL_CHAR_PATTERN.containsMatchIn(value)) {
            violations.add("특수문자를 1개 이상 포함해야 합니다")
        }

        if (violations.isNotEmpty()) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(
                    "비밀번호가 복잡성 요구사항을 충족하지 않습니다: ${violations.joinToString(", ")}",
                ).addConstraintViolation()
            return false
        }

        return true
    }
}
