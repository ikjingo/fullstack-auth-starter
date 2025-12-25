package com.starter.api.auth.service.auth

import com.starter.api.auth.config.AccountLockoutProperties
import com.starter.api.auth.event.AccountLockedEvent
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 로그인 시도 관리 서비스
 * - 로그인 실패 횟수 추적
 * - 계정 잠금 관리
 * - 무차별 대입 공격 방지
 */
@Service
class LoginAttemptService(
    private val userRepository: UserRepository,
    private val lockoutProperties: AccountLockoutProperties,
    private val eventPublisher: AuthEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 계정이 잠겨있는지 확인
     * @throws CoreApiException 계정이 잠겨있으면 ACCOUNT_LOCKED 예외 발생
     */
    fun checkAccountLocked(user: UserEntity) {
        if (!lockoutProperties.enabled) {
            return
        }

        val lockoutUntil = user.lockoutUntil
        if (lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now())) {
            val remainingMinutes =
                java.time.Duration
                    .between(
                        LocalDateTime.now(),
                        lockoutUntil,
                    ).toMinutes() + 1
            log.warn("Login attempt on locked account: ${user.email}, locked for $remainingMinutes more minutes")
            throw CoreApiException(ErrorType.ACCOUNT_LOCKED)
        }

        // 잠금 시간이 지났으면 잠금 해제
        if (lockoutUntil != null && !lockoutUntil.isAfter(LocalDateTime.now())) {
            resetFailedAttempts(user.id)
        }
    }

    /**
     * 로그인 실패 기록
     * 최대 실패 횟수 초과 시 계정 잠금
     * REQUIRES_NEW: 외부 트랜잭션이 롤백되어도 실패 기록은 저장됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordFailedAttempt(user: UserEntity) {
        if (!lockoutProperties.enabled) {
            return
        }

        // 새 트랜잭션에서 최신 데이터 로드
        val freshUser = userRepository.findById(user.id).orElseThrow()

        freshUser.failedLoginAttempts++

        if (freshUser.failedLoginAttempts >= lockoutProperties.maxFailedAttempts) {
            val lockUntil = LocalDateTime.now().plusMinutes(lockoutProperties.lockDurationMinutes)
            freshUser.lockoutUntil = lockUntil
            log.warn(
                "Account locked due to {} failed login attempts: {}",
                freshUser.failedLoginAttempts,
                freshUser.email,
            )

            // 계정 잠금 이벤트 발행
            eventPublisher.publishAccountLocked(
                AccountLockedEvent(
                    userId = freshUser.id,
                    email = freshUser.email,
                    failedAttempts = freshUser.failedLoginAttempts,
                    lockUntil = lockUntil,
                ),
            )
        } else {
            log.info(
                "Failed login attempt {} of {} for user: {}",
                freshUser.failedLoginAttempts,
                lockoutProperties.maxFailedAttempts,
                freshUser.email,
            )
        }

        userRepository.save(freshUser)
    }

    /**
     * 로그인 성공 시 실패 카운터 초기화
     * REQUIRES_NEW: 외부 트랜잭션과 독립적으로 초기화 수행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun resetFailedAttempts(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow()
        if (user.failedLoginAttempts > 0 || user.lockoutUntil != null) {
            user.failedLoginAttempts = 0
            user.lockoutUntil = null
            userRepository.save(user)
            log.debug("Reset failed login attempts for user: ${user.email}")
        }
    }

    /**
     * 로그인 성공 시 실패 카운터 초기화 (UserEntity 버전)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun resetFailedAttempts(user: UserEntity) {
        resetFailedAttempts(user.id)
    }

    /**
     * 남은 시도 횟수 조회
     */
    fun getRemainingAttempts(user: UserEntity): Int = (lockoutProperties.maxFailedAttempts - user.failedLoginAttempts).coerceAtLeast(0)

    /**
     * 계정 잠금 강제 해제 (관리자용)
     */
    @Transactional
    fun unlockAccount(user: UserEntity) {
        user.failedLoginAttempts = 0
        user.lockoutUntil = null
        userRepository.save(user)
        log.info("Account manually unlocked: ${user.email}")
    }
}
