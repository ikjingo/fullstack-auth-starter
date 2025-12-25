/**
 * 프로필 관리 API 서비스
 *
 * 인증된 사용자의 프로필 관리 기능을 제공합니다.
 * - 닉네임 수정
 * - 비밀번호 설정/변경
 */
import { apiClient } from './client'
import type {
  UpdateNicknameFormSchema,
  ChangePasswordFormSchema,
} from '@/schemas'
import type { AuthResponse, MessageResponse } from './auth'

// ============================================================================
// API 엔드포인트 상수
// ============================================================================

const ENDPOINTS = {
  // 프로필
  UPDATE_NICKNAME: '/auth/update-nickname',

  // 비밀번호 관리 (인증된 사용자)
  SET_PASSWORD: '/auth/set-password',
  CHANGE_PASSWORD: '/auth/change-password',
} as const

// ============================================================================
// API 서비스
// ============================================================================

export const profileApi = {
  // --------------------------------------------------------------------------
  // 프로필 수정
  // --------------------------------------------------------------------------

  /**
   * 닉네임 수정
   */
  updateNickname: async (
    data: UpdateNicknameFormSchema
  ): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(
      ENDPOINTS.UPDATE_NICKNAME,
      data
    )
    apiClient.setToken(response.token)
    return response
  },

  // --------------------------------------------------------------------------
  // 비밀번호 관리 (인증된 사용자)
  // --------------------------------------------------------------------------

  /**
   * 비밀번호 설정
   */
  setPassword: async (data: {
    password: string
    confirmPassword: string
  }): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(
      ENDPOINTS.SET_PASSWORD,
      data
    )
    apiClient.setToken(response.token)
    return response
  },

  /**
   * 비밀번호 변경
   */
  changePassword: async (
    data: ChangePasswordFormSchema
  ): Promise<MessageResponse> => {
    return apiClient.post<MessageResponse>(ENDPOINTS.CHANGE_PASSWORD, data)
  },
}
