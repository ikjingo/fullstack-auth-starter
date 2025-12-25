/**
 * 통합 Query Key 관리 모듈
 *
 * TanStack Query의 Query Key를 중앙에서 관리하여
 * 캐시 일관성과 타입 안전성을 보장합니다.
 *
 * 사용법:
 * ```typescript
 * import { queryKeys } from '@/api/client'
 *
 * // Query에서 사용
 * useQuery({
 *   queryKey: queryKeys.auth.user(),
 *   queryFn: () => authApi.getMe(),
 * })
 *
 * // 캐시 무효화
 * queryClient.invalidateQueries({ queryKey: queryKeys.auth.all })
 * ```
 */

/**
 * 통합 Query Key 객체
 *
 * 계층적 구조로 관련 쿼리들을 그룹화하고,
 * 타입 안전한 Query Key 생성을 지원합니다.
 */
export const queryKeys = {
  /**
   * 인증 관련 Query Keys
   */
  auth: {
    all: ['auth'] as const,
    user: () => [...queryKeys.auth.all, 'user'] as const,
  },
} as const

/**
 * Query Key 타입 추출 유틸리티
 *
 * 특정 Query Key의 타입을 추출할 때 사용
 */
export type QueryKeyOf<T extends (...args: never[]) => readonly unknown[]> =
  ReturnType<T>

// 편의를 위한 개별 타입 export
export type AuthQueryKey = (typeof queryKeys.auth)[keyof typeof queryKeys.auth]
