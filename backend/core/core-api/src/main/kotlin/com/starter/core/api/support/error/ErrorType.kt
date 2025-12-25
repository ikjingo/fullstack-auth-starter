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
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "유효하지 않은 OAuth 토큰입니다.", LogLevel.WARN),
    OAUTH_ACCOUNT_NO_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "소셜 로그인 계정입니다. 소셜 로그인을 이용해주세요.", LogLevel.WARN),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "접근 권한이 없습니다.", LogLevel.WARN),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "리소스를 찾을 수 없습니다.", LogLevel.WARN),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "사용자를 찾을 수 없습니다.", LogLevel.WARN),
    API_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "등록된 API 토큰을 찾을 수 없습니다.", LogLevel.WARN),

    // Password Reset
    INVALID_RESET_CODE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "인증번호가 올바르지 않거나 만료되었습니다.", LogLevel.WARN),
    CODE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "인증번호 확인이 필요합니다.", LogLevel.WARN),

    // Password Setting
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, ErrorCode.E400, "비밀번호가 일치하지 않습니다.", LogLevel.WARN),
    PASSWORD_ALREADY_SET(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 비밀번호가 설정되어 있습니다.", LogLevel.WARN),
    NO_PASSWORD_SET(HttpStatus.BAD_REQUEST, ErrorCode.E400, "비밀번호가 설정되지 않은 계정입니다. 먼저 비밀번호를 설정해주세요.", LogLevel.WARN),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "현재 비밀번호가 올바르지 않습니다.", LogLevel.WARN),

    // Social Account Linking
    SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "해당 소셜 계정은 이미 다른 사용자와 연결되어 있습니다.", LogLevel.WARN),
    SOCIAL_PROVIDER_ALREADY_LINKED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 해당 소셜 서비스와 연동되어 있습니다.", LogLevel.WARN),
    SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "연동된 소셜 계정을 찾을 수 없습니다.", LogLevel.WARN),
    SOCIAL_ACCOUNT_NOT_LINKED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "해당 소셜 계정과 연동된 계정이 없습니다. 먼저 회원가입 후 소셜 계정을 연동해주세요.", LogLevel.WARN),
    CANNOT_UNLINK_ONLY_LOGIN_METHOD(
        HttpStatus.BAD_REQUEST,
        ErrorCode.E400,
        "마지막 로그인 방법은 해제할 수 없습니다. 먼저 비밀번호를 설정하거나 다른 소셜 계정을 연동해주세요.",
        LogLevel.WARN,
    ),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.E429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", LogLevel.WARN),

    // 423 Locked
    ACCOUNT_LOCKED(HttpStatus.LOCKED, ErrorCode.E423, "계정이 잠겼습니다. 잠시 후 다시 시도해주세요.", LogLevel.WARN),

    // Two-Factor Authentication (2FA)
    TWO_FACTOR_ALREADY_ENABLED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "2차 인증이 이미 활성화되어 있습니다.", LogLevel.WARN),
    TWO_FACTOR_NOT_ENABLED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "2차 인증이 활성화되어 있지 않습니다.", LogLevel.WARN),
    TWO_FACTOR_NOT_INITIATED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "2차 인증 설정이 시작되지 않았습니다.", LogLevel.WARN),
    INVALID_TWO_FACTOR_CODE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "유효하지 않은 인증 코드입니다.", LogLevel.WARN),
    TWO_FACTOR_REQUIRED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "2차 인증이 필요합니다.", LogLevel.WARN),

    // 500 Internal Server Error
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "서버 오류가 발생했습니다.", LogLevel.ERROR),
}
