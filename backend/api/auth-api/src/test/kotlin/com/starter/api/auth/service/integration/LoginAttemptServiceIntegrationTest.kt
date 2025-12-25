package com.starter.api.auth.service.integration

import com.starter.api.auth.config.AccountLockoutProperties
import com.starter.api.auth.service.auth.LoginAttemptService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "security.account-lockout.enabled=true",
        "security.account-lockout.max-failed-attempts=3",
        "security.account-lockout.lock-duration-minutes=15",
    ],
)
@DisplayName("LoginAttemptService 통합 테스트")
class LoginAttemptServiceIntegrationTest {
    @Autowired
    private lateinit var loginAttemptService: LoginAttemptService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var lockoutProperties: AccountLockoutProperties

    private lateinit var testUser: UserEntity

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        testUser =
            userRepository.save(
                UserEntity(
                    email = "test@example.com",
                    password = "encodedPassword",
                    nickname = "테스트유저",
                ),
            )
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Nested
    @DisplayName("로그인 시도 실패 기록")
    inner class RecordFailedAttemptTest {
        @Test
        fun `실패 횟수가 증가해야 한다`() {
            // When
            loginAttemptService.recordFailedAttempt(testUser)

            // Then
            val updatedUser = userRepository.findById(testUser.id).orElseThrow()
            assertThat(updatedUser.failedLoginAttempts).isEqualTo(1)
        }

        @Test
        fun `최대 실패 횟수 도달 시 계정이 잠겨야 한다`() {
            // Given - 최대 실패 횟수 - 1 만큼 실패 기록
            repeat(lockoutProperties.maxFailedAttempts - 1) {
                loginAttemptService.recordFailedAttempt(testUser)
            }

            // When - 마지막 실패 기록
            loginAttemptService.recordFailedAttempt(testUser)

            // Then
            val updatedUser = userRepository.findById(testUser.id).orElseThrow()
            assertThat(updatedUser.failedLoginAttempts).isEqualTo(lockoutProperties.maxFailedAttempts)
            assertThat(updatedUser.lockoutUntil).isNotNull
            assertThat(updatedUser.lockoutUntil).isAfter(LocalDateTime.now())
        }
    }

    @Nested
    @DisplayName("계정 잠금 확인")
    inner class CheckAccountLockedTest {
        @Test
        fun `잠기지 않은 계정은 예외가 발생하지 않아야 한다`() {
            // When & Then - 예외 없이 통과
            loginAttemptService.checkAccountLocked(testUser)
        }

        @Test
        fun `잠긴 계정은 예외가 발생해야 한다`() {
            // Given
            testUser.lockoutUntil = LocalDateTime.now().plusMinutes(15)
            userRepository.save(testUser)

            // When & Then
            assertThatThrownBy {
                loginAttemptService.checkAccountLocked(testUser)
            }.isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_LOCKED)
        }

        @Test
        fun `잠금 시간이 지난 계정은 자동으로 잠금 해제되어야 한다`() {
            // Given - 잠금 시간이 과거인 경우
            testUser.lockoutUntil = LocalDateTime.now().minusMinutes(1)
            testUser.failedLoginAttempts = 5
            userRepository.save(testUser)

            // When
            loginAttemptService.checkAccountLocked(testUser)

            // Then - 자동 해제됨
            val updatedUser = userRepository.findById(testUser.id).orElseThrow()
            assertThat(updatedUser.lockoutUntil).isNull()
            assertThat(updatedUser.failedLoginAttempts).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("로그인 성공 시 초기화")
    inner class ResetFailedAttemptsTest {
        @Test
        fun `실패 횟수가 초기화되어야 한다`() {
            // Given
            testUser.failedLoginAttempts = 3
            userRepository.save(testUser)

            // When
            loginAttemptService.resetFailedAttempts(testUser)

            // Then
            val updatedUser = userRepository.findById(testUser.id).orElseThrow()
            assertThat(updatedUser.failedLoginAttempts).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("남은 시도 횟수 조회")
    inner class GetRemainingAttemptsTest {
        @Test
        fun `남은 시도 횟수를 정확히 반환해야 한다`() {
            // Given
            testUser.failedLoginAttempts = 2
            userRepository.save(testUser)

            // When
            val remaining = loginAttemptService.getRemainingAttempts(testUser)

            // Then
            assertThat(remaining).isEqualTo(lockoutProperties.maxFailedAttempts - 2)
        }
    }

    @Nested
    @DisplayName("계정 잠금 해제")
    inner class UnlockAccountTest {
        @Test
        fun `잠금 해제 시 모든 필드가 초기화되어야 한다`() {
            // Given
            testUser.lockoutUntil = LocalDateTime.now().plusMinutes(15)
            testUser.failedLoginAttempts = 5
            val savedUser = userRepository.save(testUser)

            // When - 최신 엔티티 사용
            loginAttemptService.unlockAccount(savedUser)

            // Then
            val updatedUser = userRepository.findById(testUser.id).orElseThrow()
            assertThat(updatedUser.lockoutUntil).isNull()
            assertThat(updatedUser.failedLoginAttempts).isEqualTo(0)
        }
    }
}
