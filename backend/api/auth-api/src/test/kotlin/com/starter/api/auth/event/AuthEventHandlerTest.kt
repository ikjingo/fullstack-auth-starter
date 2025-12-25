package com.starter.api.auth.event

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("AuthEventHandler")
class AuthEventHandlerTest {
    private lateinit var handler: AuthEventHandler

    @BeforeEach
    fun setUp() {
        handler = AuthEventHandler()
    }

    @Nested
    @DisplayName("handleUserRegistered")
    inner class HandleUserRegisteredTest {
        @Test
        fun `UserRegisteredEvent를 처리해야 한다`() {
            // Given
            val event = UserRegisteredEvent(1L, "test@example.com", "테스트")

            // When & Then - 로그만 출력하므로 예외 없이 완료되면 성공
            handler.handleUserRegistered(event)
        }

        @Test
        fun `닉네임이 null인 UserRegisteredEvent를 처리해야 한다`() {
            // Given
            val event = UserRegisteredEvent(1L, "test@example.com", null)

            // When & Then
            handler.handleUserRegistered(event)
        }
    }

    @Nested
    @DisplayName("handleLoginSuccess")
    inner class HandleLoginSuccessTest {
        @Test
        fun `LoginSuccessEvent를 처리해야 한다`() {
            // Given
            val event = LoginSuccessEvent(1L, "test@example.com", "192.168.1.1", "Mozilla/5.0")

            // When & Then
            handler.handleLoginSuccess(event)
        }

        @Test
        fun `IP와 UserAgent가 null인 LoginSuccessEvent를 처리해야 한다`() {
            // Given
            val event = LoginSuccessEvent(1L, "test@example.com", null, null)

            // When & Then
            handler.handleLoginSuccess(event)
        }
    }

    @Nested
    @DisplayName("handleLoginFailure")
    inner class HandleLoginFailureTest {
        @Test
        fun `LoginFailureEvent를 처리해야 한다`() {
            // Given
            val event = LoginFailureEvent("test@example.com", "잘못된 비밀번호", "192.168.1.1", "Mozilla/5.0")

            // When & Then
            handler.handleLoginFailure(event)
        }

        @Test
        fun `IP가 null인 LoginFailureEvent를 처리해야 한다`() {
            // Given
            val event = LoginFailureEvent("test@example.com", "잘못된 비밀번호", null, null)

            // When & Then
            handler.handleLoginFailure(event)
        }
    }

    @Nested
    @DisplayName("handlePasswordChanged")
    inner class HandlePasswordChangedTest {
        @Test
        fun `RESET 타입의 PasswordChangedEvent를 처리해야 한다`() {
            // Given
            val event = PasswordChangedEvent(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.RESET)

            // When & Then
            handler.handlePasswordChanged(event)
        }

        @Test
        fun `CHANGE 타입의 PasswordChangedEvent를 처리해야 한다`() {
            // Given
            val event = PasswordChangedEvent(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.CHANGE)

            // When & Then
            handler.handlePasswordChanged(event)
        }

        @Test
        fun `SET 타입의 PasswordChangedEvent를 처리해야 한다`() {
            // Given
            val event = PasswordChangedEvent(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.SET)

            // When & Then
            handler.handlePasswordChanged(event)
        }
    }

    @Nested
    @DisplayName("handleAccountLocked")
    inner class HandleAccountLockedTest {
        @Test
        fun `AccountLockedEvent를 처리해야 한다`() {
            // Given
            val lockUntil = LocalDateTime.now().plusMinutes(15)
            val event = AccountLockedEvent(1L, "test@example.com", 5, lockUntil)

            // When & Then
            handler.handleAccountLocked(event)
        }
    }

    @Nested
    @DisplayName("handleLogout")
    inner class HandleLogoutTest {
        @Test
        fun `LogoutEvent를 처리해야 한다`() {
            // Given
            val event = LogoutEvent(1L, "test@example.com")

            // When & Then
            handler.handleLogout(event)
        }
    }

    @Nested
    @DisplayName("handleTokenRefreshed")
    inner class HandleTokenRefreshedTest {
        @Test
        fun `TokenRefreshedEvent를 처리해야 한다`() {
            // Given
            val event = TokenRefreshedEvent(1L, "test@example.com")

            // When & Then
            handler.handleTokenRefreshed(event)
        }
    }

    @Nested
    @DisplayName("이벤트 타임스탬프")
    inner class EventTimestampTest {
        @Test
        fun `이벤트 타임스탬프가 설정된 이벤트를 처리해야 한다`() {
            // Given
            val event = UserRegisteredEvent(1L, "test@example.com", "테스트")

            // When & Then
            handler.handleUserRegistered(event)
            // 타임스탬프는 이벤트 생성 시 자동 설정됨
        }
    }
}
