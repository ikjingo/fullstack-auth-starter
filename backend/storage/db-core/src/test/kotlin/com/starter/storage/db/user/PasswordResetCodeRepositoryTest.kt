package com.starter.storage.db.user

import com.starter.storage.db.RepositoryTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@DisplayName("PasswordResetCodeRepository")
class PasswordResetCodeRepositoryTest : RepositoryTestSupport() {
    @Autowired
    private lateinit var passwordResetCodeRepository: PasswordResetCodeRepository

    @BeforeEach
    fun setUp() {
        passwordResetCodeRepository.deleteAll()
    }

    private fun createResetCode(
        email: String = "test@example.com",
        code: String = "123456",
        expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(10),
        used: Boolean = false,
        verified: Boolean = false,
    ): PasswordResetCodeEntity =
        passwordResetCodeRepository.save(
            PasswordResetCodeEntity(
                email = email,
                code = code,
                expiresAt = expiresAt,
                used = used,
                verified = verified,
            ),
        )

    @Nested
    @DisplayName("findByEmailAndCodeAndUsedFalse")
    inner class FindByEmailAndCodeAndUsedFalseTest {
        @Test
        fun `이메일과 코드로 미사용 인증코드를 찾을 수 있다`() {
            // Given
            val resetCode = createResetCode()

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNotNull
            assertThat(found?.id).isEqualTo(resetCode.id)
            assertThat(found?.email).isEqualTo("test@example.com")
            assertThat(found?.code).isEqualTo("123456")
        }

        @Test
        fun `사용된 인증코드는 조회되지 않는다`() {
            // Given
            createResetCode(used = true)

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `다른 이메일의 인증코드는 조회되지 않는다`() {
            // Given
            createResetCode(email = "other@example.com")

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `다른 코드는 조회되지 않는다`() {
            // Given
            createResetCode(code = "654321")

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findByEmailAndCodeAndVerifiedTrueAndUsedFalse")
    inner class FindByEmailAndCodeAndVerifiedTrueAndUsedFalseTest {
        @Test
        fun `이메일과 코드로 검증된 미사용 인증코드를 찾을 수 있다`() {
            // Given
            val resetCode = createResetCode(verified = true)

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNotNull
            assertThat(found?.id).isEqualTo(resetCode.id)
            assertThat(found?.verified).isTrue()
        }

        @Test
        fun `검증되지 않은 인증코드는 조회되지 않는다`() {
            // Given
            createResetCode(verified = false)

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `사용된 인증코드는 조회되지 않는다`() {
            // Given
            createResetCode(verified = true, used = true)

            // When
            val found =
                passwordResetCodeRepository.findByEmailAndCodeAndVerifiedTrueAndUsedFalse(
                    "test@example.com",
                    "123456",
                )

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("invalidateAllByEmail")
    inner class InvalidateAllByEmailTest {
        @Test
        fun `이메일의 모든 미사용 인증코드를 무효화할 수 있다`() {
            // Given
            createResetCode(code = "111111")
            createResetCode(code = "222222")
            createResetCode(code = "333333")

            // When
            val invalidatedCount = passwordResetCodeRepository.invalidateAllByEmail("test@example.com")

            // Then
            assertThat(invalidatedCount).isEqualTo(3)

            // Verify all are invalidated
            val notUsed =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "test@example.com",
                    "111111",
                )
            assertThat(notUsed).isNull()
        }

        @Test
        fun `이미 사용된 인증코드는 무효화 대상에 포함되지 않는다`() {
            // Given
            createResetCode(code = "111111", used = false)
            createResetCode(code = "222222", used = true)

            // When
            val invalidatedCount = passwordResetCodeRepository.invalidateAllByEmail("test@example.com")

            // Then
            assertThat(invalidatedCount).isEqualTo(1)
        }

        @Test
        fun `다른 이메일의 인증코드는 영향받지 않는다`() {
            // Given
            createResetCode(email = "test@example.com", code = "111111")
            createResetCode(email = "other@example.com", code = "222222")

            // When
            val invalidatedCount = passwordResetCodeRepository.invalidateAllByEmail("test@example.com")

            // Then
            assertThat(invalidatedCount).isEqualTo(1)

            // Verify other email's code is not affected
            val otherCode =
                passwordResetCodeRepository.findByEmailAndCodeAndUsedFalse(
                    "other@example.com",
                    "222222",
                )
            assertThat(otherCode).isNotNull
        }

        @Test
        fun `무효화할 인증코드가 없으면 0을 반환한다`() {
            // When
            val invalidatedCount = passwordResetCodeRepository.invalidateAllByEmail("nonexistent@example.com")

            // Then
            assertThat(invalidatedCount).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("PasswordResetCodeEntity 유효성 검증")
    inner class EntityValidationTest {
        @Test
        fun `유효한 인증코드의 isValid는 true를 반환한다`() {
            // Given
            val resetCode =
                PasswordResetCodeEntity(
                    email = "test@example.com",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(10),
                    used = false,
                )

            // Then
            assertThat(resetCode.isValid()).isTrue()
            assertThat(resetCode.isExpired()).isFalse()
        }

        @Test
        fun `만료된 인증코드의 isValid는 false를 반환한다`() {
            // Given
            val resetCode =
                PasswordResetCodeEntity(
                    email = "test@example.com",
                    code = "123456",
                    expiresAt = LocalDateTime.now().minusMinutes(1),
                    used = false,
                )

            // Then
            assertThat(resetCode.isValid()).isFalse()
            assertThat(resetCode.isExpired()).isTrue()
        }

        @Test
        fun `사용된 인증코드의 isValid는 false를 반환한다`() {
            // Given
            val resetCode =
                PasswordResetCodeEntity(
                    email = "test@example.com",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(10),
                    used = true,
                )

            // Then
            assertThat(resetCode.isValid()).isFalse()
        }

        @Test
        fun `markAsUsed는 used를 true로 설정한다`() {
            // Given
            val resetCode =
                PasswordResetCodeEntity(
                    email = "test@example.com",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(10),
                )

            // When
            resetCode.markAsUsed()

            // Then
            assertThat(resetCode.used).isTrue()
        }

        @Test
        fun `markAsVerified는 verified를 true로 설정한다`() {
            // Given
            val resetCode =
                PasswordResetCodeEntity(
                    email = "test@example.com",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(10),
                )

            // When
            resetCode.markAsVerified()

            // Then
            assertThat(resetCode.verified).isTrue()
        }
    }
}
