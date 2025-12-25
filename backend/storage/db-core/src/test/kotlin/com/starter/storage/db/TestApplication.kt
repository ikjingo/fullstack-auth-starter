package com.starter.storage.db

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["com.starter.storage.db"])
@EntityScan(basePackages = ["com.starter.storage.db"])
class TestApplication
