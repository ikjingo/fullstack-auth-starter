/**
 * API 클라이언트
 *
 * Fetch API 기반의 HTTP 클라이언트
 * - 자동 토큰 인증
 * - 401 응답 시 자동 토큰 갱신
 * - 표준화된 에러 처리
 * - 타입 안전한 요청/응답
 */
import type { ApiResponseWrapper, RequestConfig, QueryParams } from './types'
import { ApiError, createNetworkError } from './errors'
import { emitSessionExpired } from './session'
import { tokenManager } from './tokenManager'

// API 기본 URL
const API_BASE_URL = '/api/v1'

/**
 * API 클라이언트 클래스
 */
class ApiClient {
  private readonly baseUrl: string

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  /**
   * URL 빌드 (쿼리 파라미터 포함)
   */
  private buildUrl(endpoint: string, params?: QueryParams): string {
    const url = new URL(`${this.baseUrl}${endpoint}`, window.location.origin)

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          url.searchParams.append(key, String(value))
        }
      })
    }

    return url.toString()
  }

  /**
   * 응답 처리 및 에러 변환
   * @param isRetry - 재시도 요청인지 여부 (무한 루프 방지)
   */
  private async handleResponse<T>(
    response: Response,
    isRetry: boolean = false
  ): Promise<T | { __retry: true; status: 401 }> {
    // 204 No Content 처리
    if (response.status === 204) {
      return undefined as T
    }

    // 401 Unauthorized - 토큰 갱신 시도 (재시도가 아닌 경우만)
    if (response.status === 401 && !isRetry) {
      if (tokenManager.hasToken()) {
        // 토큰 갱신 시도
        const refreshed = await tokenManager.refreshAccessToken()
        if (refreshed) {
          // 갱신 성공 - 재시도 필요 표시
          return { __retry: true, status: 401 } as T | { __retry: true; status: 401 }
        }
        // 갱신 실패 - 세션 만료 이벤트
        emitSessionExpired()
      }
    }

    let json: ApiResponseWrapper<T>
    try {
      json = (await response.json()) as ApiResponseWrapper<T>
    } catch {
      // JSON 파싱 실패
      throw new ApiError({
        message: '서버 응답을 처리할 수 없습니다.',
        status: response.status,
      })
    }

    // 백엔드 ApiResponse 형식 처리
    if ('result' in json) {
      if (json.result === 'ERROR' && json.error) {
        throw new ApiError({
          message: json.error.message,
          code: json.error.code,
          status: response.status,
          data: json.error.data,
        })
      }
      return json.data as T
    }

    // 기존 형식 (ApiResponse가 아닌 경우) 처리
    if (!response.ok) {
      let message = '요청 처리 중 오류가 발생했습니다.'
      if (
        typeof json === 'object' &&
        json !== null &&
        'message' in json &&
        typeof (json as { message: unknown }).message === 'string'
      ) {
        message = (json as { message: string }).message
      }
      throw new ApiError({
        message,
        status: response.status,
        data: json,
      })
    }

    return json as unknown as T
  }

  /**
   * 요청 실행 (공통 로직)
   * @param isRetry - 재시도 요청인지 여부 (무한 루프 방지)
   */
  private async request<T>(
    method: string,
    endpoint: string,
    data?: unknown,
    config?: RequestConfig,
    isRetry: boolean = false
  ): Promise<T> {
    const { params, ...fetchConfig } = config ?? {}
    const url = this.buildUrl(endpoint, params)

    try {
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...tokenManager.getAuthHeader(),
          ...fetchConfig.headers,
        },
        body: data !== undefined ? JSON.stringify(data) : undefined,
        ...fetchConfig,
      })

      const result = await this.handleResponse<T>(response, isRetry)

      // 토큰 갱신 후 재시도 필요한 경우
      if (
        result &&
        typeof result === 'object' &&
        '__retry' in result &&
        (result as { __retry: boolean }).__retry
      ) {
        // 갱신된 토큰으로 원래 요청 재시도
        return this.request<T>(method, endpoint, data, config, true)
      }

      return result as T
    } catch (error) {
      // 이미 ApiError인 경우 그대로 던짐
      if (error instanceof ApiError) {
        throw error
      }
      // 네트워크 에러 등 처리
      throw createNetworkError(error instanceof Error ? error : undefined)
    }
  }

  /**
   * GET 요청
   */
  async get<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>('GET', endpoint, undefined, config)
  }

  /**
   * POST 요청
   */
  async post<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>('POST', endpoint, data, config)
  }

  /**
   * PUT 요청
   */
  async put<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>('PUT', endpoint, data, config)
  }

  /**
   * PATCH 요청
   */
  async patch<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>('PATCH', endpoint, data, config)
  }

  /**
   * DELETE 요청
   */
  async delete<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>('DELETE', endpoint, undefined, config)
  }

  /**
   * 토큰 설정
   */
  setToken(token: string): void {
    tokenManager.setTokens(token)
  }

  /**
   * 토큰 설정 (Access Token + Refresh Token)
   */
  setTokens(accessToken: string, refreshToken: string): void {
    tokenManager.setTokens(accessToken, refreshToken)
  }

  /**
   * 토큰 삭제
   */
  clearToken(): void {
    tokenManager.clearTokens()
  }
}

// 싱글톤 인스턴스
export const apiClient = new ApiClient(API_BASE_URL)
export { API_BASE_URL }
