/**
 * 네비게이션 관련 스타일 유틸리티
 * 헤더, 푸터, 메뉴, 네비게이션 링크 등
 */

// ============================================================================
// 헤더
// ============================================================================

/** 헤더 스타일 */
export const header = 'fixed top-0 left-0 right-0 z-[1000] h-14 bg-card border-b border-border'

/** 헤더 내부 컨테이너 */
export const headerInner = 'max-w-[1200px] mx-auto px-6 h-full flex items-center justify-between gap-6'

// ============================================================================
// 푸터
// ============================================================================

/** 푸터 스타일 */
export const footer = 'mt-auto bg-card border-t border-border'

/** 푸터 내부 컨테이너 */
export const footerInner = 'max-w-[1200px] mx-auto p-6 flex items-center justify-center gap-8'

/** 푸터 링크 스타일 */
export const footerLink = 'text-muted-foreground text-xs no-underline transition-colors hover:text-foreground'

/** 푸터 소셜 링크 스타일 */
export const footerSocialLink = 'text-muted-foreground flex items-center transition-colors hover:text-foreground'

// ============================================================================
// 네비게이션 링크
// ============================================================================

/** 네비게이션 링크 (기본) */
export const navLink = 'px-4 py-2 rounded-md text-sm font-medium no-underline transition-colors'

/** 네비게이션 링크 (활성) */
export const navLinkActive = 'text-foreground bg-active-bg font-medium'

/** 네비게이션 링크 (비활성) */
export const navLinkInactive = 'text-muted-foreground hover:text-foreground hover:bg-hover-bg'

// ============================================================================
// 모바일 메뉴
// ============================================================================

/** 모바일 메뉴 스타일 */
export const mobileMenu = 'absolute top-14 left-0 right-0 bg-card border-b border-border'

/** 모바일 메뉴 아이템 (기본) */
export const mobileMenuItem = 'py-3 px-4 rounded-md text-sm font-medium no-underline text-muted-foreground hover:text-foreground hover:bg-hover-bg transition-colors'

/** 모바일 메뉴 아이템 (아이콘 포함) */
export const mobileMenuItemWithIcon = `${mobileMenuItem} flex items-center gap-2`

/** 모바일 메뉴 아이템 (활성) */
export const mobileMenuItemActive = 'py-3 px-4 rounded-md text-sm font-medium no-underline bg-primary/10 text-primary'

// ============================================================================
// 반응형 숨김
// ============================================================================

/** 반응형 숨김 (모바일) - 모바일에서 숨김 */
export const hiddenMobile = 'hidden md:flex'

/** 반응형 숨김 (데스크톱) - 데스크톱에서 숨김 */
export const hiddenDesktop = 'flex md:hidden'
