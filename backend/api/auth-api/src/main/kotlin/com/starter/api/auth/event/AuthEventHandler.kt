package com.starter.api.auth.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 인증 관련 도메인 이벤트를 처리하는 핸들러
 *
 * 비동기로 처리되어 메인 트랜잭션에 영향을 주지 않습니다.
 *
 * ## 현재 기능
 * - 모든 인증 이벤트 로깅 (감사 추적)
 *
 * ## 향후 확장 계획 (GitHub Issue 참조)
 * - 이메일 알림: 환영 메일, 비밀번호 변경, 계정 잠금, 새 디바이스 감지
 * - 보안 알림: Slack 연동, 의심스러운 활동 감지
 * - 외부 연동: 마케팅 시스템, 분석 플랫폼
 */
@Component
class AuthEventHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 사용자 등록 이벤트 처리
     * - 환영 이메일 발송 (향후 구현)
     * - 분석 데이터 수집
     */
    @Async
    @EventListener
    fun handleUserRegistered(event: UserRegisteredEvent) {
        log.info(
            "[Event] User registered: userId={}, email={}, nickname={}, timestamp={}",
            event.userId,
            event.email,
            event.nickname,
            event.timestamp,
        )
    }

    /**
     * 로그인 성공 이벤트 처리
     * - 마지막 로그인 시간 업데이트 (이미 처리됨)
     * - 의심스러운 로그인 감지
     */
    @Async
    @EventListener
    fun handleLoginSuccess(event: LoginSuccessEvent) {
        log.info(
            "[Event] Login success: userId={}, email={}, ip={}, timestamp={}",
            event.userId,
            event.email,
            event.ipAddress ?: "unknown",
            event.timestamp,
        )
    }

    /**
     * 로그인 실패 이벤트 처리
     * - 보안 알림 발송
     * - 브루트포스 공격 감지
     */
    @Async
    @EventListener
    fun handleLoginFailure(event: LoginFailureEvent) {
        log.warn(
            "[Event] Login failure: email={}, reason={}, ip={}, timestamp={}",
            event.email,
            event.reason,
            event.ipAddress ?: "unknown",
            event.timestamp,
        )
    }

    /**
     * 비밀번호 변경 이벤트 처리
     * - 보안 알림 이메일 발송
     */
    @Async
    @EventListener
    fun handlePasswordChanged(event: PasswordChangedEvent) {
        log.info(
            "[Event] Password changed: userId={}, email={}, type={}, timestamp={}",
            event.userId,
            event.email,
            event.changeType,
            event.timestamp,
        )
    }

    /**
     * 계정 잠금 이벤트 처리
     * - 계정 소유자에게 알림
     * - 보안팀 알림 (선택)
     */
    @Async
    @EventListener
    fun handleAccountLocked(event: AccountLockedEvent) {
        log.warn(
            "[Event] Account locked: userId={}, email={}, failedAttempts={}, lockUntil={}, timestamp={}",
            event.userId,
            event.email,
            event.failedAttempts,
            event.lockUntil,
            event.timestamp,
        )
    }

    /**
     * 로그아웃 이벤트 처리
     */
    @Async
    @EventListener
    fun handleLogout(event: LogoutEvent) {
        log.debug(
            "[Event] User logged out: userId={}, email={}, timestamp={}",
            event.userId,
            event.email,
            event.timestamp,
        )
    }

    /**
     * 토큰 갱신 이벤트 처리
     */
    @Async
    @EventListener
    fun handleTokenRefreshed(event: TokenRefreshedEvent) {
        log.debug(
            "[Event] Token refreshed: userId={}, email={}, timestamp={}",
            event.userId,
            event.email,
            event.timestamp,
        )
    }
}
