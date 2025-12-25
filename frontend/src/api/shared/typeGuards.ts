/**
 * 타입 가드 및 변환 유틸리티
 *
 * 백엔드-프론트엔드 간 타입 안전한 변환을 위한 함수들
 */
import { UserRole, type UserRoleValue } from './constants'

// ============================================================================
// 타입 가드
// ============================================================================

/**
 * 사용자 역할인지 확인
 */
export function isUserRole(value: unknown): value is UserRoleValue {
  return (
    typeof value === 'string' &&
    Object.values(UserRole).includes(value as UserRoleValue)
  )
}

// ============================================================================
// 유효성 검증 함수
// ============================================================================

/**
 * 사용자 역할 검증 및 반환
 * 유효하지 않으면 기본값 반환
 */
export function validateUserRole(
  value: unknown,
  defaultValue: UserRoleValue = UserRole.USER
): UserRoleValue {
  return isUserRole(value) ? value : defaultValue
}
