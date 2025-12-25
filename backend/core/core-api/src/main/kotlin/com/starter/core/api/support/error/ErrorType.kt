package com.starter.core.api.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(
    val status: HttpStatus,
    val code: ErrorCode,
    val message: String,
    val logLevel: LogLevel,
) {
    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, ErrorCode.E400, "잘못된 요청입니다.", LogLevel.WARN),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 사용 중인 이메일입니다.", LogLevel.WARN),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "인증이 필요합니다.", LogLevel.WARN),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "이메일 또는 비밀번호가 올바르지 않습니다.", LogLevel.WARN),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "유효하지 않은 토큰입니다.", LogLevel.WARN),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "접근 권한이 없습니다.", LogLevel.WARN),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "리소스를 찾을 수 없습니다.", LogLevel.WARN),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "사용자를 찾을 수 없습니다.", LogLevel.WARN),
    API_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "등록된 API 토큰을 찾을 수 없습니다.", LogLevel.WARN),

    // Password Management
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, ErrorCode.E400, "비밀번호가 일치하지 않습니다.", LogLevel.WARN),
    PASSWORD_ALREADY_SET(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 비밀번호가 설정되어 있습니다.", LogLevel.WARN),
    NO_PASSWORD_SET(HttpStatus.BAD_REQUEST, ErrorCode.E400, "비밀번호가 설정되지 않은 계정입니다. 먼저 비밀번호를 설정해주세요.", LogLevel.WARN),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "현재 비밀번호가 올바르지 않습니다.", LogLevel.WARN),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.E429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", LogLevel.WARN),

    // 423 Locked
    ACCOUNT_LOCKED(HttpStatus.LOCKED, ErrorCode.E423, "계정이 잠겼습니다. 잠시 후 다시 시도해주세요.", LogLevel.WARN),

    // 500 Internal Server Error
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "서버 오류가 발생했습니다.", LogLevel.ERROR),
}
