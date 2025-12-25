package com.starter.api.auth.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 강력한 비밀번호 검증 어노테이션
 *
 * 비밀번호는 다음 조건을 모두 만족해야 합니다:
 * - 최소 8자 이상
 * - 최소 1개의 대문자 포함
 * - 최소 1개의 소문자 포함
 * - 최소 1개의 숫자 포함
 * - 최소 1개의 특수문자 포함 (!@#$%^&*()_+-=[]{}|;':\",./<>?)
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StrongPasswordValidator::class])
@MustBeDocumented
annotation class StrongPassword(
    val message: String = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
