/**
 * 백엔드-프론트엔드 공유 상수
 *
 * 백엔드 enum 값들과 동기화된 상수 정의
 */

// ============================================================================
// 게임 타입 (Backend: GameType enum)
// ============================================================================

/**
 * 백엔드 게임 타입
 * @see backend/storage/db-core/.../NicknameRepositoryCustom.kt
 */
export const BackendGameType = {
  MAPLESTORY: 'MAPLESTORY',
  LOSTARK: 'LOSTARK',
  DICTIONARY: 'DICTIONARY',
} as const

export type BackendGameTypeValue =
  (typeof BackendGameType)[keyof typeof BackendGameType]

/**
 * 프론트엔드 게임 타입 (한글)
 */
export const FrontendGameType = {
  MAPLESTORY: '메이플스토리',
  LOSTARK: '로스트아크',
  DICTIONARY: '한글사전',
} as const

export type FrontendGameTypeValue =
  (typeof FrontendGameType)[keyof typeof FrontendGameType]

/**
 * 게임 타입 매핑 테이블
 */
export const GAME_TYPE_MAP = {
  // Backend -> Frontend
  toFrontend: {
    [BackendGameType.MAPLESTORY]: FrontendGameType.MAPLESTORY,
    [BackendGameType.LOSTARK]: FrontendGameType.LOSTARK,
    [BackendGameType.DICTIONARY]: FrontendGameType.DICTIONARY,
  } as const,
  // Frontend -> Backend
  toBackend: {
    [FrontendGameType.MAPLESTORY]: BackendGameType.MAPLESTORY,
    [FrontendGameType.LOSTARK]: BackendGameType.LOSTARK,
    [FrontendGameType.DICTIONARY]: BackendGameType.DICTIONARY,
  } as const,
} as const

// ============================================================================
// 서비스 타입 (Backend: ServiceType / GameType)
// ============================================================================

/**
 * 서비스 타입 (백엔드에서 GameType과 동일하게 사용)
 */
export const ServiceType = BackendGameType

export type ServiceTypeValue = BackendGameTypeValue

/**
 * 서비스 표시 정보
 */
export const SERVICE_DISPLAY_INFO: Record<
  ServiceTypeValue,
  { name: string; description: string; color: string }
> = {
  [ServiceType.MAPLESTORY]: {
    name: '메이플스토리',
    description: '메이플스토리에서 수집된 닉네임',
    color: 'bg-orange-500',
  },
  [ServiceType.LOSTARK]: {
    name: '로스트아크',
    description: '로스트아크에서 수집된 닉네임',
    color: 'bg-blue-500',
  },
  [ServiceType.DICTIONARY]: {
    name: '한글사전',
    description: '한글 사전에서 수집된 닉네임',
    color: 'bg-green-500',
  },
} as const

// ============================================================================
// 희귀도 타입 (Backend: Rarity calculation)
// ============================================================================

/**
 * 희귀도 등급
 */
export const Rarity = {
  SSS: 'SSS',
  SS: 'SS',
  S: 'S',
  A: 'A',
  B: 'B',
  C: 'C',
} as const

export type RarityValue = (typeof Rarity)[keyof typeof Rarity]

/**
 * 희귀도 표시 정보
 */
export const RARITY_DISPLAY_INFO: Record<
  RarityValue,
  { label: string; color: string; bgColor: string }
> = {
  [Rarity.SSS]: {
    label: 'SSS',
    color: 'text-yellow-400',
    bgColor: 'bg-yellow-400/10',
  },
  [Rarity.SS]: {
    label: 'SS',
    color: 'text-purple-400',
    bgColor: 'bg-purple-400/10',
  },
  [Rarity.S]: {
    label: 'S',
    color: 'text-blue-400',
    bgColor: 'bg-blue-400/10',
  },
  [Rarity.A]: {
    label: 'A',
    color: 'text-green-400',
    bgColor: 'bg-green-400/10',
  },
  [Rarity.B]: {
    label: 'B',
    color: 'text-gray-400',
    bgColor: 'bg-gray-400/10',
  },
  [Rarity.C]: {
    label: 'C',
    color: 'text-gray-500',
    bgColor: 'bg-gray-500/10',
  },
} as const

// ============================================================================
// 정렬 타입
// ============================================================================

/**
 * 정렬 타입
 */
export const SortType = {
  SCORE_DESC: 'SCORE_DESC',
  UPDATED_DESC: 'UPDATED_DESC',
  UPDATED_ASC: 'UPDATED_ASC',
  SCORE_DESC_UPDATED_DESC: 'SCORE_DESC_UPDATED_DESC',
  SCORE_DESC_UPDATED_ASC: 'SCORE_DESC_UPDATED_ASC',
} as const

export type SortTypeValue = (typeof SortType)[keyof typeof SortType]

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
