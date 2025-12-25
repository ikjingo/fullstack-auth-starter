---
name: backend-test-generator
description: Kotlin/Spring Boot 백엔드 테스트 코드를 생성하는 스킬. 사용자가 "백엔드 테스트 작성해 줘", "서비스 테스트 만들어 줘", "컨트롤러 테스트 생성해 줘" 등의 요청을 할 때 사용합니다. JUnit 5, MockK, Spring Boot Test를 활용한 단위/통합 테스트를 생성합니다.
---

# Backend Test Generator

Kotlin/Spring Boot 프로젝트를 위한 포괄적인 테스트 코드를 생성하는 스킬입니다.

## 테스트 프레임워크

| 프레임워크 | 용도 |
|-----------|------|
| JUnit 5 | 테스트 프레임워크 |
| MockK | Kotlin 전용 모킹 라이브러리 |
| Spring Boot Test | 통합 테스트 지원 |
| @DataJpaTest | Repository 테스트 |
| @WebMvcTest | Controller 테스트 |
| Testcontainers | 데이터베이스 통합 테스트 (선택) |

## 프로젝트 구조

```
backend/
├── api/auth-api/          # API 모듈
│   └── src/
│       ├── main/kotlin/   # 소스 코드
│       └── test/kotlin/   # 테스트 코드
├── core/core-api/         # 핵심 API 모듈
└── storage/db-core/       # 저장소 모듈
```

## 테스트 파일 위치 규칙

| 소스 파일 | 테스트 파일 |
|----------|------------|
| `src/main/kotlin/.../.../SomeService.kt` | `src/test/kotlin/.../.../SomeServiceTest.kt` |
| `src/main/kotlin/.../.../SomeController.kt` | `src/test/kotlin/.../.../SomeControllerTest.kt` |
| `src/main/kotlin/.../.../SomeRepository.kt` | `src/test/kotlin/.../.../SomeRepositoryTest.kt` |

## 테스트 유형별 가이드

### 1. Service 단위 테스트

Service 클래스의 비즈니스 로직을 테스트합니다. MockK를 사용하여 의존성을 모킹합니다.

```kotlin
@ExtendWith(MockKExtension::class)
class SomeServiceTest {

    @MockK
    private lateinit var someRepository: SomeRepository

    @InjectMockKs
    private lateinit var someService: SomeService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `정상적인 요청 시 데이터를 반환해야 한다`() {
        // Given
        val expected = SomeEntity(id = 1, name = "test")
        every { someRepository.findById(1) } returns Optional.of(expected)

        // When
        val result = someService.getById(1)

        // Then
        assertThat(result).isEqualTo(expected)
        verify(exactly = 1) { someRepository.findById(1) }
    }

    @Test
    fun `존재하지 않는 ID 조회 시 예외를 던져야 한다`() {
        // Given
        every { someRepository.findById(999) } returns Optional.empty()

        // When & Then
        assertThrows<NotFoundException> {
            someService.getById(999)
        }
    }
}
```

### 2. Controller 테스트

@WebMvcTest를 사용하여 컨트롤러 레이어만 테스트합니다.

```kotlin
@WebMvcTest(SomeController::class)
class SomeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var someService: SomeService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET 요청 시 데이터 목록을 반환해야 한다`() {
        // Given
        val response = listOf(SomeResponse(id = 1, name = "test"))
        every { someService.getAll() } returns response

        // When & Then
        mockMvc.perform(get("/api/v1/some"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].id").value(1))
    }

    @Test
    fun `POST 요청 시 새 데이터를 생성해야 한다`() {
        // Given
        val request = SomeRequest(name = "new item")
        val response = SomeResponse(id = 1, name = "new item")
        every { someService.create(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/some")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").value(1))
    }
}
```

### 3. Repository 테스트

@DataJpaTest를 사용하여 JPA Repository를 테스트합니다.

```kotlin
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SomeRepositoryTest {

    @Autowired
    private lateinit var someRepository: SomeRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `저장된 엔티티를 조회할 수 있어야 한다`() {
        // Given
        val entity = SomeEntity(name = "test")
        entityManager.persistAndFlush(entity)

        // When
        val found = someRepository.findById(entity.id)

        // Then
        assertThat(found).isPresent
        assertThat(found.get().name).isEqualTo("test")
    }

    @Test
    fun `커스텀 쿼리가 올바르게 동작해야 한다`() {
        // Given
        val entity1 = SomeEntity(name = "test1", active = true)
        val entity2 = SomeEntity(name = "test2", active = false)
        entityManager.persistAndFlush(entity1)
        entityManager.persistAndFlush(entity2)

        // When
        val activeEntities = someRepository.findAllByActiveTrue()

        // Then
        assertThat(activeEntities).hasSize(1)
        assertThat(activeEntities[0].name).isEqualTo("test1")
    }
}
```

## 테스트 작성 원칙

### Given-When-Then 패턴

모든 테스트는 Given-When-Then 패턴을 따릅니다:
- **Given**: 테스트 조건 설정 (데이터, 모킹)
- **When**: 테스트 대상 메서드 실행
- **Then**: 결과 검증

### 테스트 네이밍 규칙

백틱(`)을 사용한 한글 설명형 테스트 이름:
```kotlin
@Test
fun `정상적인 요청 시 성공 응답을 반환해야 한다`() { }

@Test
fun `필수 파라미터 누락 시 400 에러를 반환해야 한다`() { }

@Test
fun `존재하지 않는 리소스 요청 시 404 에러를 반환해야 한다`() { }
```

### 테스트 케이스 분류

| 카테고리 | 테스트 항목 |
|---------|-----------|
| 정상 케이스 | 유효한 입력에 대한 예상 결과 |
| 엣지 케이스 | 경계값, null, 빈 값 처리 |
| 에러 케이스 | 예외 상황, 유효성 검증 실패 |
| 보안 케이스 | 인증/인가 검증 |

## 테스트 실행

```bash
# 전체 테스트 실행
cd backend && ./gradlew test

# 특정 모듈 테스트
cd backend && ./gradlew :api:auth-api:test

# 특정 테스트 클래스 실행
cd backend && ./gradlew test --tests "SomeServiceTest"

# 테스트 커버리지 리포트
cd backend && ./gradlew jacocoTestReport
```

## 필수 의존성

`build.gradle.kts`에 다음 의존성이 필요합니다:

```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
```

## 워크플로우

1. 테스트 대상 파일 분석 (클래스, 메서드, 의존성 파악)
2. 테스트 파일 경로 결정 (`src/test/kotlin/...`)
3. 테스트 유형 결정 (Service/Controller/Repository)
4. 테스트 케이스 작성 (정상/엣지/에러 케이스)
5. 테스트 실행 및 검증

## 참고 자료

상세한 테스트 패턴과 예시는 `references/test_patterns.md`를 참조합니다.
