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
src/main/kotlin/com/starter/storage/db/
├── core/                          # 공통 설정
│   ├── config/
│   │   └── JpaConfig.kt           # JPA 설정
│   └── entity/
│       └── BaseEntity.kt          # 기본 엔티티 (생성/수정 시간)
├── user/                          # 사용자 관련
│   ├── UserEntity.kt              # 사용자 엔티티
│   ├── UserRepository.kt          # 사용자 레포지토리
│   ├── RefreshTokenEntity.kt      # 리프레시 토큰
│   └── *RepositoryCustomImpl.kt   # QueryDSL 구현체
└── token/                         # 토큰 관리
    ├── TokenBlacklistEntity.kt    # 토큰 블랙리스트
    └── TokenBlacklistRepository.kt
```

## 데이터베이스 스키마

### 사용자 관련 테이블

| 테이블 | 설명 |
|-------|------|
| users | 사용자 정보 |
| refresh_tokens | 리프레시 토큰 |
| token_blacklist | 로그아웃된 토큰 |

## 마이그레이션 히스토리

| 버전 | 설명 |
|-----|------|
| V1 | 초기 스키마 생성 |
| V2 | version 컬럼 추가 (낙관적 잠금) |
| V3 | 성능 인덱스 추가 |
| V4 | 게임 닉네임 관련 테이블 제거 |

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
- app-api
