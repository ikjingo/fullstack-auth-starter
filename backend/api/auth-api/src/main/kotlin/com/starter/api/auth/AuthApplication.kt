package com.starter.api.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.starter.api.auth",
        "com.starter.core.api",
        "com.starter.storage.db",
    ],
)
@EntityScan(basePackages = ["com.starter.storage.db"])
@ConfigurationPropertiesScan
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
