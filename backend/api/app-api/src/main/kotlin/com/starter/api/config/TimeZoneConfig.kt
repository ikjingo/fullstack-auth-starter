package com.starter.api.config

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.TimeZone

@Component
class TimeZoneConfig {
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
}
