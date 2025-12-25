/**
 * API 응답 목 데이터 팩토리
 */

/**
 * 로그인 응답 생성
 */
export function createMockLoginResponse(overrides?: {
  token?: string
  refreshToken?: string
  user?: { id: string; email: string; nickname: string; role: 'USER' | 'ADMIN' }
}) {
  return {
    token: overrides?.token ?? 'mock-access-token',
    refreshToken: overrides?.refreshToken ?? 'mock-refresh-token',
    user: overrides?.user ?? {
      id: 'user-1',
      email: 'test@example.com',
      nickname: '테스트유저',
      role: 'USER' as const,
    },
  }
}

/**
 * API 에러 응답 생성
 */
export function createMockApiErrorResponse(overrides?: {
  errorType?: string
  errorCode?: string
  message?: string
}) {
  return {
    success: false,
    error: {
      errorType: overrides?.errorType ?? 'INTERNAL_SERVER_ERROR',
      errorCode: overrides?.errorCode ?? 'E500',
      message: overrides?.message ?? '서버 오류가 발생했습니다.',
    },
  }
}
