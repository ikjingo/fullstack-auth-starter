package com.starter.api.auth.service.token

import com.starter.storage.db.token.TokenBlacklistEntity
import com.starter.storage.db.token.TokenBlacklistRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("TokenBlacklistService 테스트")
class TokenBlacklistServiceTest {
    private lateinit var tokenBlacklistRepository: TokenBlacklistRepository
    private lateinit var tokenBlacklistService: TokenBlacklistService

    @BeforeEach
    fun setUp() {
        tokenBlacklistRepository = mockk(relaxed = true)
        tokenBlacklistService = TokenBlacklistService(tokenBlacklistRepository)
    }

    @Nested
    @DisplayName("blacklistToken")
    inner class BlacklistTokenTest {
        @Test
        fun `새로운 토큰을 블랙리스트에 추가해야 한다`() {
            // Given
            val token = "test-jwt-token"
            val expiresAt = LocalDateTime.now().plusHours(1)
            every { tokenBlacklistRepository.existsByToken(token) } returns false

            val entitySlot = slot<TokenBlacklistEntity>()
            every { tokenBlacklistRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            tokenBlacklistService.blacklistToken(token, expiresAt)

            // Then
            verify(exactly = 1) { tokenBlacklistRepository.save(any()) }
            assertThat(entitySlot.captured.token).isEqualTo(token)
            assertThat(entitySlot.captured.expiresAt).isEqualTo(expiresAt)
        }

        @Test
        fun `이미 블랙리스트에 있는 토큰은 다시 추가하지 않아야 한다`() {
            // Given
            val token = "existing-token"
            val expiresAt = LocalDateTime.now().plusHours(1)
            every { tokenBlacklistRepository.existsByToken(token) } returns true

            // When
            tokenBlacklistService.blacklistToken(token, expiresAt)

            // Then
            verify(exactly = 0) { tokenBlacklistRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("isBlacklisted")
    inner class IsBlacklistedTest {
        @Test
        fun `블랙리스트에 있는 토큰은 true를 반환해야 한다`() {
            // Given
            val token = "blacklisted-token"
            every { tokenBlacklistRepository.existsByToken(token) } returns true

            // When
            val result = tokenBlacklistService.isBlacklisted(token)

            // Then
            assertThat(result).isTrue()
        }

        @Test
        fun `블랙리스트에 없는 토큰은 false를 반환해야 한다`() {
            // Given
            val token = "valid-token"
            every { tokenBlacklistRepository.existsByToken(token) } returns false

            // When
            val result = tokenBlacklistService.isBlacklisted(token)

            // Then
            assertThat(result).isFalse()
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens")
    inner class CleanupExpiredTokensTest {
        @Test
        fun `만료된 토큰을 삭제해야 한다`() {
            // Given
            every { tokenBlacklistRepository.deleteExpiredTokens(any()) } returns 5

            // When
            tokenBlacklistService.cleanupExpiredTokens()

            // Then
            verify(exactly = 1) { tokenBlacklistRepository.deleteExpiredTokens(any()) }
        }

        @Test
        fun `삭제할 토큰이 없어도 정상 동작해야 한다`() {
            // Given
            every { tokenBlacklistRepository.deleteExpiredTokens(any()) } returns 0

            // When
            tokenBlacklistService.cleanupExpiredTokens()

            // Then
            verify(exactly = 1) { tokenBlacklistRepository.deleteExpiredTokens(any()) }
        }
    }
}
