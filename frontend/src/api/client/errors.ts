/**
 * API 에러 처리 모듈
 *
 * 백엔드 ErrorType과 동기화된 에러 코드 정의 및 에러 처리 유틸리티 제공
 * @see backend/core/core-api/.../error/ErrorType.kt
 * @see backend/core/core-api/.../error/ErrorCode.kt
 */

// ============================================================================
// HTTP 상태 기반 에러 코드
// ============================================================================

/**
 * HTTP 상태 기반 에러 코드 (백엔드 ErrorCode enum과 동기화)
 * @see backend/core/core-api/.../error/ErrorCode.kt
 */
export const ErrorCode = {
  E400: 'E400', // Bad Request
  E401: 'E401', // Unauthorized
  E403: 'E403', // Forbidden
  E404: 'E404', // Not Found
  E500: 'E500', // Internal Server Error
} as const

export type ErrorCodeType = (typeof ErrorCode)[keyof typeof ErrorCode]

// ============================================================================
// 도메인별 에러 타입
// ============================================================================

/**
 * 공통 에러 (모든 도메인에서 발생 가능)
 */
export const CommonErrorType = {
  INVALID_REQUEST: 'INVALID_REQUEST',
  NOT_FOUND: 'NOT_FOUND',
  FORBIDDEN: 'FORBIDDEN',
  DEFAULT_ERROR: 'DEFAULT_ERROR',
} as const

/**
 * 인증 관련 에러
 */
export const AuthErrorType = {
  UNAUTHORIZED: 'UNAUTHORIZED',
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  INVALID_TOKEN: 'INVALID_TOKEN',
  DUPLICATE_EMAIL: 'DUPLICATE_EMAIL',
  USER_NOT_FOUND: 'USER_NOT_FOUND',
} as const

/**
 * 비밀번호 관련 에러
 */
export const PasswordErrorType = {
  PASSWORD_MISMATCH: 'PASSWORD_MISMATCH',
  PASSWORD_ALREADY_SET: 'PASSWORD_ALREADY_SET',
  NO_PASSWORD_SET: 'NO_PASSWORD_SET',
  INVALID_CURRENT_PASSWORD: 'INVALID_CURRENT_PASSWORD',
} as const

/**
 * API 토큰 관련 에러
 */
export const ApiTokenErrorType = {
  API_TOKEN_NOT_FOUND: 'API_TOKEN_NOT_FOUND',
} as const

/**
 * 속도 제한 관련 에러
 */
export const RateLimitErrorType = {
  TOO_MANY_REQUESTS: 'TOO_MANY_REQUESTS',
  ACCOUNT_LOCKED: 'ACCOUNT_LOCKED',
} as const

/**
 * 프론트엔드 전용 에러 (네트워크, 클라이언트 에러)
 */
export const ClientErrorType = {
  NETWORK_ERROR: 'NETWORK_ERROR',
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
} as const

/**
 * 통합 에러 타입 (백엔드 ErrorType과 동기화)
 * @see backend/core/core-api/.../error/ErrorType.kt
 */
export const ErrorType = {
  ...CommonErrorType,
  ...AuthErrorType,
  ...PasswordErrorType,
  ...ApiTokenErrorType,
  ...RateLimitErrorType,
  ...ClientErrorType,
} as const

export type ErrorTypeValue = (typeof ErrorType)[keyof typeof ErrorType]

// 도메인별 타입도 export
export type CommonErrorTypeValue =
  (typeof CommonErrorType)[keyof typeof CommonErrorType]
export type AuthErrorTypeValue =
  (typeof AuthErrorType)[keyof typeof AuthErrorType]
export type PasswordErrorTypeValue =
  (typeof PasswordErrorType)[keyof typeof PasswordErrorType]
export type ApiTokenErrorTypeValue =
  (typeof ApiTokenErrorType)[keyof typeof ApiTokenErrorType]
export type RateLimitErrorTypeValue =
  (typeof RateLimitErrorType)[keyof typeof RateLimitErrorType]
export type ClientErrorTypeValue =
  (typeof ClientErrorType)[keyof typeof ClientErrorType]

/**
 * API 에러 클래스
 *
 * 백엔드 에러 응답을 표준화된 형태로 관리
 */
export class ApiError extends Error {
  readonly code: ErrorCodeType | string
  readonly status: number
  readonly data?: unknown
  readonly errorType?: ErrorTypeValue

