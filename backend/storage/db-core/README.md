# DB Core Module

데이터베이스 엔티티, 레포지토리, 마이그레이션을 관리하는 스토리지 모듈입니다.

## 주요 기능

- **JPA 엔티티**: 도메인 모델과 매핑된 엔티티 클래스
- **레포지토리**: Spring Data JPA + QueryDSL 기반 데이터 접근
- **마이그레이션**: Flyway 기반 스키마 버전 관리
- **인덱스 최적화**: 성능 최적화를 위한 인덱스 설정

## 기술 스택

- Kotlin + Spring Data JPA
- QueryDSL 5.1.0
- PostgreSQL 16
- Flyway (DB Migration)
- H2 (테스트용)

## 프로젝트 구조

```
src/main/kotlin/com/zenless/storage/db/
├── core/                          # 공통 설정
│   ├── config/
│   │   └── JpaConfig.kt           # JPA 설정
│   └── entity/
│       └── BaseEntity.kt          # 기본 엔티티 (생성/수정 시간)
├── user/                          # 사용자 관련
│   ├── UserEntity.kt              # 사용자 엔티티
│   ├── UserRepository.kt          # 사용자 레포지토리
│   ├── RefreshTokenEntity.kt      # 리프레시 토큰
│   ├── SocialAccountEntity.kt     # 소셜 계정 연동
│   ├── PasswordResetCodeEntity.kt # 비밀번호 재설정 코드
│   └── *RepositoryCustomImpl.kt   # QueryDSL 구현체
├── token/                         # 토큰 관리
│   ├── TokenBlacklistEntity.kt    # 토큰 블랙리스트
│   └── TokenBlacklistRepository.kt
├── apitoken/                      # API 토큰 (외부 서비스용)
│   ├── ApiTokenEntity.kt
│   └── ApiTokenRepository.kt
└── nickname/                      # 닉네임 관리
    ├── NicknameEntity.kt          # 닉네임
    ├── ScoreRuleEntity.kt         # 점수 규칙
    ├── RarityThresholdEntity.kt   # 등급 기준
    ├── ServiceSettingEntity.kt    # 서비스 설정
    ├── DictionaryWordEntity.kt    # 사전 단어
    ├── MapleStoryCharacterEntity.kt  # 메이플스토리 캐릭터
    ├── LostArkCharacterEntity.kt  # 로스트아크 캐릭터
    ├── NicknameScoreCalculator.kt # 점수 계산 유틸
    └── *Repository*.kt            # 레포지토리들
```

## 데이터베이스 스키마

### 사용자 관련 테이블

| 테이블 | 설명 |
|-------|------|
| users | 사용자 정보 |
| social_accounts | 소셜 계정 연동 |
| refresh_tokens | 리프레시 토큰 |
| password_reset_codes | 비밀번호 재설정 코드 |
| token_blacklist | 로그아웃된 토큰 |
| api_tokens | 외부 API 토큰 |

### 닉네임 관련 테이블

| 테이블 | 설명 |
|-------|------|
| nicknames | 닉네임 정보 |
| score_rules | 점수 계산 규칙 |
| rarity_thresholds | 등급 기준 |
| service_settings | 서비스 활성화 설정 |
| dictionary_words | 사전 단어 |
| maplestory_characters | 메이플스토리 캐릭터 |
| lostark_characters | 로스트아크 캐릭터 |
| nickname_applied_rules | 적용된 점수 규칙 |

## 마이그레이션 히스토리

| 버전 | 설명 |
|-----|------|
| V1 | 초기 스키마 생성 |
| V2 | version 컬럼 추가 (낙관적 잠금) |
| V3 | 성능 인덱스 추가 (pg_trgm 포함) |
| V4 | 2FA 필드 추가 |
| V5 | 세션 정보 필드 추가 |
| V6 | 복합 인덱스 추가 |

## 인덱스 전략

### 성능 최적화 인덱스

```sql
-- 사용자 조회
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_lockout_until ON users(lockout_until) WHERE lockout_until IS NOT NULL;

-- 토큰 관리
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_token_blacklist_token ON token_blacklist(token);

-- 닉네임 검색 (한글 부분 일치)
CREATE INDEX idx_nickname_trgm ON nicknames USING gin (nickname gin_trgm_ops);
CREATE INDEX idx_nickname_score_updated ON nicknames(score DESC, updated_at DESC);
```

## 사용 예시

### 레포지토리 사용

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findByEmail(email: String): UserEntity? =
        userRepository.findByEmail(email)

    fun findActiveUsers(): List<UserEntity> =
        userRepository.findByStatus(UserStatus.ACTIVE)
}
```

### QueryDSL 커스텀 쿼리

```kotlin
@Repository
class UserRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : UserRepositoryCustom {

    override fun findUsersWithCondition(status: UserStatus): List<UserEntity> {
        val user = QUserEntity.userEntity

        return queryFactory
            .selectFrom(user)
            .where(user.status.eq(status))
            .orderBy(user.createdAt.desc())
            .fetch()
    }
}
```

## 의존성

```kotlin
// build.gradle.kts
val queryDslVersion = "5.1.0"

dependencies {
    implementation(project(":core:core-domain"))

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    // Test
    testImplementation("com.h2database:h2")
}
```

## 테스트

```bash
# 테스트 실행
./gradlew :storage:db-core:test

# 커버리지 리포트
./gradlew :storage:db-core:jacocoTestReport
```

## 사용 모듈

이 모듈을 의존하는 모듈:
- auth-api
- nickname-api
- admin-api
- app-api
