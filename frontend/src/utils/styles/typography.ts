/**
 * 타이포그래피 관련 스타일 유틸리티
 * 텍스트 스타일, 링크, 버튼 텍스트 등
 */

// ============================================================================
// 텍스트 크기 및 색상
// ============================================================================

/** 작은 텍스트 */
export const textSm = 'text-sm'

/** 뮤트된 텍스트 */
export const textMuted = 'text-muted-foreground'

/** 뮤트된 작은 텍스트 */
export const textSmMuted = 'text-sm text-muted-foreground'

// ============================================================================
// 텍스트 정렬
// ============================================================================

/** 텍스트 가운데 정렬 */
export const textCenter = 'text-center'

/** 텍스트 오른쪽 정렬 */
export const textRight = 'text-right'

/** 텍스트 왼쪽 정렬 */
export const textLeft = 'text-left'

// ============================================================================
// 링크 스타일
// ============================================================================

/** 기본 링크 스타일 */
export const link = 'text-primary hover:underline'

/** 링크 스타일 (medium weight) */
export const linkMedium = 'text-primary hover:underline font-medium'

// ============================================================================
// 텍스트 버튼
// ============================================================================

/** 텍스트 버튼 (인라인 액션) */
export const textButton = 'text-sm text-muted-foreground hover:text-foreground transition-colors'

/** 텍스트 버튼 (primary) */
export const textButtonPrimary = 'text-sm text-primary hover:text-foreground transition-colors'

/** 텍스트 버튼 (비활성화) */
export const textButtonDisabled = 'text-sm text-muted-foreground/50'

// ============================================================================
// 특수 텍스트 스타일
// ============================================================================

/** 텍스트 스타일 (그룹 호버) */
export const textGroupHover = 'text-muted-foreground group-hover:text-foreground'

/** 재전송 링크 스타일 */
export const resendLink = 'text-primary hover:text-primary/80 text-sm font-medium transition-colors'

/** 재전송 링크 (whitespace-nowrap) */
export const resendLinkNowrap = 'text-primary hover:text-primary/80 text-sm font-medium whitespace-nowrap transition-colors'