  constructor(params: {
    message: string
    code?: string
    status: number
    data?: unknown
  }) {
    super(params.message)
    this.name = 'ApiError'
    this.code = params.code ?? ErrorCode.E500
    this.status = params.status
    this.data = params.data
    this.errorType = this.inferErrorType(params.code, params.status)

    // Error 클래스 상속 시 prototype chain 복원
    Object.setPrototypeOf(this, ApiError.prototype)
  }

  /**
   * 에러 코드와 상태로부터 상세 에러 타입 추론
   */
  private inferErrorType(
    code?: string,
    status?: number
  ): ErrorTypeValue | undefined {
    // 백엔드에서 전달된 코드가 ErrorType에 있으면 사용
    if (code && Object.values(ErrorType).includes(code as ErrorTypeValue)) {
      return code as ErrorTypeValue
    }

    // HTTP 상태 코드 기반 기본 타입 반환
    switch (status) {
      case 400:
        return ErrorType.INVALID_REQUEST
      case 401:
        return ErrorType.UNAUTHORIZED
      case 403:
        return ErrorType.FORBIDDEN
      case 404:
        return ErrorType.NOT_FOUND
      case 500:
        return ErrorType.DEFAULT_ERROR
      default:
        return ErrorType.UNKNOWN_ERROR
    }
  }

  /**
   * 인증 에러인지 확인
   */
  isAuthError(): boolean {
    return this.status === 401 || this.code === ErrorCode.E401
  }

  /**
   * 권한 에러인지 확인
   */
  isForbiddenError(): boolean {
    return this.status === 403 || this.code === ErrorCode.E403
  }

  /**
   * 리소스 없음 에러인지 확인
   */
  isNotFoundError(): boolean {
    return this.status === 404 || this.code === ErrorCode.E404
  }

  /**
   * 유효성 검증 에러인지 확인
   */
  isValidationError(): boolean {
    return this.status === 400 || this.code === ErrorCode.E400
  }

  /**
   * 서버 에러인지 확인
   */
  isServerError(): boolean {
    return this.status >= 500 || this.code === ErrorCode.E500
  }

  /**
   * JSON 직렬화 지원
   */
  toJSON(): {
    name: string
    message: string
    code: string
    status: number
    data?: unknown
  } {
    return {
      name: this.name,
      message: this.message,
      code: this.code,
      status: this.status,
      data: this.data,
    }
  }
}

/**
 * 타입 가드: ApiError 인스턴스 확인
 */
export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

/**
 * 에러 메시지 추출 유틸리티
 *
 * 다양한 에러 타입에서 사용자 친화적인 메시지를 추출
 */
export function getErrorMessage(
  error: unknown,
  defaultMessage = '오류가 발생했습니다.'
): string {
  if (isApiError(error)) {
    return error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  if (typeof error === 'string') {
    return error
  }

  if (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message: unknown }).message === 'string'
  ) {
    return (error as { message: string }).message
  }

  return defaultMessage
}

/**
 * 네트워크 에러 생성 유틸리티
 */
export function createNetworkError(originalError?: Error): ApiError {
  return new ApiError({
    message:
      originalError?.message ?? '네트워크 연결을 확인해주세요.',
    code: ErrorType.NETWORK_ERROR,
    status: 0,
  })
}

/**
 * 알 수 없는 에러를 ApiError로 변환
 */
export function toApiError(error: unknown): ApiError {
  if (isApiError(error)) {
    return error
  }

  if (error instanceof Error) {
    // TypeError는 보통 네트워크 에러
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      return createNetworkError(error)
    }

    return new ApiError({
      message: error.message,
      code: ErrorType.UNKNOWN_ERROR,
      status: 0,
    })
  }

  return new ApiError({
    message: getErrorMessage(error),
    code: ErrorType.UNKNOWN_ERROR,
    status: 0,
  })
}

/**
 * 특정 에러 타입인지 확인
 */
export function hasErrorType(
  error: unknown,
  errorType: ErrorTypeValue
): boolean {
  if (!isApiError(error)) {
    return false
  }
  return error.errorType === errorType || error.code === errorType
}

/**
 * 에러 코드 확인
 */
export function hasErrorCode(
  error: unknown,
  errorCode: ErrorCodeType
): boolean {
  if (!isApiError(error)) {
    return false
  }
  return error.code === errorCode
}
