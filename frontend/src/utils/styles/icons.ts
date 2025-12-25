/**
 * 아이콘 관련 스타일 유틸리티
 * 아이콘 크기, 컨테이너, 아바타 등
 */

// ============================================================================
// 아이콘 크기
// ============================================================================

/** 아이콘 크기 - 작은 (16px, w-4 h-4) */
export const iconSm = 'w-4 h-4'

/** 아이콘 크기 - 중간 (20px, w-5 h-5) */
export const iconMd = 'w-5 h-5'

/** 아이콘 크기 - 큰 (32px, w-8 h-8) */
export const iconLg = 'w-8 h-8'

// ============================================================================
// 아이콘 + 텍스트 조합
// ============================================================================

/** 아이콘 + 텍스트 조합 (작은 마진) */
export const iconWithTextSm = `${iconSm} mr-1`

/** 아이콘 + 텍스트 조합 (기본 마진) */
export const iconWithText = `${iconSm} mr-2`

// ============================================================================
// 아이콘 컨테이너
// ============================================================================

/** 아이콘 컨테이너 (원형, 큰) */
export const iconContainerLg = 'w-16 h-16 rounded-full bg-muted/50 flex items-center justify-center'

/** 아이콘 컨테이너 (primary 배경) */
export const iconContainerPrimary = 'rounded-full bg-primary/10 flex items-center justify-center'

// ============================================================================
// 아바타
// ============================================================================

/** 사용자 아바타 (그라데이션) */
export const avatarGradient = 'bg-gradient-to-br from-violet-500 to-pink-500 rounded-full flex items-center justify-center'

/** 로고 그라데이션 */
export const logoGradient = 'bg-gradient-to-br from-violet-500 to-pink-500 rounded-md flex items-center justify-center'

/** 사용자 아바타 컨테이너 (작은) */
export const avatarSm = 'w-6 h-6 rounded-full bg-muted flex items-center justify-center'

/** 사용자 아바타 컨테이너 (중간) */
export const avatarMd = 'w-9 h-9 rounded-full bg-muted flex items-center justify-center'
