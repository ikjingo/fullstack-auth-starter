package com.starter.storage.db.user

import com.starter.storage.db.RepositoryTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@DisplayName("RefreshTokenRepository")
class RefreshTokenRepositoryTest : RepositoryTestSupport() {
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: UserEntity

    @BeforeEach
    fun setUp() {
        refreshTokenRepository.deleteAll()
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
    @DisplayName("findByToken")
    inner class FindByTokenTest {
        @Test
        fun `토큰으로 리프레시 토큰을 찾을 수 있다`() {
            // Given
            val token = "test-refresh-token-123"
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = token,
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val found = refreshTokenRepository.findByToken(token)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.token).isEqualTo(token)
            assertThat(found?.user?.id).isEqualTo(testUser.id)
        }

        @Test
        fun `존재하지 않는 토큰으로 조회하면 null을 반환한다`() {
            // When
            val found = refreshTokenRepository.findByToken("nonexistent-token")

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findByUserAndRevokedFalse")
    inner class FindByUserAndRevokedFalseTest {
        @Test
        fun `사용자의 유효한 토큰 목록을 조회할 수 있다`() {
            // Given
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token-1",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token-2",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val tokens = refreshTokenRepository.findByUserAndRevokedFalse(testUser)

            // Then
            assertThat(tokens).hasSize(2)
        }

        @Test
        fun `폐기된 토큰은 조회되지 않는다`() {
            // Given
            val revokedToken =
                RefreshTokenEntity(
                    user = testUser,
                    token = "revoked-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                )
            revokedToken.revoke()
            refreshTokenRepository.save(revokedToken)

            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val tokens = refreshTokenRepository.findByUserAndRevokedFalse(testUser)

            // Then
            assertThat(tokens).hasSize(1)
            assertThat(tokens.first().token).isEqualTo("valid-token")
        }
    }

    @Nested
    @DisplayName("revokeAllByUser")
    inner class RevokeAllByUserTest {
        @Test
        fun `사용자의 모든 토큰을 폐기할 수 있다`() {
            // Given
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "token-1",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "token-2",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val revokedCount = refreshTokenRepository.revokeAllByUser(testUser)

            // Then
            assertThat(revokedCount).isEqualTo(2)

            val validTokens = refreshTokenRepository.findByUserAndRevokedFalse(testUser)
            assertThat(validTokens).isEmpty()
        }

        @Test
        fun `이미 폐기된 토큰도 포함하여 모든 토큰이 업데이트된다`() {
            // Given
            val revokedToken =
                RefreshTokenEntity(
                    user = testUser,
                    token = "already-revoked",
                    expiresAt = LocalDateTime.now().plusDays(7),
                )
            revokedToken.revoke()
            refreshTokenRepository.save(revokedToken)

            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val revokedCount = refreshTokenRepository.revokeAllByUser(testUser)

            // Then
            // 구현은 이미 폐기된 토큰도 포함하여 모든 토큰을 업데이트함
            assertThat(revokedCount).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("deleteExpiredTokens")
    inner class DeleteExpiredTokensTest {
        @Test
        fun `만료된 토큰을 삭제할 수 있다`() {
            // Given
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "expired-token",
                    expiresAt = LocalDateTime.now().minusDays(1),
                ),
            )
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val deletedCount = refreshTokenRepository.deleteExpiredTokens()

            // Then
            assertThat(deletedCount).isEqualTo(1)

            val allTokens = refreshTokenRepository.findAll()
            assertThat(allTokens).hasSize(1)
            assertThat(allTokens.first().token).isEqualTo("valid-token")
        }

        @Test
        fun `만료된 토큰이 없으면 0을 반환한다`() {
            // Given
            refreshTokenRepository.save(
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                ),
            )

            // When
            val deletedCount = refreshTokenRepository.deleteExpiredTokens()

            // Then
            assertThat(deletedCount).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("RefreshTokenEntity 유효성 검증")
    inner class TokenValidationTest {
        @Test
        fun `유효한 토큰의 isValid는 true를 반환한다`() {
            // Given
            val token =
                RefreshTokenEntity(
                    user = testUser,
                    token = "valid-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                )

            // Then
            assertThat(token.isValid()).isTrue()
            assertThat(token.isExpired()).isFalse()
        }

        @Test
        fun `만료된 토큰의 isValid는 false를 반환한다`() {
            // Given
            val token =
                RefreshTokenEntity(
                    user = testUser,
                    token = "expired-token",
                    expiresAt = LocalDateTime.now().minusHours(1),
                )

            // Then
            assertThat(token.isValid()).isFalse()
            assertThat(token.isExpired()).isTrue()
        }

        @Test
        fun `폐기된 토큰의 isValid는 false를 반환한다`() {
            // Given
            val token =
                RefreshTokenEntity(
                    user = testUser,
                    token = "revoked-token",
                    expiresAt = LocalDateTime.now().plusDays(7),
                )
            token.revoke()

            // Then
            assertThat(token.isValid()).isFalse()
            assertThat(token.revoked).isTrue()
        }
    }
}
