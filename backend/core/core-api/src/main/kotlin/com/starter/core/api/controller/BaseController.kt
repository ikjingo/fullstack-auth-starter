package com.starter.core.api.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class BaseController {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    fun logActionWithJson(
        action: String,
        managerId: Int,
        data: Any,
    ) {
        try {
            val jsonData = objectMapper.writeValueAsString(data)
            log.info("[$action] manager_id: $managerId, data: $jsonData")
        } catch (e: Exception) {
            log.error("Failed to log request data for action: $action, manager_id: $managerId", e)
        }
    }
}
