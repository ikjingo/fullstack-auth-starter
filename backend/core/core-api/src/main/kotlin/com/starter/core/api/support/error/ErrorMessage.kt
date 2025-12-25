package com.starter.core.api.support.error

import java.time.Instant

/**
 * API 에러 응답 메시지
 *
 * @property code 에러 코드 (ErrorCode enum 값)
 * @property message 사용자 친화적 에러 메시지
 * @property data 추가 에러 데이터 (유효성 검증 오류 등)
 * @property timestamp 에러 발생 시간 (ISO-8601 형식)
 * @property path 에러가 발생한 요청 경로
 */
data class ErrorMessage(
    val code: String,
    val message: String,
    val data: Any? = null,
    val timestamp: String = Instant.now().toString(),
    val path: String? = null,
) {
    companion object {
        fun of(
            errorType: ErrorType,
            data: Any? = null,
            messageOverride: String? = null,
            path: String? = null,
        ): ErrorMessage =
            ErrorMessage(
                code = errorType.code.name,
                message = messageOverride ?: errorType.message,
                data = data,
                timestamp = Instant.now().toString(),
                path = path,
            )
    }
}
