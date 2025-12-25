/**
 * UI 컴포넌트 관련 스타일 유틸리티
 * 버튼, 입력, 테이블, 뱃지 등
 */

// ============================================================================
// 버튼
// ============================================================================

/** 버튼 기본 높이 및 텍스트 */
export const buttonBase = 'h-12 text-base'

/** 파괴적 버튼 스타일 (ghost) */
export const destructiveGhost = 'justify-start text-destructive hover:text-destructive hover:bg-destructive/10'

// ============================================================================
// 입력 필드
// ============================================================================

/** 폼 input 기본 높이 */
export const inputHeight = 'h-12'

/** Input 내부 아이콘 위치 (왼쪽) */
export const inputIconLeft = 'absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground'

/** Input 내부 아이콘 위치 (오른쪽) */
export const inputIconRight = 'absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors'

/** 입력 클리어 버튼 위치 */
export const inputClearButton = 'absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors'

/** 셀렉트/드롭다운 트리거 */
export const selectTrigger = 'w-full bg-background border-border'

/** 인증 코드 입력 컨테이너 */
export const verificationCodeInput = 'bg-background border border-border rounded-lg px-4 h-12 focus-within:ring-2 focus-within:ring-ring focus-within:ring-offset-2'

/** 투명 입력 (인증 코드용) */
export const transparentInput = 'border-0 p-0 h-auto focus-visible:ring-0 focus-visible:ring-offset-0 text-base flex-1 bg-transparent'

// ============================================================================
// 테이블
// ============================================================================

/** 테이블 컨테이너 스타일 */
export const tableContainer = 'rounded-xl border bg-card overflow-x-auto'

/** 테이블 헤더 행 스타일 */
export const tableHeaderRow = 'bg-muted/50 hover:bg-muted/50'

/** 테이블 데이터 행 스타일 (호버 가능) */
export const tableDataRow = 'cursor-pointer hover:bg-muted/50 transition-colors'

/** 테이블 캡션 (스크린 리더 전용) */
export const tableCaption = 'sr-only'

/** 탭 리스트 컨테이너 */
export const tabsList = 'h-10 p-1 bg-card'

// ============================================================================
// 뱃지
// ============================================================================

/** 뱃지 아바타 (닉네임 첫 글자) */
export const badgeAvatar = 'w-9 h-9 rounded-md flex items-center justify-center shrink-0'

/** 뱃지 아바타 텍스트 */
export const badgeAvatarText = 'text-sm font-semibold'

/** 게임 뱃지 스타일 */
export const gameBadge = 'text-xs rounded-md'

/** 등급 뱃지 스타일 */
export const rarityBadge = 'rounded-md'

/** 게임 탭 트리거 */
export const gameTabTrigger = 'px-5 py-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground'

// ============================================================================
// 스켈레톤
// ============================================================================

/** 스켈레톤 아이템 행 */
export const skeletonRow = 'flex items-center gap-4 p-4 border-b border-border'

// ============================================================================
// 진행 바
// ============================================================================

/** 진행 바 컨테이너 */
export const progressBar = 'w-full bg-muted rounded-full h-2'

/** 진행 바 채움 (트랜지션) */
export const progressBarFill = 'h-2 rounded-full transition-all duration-300'

// ============================================================================
// 호버 효과
// ============================================================================

/** 호버 배경 (인터랙티브 요소) */
export const hoverBg = 'hover:bg-hover-bg'

/** 활성 배경 (선택된 상태) */
export const activeBg = 'bg-active-bg'

/** 행 호버 효과 (리스트 아이템) */
export const listItemHover = 'hover:bg-muted/50 transition-colors'

/** 행 호버 + 패딩 (어드민 리스트) */
export const adminListItem = 'p-4 hover:bg-muted/50 transition-colors'

// ============================================================================
// 뮤트된 배경
// ============================================================================

/** 뮤트된 배경 (50% 투명도) */
export const mutedBg = 'bg-muted/50'

/** 뮤트된 배경 + rounded */
export const mutedBgRounded = 'bg-muted/50 rounded-lg'

// ============================================================================
// 코드 블록
// ============================================================================

/** 코드 블록 스타일 */
export const codeBlock = 'bg-zinc-950 text-zinc-100 p-4 rounded-lg text-sm overflow-x-auto'

/** 코드 복사 버튼 */
export const codeCopyButton = 'absolute top-2 right-2 p-2 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-zinc-100 opacity-0 group-hover:opacity-100 transition-opacity'
