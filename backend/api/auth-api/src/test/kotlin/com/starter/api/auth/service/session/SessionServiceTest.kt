package com.starter.api.auth.service.session

import com.starter.api.auth.config.SessionProperties
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenEntity
import com.starter.storage.db.user.RefreshTokenRepository
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

@DisplayName("SessionService 테스트")
class SessionServiceTest {
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var userRepository: UserRepository
    private lateinit var sessionProperties: SessionProperties
    private lateinit var sessionService: SessionService

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        sessionProperties = SessionProperties(maxSessionsPerUser = 3)
        sessionService = SessionService(refreshTokenRepository, userRepository, sessionProperties)
    }

    private fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
    ): UserEntity =
        UserEntity(
            email = email,
            password = "password",
            nickname = "테스트유저",
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }

    private fun createRefreshToken(
        id: Long,
        user: UserEntity,
        token: String = "token-$id",
        revoked: Boolean = false,
    ): RefreshTokenEntity =
        RefreshTokenEntity(
            user = user,
            token = token,
            expiresAt = LocalDateTime.now().plusDays(7),
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
            ipAddress = "192.168.1.1",
        ).apply {
            val idField = this::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
            if (revoked) {
                this.revoke()
            }
        }

    @Nested
    @DisplayName("enforceSessionLimit")
    inner class EnforceSessionLimitTest {
        @Test
        fun `세션 제한 미만인 경우 아무 동작도 하지 않아야 한다`() {
            // Given
            val user = createUser()
            every { refreshTokenRepository.countActiveSessionsByUser(user) } returns 2L

            // When
            sessionService.enforceSessionLimit(user)

            // Then
            verify(exactly = 0) { refreshTokenRepository.findOldestActiveSessionByUser(user) }
        }

        @Test
        fun `세션 제한 초과 시 가장 오래된 세션을 만료시켜야 한다`() {
            // Given
            val user = createUser()
            val oldSession = createRefreshToken(1L, user)

            every { refreshTokenRepository.countActiveSessionsByUser(user) } returns 3L
            every { refreshTokenRepository.findOldestActiveSessionByUser(user) } returns oldSession

            // When
            sessionService.enforceSessionLimit(user)

            // Then
            verify(exactly = 1) { refreshTokenRepository.findOldestActiveSessionByUser(user) }
            assertThat(oldSession.revoked).isTrue()
        }

        @Test
        fun `세션 제한을 많이 초과한 경우 여러 세션을 만료시켜야 한다`() {
            // Given
            val user = createUser()
            val oldSession1 = createRefreshToken(1L, user, "token-1")
            val oldSession2 = createRefreshToken(2L, user, "token-2")

            every { refreshTokenRepository.countActiveSessionsByUser(user) } returns 5L
            every { refreshTokenRepository.findOldestActiveSessionByUser(user) } returnsMany
                listOf(oldSession1, oldSession2, null)

            // When
            sessionService.enforceSessionLimit(user)

            // Then
            verify(exactly = 3) { refreshTokenRepository.findOldestActiveSessionByUser(user) }
        }
    }

    @Nested
    @DisplayName("getActiveSessions")
    inner class GetActiveSessionsTest {
        @Test
        fun `활성 세션 목록을 반환해야 한다`() {
            // Given
            val user = createUser()
            val session1 = createRefreshToken(1L, user, "token-1")
            val session2 = createRefreshToken(2L, user, "token-2")

            every { userRepository.findById(1L) } returns Optional.of(user)
            every { refreshTokenRepository.findActiveSessionsByUser(user) } returns listOf(session1, session2)

            // When
            val result = sessionService.getActiveSessions(1L)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo(1L)
            assertThat(result[1].id).isEqualTo(2L)
        }

        @Test
        fun `사용자를 찾을 수 없으면 예외가 발생해야 한다`() {
            // Given
            every { userRepository.findById(1L) } returns Optional.empty()

            // When & Then
            assertThatThrownBy { sessionService.getActiveSessions(1L) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("revokeSession")
    inner class RevokeSessionTest {
        @Test
        fun `특정 세션을 만료시켜야 한다`() {
            // Given
            val user = createUser()
            val session = createRefreshToken(1L, user)

            every { userRepository.findById(1L) } returns Optional.of(user)
            every { refreshTokenRepository.findByIdAndUser(1L, user) } returns session

            // When
            val result = sessionService.revokeSession(1L, 1L)

            // Then
            assertThat(result).isTrue()
            assertThat(session.revoked).isTrue()
        }

        @Test
        fun `세션을 찾을 수 없으면 예외가 발생해야 한다`() {
            // Given
            val user = createUser()
            every { userRepository.findById(1L) } returns Optional.of(user)
            every { refreshTokenRepository.findByIdAndUser(1L, user) } returns null

            // When & Then
            assertThatThrownBy { sessionService.revokeSession(1L, 1L) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
        }

        @Test
        fun `이미 만료된 세션을 다시 만료시키면 예외가 발생해야 한다`() {
            // Given
            val user = createUser()
            val session = createRefreshToken(1L, user, revoked = true)

            every { userRepository.findById(1L) } returns Optional.of(user)
            every { refreshTokenRepository.findByIdAndUser(1L, user) } returns session

            // When & Then
            assertThatThrownBy { sessionService.revokeSession(1L, 1L) }
                .isInstanceOf(CoreApiException::class.java)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("revokeAllOtherSessions")
    inner class RevokeAllOtherSessionsTest {
        @Test
        fun `현재 세션을 제외한 모든 세션을 만료시켜야 한다`() {
            // Given
            val user = createUser()
            val currentSession = createRefreshToken(1L, user, "current-token")
            val otherSession1 = createRefreshToken(2L, user, "other-token-1")
            val otherSession2 = createRefreshToken(3L, user, "other-token-2")

            every { userRepository.findById(1L) } returns Optional.of(user)
            every {
                refreshTokenRepository.findActiveSessionsByUser(user)
            } returns listOf(currentSession, otherSession1, otherSession2)

            // When
            val result = sessionService.revokeAllOtherSessions(1L, "current-token")

            // Then
            assertThat(result).isEqualTo(2)
            assertThat(currentSession.revoked).isFalse()
            assertThat(otherSession1.revoked).isTrue()
            assertThat(otherSession2.revoked).isTrue()
        }

        @Test
        fun `다른 세션이 없으면 0을 반환해야 한다`() {
            // Given
            val user = createUser()
            val currentSession = createRefreshToken(1L, user, "current-token")

            every { userRepository.findById(1L) } returns Optional.of(user)
            every {
                refreshTokenRepository.findActiveSessionsByUser(user)
            } returns listOf(currentSession)

            // When
            val result = sessionService.revokeAllOtherSessions(1L, "current-token")

            // Then
            assertThat(result).isEqualTo(0)
            assertThat(currentSession.revoked).isFalse()
        }
    }
}
