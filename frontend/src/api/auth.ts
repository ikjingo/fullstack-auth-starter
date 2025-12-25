/**
 * 인증 API 서비스
 *
 * 핵심 인증 기능을 제공합니다.
 * - 로그인/회원가입/로그아웃
 * - 토큰 관리
 *
 * @see profile.ts - 프로필 관리 (닉네임, 비밀번호 변경)
 * @see password.ts - 비밀번호 관리
 */
import { apiClient } from './client'
import type { LoginFormSchema, RegisterFormSchema } from '@/schemas'

// ============================================================================
// 타입 정의
// ============================================================================

/**
 * 사용자 정보 응답 타입
 */
export interface UserResponse {
  id: number
  email: string
  nickname: string
  role: string
  createdAt: string
  hasPassword: boolean
  profileImageUrl?: string | null
}

/**
 * 인증 응답 타입 (로그인, 회원가입 등)
 */
export interface AuthResponse {
  user: {
    id: string
    email: string
    nickname: string
    role: 'USER' | 'ADMIN'
    profileImageUrl?: string | null
    hasPassword: boolean
  }
  token: string
  refreshToken?: string
}

/**
 * 토큰 응답 타입
 */
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

/**
 * 메시지 응답 타입
 */
export interface MessageResponse {
  message: string
}

// ============================================================================
// API 엔드포인트 상수
// ============================================================================

const ENDPOINTS = {
  SIGNIN: '/auth/signin',
  SIGNUP: '/auth/signup',
  REFRESH: '/auth/refresh',
  ME: '/auth/me',
} as const

// ============================================================================
// API 서비스
// ============================================================================

export const authApi = {
  /**
   * 이메일/비밀번호 로그인
   */
  login: async (data: LoginFormSchema): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(ENDPOINTS.SIGNIN, data)
    if (response.refreshToken) {
      apiClient.setTokens(response.token, response.refreshToken)
    } else {
      apiClient.setToken(response.token)
    }
    return response
  },

  /**
   * 회원가입
   */
  register: async (
    data: Omit<RegisterFormSchema, 'confirmPassword'>
  ): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(ENDPOINTS.SIGNUP, data)
    if (response.refreshToken) {
      apiClient.setTokens(response.token, response.refreshToken)
    } else {
      apiClient.setToken(response.token)
    }
    return response
  },

  /**
   * 로그아웃
   */
  logout: (): void => {
    apiClient.clearToken()
  },

  /**
   * 현재 사용자 정보 조회
   */
  getMe: (): Promise<UserResponse> => {
    return apiClient.get<UserResponse>(ENDPOINTS.ME)
  },

  /**
   * 토큰 갱신
   * @deprecated tokenManager.refreshAccessToken() 사용 (자동 갱신)
   */
  refreshToken: async (): Promise<string> => {
    // TokenManager에서 자동 처리됨
    const response = await apiClient.post<TokenResponse>(ENDPOINTS.REFRESH, {})
    apiClient.setTokens(response.accessToken, response.refreshToken)
    return response.accessToken
  },
}
