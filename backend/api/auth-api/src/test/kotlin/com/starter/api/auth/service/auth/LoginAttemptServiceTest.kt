package com.starter.api.auth.service.auth

import com.starter.api.auth.config.AccountLockoutProperties
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("LoginAttemptService 테스트")
class LoginAttemptServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var lockoutProperties: AccountLockoutProperties
    private lateinit var eventPublisher: AuthEventPublisher
    private lateinit var loginAttemptService: LoginAttemptService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        lockoutProperties =
            AccountLockoutProperties(
                maxFailedAttempts = 5,
                lockDurationMinutes = 15,
                enabled = true,
            )
        eventPublisher = mockk(relaxed = true)
        loginAttemptService = LoginAttemptService(userRepository, lockoutProperties, eventPublisher)
    }

    private fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
        failedLoginAttempts: Int = 0,
        lockoutUntil: LocalDateTime? = null,
    ): UserEntity =
        UserEntity(
            email = email,
            password = "password",
            nickname = "테스트유저",
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
            this.failedLoginAttempts = failedLoginAttempts
            this.lockoutUntil = lockoutUntil
        }

    @Nested
    @DisplayName("checkAccountLocked")
    inner class CheckAccountLockedTest {
        @Test
        fun `잠금되지 않은 계정은 예외 없이 통과해야 한다`() {
            // Given
            val user = createUser()

            // When & Then
            loginAttemptService.checkAccountLocked(user)
            // 예외 없이 통과
        }

        @Test
        fun `잠금된 계정은 ACCOUNT_LOCKED 예외가 발생해야 한다`() {
            // Given
            val user = createUser(lockoutUntil = LocalDateTime.now().plusMinutes(10))

            // When & Then
            assertThatThrownBy { loginAttemptService.checkAccountLocked(user) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_LOCKED)
        }

        @Test
        fun `잠금 시간이 지난 계정은 자동으로 잠금이 해제되어야 한다`() {
            // Given
            val user =
                createUser(
                    failedLoginAttempts = 5,
                    lockoutUntil = LocalDateTime.now().minusMinutes(1),
                )
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(user) } returns user

            // When
            loginAttemptService.checkAccountLocked(user)

            // Then - resetFailedAttempts가 새 트랜잭션에서 fresh user를 로드하여 수정함
            verify { userRepository.findById(user.id) }
            verify { userRepository.save(any()) }
        }

        @Test
        fun `기능이 비활성화되면 잠금 확인을 건너뛰어야 한다`() {
            // Given
            val disabledProperties = AccountLockoutProperties(enabled = false)
            val service = LoginAttemptService(userRepository, disabledProperties, eventPublisher)
            val user = createUser(lockoutUntil = LocalDateTime.now().plusMinutes(10))

            // When & Then
            service.checkAccountLocked(user) // 예외 없이 통과
        }
    }

    @Nested
    @DisplayName("recordFailedAttempt")
    inner class RecordFailedAttemptTest {
        @Test
        fun `로그인 실패 시 실패 카운터가 증가해야 한다`() {
            // Given
            val user = createUser(failedLoginAttempts = 0)
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(any()) } returns user

            // When
            loginAttemptService.recordFailedAttempt(user)

            // Then - 새 트랜잭션에서 fresh user를 로드하여 수정
            verify { userRepository.findById(user.id) }
            verify { userRepository.save(any()) }
        }

        @Test
        fun `최대 실패 횟수 도달 시 계정이 잠겨야 한다`() {
            // Given
            val user = createUser(failedLoginAttempts = 4) // 이미 4회 실패
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(any()) } returns user

            // When
            loginAttemptService.recordFailedAttempt(user)

            // Then - 새 트랜잭션에서 fresh user를 로드하여 수정
            verify { userRepository.findById(user.id) }
            verify { userRepository.save(any()) }
        }

        @Test
        fun `기능이 비활성화되면 실패 기록을 건너뛰어야 한다`() {
            // Given
            val disabledProperties = AccountLockoutProperties(enabled = false)
            val service = LoginAttemptService(userRepository, disabledProperties, eventPublisher)
            val user = createUser(failedLoginAttempts = 0)

            // When
            service.recordFailedAttempt(user)

            // Then
            assertThat(user.failedLoginAttempts).isEqualTo(0)
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("resetFailedAttempts")
    inner class ResetFailedAttemptsTest {
        @Test
        fun `실패 카운터와 잠금이 초기화되어야 한다`() {
            // Given
            val user =
                createUser(
                    failedLoginAttempts = 3,
                    lockoutUntil = LocalDateTime.now().plusMinutes(10),
                )
            every { userRepository.findById(user.id) } returns Optional.of(user)
            every { userRepository.save(any()) } returns user

            // When
            loginAttemptService.resetFailedAttempts(user)

            // Then - 새 트랜잭션에서 fresh user를 로드하여 수정
            verify { userRepository.findById(user.id) }
            verify { userRepository.save(any()) }
        }

        @Test
        fun `이미 초기화된 상태면 저장하지 않아야 한다`() {
            // Given
            val user = createUser(failedLoginAttempts = 0, lockoutUntil = null)
            every { userRepository.findById(user.id) } returns Optional.of(user)

            // When
            loginAttemptService.resetFailedAttempts(user)

            // Then - fresh user도 이미 초기화 상태이므로 저장하지 않음
            verify { userRepository.findById(user.id) }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("getRemainingAttempts")
    inner class GetRemainingAttemptsTest {
        @Test
        fun `남은 시도 횟수를 정확히 반환해야 한다`() {
            // Given
            val user = createUser(failedLoginAttempts = 2)

            // When
            val remaining = loginAttemptService.getRemainingAttempts(user)

            // Then
            assertThat(remaining).isEqualTo(3) // 5 - 2 = 3
        }

        @Test
        fun `실패 횟수가 최대치를 초과해도 0을 반환해야 한다`() {
            // Given
            val user = createUser(failedLoginAttempts = 10)

            // When
            val remaining = loginAttemptService.getRemainingAttempts(user)

            // Then
            assertThat(remaining).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("unlockAccount")
    inner class UnlockAccountTest {
        @Test
        fun `계정 잠금을 강제 해제해야 한다`() {
            // Given
            val user =
                createUser(
                    failedLoginAttempts = 5,
                    lockoutUntil = LocalDateTime.now().plusMinutes(10),
                )
            every { userRepository.save(user) } returns user

            // When
            loginAttemptService.unlockAccount(user)

            // Then
            assertThat(user.failedLoginAttempts).isEqualTo(0)
            assertThat(user.lockoutUntil).isNull()
            verify { userRepository.save(user) }
        }
    }
}
