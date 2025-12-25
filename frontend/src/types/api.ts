/**
 * API 공통 타입 정의
 *
 * API 클라이언트에서 사용하는 공통 타입들을 정의합니다.
 */

/**
 * 백엔드 ApiResponse 래퍼 타입
 */
export interface ApiResponseWrapper<T> {
  result: 'SUCCESS' | 'ERROR'
  data: T | null
  error: {
    code: string
    message: string
    data?: unknown
  } | null
}

/**
 * 쿼리 파라미터 타입
 */
export type QueryParamValue = string | number | boolean | undefined | null

/**
 * 쿼리 파라미터 객체 타입
 */
export type QueryParams = Record<string, QueryParamValue>

/**
 * API 요청 설정
 */
export interface RequestConfig extends Omit<RequestInit, 'body' | 'method'> {
  params?: QueryParams
}

/**
 * API 에러 인터페이스 (레거시 호환용)
 * 새 코드에서는 errors.ts의 ApiError 클래스 사용 권장
 * @deprecated errors.ts의 ApiError 클래스를 사용하세요
 */
export interface ApiErrorLegacy {
  message: string
  code?: string
  status: number
  data?: unknown
}

/**
 * 페이지네이션 파라미터
 */
export interface PaginationParams {
  page?: number
  size?: number
}

/**
 * 페이지 응답 공통 타입
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
  hasNext: boolean
}
