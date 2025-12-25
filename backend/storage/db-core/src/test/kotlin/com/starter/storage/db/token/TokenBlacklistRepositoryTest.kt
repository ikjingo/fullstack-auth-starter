package com.starter.storage.db.token

import com.starter.storage.db.RepositoryTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@DisplayName("TokenBlacklistRepository")
class TokenBlacklistRepositoryTest : RepositoryTestSupport() {
    @Autowired
    private lateinit var tokenBlacklistRepository: TokenBlacklistRepository

    @BeforeEach
    fun setUp() {
        tokenBlacklistRepository.deleteAll()
    }

    @Nested
    @DisplayName("existsByToken")
    inner class ExistsByTokenTest {
        @Test
        fun `블랙리스트에 있는 토큰은 true를 반환한다`() {
            // Given
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "blacklisted-token-123",
                    expiresAt = LocalDateTime.now().plusDays(1),
                ),
            )

            // When
            val exists = tokenBlacklistRepository.existsByToken("blacklisted-token-123")

            // Then
            assertThat(exists).isTrue()
        }

        @Test
        fun `블랙리스트에 없는 토큰은 false를 반환한다`() {
            // When
            val exists = tokenBlacklistRepository.existsByToken("nonexistent-token")

            // Then
            assertThat(exists).isFalse()
        }

        @Test
        fun `만료된 토큰도 블랙리스트에 있으면 true를 반환한다`() {
            // Given
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "expired-blacklisted-token",
                    expiresAt = LocalDateTime.now().minusHours(1),
                ),
            )

            // When
            val exists = tokenBlacklistRepository.existsByToken("expired-blacklisted-token")

            // Then
            assertThat(exists).isTrue()
        }
    }

    @Nested
    @DisplayName("deleteExpiredTokens")
    inner class DeleteExpiredTokensTest {
        @Test
        fun `만료된 토큰을 삭제할 수 있다`() {
            // Given
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "expired-token-1",
                    expiresAt = LocalDateTime.now().minusHours(1),
                ),
            )
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "expired-token-2",
                    expiresAt = LocalDateTime.now().minusDays(1),
                ),
            )
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(1),
                ),
            )

            // When
            val deletedCount = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now())

            // Then
            assertThat(deletedCount).isEqualTo(2)

            val remainingTokens = tokenBlacklistRepository.findAll()
            assertThat(remainingTokens).hasSize(1)
            assertThat(remainingTokens.first().token).isEqualTo("valid-token")
        }

        @Test
        fun `만료된 토큰이 없으면 0을 반환한다`() {
            // Given
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(1),
                ),
            )

            // When
            val deletedCount = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now())

            // Then
            assertThat(deletedCount).isEqualTo(0)
        }

        @Test
        fun `특정 시점 기준으로 만료된 토큰을 삭제한다`() {
            // Given
            val futureTime = LocalDateTime.now().plusHours(2)
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "token-expires-in-1-hour",
                    expiresAt = LocalDateTime.now().plusHours(1),
                ),
            )
            tokenBlacklistRepository.save(
                TokenBlacklistEntity(
                    token = "token-expires-in-3-hours",
                    expiresAt = LocalDateTime.now().plusHours(3),
                ),
            )

            // When
            val deletedCount = tokenBlacklistRepository.deleteExpiredTokens(futureTime)

            // Then
            assertThat(deletedCount).isEqualTo(1)

            val remaining = tokenBlacklistRepository.findAll()
            assertThat(remaining).hasSize(1)
            assertThat(remaining.first().token).isEqualTo("token-expires-in-3-hours")
        }
    }

    @Nested
    @DisplayName("TokenBlacklistEntity 유효성 검증")
    inner class EntityValidationTest {
        @Test
        fun `유효한 토큰의 isExpired는 false를 반환한다`() {
            // Given
            val entity =
                TokenBlacklistEntity(
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(1),
                )

            // Then
            assertThat(entity.isExpired()).isFalse()
        }

        @Test
        fun `만료된 토큰의 isExpired는 true를 반환한다`() {
            // Given
            val entity =
                TokenBlacklistEntity(
                    token = "expired-token",
                    expiresAt = LocalDateTime.now().minusMinutes(1),
                )

            // Then
            assertThat(entity.isExpired()).isTrue()
        }
    }

    @Nested
    @DisplayName("토큰 저장 및 조회")
    inner class SaveAndFindTest {
        @Test
        fun `토큰을 저장하고 조회할 수 있다`() {
            // Given
            val expiresAt = LocalDateTime.now().plusDays(1)
            val entity =
                tokenBlacklistRepository.save(
                    TokenBlacklistEntity(
                        token = "test-token",
                        expiresAt = expiresAt,
                    ),
                )

            // When
            val found = tokenBlacklistRepository.findById(entity.id)

            // Then
            assertThat(found).isPresent
            assertThat(found.get().token).isEqualTo("test-token")
            assertThat(found.get().expiresAt).isEqualToIgnoringNanos(expiresAt)
        }
    }
}
