/**
 * 토큰 관리자 (하이브리드 저장)
 *
 * - Access Token: 메모리 저장 (XSS 보호)
 * - Refresh Token: localStorage 저장 (새로고침 후에도 유지)
 * - 자동 토큰 갱신 지원
 * - 동시 갱신 요청 방지
 */

import type { TokenResponse } from '../auth'

/**
 * API 기본 URL
 */
const API_BASE_URL = '/api/v1'

/**
 * localStorage 키
 */
const REFRESH_TOKEN_KEY = 'zenless-refresh-token'

/**
 * 토큰 관리자 클래스
 */
class TokenManager {
  private accessToken: string | null = null
  private refreshPromise: Promise<boolean> | null = null
  private initialized: boolean = false

  /**
   * 토큰 설정
   * - accessToken은 메모리에 저장
   * - refreshToken은 localStorage에 저장
   * - 토큰이 설정되면 초기화 완료로 표시
   */
  setTokens(accessToken: string, refreshToken?: string): void {
    this.accessToken = accessToken
    if (refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    }
    // 토큰이 명시적으로 설정되면 초기화 완료 (로그인 직후 불필요한 refresh 방지)
    this.initialized = true
  }

  /**
   * Access Token 조회
   */
  getAccessToken(): string | null {
    return this.accessToken
  }

  /**
   * Refresh Token 조회 (localStorage에서)
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  }

  /**
   * 토큰 삭제
   */
  clearTokens(): void {
    this.accessToken = null
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  }

  /**
   * 토큰이 있는지 확인
   */
  hasToken(): boolean {
    return this.accessToken !== null
  }

  /**
   * Refresh Token이 저장되어 있는지 확인
   */
  hasRefreshToken(): boolean {
    return localStorage.getItem(REFRESH_TOKEN_KEY) !== null
  }

  /**
   * 초기화 여부 확인
   */
  isInitialized(): boolean {
    return this.initialized
  }

  /**
   * localStorage에서 토큰 복원 및 갱신
   * 앱 시작 시 호출하여 로그인 상태 복원
   *
   * @returns 복원 성공 여부
   */
  async initializeFromStorage(): Promise<boolean> {
    if (this.initialized) {
      return this.hasToken()
    }

    const refreshToken = this.getRefreshToken()
    if (!refreshToken) {
      this.initialized = true
      return false
    }

    // Refresh Token으로 Access Token 갱신 시도
    const success = await this.refreshAccessToken()
    this.initialized = true
    return success
  }

  /**
   * Access Token 갱신
   *
   * @returns 갱신 성공 여부
   */
  async refreshAccessToken(): Promise<boolean> {
    const refreshToken = this.getRefreshToken()
    if (!refreshToken) {
      return false
    }

    // 이미 갱신 중이면 기존 Promise 반환 (중복 갱신 방지)
    if (this.refreshPromise) {
      return this.refreshPromise
    }

    this.refreshPromise = this.doRefresh(refreshToken)
    const result = await this.refreshPromise
    this.refreshPromise = null
    return result
  }

  /**
   * 실제 토큰 갱신 로직
   */
  private async doRefresh(refreshToken: string): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      })

      if (!response.ok) {
        this.clearTokens()
        return false
      }

      const json = await response.json()
      // 백엔드 ApiResponse 형식 처리
      const data: TokenResponse = 'data' in json ? json.data : json

      this.setTokens(data.accessToken, data.refreshToken)
      return true
    } catch {
      this.clearTokens()
      return false
    }
  }

  /**
   * 인증 헤더 생성
   */
  getAuthHeader(): HeadersInit {
    const token = this.getAccessToken()
    return token ? { Authorization: `Bearer ${token}` } : {}
  }
}

// 싱글톤 인스턴스
export const tokenManager = new TokenManager()

// ============================================================================
// 하위 호환성을 위한 레거시 함수 (점진적 마이그레이션용)
// ============================================================================

/**
 * @deprecated tokenManager.getAccessToken() 사용
 */
export function getToken(): string | null {
  return tokenManager.getAccessToken()
}

/**
 * @deprecated tokenManager.setTokens() 사용
 */
export function setToken(token: string): void {
  tokenManager.setTokens(token)
}

/**
 * @deprecated tokenManager.clearTokens() 사용
 */
export function clearToken(): void {
  tokenManager.clearTokens()
}

/**
 * @deprecated tokenManager.getAuthHeader() 사용
 */
export function getAuthHeader(): HeadersInit {
  return tokenManager.getAuthHeader()
}
