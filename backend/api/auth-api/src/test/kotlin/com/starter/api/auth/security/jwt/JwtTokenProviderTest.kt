package com.starter.api.auth.security.jwt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {
    private lateinit var jwtProperties: JwtProperties
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        jwtProperties =
            JwtProperties(
                secret = "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256",
                accessTokenExpiration = 3600000L, // 1시간
                refreshTokenExpiration = 604800000L, // 7일
                issuer = "test-issuer",
            )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
    }

    @Nested
    @DisplayName("createAccessToken 메서드")
    inner class CreateAccessTokenTest {
        @Test
        fun `액세스 토큰을 생성해야 한다`() {
            // Given
            val userId = 1L
            val email = "test@example.com"
            val roles = listOf("USER")

            // When
            val token = jwtTokenProvider.createAccessToken(userId, email, roles)

            // Then
            assertThat(token).isNotBlank()
            assertThat(jwtTokenProvider.validateToken(token)).isTrue()
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId)
            assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email)
            assertThat(jwtTokenProvider.getRoles(token)).containsExactly("USER")
            assertThat(jwtTokenProvider.isAccessToken(token)).isTrue()
        }

        @Test
        fun `다중 역할로 액세스 토큰을 생성할 수 있어야 한다`() {
            // Given
            val userId = 1L
            val email = "admin@example.com"
            val roles = listOf("USER", "ADMIN")

            // When
            val token = jwtTokenProvider.createAccessToken(userId, email, roles)

            // Then
            assertThat(jwtTokenProvider.getRoles(token)).containsExactly("USER", "ADMIN")
        }
    }

    @Nested
    @DisplayName("createRefreshToken 메서드")
    inner class CreateRefreshTokenTest {
        @Test
        fun `리프레시 토큰을 생성해야 한다`() {
            // Given
            val userId = 1L
            val email = "test@example.com"

            // When
            val token = jwtTokenProvider.createRefreshToken(userId, email)

            // Then
            assertThat(token).isNotBlank()
            assertThat(jwtTokenProvider.validateToken(token)).isTrue()
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId)
            assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue()
            assertThat(jwtTokenProvider.getRoles(token)).isEmpty()
        }
    }

    @Nested
    @DisplayName("validateToken 메서드")
    inner class ValidateTokenTest {
        @Test
        fun `유효한 토큰은 true를 반환해야 한다`() {
            // Given
            val token = jwtTokenProvider.createAccessToken(1L, "test@example.com", listOf("USER"))

            // When
            val isValid = jwtTokenProvider.validateToken(token)

            // Then
            assertThat(isValid).isTrue()
        }

        @Test
        fun `잘못된 형식의 토큰은 false를 반환해야 한다`() {
            // Given
            val invalidToken = "invalid.token.format"

            // When
            val isValid = jwtTokenProvider.validateToken(invalidToken)

            // Then
            assertThat(isValid).isFalse()
        }

        @Test
        fun `빈 토큰은 false를 반환해야 한다`() {
            // Given
            val emptyToken = ""

            // When
            val isValid = jwtTokenProvider.validateToken(emptyToken)

            // Then
            assertThat(isValid).isFalse()
        }

        @Test
        fun `다른 시크릿으로 생성된 토큰은 false를 반환해야 한다`() {
            // Given
            val otherProperties =
                JwtProperties(
                    secret = "another-secret-key-that-is-at-least-32-characters-long",
                    accessTokenExpiration = 3600000L,
                    refreshTokenExpiration = 604800000L,
                    issuer = "other-issuer",
                )
            val otherProvider = JwtTokenProvider(otherProperties)
            val tokenFromOther = otherProvider.createAccessToken(1L, "test@example.com", listOf("USER"))

            // When
            val isValid = jwtTokenProvider.validateToken(tokenFromOther)

            // Then
            assertThat(isValid).isFalse()
        }
    }

    @Nested
    @DisplayName("getTokenType 메서드")
    inner class GetTokenTypeTest {
        @Test
        fun `액세스 토큰의 타입은 ACCESS여야 한다`() {
            // Given
            val token = jwtTokenProvider.createAccessToken(1L, "test@example.com", listOf("USER"))

            // When
            val tokenType = jwtTokenProvider.getTokenType(token)

            // Then
            assertThat(tokenType).isEqualTo(JwtTokenProvider.TokenType.ACCESS)
        }

        @Test
        fun `리프레시 토큰의 타입은 REFRESH여야 한다`() {
            // Given
            val token = jwtTokenProvider.createRefreshToken(1L, "test@example.com")

            // When
            val tokenType = jwtTokenProvider.getTokenType(token)

            // Then
            assertThat(tokenType).isEqualTo(JwtTokenProvider.TokenType.REFRESH)
        }
    }

    @Nested
    @DisplayName("isAccessToken / isRefreshToken 메서드")
    inner class TokenTypeCheckTest {
        @Test
        fun `액세스 토큰에 대해 isAccessToken은 true, isRefreshToken은 false를 반환해야 한다`() {
            // Given
            val accessToken = jwtTokenProvider.createAccessToken(1L, "test@example.com", listOf("USER"))

            // When & Then
            assertThat(jwtTokenProvider.isAccessToken(accessToken)).isTrue()
            assertThat(jwtTokenProvider.isRefreshToken(accessToken)).isFalse()
        }

        @Test
        fun `리프레시 토큰에 대해 isAccessToken은 false, isRefreshToken은 true를 반환해야 한다`() {
            // Given
            val refreshToken = jwtTokenProvider.createRefreshToken(1L, "test@example.com")

            // When & Then
            assertThat(jwtTokenProvider.isAccessToken(refreshToken)).isFalse()
            assertThat(jwtTokenProvider.isRefreshToken(refreshToken)).isTrue()
        }
    }

    @Nested
    @DisplayName("getUserId 메서드")
    inner class GetUserIdTest {
        @Test
        fun `토큰에서 사용자 ID를 추출해야 한다`() {
            // Given
            val userId = 123L
            val token = jwtTokenProvider.createAccessToken(userId, "test@example.com", listOf("USER"))

            // When
            val extractedUserId = jwtTokenProvider.getUserId(token)

            // Then
            assertThat(extractedUserId).isEqualTo(userId)
        }
    }

    @Nested
    @DisplayName("getEmail 메서드")
    inner class GetEmailTest {
        @Test
        fun `토큰에서 이메일을 추출해야 한다`() {
            // Given
            val email = "user@example.com"
            val token = jwtTokenProvider.createAccessToken(1L, email, listOf("USER"))

            // When
            val extractedEmail = jwtTokenProvider.getEmail(token)

            // Then
            assertThat(extractedEmail).isEqualTo(email)
        }
    }
}
