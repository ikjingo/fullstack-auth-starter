/**
 * 백엔드-프론트엔드 공유 상수
 *
 * 백엔드 enum 값들과 동기화된 상수 정의
 */

// ============================================================================
// 사용자 역할
// ============================================================================

/**
 * 사용자 역할
 */
export const UserRole = {
  USER: 'USER',
  ADMIN: 'ADMIN',
} as const

export type UserRoleValue = (typeof UserRole)[keyof typeof UserRole]
