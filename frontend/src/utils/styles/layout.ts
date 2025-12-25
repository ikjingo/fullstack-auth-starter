/**
 * 레이아웃 관련 스타일 유틸리티
 * 페이지 컨테이너, 카드, 헤더/푸터 등
 */

// ============================================================================
// 페이지 레이아웃
// ============================================================================

/** 페이지 최상위 컨테이너 */
export const pageContainer = 'w-full min-h-full bg-background'

/** 기본 페이지 컨텐츠 영역 (max-width: 1200px) */
export const pageContent = 'max-w-[1200px] mx-auto py-8 px-4'

/** 좁은 페이지 컨텐츠 영역 - 폼, 로그인 등 (max-width: 400px) */
export const pageContentNarrow = 'max-w-[400px] mx-auto'

/** 중간 너비 페이지 컨텐츠 영역 (max-width: 600px) */
export const pageContentMedium = 'max-w-[600px] mx-auto'

/** 넓은 페이지 컨텐츠 영역 - 마이페이지 등 (max-width: 1000px) */
export const pageContentWide = 'max-w-[1000px] mx-auto py-8 px-4'

/** 페이지 최소 높이 (헤더 제외) */
export const pageMinHeight = 'min-h-[calc(100vh-3.5rem)]'

// ============================================================================
// 페이지 헤더
// ============================================================================

/** 페이지 헤더 컨테이너 */
export const pageHeader = 'text-center mb-8'

/** 페이지 제목 */
export const pageTitle = 'text-2xl font-bold text-foreground mb-2'

/** 페이지 부제목 */
export const pageSubtitle = 'text-muted-foreground'

// ============================================================================
// 카드
// ============================================================================

/** 기본 카드 스타일 */
export const card = 'bg-card border border-border rounded-xl'

/** 카드 패딩 - 기본 (p-6) */
export const cardPadding = 'p-6'

/** 카드 패딩 - 작은 (p-4) */
export const cardPaddingSm = 'p-4'

/** 카드 패딩 - 큰, 반응형 (p-6 md:p-10) */
export const cardPaddingLg = 'p-6 md:p-10'

/** 큰 라운드 카드 (rounded-2xl) */
export const cardRoundedLg = 'bg-card border border-border rounded-2xl'

// ============================================================================
// 구분선
// ============================================================================

/** 구분선 (수평) */
export const divider = 'border-t border-border'

/** 섹션 구분선 (divider의 별칭) */
export const sectionDivider = divider

/** 섹션 구분선 (패딩 포함) */
export const sectionDividerPadded = 'mt-4 pt-4 border-t border-border'

/** OR 구분선 컨테이너 */
export const orDivider = 'relative my-6'

/** OR 구분선 라인 */
export const orDividerLine = 'absolute inset-0 flex items-center'

/** OR 구분선 텍스트 */
export const orDividerText = 'relative flex justify-center text-xs uppercase'

/** OR 구분선 텍스트 배경 */
export const orDividerTextBg = 'bg-card px-2 text-muted-foreground'

// ============================================================================
// 모달 크기
// ============================================================================

/** 모달 크기 - 작은 (max-w-sm) */
export const modalSm = 'max-w-sm'

/** 모달 크기 - 중간 (max-w-md) */
export const modalMd = 'max-w-md'

/** 모달 크기 - 큰 (max-w-lg) */
export const modalLg = 'max-w-lg'

/** 모달 크기 - 매우 큰 (max-w-xl) */
export const modalXl = 'max-w-xl'

// ============================================================================
// 상대 위치 컨테이너
// ============================================================================

/** 상대 위치 컨테이너 (margin-y) */
export const relativeContainerY = 'relative my-6'
