package com.starter.storage.db.user

import com.starter.storage.db.RepositoryTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("UserRepository")
class UserRepositoryTest : RepositoryTestSupport() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var socialAccountRepository: SocialAccountRepository

    private lateinit var testUser: UserEntity

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        testUser =
            userRepository.save(
                UserEntity(
                    email = "test@example.com",
                    password = "encodedPassword123",
                    nickname = "테스트유저",
                ),
            )
    }

    @Nested
    @DisplayName("findByEmail")
    inner class FindByEmailTest {
        @Test
        fun `이메일로 사용자를 찾을 수 있다`() {
            // When
            val found = userRepository.findByEmail("test@example.com")

            // Then
            assertThat(found).isNotNull
            assertThat(found?.email).isEqualTo("test@example.com")
            assertThat(found?.nickname).isEqualTo("테스트유저")
        }

        @Test
        fun `존재하지 않는 이메일로 조회하면 null을 반환한다`() {
            // When
            val found = userRepository.findByEmail("nonexistent@example.com")

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    inner class ExistsByEmailTest {
        @Test
        fun `존재하는 이메일은 true를 반환한다`() {
            // When
            val exists = userRepository.existsByEmail("test@example.com")

            // Then
            assertThat(exists).isTrue()
        }

        @Test
        fun `존재하지 않는 이메일은 false를 반환한다`() {
            // When
            val exists = userRepository.existsByEmail("nonexistent@example.com")

            // Then
            assertThat(exists).isFalse()
        }
    }

    @Nested
    @DisplayName("findByEmailAndStatus")
    inner class FindByEmailAndStatusTest {
        @Test
        fun `활성 상태의 사용자를 찾을 수 있다`() {
            // When
            val found = userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.status).isEqualTo(UserStatus.ACTIVE)
        }

        @Test
        fun `비활성 상태의 사용자는 찾지 않는다`() {
            // Given
            testUser.deactivate()
            userRepository.save(testUser)

            // When
            val found = userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE)

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `정지된 상태로 조회하면 정지된 사용자만 찾는다`() {
            // Given
            testUser.suspend()
            userRepository.save(testUser)

            // When
            val foundSuspended = userRepository.findByEmailAndStatus("test@example.com", UserStatus.SUSPENDED)
            val foundActive = userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE)

            // Then
            assertThat(foundSuspended).isNotNull
            assertThat(foundActive).isNull()
        }
    }

    @Nested
    @DisplayName("findByIdWithSocialAccounts")
    inner class FindByIdWithSocialAccountsTest {
        @Test
        fun `사용자와 소셜 계정을 함께 조회할 수 있다`() {
            // Given
            val socialAccount =
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                )
            testUser.addSocialAccount(socialAccount)
            userRepository.saveAndFlush(testUser)

            // When
            val found = userRepository.findByIdWithSocialAccounts(testUser.id)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.socialAccounts).hasSize(1)
            assertThat(found?.socialAccounts?.first()?.provider).isEqualTo(AuthProvider.GOOGLE)
        }

        @Test
        fun `소셜 계정이 없는 사용자도 조회할 수 있다`() {
            // When
            val found = userRepository.findByIdWithSocialAccounts(testUser.id)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.socialAccounts).isEmpty()
        }

        @Test
        fun `존재하지 않는 ID로 조회하면 null을 반환한다`() {
            // When
            val found = userRepository.findByIdWithSocialAccounts(999999L)

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("계정 잠금 필드")
    inner class AccountLockFieldsTest {
        @Test
        fun `실패한 로그인 시도 횟수를 업데이트할 수 있다`() {
            // Given
            testUser.failedLoginAttempts = 5
            userRepository.save(testUser)

            // When
            val found = userRepository.findById(testUser.id).orElseThrow()

            // Then
            assertThat(found.failedLoginAttempts).isEqualTo(5)
        }

        @Test
        fun `계정 잠금 시간을 설정할 수 있다`() {
            // Given
            val lockoutTime =
                java.time.LocalDateTime
                    .now()
                    .plusMinutes(15)
            testUser.lockoutUntil = lockoutTime
            userRepository.save(testUser)

            // When
            val found = userRepository.findById(testUser.id).orElseThrow()

            // Then
            assertThat(found.lockoutUntil).isNotNull
            assertThat(found.lockoutUntil).isEqualToIgnoringNanos(lockoutTime)
        }
    }
}
