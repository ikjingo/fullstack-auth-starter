package com.starter.storage.db

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

/**
 * Repository 테스트를 위한 기본 지원 클래스
 *
 * @DataJpaTest를 사용하여 JPA 관련 컴포넌트만 로드합니다.
 * H2 인메모리 데이터베이스를 사용합니다.
 */
@DataJpaTest
@ContextConfiguration(classes = [TestApplication::class])
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.flyway.enabled=false",
    ],
)
abstract class RepositoryTestSupport
