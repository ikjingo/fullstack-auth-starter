package com.starter.api.auth.event

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

@DisplayName("AuthEventPublisher")
class AuthEventPublisherTest {
    private lateinit var publisher: AuthEventPublisher
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun setUp() {
        applicationEventPublisher = mockk(relaxed = true)
        publisher = AuthEventPublisher(applicationEventPublisher)
    }

    @Nested
    @DisplayName("publish")
    inner class PublishTest {
        @Test
        fun `이벤트를 ApplicationEventPublisher로 발행해야 한다`() {
            // Given
            val event = UserRegisteredEvent(1L, "test@example.com", "테스트")

            // When
            publisher.publish(event)

            // Then
            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("publishUserRegistered")
    inner class PublishUserRegisteredTest {
        @Test
        fun `UserRegisteredEvent를 발행해야 한다`() {
            // Given
            val eventSlot = slot<UserRegisteredEvent>()

            // When
            publisher.publishUserRegistered(1L, "test@example.com", "테스트")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.userId).isEqualTo(1L)
            assertThat(eventSlot.captured.email).isEqualTo("test@example.com")
            assertThat(eventSlot.captured.nickname).isEqualTo("테스트")
        }

        @Test
        fun `닉네임이 null이어도 이벤트를 발행해야 한다`() {
            // Given
            val eventSlot = slot<UserRegisteredEvent>()

            // When
            publisher.publishUserRegistered(1L, "test@example.com", null)

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.nickname).isNull()
        }
    }

    @Nested
    @DisplayName("publishLoginSuccess")
    inner class PublishLoginSuccessTest {
        @Test
        fun `LoginSuccessEvent를 발행해야 한다`() {
            // Given
            val eventSlot = slot<LoginSuccessEvent>()

            // When
            publisher.publishLoginSuccess(1L, "test@example.com", "192.168.1.1", "Mozilla/5.0")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.userId).isEqualTo(1L)
            assertThat(eventSlot.captured.email).isEqualTo("test@example.com")
            assertThat(eventSlot.captured.ipAddress).isEqualTo("192.168.1.1")
            assertThat(eventSlot.captured.userAgent).isEqualTo("Mozilla/5.0")
        }

        @Test
        fun `IP와 UserAgent가 null이어도 이벤트를 발행해야 한다`() {
            // Given
            val eventSlot = slot<LoginSuccessEvent>()

            // When
            publisher.publishLoginSuccess(1L, "test@example.com")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.ipAddress).isNull()
            assertThat(eventSlot.captured.userAgent).isNull()
        }
    }

    @Nested
    @DisplayName("publishLoginFailure")
    inner class PublishLoginFailureTest {
        @Test
        fun `LoginFailureEvent를 발행해야 한다`() {
            // Given
            val eventSlot = slot<LoginFailureEvent>()

            // When
            publisher.publishLoginFailure("test@example.com", "잘못된 비밀번호", "192.168.1.1", "Mozilla/5.0")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.email).isEqualTo("test@example.com")
            assertThat(eventSlot.captured.reason).isEqualTo("잘못된 비밀번호")
            assertThat(eventSlot.captured.ipAddress).isEqualTo("192.168.1.1")
        }
    }

    @Nested
    @DisplayName("publishPasswordChanged")
    inner class PublishPasswordChangedTest {
        @Test
        fun `PasswordChangedEvent를 RESET 타입으로 발행해야 한다`() {
            // Given
            val eventSlot = slot<PasswordChangedEvent>()

            // When
            publisher.publishPasswordChanged(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.RESET)

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.userId).isEqualTo(1L)
            assertThat(eventSlot.captured.changeType).isEqualTo(PasswordChangedEvent.PasswordChangeType.RESET)
        }

        @Test
        fun `PasswordChangedEvent를 CHANGE 타입으로 발행해야 한다`() {
            // Given
            val eventSlot = slot<PasswordChangedEvent>()

            // When
            publisher.publishPasswordChanged(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.CHANGE)

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.changeType).isEqualTo(PasswordChangedEvent.PasswordChangeType.CHANGE)
        }

        @Test
        fun `PasswordChangedEvent를 SET 타입으로 발행해야 한다`() {
            // Given
            val eventSlot = slot<PasswordChangedEvent>()

            // When
            publisher.publishPasswordChanged(1L, "test@example.com", PasswordChangedEvent.PasswordChangeType.SET)

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.changeType).isEqualTo(PasswordChangedEvent.PasswordChangeType.SET)
        }
    }

    @Nested
    @DisplayName("publishAccountLocked")
    inner class PublishAccountLockedTest {
        @Test
        fun `AccountLockedEvent를 발행해야 한다`() {
            // Given
            val lockUntil = LocalDateTime.now().plusMinutes(15)
            val event = AccountLockedEvent(1L, "test@example.com", 5, lockUntil)

            // When
            publisher.publishAccountLocked(event)

            // Then
            verify { applicationEventPublisher.publishEvent(event) }
        }
    }

    @Nested
    @DisplayName("publishLogout")
    inner class PublishLogoutTest {
        @Test
        fun `LogoutEvent를 발행해야 한다`() {
            // Given
            val eventSlot = slot<LogoutEvent>()

            // When
            publisher.publishLogout(1L, "test@example.com")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.userId).isEqualTo(1L)
            assertThat(eventSlot.captured.email).isEqualTo("test@example.com")
        }
    }

    @Nested
    @DisplayName("publishTokenRefreshed")
    inner class PublishTokenRefreshedTest {
        @Test
        fun `TokenRefreshedEvent를 발행해야 한다`() {
            // Given
            val eventSlot = slot<TokenRefreshedEvent>()

            // When
            publisher.publishTokenRefreshed(1L, "test@example.com")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            assertThat(eventSlot.captured.userId).isEqualTo(1L)
            assertThat(eventSlot.captured.email).isEqualTo("test@example.com")
        }
    }

    @Nested
    @DisplayName("이벤트 타임스탬프")
    inner class EventTimestampTest {
        @Test
        fun `모든 이벤트에 타임스탬프가 설정되어야 한다`() {
            // Given
            val beforeTime = LocalDateTime.now()
            val eventSlot = slot<UserRegisteredEvent>()

            // When
            publisher.publishUserRegistered(1L, "test@example.com", "테스트")

            // Then
            verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }
            val afterTime = LocalDateTime.now()
            assertThat(eventSlot.captured.timestamp).isBetween(beforeTime, afterTime)
        }
    }
}
