# Backend Test Patterns Reference

이 문서는 Zenless 프로젝트의 백엔드 테스트 작성에 대한 상세 가이드입니다.

## 목차

1. [프로젝트 구조 이해](#프로젝트-구조-이해)
2. [NicknameService 테스트 예시](#nicknameservice-테스트-예시)
3. [NicknameController 테스트 예시](#nicknamecontroller-테스트-예시)
4. [Repository 테스트 예시](#repository-테스트-예시)
5. [Mocking 패턴](#mocking-패턴)
6. [테스트 데이터 생성](#테스트-데이터-생성)
7. [통합 테스트](#통합-테스트)

---

## 프로젝트 구조 이해

### 모듈 구조

```
backend/
├── api/
│   └── auth-api/                    # 인증 및 닉네임 API
│       └── src/main/kotlin/com/zenless/api/auth/
│           ├── controller/          # REST Controllers
│           │   ├── NicknameController.kt
│           │   ├── AuthController.kt
│           │   └── request/         # Request DTOs
│           └── service/             # Business Logic
│               ├── NicknameService.kt
│               └── AuthService.kt
├── core/
│   └── core-api/                    # 핵심 API 컴포넌트
│       └── src/main/kotlin/com/zenless/core/api/
│           └── support/
│               ├── response/        # ApiResponse 래퍼
│               └── error/           # 에러 처리
└── storage/
    └── db-core/                     # JPA 엔티티 및 리포지토리
        └── src/main/kotlin/com/zenless/storage/db/
            └── nickname/
                ├── NicknameEntity.kt
                ├── NicknameRepository.kt
                └── NicknameRepositoryCustomImpl.kt
```

### ApiResponse 래퍼 구조

모든 API 응답은 `ApiResponse` 래퍼로 감싸집니다:

```kotlin
data class ApiResponse<T>(
    val result: ResultType,  // SUCCESS or ERROR
    val data: T?,
    val error: ErrorMessage?
)
```

---

## NicknameService 테스트 예시

### 파일 위치
`backend/api/auth-api/src/test/kotlin/com/zenless/api/auth/service/NicknameServiceTest.kt`

### 전체 테스트 코드

```kotlin
package com.zenless.api.auth.service

import com.zenless.api.auth.controller.request.SaveNicknameRequest
import com.zenless.storage.db.nickname.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockKExtension::class)
class NicknameServiceTest {

    @MockK
    private lateinit var nicknameRepository: NicknameRepository

    @MockK
    private lateinit var mapleStoryCharacterRepository: MapleStoryCharacterRepository

    @MockK
    private lateinit var lostArkCharacterRepository: LostArkCharacterRepository

    @InjectMockKs
    private lateinit var nicknameService: NicknameService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `새로운 닉네임을 저장할 수 있어야 한다`() {
        // Given
        val request = SaveNicknameRequest(
            nickname = "테스트닉네임",
            game = "MAPLESTORY",
            worldName = "스카니아",
            className = "아크메이지(불,독)",
            characterLevel = 260,
            guildName = "테스트길드"
        )

        every { nicknameRepository.findByNickname("테스트닉네임") } returns Optional.empty()
        every { nicknameRepository.save(any()) } answers { firstArg() }
        every { mapleStoryCharacterRepository.save(any()) } answers { firstArg() }

        // When
        val result = nicknameService.saveNicknames(listOf(request))

        // Then
        assertThat(result.savedCount).isEqualTo(1)
        verify(exactly = 1) { nicknameRepository.save(any()) }
        verify(exactly = 1) { mapleStoryCharacterRepository.save(any()) }
    }

    @Test
    fun `기존 닉네임의 점수가 더 높으면 업데이트해야 한다`() {
        // Given
        val existingNickname = NicknameEntity(
            nickname = "테스트닉네임",
            score = 50,
            rarity = RarityType.C
        ).apply { id = 1L }

        val request = SaveNicknameRequest(
            nickname = "테스트닉네임",
            game = "MAPLESTORY",
            worldName = "스카니아",
            className = "아크메이지(불,독)",
            characterLevel = 280,  // 높은 레벨 -> 높은 점수
            guildName = "테스트길드"
        )

        every { nicknameRepository.findByNickname("테스트닉네임") } returns Optional.of(existingNickname)
        every { mapleStoryCharacterRepository.save(any()) } answers { firstArg() }

        // When
        val result = nicknameService.saveNicknames(listOf(request))

        // Then
        assertThat(result.savedCount).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `닉네임 목록을 페이징하여 조회할 수 있어야 한다`() {
        // Given
        val nicknames = listOf(
            createNicknameEntity(1L, "닉네임1", 100, RarityType.SSS),
            createNicknameEntity(2L, "닉네임2", 80, RarityType.SS)
        )
        val page = PageImpl(nicknames, PageRequest.of(0, 20), 2)

        every {
            nicknameRepository.searchNicknames(
                any(), any(), any(), any(), any(), any()
            )
        } returns page

        // When
        val result = nicknameService.getNicknames(
            games = null,
            rarities = null,
            search = null,
            sort = "SCORE_DESC",
            gameMode = "OR",
            page = 1,
            size = 20
        )

        // Then
        assertThat(result.nicknames).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.currentPage).isEqualTo(1)
    }

    @Test
    fun `게임 필터로 닉네임을 검색할 수 있어야 한다`() {
        // Given
        val nicknames = listOf(
            createNicknameEntity(1L, "로아닉네임", 90, RarityType.S)
        )
        val page = PageImpl(nicknames, PageRequest.of(0, 20), 1)

        every {
            nicknameRepository.searchNicknames(
                any(), any(), eq(listOf(GameType.LOSTARK)), any(), any(), any()
            )
        } returns page

        // When
        val result = nicknameService.getNicknames(
            games = listOf("LOSTARK"),
            rarities = null,
            search = null,
            sort = "SCORE_DESC",
            gameMode = "OR",
            page = 1,
            size = 20
        )

        // Then
        assertThat(result.nicknames).hasSize(1)
    }

    private fun createNicknameEntity(
        id: Long,
        nickname: String,
        score: Int,
        rarity: RarityType
    ): NicknameEntity {
        return NicknameEntity(
            nickname = nickname,
            score = score,
            rarity = rarity
        ).apply { this.id = id }
    }
}
```

---

## NicknameController 테스트 예시

### 파일 위치
`backend/api/auth-api/src/test/kotlin/com/zenless/api/auth/controller/NicknameControllerTest.kt`

### 전체 테스트 코드

```kotlin
package com.zenless.api.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.zenless.api.auth.controller.request.SaveNicknameRequest
import com.zenless.api.auth.controller.response.NicknamePageResponse
import com.zenless.api.auth.controller.response.NicknameResponse
import com.zenless.api.auth.controller.response.SaveNicknameResultResponse
import com.zenless.api.auth.service.NicknameService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(NicknameController::class)
class NicknameControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var nicknameService: NicknameService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `닉네임 목록 조회 시 페이징 정보와 함께 반환해야 한다`() {
        // Given
        val response = NicknamePageResponse(
            nicknames = listOf(
                NicknameResponse(
                    id = 1,
                    nickname = "테스트닉네임",
                    games = listOf("MAPLESTORY"),
                    mapleStoryInfo = null,
                    lostArkInfo = null,
                    score = 100,
                    rarity = "SSS",
                    createdAt = "2024-01-01T00:00:00",
                    updatedAt = "2024-01-01T00:00:00"
                )
            ),
            totalElements = 1,
            totalPages = 1,
            currentPage = 1,
            pageSize = 20,
            hasNext = false
        )

        every {
            nicknameService.getNicknames(any(), any(), any(), any(), any(), any(), any())
        } returns response

        // When & Then
        mockMvc.perform(
            get("/api/v1/nicknames")
                .param("page", "1")
                .param("size", "20")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.nicknames").isArray)
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.currentPage").value(1))
    }

    @Test
    fun `게임과 희귀도 필터로 닉네임을 조회할 수 있어야 한다`() {
        // Given
        val response = NicknamePageResponse(
            nicknames = emptyList(),
            totalElements = 0,
            totalPages = 0,
            currentPage = 1,
            pageSize = 20,
            hasNext = false
        )

        every {
            nicknameService.getNicknames(any(), any(), any(), any(), any(), any(), any())
        } returns response

        // When & Then
        mockMvc.perform(
            get("/api/v1/nicknames")
                .param("games", "MAPLESTORY,LOSTARK")
                .param("rarities", "SSS,SS")
                .param("search", "테스트")
                .param("sort", "SCORE_DESC")
                .param("gameMode", "OR")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
    }

    @Test
    fun `닉네임을 저장할 수 있어야 한다`() {
        // Given
        val requests = listOf(
            SaveNicknameRequest(
                nickname = "새닉네임",
                game = "MAPLESTORY",
                worldName = "스카니아",
                className = "아크메이지(불,독)",
                characterLevel = 260,
                guildName = "테스트길드"
            )
        )

        val response = SaveNicknameResultResponse(
            savedCount = 1,
            message = "1개 저장 완료"
        )

        every { nicknameService.saveNicknames(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/nicknames")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.savedCount").value(1))
    }

    @Test
    fun `빈 요청 목록도 처리할 수 있어야 한다`() {
        // Given
        val response = SaveNicknameResultResponse(
            savedCount = 0,
            message = "0개 저장 완료"
        )

        every { nicknameService.saveNicknames(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/nicknames")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.savedCount").value(0))
    }
}
```

---

## Repository 테스트 예시

### 파일 위치
`backend/storage/db-core/src/test/kotlin/com/zenless/storage/db/nickname/NicknameRepositoryTest.kt`

### 전체 테스트 코드

```kotlin
package com.zenless.storage.db.nickname

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest

@DataJpaTest
class NicknameRepositoryTest {

    @Autowired
    private lateinit var nicknameRepository: NicknameRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `닉네임으로 엔티티를 조회할 수 있어야 한다`() {
        // Given
        val nickname = NicknameEntity(
            nickname = "테스트닉네임",
            score = 100,
            rarity = RarityType.SSS
        )
        entityManager.persistAndFlush(nickname)

        // When
        val found = nicknameRepository.findByNickname("테스트닉네임")

        // Then
        assertThat(found).isPresent
        assertThat(found.get().nickname).isEqualTo("테스트닉네임")
        assertThat(found.get().score).isEqualTo(100)
    }

    @Test
    fun `존재하지 않는 닉네임 조회 시 빈 Optional을 반환해야 한다`() {
        // When
        val found = nicknameRepository.findByNickname("존재하지않는닉네임")

        // Then
        assertThat(found).isEmpty
    }

    @Test
    fun `희귀도로 닉네임을 필터링할 수 있어야 한다`() {
        // Given
        val sssNickname = NicknameEntity(nickname = "SSS닉네임", score = 100, rarity = RarityType.SSS)
        val aaNickname = NicknameEntity(nickname = "SS닉네임", score = 80, rarity = RarityType.SS)
        entityManager.persistAndFlush(sssNickname)
        entityManager.persistAndFlush(aaNickname)

        // When
        val result = nicknameRepository.searchNicknames(
            search = null,
            rarities = listOf(RarityType.SSS),
            games = null,
            gameFilterMode = GameFilterMode.OR,
            sortType = SortType.SCORE_DESC,
            pageable = PageRequest.of(0, 20)
        )

        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].rarity).isEqualTo(RarityType.SSS)
    }

    @Test
    fun `점수 내림차순으로 정렬되어야 한다`() {
        // Given
        val lowScore = NicknameEntity(nickname = "낮은점수", score = 50, rarity = RarityType.C)
        val highScore = NicknameEntity(nickname = "높은점수", score = 100, rarity = RarityType.SSS)
        entityManager.persistAndFlush(lowScore)
        entityManager.persistAndFlush(highScore)

        // When
        val result = nicknameRepository.searchNicknames(
            search = null,
            rarities = null,
            games = null,
            gameFilterMode = GameFilterMode.OR,
            sortType = SortType.SCORE_DESC,
            pageable = PageRequest.of(0, 20)
        )

        // Then
        assertThat(result.content).hasSize(2)
        assertThat(result.content[0].score).isEqualTo(100)
        assertThat(result.content[1].score).isEqualTo(50)
    }
}
```

---

## Mocking 패턴

### MockK 기본 사용법

```kotlin
// 단순 반환값 설정
every { repository.findById(1) } returns Optional.of(entity)

// 인자 매칭
every { repository.findByName(any()) } returns listOf()
every { repository.findByAge(eq(20)) } returns listOf()

// 예외 던지기
every { service.doSomething() } throws RuntimeException("Error")

// 호출 검증
verify(exactly = 1) { repository.save(any()) }
verify { repository.findById(any()) wasNot Called }

// 순서 검증
verifyOrder {
    repository.findById(1)
    repository.save(any())
}
```

### Capture를 이용한 인자 검증

```kotlin
val slot = slot<NicknameEntity>()
every { repository.save(capture(slot)) } answers { slot.captured }

service.saveNickname(request)

assertThat(slot.captured.nickname).isEqualTo("테스트")
assertThat(slot.captured.score).isGreaterThan(0)
```

---

## 테스트 데이터 생성

### 팩토리 함수 패턴

```kotlin
object TestDataFactory {

    fun createNicknameEntity(
        id: Long = 1L,
        nickname: String = "테스트닉네임",
        score: Int = 100,
        rarity: RarityType = RarityType.SSS
    ): NicknameEntity {
        return NicknameEntity(
            nickname = nickname,
            score = score,
            rarity = rarity
        ).apply { this.id = id }
    }

    fun createSaveNicknameRequest(
        nickname: String = "테스트닉네임",
        game: String = "MAPLESTORY",
        worldName: String = "스카니아",
        className: String = "아크메이지(불,독)",
        characterLevel: Int = 260,
        guildName: String? = null
    ): SaveNicknameRequest {
        return SaveNicknameRequest(
            nickname = nickname,
            game = game,
            worldName = worldName,
            className = className,
            characterLevel = characterLevel,
            guildName = guildName
        )
    }
}
```

---

## 통합 테스트

### @SpringBootTest 전체 통합 테스트

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NicknameIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var nicknameRepository: NicknameRepository

    @Test
    fun `닉네임 저장 후 조회 통합 테스트`() {
        // Given
        val request = listOf(
            SaveNicknameRequest(
                nickname = "통합테스트닉네임",
                game = "MAPLESTORY",
                worldName = "스카니아",
                className = "아크메이지(불,독)",
                characterLevel = 260,
                guildName = null
            )
        )

        // When - 저장
        mockMvc.perform(
            post("/api/v1/nicknames")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)

        // Then - 조회
        val saved = nicknameRepository.findByNickname("통합테스트닉네임")
        assertThat(saved).isPresent
    }
}
```

---

## 테스트 설정 파일

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
```

### Testcontainers 사용 시

```kotlin
@Testcontainers
@SpringBootTest
class PostgresIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `실제 PostgreSQL에서 테스트`() {
        // ...
    }
}
```
