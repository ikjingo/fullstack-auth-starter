/**
 * 피드백 관련 스타일 유틸리티
 * 토스트, 에러, 로딩, 빈 상태, 상태 표시 등
 */

// ============================================================================
// 토스트
// ============================================================================

/** 토스트 컨테이너 */
export const toastContainer = 'fixed top-20 right-2 md:right-4 z-50 flex flex-col gap-2 max-w-[calc(100vw-1rem)] md:max-w-sm'

/** 토스트 닫기 버튼 */
export const toastCloseButton = 'flex-shrink-0 opacity-60 hover:opacity-100 transition-opacity rounded focus:outline-none focus:ring-2 focus:ring-current focus:ring-offset-2 focus:ring-offset-transparent'

// ============================================================================
// 에러 상태
// ============================================================================

/** 에러 텍스트 스타일 */
export const errorText = 'text-sm text-destructive'

/** 에러 border 스타일 */
export const errorBorder = 'border-destructive focus-visible:ring-destructive'

// ============================================================================
// 로딩 상태
// ============================================================================

/** 로딩 오버레이 */
export const loadingOverlay = 'absolute inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center z-10'

/** 로딩 스피너 컨테이너 */
export const loadingSpinner = 'flex items-center justify-center gap-2'

// ============================================================================
// 빈 상태
// ============================================================================

/** 빈 상태 아이콘 컨테이너 */
export const emptyStateIcon = 'w-16 h-16 rounded-full bg-muted/50 flex items-center justify-center mb-4'

// ============================================================================
// 상태 표시 (Success, Warning, Info)
// ============================================================================

/** 성공 배경 */
export const successBg = 'bg-green-500/10'

/** 성공 텍스트 */
export const successText = 'text-green-600 dark:text-green-400'

/** 경고 배경 */
export const warningBg = 'bg-yellow-500/10'

/** 경고 텍스트 */
export const warningText = 'text-yellow-600 dark:text-yellow-400'

/** 정보 배경 */
export const infoBg = 'bg-blue-500/10'

/** 정보 텍스트 */
export const infoText = 'text-blue-600 dark:text-blue-400'
