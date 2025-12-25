package com.starter.storage.db.user

import com.starter.storage.db.RepositoryTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("SocialAccountRepository")
class SocialAccountRepositoryTest : RepositoryTestSupport() {
    @Autowired
    private lateinit var socialAccountRepository: SocialAccountRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: UserEntity
    private lateinit var testUser2: UserEntity

    @BeforeEach
    fun setUp() {
        socialAccountRepository.deleteAll()
        userRepository.deleteAll()

        testUser =
            userRepository.save(
                UserEntity(
                    email = "test@example.com",
                    password = "encodedPassword123",
                    nickname = "테스트유저",
                ),
            )

        testUser2 =
            userRepository.save(
                UserEntity(
                    email = "test2@example.com",
                    password = "encodedPassword456",
                    nickname = "테스트유저2",
                ),
            )
    }

    @Nested
    @DisplayName("findByProviderAndProviderId")
    inner class FindByProviderAndProviderIdTest {
        @Test
        fun `provider와 providerId로 소셜 계정을 찾을 수 있다`() {
            // Given
            val socialAccount =
                socialAccountRepository.save(
                    SocialAccountEntity(
                        user = testUser,
                        provider = AuthProvider.GOOGLE,
                        providerId = "google-id-123",
                    ),
                )

            // When
            val found =
                socialAccountRepository.findByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "google-id-123",
                )

            // Then
            assertThat(found).isNotNull
            assertThat(found?.id).isEqualTo(socialAccount.id)
            assertThat(found?.provider).isEqualTo(AuthProvider.GOOGLE)
            assertThat(found?.providerId).isEqualTo("google-id-123")
        }

