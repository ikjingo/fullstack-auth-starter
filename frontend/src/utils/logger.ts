/**
 * 에러 로깅 유틸리티
 *
 * 개발 환경에서만 console 출력, 프로덕션에서는 에러 리포팅 서비스로 전송 가능
 */

const isDevelopment = import.meta.env.DEV

/**
 * 에러 로깅 (개발 환경에서만 console 출력)
 * 프로덕션에서는 Sentry 등의 에러 리포팅 서비스로 전송 가능
 */
export function logError(message: string, error?: unknown): void {
  if (isDevelopment) {
    console.error(message, error)
  }
  // PLANNED: Sentry 등 에러 리포팅 서비스 통합 (GitHub Issue 참조)
}

/**
 * 경고 로깅 (개발 환경에서만 console 출력)
 */
export function logWarn(message: string, data?: unknown): void {
  if (isDevelopment) {
    console.warn(message, data)
  }
}

/**
 * 디버그 로깅 (개발 환경에서만 console 출력)
 */
export function logDebug(message: string, data?: unknown): void {
  if (isDevelopment) {
    console.log(message, data)
  }
}

/**
 * API 에러 타입 정의
 */
export interface ApiErrorLike {
  message?: string
  code?: string
  status?: number
  data?: unknown
}

/**
 * API 에러에서 사용자에게 보여줄 메시지 추출
 */
export function getErrorMessage(error: unknown, defaultMessage: string): string {
  if (error instanceof Error) {
    return error.message || defaultMessage
  }

  if (typeof error === 'object' && error !== null) {
    const errorObj = error as ApiErrorLike
    return errorObj.message || defaultMessage
  }

  return defaultMessage
}

/**
 * API 에러에서 에러 코드 추출
 */
export function getErrorCode(error: unknown): string | undefined {
  if (typeof error === 'object' && error !== null) {
    const errorObj = error as ApiErrorLike
    return errorObj.code
  }
  return undefined
}

/**
 * 에러가 특정 코드인지 확인
 */
export function isErrorCode(error: unknown, code: string): boolean {
  return getErrorCode(error) === code
}

/**
 * API 에러인지 확인 (status 속성 존재 여부로 판단)
 */
export function isApiError(error: unknown): error is ApiErrorLike {
  return (
    typeof error === 'object' &&
    error !== null &&
    'status' in error &&
    typeof (error as { status: unknown }).status === 'number'
  )
}
