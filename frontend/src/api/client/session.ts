/**
 * 세션 이벤트 유틸리티
 */

// 세션 만료 이벤트 이름
export const SESSION_EXPIRED_EVENT = 'session-expired'

/**
 * 세션 만료 이벤트 발생
 */
export function emitSessionExpired(): void {
  window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT))
}