        @Test
        fun `존재하지 않는 provider로 조회하면 null을 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val found =
                socialAccountRepository.findByProviderAndProviderId(
                    AuthProvider.KAKAO,
                    "google-id-123",
                )

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `존재하지 않는 providerId로 조회하면 null을 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val found =
                socialAccountRepository.findByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "nonexistent-id",
                )

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findByProviderId")
    inner class FindByProviderIdTest {
        @Test
        fun `providerId로 소셜 계정을 찾을 수 있다`() {
            // Given
            val socialAccount =
                socialAccountRepository.save(
                    SocialAccountEntity(
                        user = testUser,
                        provider = AuthProvider.GOOGLE,
                        providerId = "unique-provider-id",
                    ),
                )

            // When
            val found = socialAccountRepository.findByProviderId("unique-provider-id")

            // Then
            assertThat(found).isNotNull
            assertThat(found?.id).isEqualTo(socialAccount.id)
        }

        @Test
        fun `존재하지 않는 providerId로 조회하면 null을 반환한다`() {
            // When
            val found = socialAccountRepository.findByProviderId("nonexistent-id")

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findAllByUser")
    inner class FindAllByUserTest {
        @Test
        fun `사용자의 모든 소셜 계정을 조회할 수 있다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.KAKAO,
                    providerId = "kakao-id-456",
                ),
            )
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.NAVER,
                    providerId = "naver-id-789",
                ),
            )

            // When
            val accounts = socialAccountRepository.findAllByUser(testUser)

            // Then
            assertThat(accounts).hasSize(3)
            assertThat(accounts.map { it.provider }).containsExactlyInAnyOrder(
                AuthProvider.GOOGLE,
                AuthProvider.KAKAO,
                AuthProvider.NAVER,
            )
        }

        @Test
        fun `소셜 계정이 없는 사용자는 빈 목록을 반환한다`() {
            // When
            val accounts = socialAccountRepository.findAllByUser(testUser)

            // Then
            assertThat(accounts).isEmpty()
        }

        @Test
        fun `다른 사용자의 소셜 계정은 조회되지 않는다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-user1",
                ),
            )
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser2,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-user2",
                ),
            )

            // When
            val accounts = socialAccountRepository.findAllByUser(testUser)

            // Then
            assertThat(accounts).hasSize(1)
            assertThat(accounts.first().providerId).isEqualTo("google-id-user1")
        }
    }

    @Nested
    @DisplayName("findByUserAndProvider")
    inner class FindByUserAndProviderTest {
        @Test
        fun `사용자와 provider로 소셜 계정을 찾을 수 있다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.KAKAO,
                    providerId = "kakao-id-456",
                ),
            )

            // When
            val found = socialAccountRepository.findByUserAndProvider(testUser, AuthProvider.GOOGLE)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.provider).isEqualTo(AuthProvider.GOOGLE)
            assertThat(found?.providerId).isEqualTo("google-id-123")
        }

        @Test
        fun `해당 provider의 계정이 없으면 null을 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val found = socialAccountRepository.findByUserAndProvider(testUser, AuthProvider.KAKAO)

            // Then
            assertThat(found).isNull()
        }

        @Test
        fun `다른 사용자의 같은 provider 계정은 조회되지 않는다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser2,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-user2",
                ),
            )

            // When
            val found = socialAccountRepository.findByUserAndProvider(testUser, AuthProvider.GOOGLE)

            // Then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("existsByProviderAndProviderId")
    inner class ExistsByProviderAndProviderIdTest {
        @Test
        fun `존재하는 provider와 providerId는 true를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val exists =
                socialAccountRepository.existsByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "google-id-123",
                )

            // Then
            assertThat(exists).isTrue()
        }

        @Test
        fun `존재하지 않는 provider는 false를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val exists =
                socialAccountRepository.existsByProviderAndProviderId(
                    AuthProvider.KAKAO,
                    "google-id-123",
                )

            // Then
            assertThat(exists).isFalse()
        }

        @Test
        fun `존재하지 않는 providerId는 false를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val exists =
                socialAccountRepository.existsByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "nonexistent-id",
                )

            // Then
            assertThat(exists).isFalse()
        }
    }

    @Nested
    @DisplayName("existsByUserAndProvider")
    inner class ExistsByUserAndProviderTest {
        @Test
        fun `사용자가 해당 provider로 연결된 계정이 있으면 true를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val exists = socialAccountRepository.existsByUserAndProvider(testUser, AuthProvider.GOOGLE)

            // Then
            assertThat(exists).isTrue()
        }

        @Test
        fun `사용자가 해당 provider로 연결된 계정이 없으면 false를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                ),
            )

            // When
            val exists = socialAccountRepository.existsByUserAndProvider(testUser, AuthProvider.KAKAO)

            // Then
            assertThat(exists).isFalse()
        }

        @Test
        fun `다른 사용자가 해당 provider로 연결된 계정이 있어도 현재 사용자는 false를 반환한다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser2,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-user2",
                ),
            )

            // When
            val exists = socialAccountRepository.existsByUserAndProvider(testUser, AuthProvider.GOOGLE)

            // Then
            assertThat(exists).isFalse()
        }
    }

    @Nested
    @DisplayName("프로필 이미지 URL")
    inner class ProfileImageUrlTest {
        @Test
        fun `프로필 이미지 URL을 저장하고 조회할 수 있다`() {
            // Given
            val profileUrl = "https://example.com/profile.jpg"
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                    profileImageUrl = profileUrl,
                ),
            )

            // When
            val found =
                socialAccountRepository.findByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "google-id-123",
                )

            // Then
            assertThat(found).isNotNull
            assertThat(found?.profileImageUrl).isEqualTo(profileUrl)
        }

        @Test
        fun `프로필 이미지 URL이 null일 수 있다`() {
            // Given
            socialAccountRepository.save(
                SocialAccountEntity(
                    user = testUser,
                    provider = AuthProvider.GOOGLE,
                    providerId = "google-id-123",
                    profileImageUrl = null,
                ),
            )

            // When
            val found =
                socialAccountRepository.findByProviderAndProviderId(
                    AuthProvider.GOOGLE,
                    "google-id-123",
                )

            // Then
            assertThat(found).isNotNull
            assertThat(found?.profileImageUrl).isNull()
        }
    }
}
