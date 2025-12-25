/**
 * 비밀번호 복구 API 서비스
 *
 * 비인증 사용자를 위한 비밀번호 복구 흐름을 제공합니다.
 * - 비밀번호 찾기 (인증코드 발송)
 * - 인증코드 확인
 * - 비밀번호 재설정
 */
import { apiClient } from './client'
import type { ForgotPasswordFormSchema } from '@/schemas'
import type { MessageResponse } from './auth'

// ============================================================================
// API 엔드포인트 상수
// ============================================================================

const ENDPOINTS = {
  FORGOT_PASSWORD: '/auth/forgot-password',
  VERIFY_CODE: '/auth/verify-code',
  RESET_PASSWORD: '/auth/reset-password',
} as const

// ============================================================================
// API 서비스
// ============================================================================

export const passwordApi = {
  /**
   * 비밀번호 찾기 (이메일로 인증코드 발송)
   */
  forgotPassword: async (
    data: ForgotPasswordFormSchema
  ): Promise<MessageResponse> => {
    return apiClient.post<MessageResponse>(ENDPOINTS.FORGOT_PASSWORD, data)
  },

  /**
   * 인증코드 확인
   */
  verifyCode: async (data: {
    email: string
    code: string
  }): Promise<MessageResponse> => {
    return apiClient.post<MessageResponse>(ENDPOINTS.VERIFY_CODE, data)
  },

  /**
   * 비밀번호 재설정
   */
  resetPassword: async (data: {
    email: string
    code: string
    newPassword: string
  }): Promise<MessageResponse> => {
    return apiClient.post<MessageResponse>(ENDPOINTS.RESET_PASSWORD, data)
  },
}
