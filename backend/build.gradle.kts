import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("com.epages.restdocs-api-spec") version "0.19.4" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    jacoco
}

java.sourceCompatibility = JavaVersion.valueOf("VERSION_${property("javaVersion")}")

allprojects {
    group = "${property("projectGroup")}"
    version = "${property("applicationVersion")}"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.mockk:mockk:1.13.16")
        testImplementation("com.ninja-squad:springmockk:4.0.2")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        kapt("org.springframework.boot:spring-boot-configuration-processor")
    }

    tasks.getByName("bootJar") {
        enabled = false
    }

    tasks.getByName("jar") {
        enabled = true
    }

    java.sourceCompatibility = JavaVersion.valueOf("VERSION_${project.property("javaVersion")}")

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // ktlint 설정
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    // JaCoCo 설정 - 명시적 의존성 추가
    tasks.named("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        dependsOn(tasks.named("compileKotlin"))
        dependsOn(tasks.named("processResources"))
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        afterEvaluate {
            classDirectories.setFrom(
                files(
                    classDirectories.files.map {
                        fileTree(it) {
                            exclude(
                                // QueryDSL generated classes
                                "**/Q*Entity*",
                                // Configuration classes
                                "**/*Config*",
                                "**/*Configuration*",
                                "**/*Properties*",
                                // Application main class
                                "**/*Application*",
                                // Request/Response DTOs
                                "**/request/**",
                                "**/response/**",
                                // Entities (mostly data classes)
                                "**/entity/**",
                            )
                        }
                    },
                ),
            )
        }
    }

    tasks.withType<JacocoCoverageVerification> {
        violationRules {
            rule {
                limit {
                    // 현재 전체 커버리지 62% (auth-api: 84%, nickname-api: 39%, db-core: 55%)
                    // 모든 모듈이 통과할 수 있는 최소 임계값, 점진적으로 증가 예정
                    minimum = "0.35".toBigDecimal()
                }
            }
        }
    }
}

// ktlint 루트 프로젝트 설정
ktlint {
    version.set("1.5.0")
}
