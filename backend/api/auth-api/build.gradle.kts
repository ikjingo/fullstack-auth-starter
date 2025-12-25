plugins {
    id("com.epages.restdocs-api-spec")
    `java-test-fixtures`
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = true
    archiveClassifier.set("lib")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// REST Docs OpenAPI 설정
openapi3 {
    setServer("http://localhost:8080")
    title = "Zenless Auth API"
    description = "Zenless 프로젝트 인증 API 문서"
    version = "1.0.0"
    format = "yaml"
    outputDirectory =
        layout.buildDirectory
            .dir("api-spec")
            .get()
            .asFile.path
}

// OpenAPI 스펙 생성 후 resources에 복사
tasks.register<Copy>("copyOpenApiSpec") {
    dependsOn("openapi3")
    from(layout.buildDirectory.dir("api-spec"))
    into(layout.projectDirectory.dir("src/main/resources/static/docs"))
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":core:core-domain"))
    implementation(project(":storage:db-core"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Password Encoding
    implementation("org.springframework.security:spring-security-crypto")

    // Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    // Rate Limiting
    implementation("com.bucket4j:bucket4j_jdk17-core:8.15.0")

    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    runtimeOnly("org.postgresql:postgresql")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")

    // REST Docs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")

    // 자체 testFixtures 사용
    testImplementation(testFixtures(project(":api:auth-api")))

    // Test Fixtures dependencies (다른 모듈에서 사용 가능)
    testFixturesImplementation(project(":core:core-api"))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-web")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-security")
    testFixturesImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testFixturesImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
    testFixturesImplementation("com.ninja-squad:springmockk:4.0.2")
}
