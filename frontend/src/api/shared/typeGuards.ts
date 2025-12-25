/**
 * 타입 가드 및 변환 유틸리티
 *
 * 백엔드-프론트엔드 간 타입 안전한 변환을 위한 함수들
 */
import {
  BackendGameType,
  FrontendGameType,
  GAME_TYPE_MAP,
  ServiceType,
  Rarity,
  SortType,
  UserRole,
  type BackendGameTypeValue,
  type FrontendGameTypeValue,
  type ServiceTypeValue,
  type RarityValue,
  type SortTypeValue,
  type UserRoleValue,
} from './constants'

// ============================================================================
// 타입 가드
// ============================================================================

/**
 * 백엔드 게임 타입인지 확인
 */
export function isBackendGameType(value: unknown): value is BackendGameTypeValue {
  return (
    typeof value === 'string' &&
    Object.values(BackendGameType).includes(value as BackendGameTypeValue)
  )
}

/**
 * 프론트엔드 게임 타입인지 확인
 */
export function isFrontendGameType(
  value: unknown
): value is FrontendGameTypeValue {
  return (
    typeof value === 'string' &&
    Object.values(FrontendGameType).includes(value as FrontendGameTypeValue)
  )
}

/**
 * 서비스 타입인지 확인
 */
export function isServiceType(value: unknown): value is ServiceTypeValue {
  return (
    typeof value === 'string' &&
    Object.values(ServiceType).includes(value as ServiceTypeValue)
  )
}

/**
 * 희귀도 타입인지 확인
 */
export function isRarity(value: unknown): value is RarityValue {
  return (
    typeof value === 'string' &&
    Object.values(Rarity).includes(value as RarityValue)
  )
}

/**
 * 정렬 타입인지 확인
 */
export function isSortType(value: unknown): value is SortTypeValue {
  return (
    typeof value === 'string' &&
    Object.values(SortType).includes(value as SortTypeValue)
  )
}

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
// 타입 변환 함수
// ============================================================================

/**
 * 백엔드 게임 타입을 프론트엔드 타입으로 변환
 * @throws 유효하지 않은 게임 타입일 경우
 */
export function toFrontendGameType(
  backendType: BackendGameTypeValue
): FrontendGameTypeValue {
  const frontendType = GAME_TYPE_MAP.toFrontend[backendType]
  if (!frontendType) {
    throw new Error(`Unknown backend game type: ${backendType}`)
  }
  return frontendType
}

/**
 * 프론트엔드 게임 타입을 백엔드 타입으로 변환
 * @throws 유효하지 않은 게임 타입일 경우
 */
export function toBackendGameType(
  frontendType: FrontendGameTypeValue
): BackendGameTypeValue {
  const backendType = GAME_TYPE_MAP.toBackend[frontendType]
  if (!backendType) {
    throw new Error(`Unknown frontend game type: ${frontendType}`)
  }
  return backendType
}

/**
 * 안전한 백엔드 -> 프론트엔드 게임 타입 변환
 * 실패 시 null 반환
 */
export function safeToFrontendGameType(
  value: unknown
): FrontendGameTypeValue | null {
  if (!isBackendGameType(value)) {
    return null
  }
  try {
    return toFrontendGameType(value)
  } catch {
    return null
  }
}

/**
 * 안전한 프론트엔드 -> 백엔드 게임 타입 변환
 * 실패 시 null 반환
 */
export function safeToBackendGameType(
  value: unknown
): BackendGameTypeValue | null {
  if (!isFrontendGameType(value)) {
    return null
  }
  try {
    return toBackendGameType(value)
  } catch {
    return null
  }
}

/**
 * 백엔드 게임 타입 배열을 프론트엔드 배열로 변환
 */
export function toFrontendGameTypes(
  backendTypes: BackendGameTypeValue[]
): FrontendGameTypeValue[] {
  return backendTypes.map(toFrontendGameType)
}

/**
 * 프론트엔드 게임 타입 배열을 백엔드 배열로 변환
 */
export function toBackendGameTypes(
  frontendTypes: FrontendGameTypeValue[]
): BackendGameTypeValue[] {
  return frontendTypes.map(toBackendGameType)
}

// ============================================================================
// 유효성 검증 함수
// ============================================================================

/**
 * 희귀도 값 검증 및 반환
 * 유효하지 않으면 기본값 반환
 */
export function validateRarity(
  value: unknown,
  defaultValue: RarityValue = Rarity.C
): RarityValue {
  return isRarity(value) ? value : defaultValue
}

/**
 * 정렬 타입 검증 및 반환
 * 유효하지 않으면 기본값 반환
 */
export function validateSortType(
  value: unknown,
  defaultValue: SortTypeValue = SortType.SCORE_DESC
): SortTypeValue {
  return isSortType(value) ? value : defaultValue
}

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
