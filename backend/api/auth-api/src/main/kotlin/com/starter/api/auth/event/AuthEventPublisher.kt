package com.starter.api.auth.event

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 인증 관련 도메인 이벤트를 발행하는 컴포넌트
 *
 * Spring ApplicationEventPublisher를 래핑하여
 * 타입 안전한 이벤트 발행 인터페이스를 제공합니다.
 */
@Component
class AuthEventPublisher(
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 이벤트를 발행합니다.
     * @param event 발행할 이벤트
     */
    fun publish(event: AuthEvent) {
        log.debug("Publishing event: ${event::class.simpleName}")
        eventPublisher.publishEvent(event)
    }

    /**
     * 사용자 등록 이벤트 발행
     */
    fun publishUserRegistered(
        userId: Long,
        email: String,
        nickname: String?,
    ) {
        publish(UserRegisteredEvent(userId, email, nickname))
    }

    /**
     * 로그인 성공 이벤트 발행
     */
    fun publishLoginSuccess(
        userId: Long,
        email: String,
        ipAddress: String? = null,
        userAgent: String? = null,
    ) {
        publish(LoginSuccessEvent(userId, email, ipAddress, userAgent))
    }

    /**
     * 로그인 실패 이벤트 발행
     */
    fun publishLoginFailure(
        email: String,
        reason: String,
        ipAddress: String? = null,
        userAgent: String? = null,
    ) {
        publish(LoginFailureEvent(email, reason, ipAddress, userAgent))
    }

    /**
     * 비밀번호 변경 이벤트 발행
     */
    fun publishPasswordChanged(
        userId: Long,
        email: String,
        changeType: PasswordChangedEvent.PasswordChangeType,
    ) {
        publish(PasswordChangedEvent(userId, email, changeType))
    }

    /**
     * 계정 잠금 이벤트 발행
     */
    fun publishAccountLocked(event: AccountLockedEvent) {
        publish(event)
    }

    /**
     * 로그아웃 이벤트 발행
     */
    fun publishLogout(
        userId: Long,
        email: String,
    ) {
        publish(LogoutEvent(userId, email))
    }

    /**
     * 토큰 갱신 이벤트 발행
     */
    fun publishTokenRefreshed(
        userId: Long,
        email: String,
    ) {
        publish(TokenRefreshedEvent(userId, email))
    }

    /**
     * 소셜 계정 연동 이벤트 발행
     */
    fun publishSocialAccountLinked(
        userId: Long,
        email: String,
        provider: String,
    ) {
        publish(SocialAccountLinkedEvent(userId, email, provider))
    }

    /**
     * 소셜 계정 연동 해제 이벤트 발행
     */
    fun publishSocialAccountUnlinked(
        userId: Long,
        email: String,
        provider: String,
    ) {
        publish(SocialAccountUnlinkedEvent(userId, email, provider))
    }
}
