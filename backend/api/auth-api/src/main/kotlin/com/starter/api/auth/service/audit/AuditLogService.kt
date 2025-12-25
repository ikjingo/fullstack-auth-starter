package com.starter.api.auth.service.audit

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service

@Service
class AuditLogService {
    private val auditLogger = LoggerFactory.getLogger("AUDIT")

    fun logLoginSuccess(
        userId: Long,
        email: String,
    ) {
        log(
            action = AuditAction.LOGIN_SUCCESS,
            userId = userId,
            details = mapOf("email" to maskEmail(email)),
        )
    }

    fun logLoginFailure(
        email: String,
        reason: String,
    ) {
        log(
            action = AuditAction.LOGIN_FAILURE,
            details =
                mapOf(
                    "email" to maskEmail(email),
                    "reason" to reason,
                ),
        )
    }

    fun logLogout(userId: Long) {
        log(
            action = AuditAction.LOGOUT,
            userId = userId,
        )
    }

    fun logSignUp(
        userId: Long,
        email: String,
    ) {
        log(
            action = AuditAction.SIGN_UP,
            userId = userId,
            details = mapOf("email" to maskEmail(email)),
        )
    }

    fun logPasswordChange(userId: Long) {
        log(
            action = AuditAction.PASSWORD_CHANGE,
            userId = userId,
        )
    }

    fun logPasswordResetRequest(email: String) {
        log(
            action = AuditAction.PASSWORD_RESET_REQUEST,
            details = mapOf("email" to maskEmail(email)),
        )
    }

    fun logAccountLocked(
        userId: Long,
        email: String,
    ) {
        log(
            action = AuditAction.ACCOUNT_LOCKED,
            userId = userId,
            details = mapOf("email" to maskEmail(email)),
        )
    }

    fun logAccountUnlocked(
        userId: Long,
        email: String,
    ) {
        log(
            action = AuditAction.ACCOUNT_UNLOCKED,
            userId = userId,
            details = mapOf("email" to maskEmail(email)),
        )
    }

    fun logTokenRefresh(userId: Long) {
        log(
            action = AuditAction.TOKEN_REFRESH,
            userId = userId,
        )
    }

    fun logAccessDenied(
        userId: Long?,
        resource: String,
        reason: String,
    ) {
        log(
            action = AuditAction.ACCESS_DENIED,
            userId = userId,
            details =
                mapOf(
                    "resource" to resource,
                    "reason" to reason,
                ),
        )
    }

    private fun log(
        action: AuditAction,
        userId: Long? = null,
        details: Map<String, Any> = emptyMap(),
    ) {
        val correlationId = MDC.get("correlationId") ?: "N/A"
        val clientIp = MDC.get("clientIp") ?: "unknown"

        val message =
            buildString {
                append("action=${action.name}")
                append(" userId=${userId ?: "anonymous"}")
                append(" correlationId=$correlationId")
                append(" clientIp=$clientIp")
                details.forEach { (key, value) ->
                    append(" $key=$value")
                }
            }

        when (action.level) {
            AuditLevel.INFO -> auditLogger.info(message)
            AuditLevel.WARN -> auditLogger.warn(message)
            AuditLevel.ERROR -> auditLogger.error(message)
        }
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email

        val localPart = email.substring(0, atIndex)
        val domain = email.substring(atIndex)

        val maskedLocal =
            if (localPart.length <= 2) {
                localPart.first() + "*".repeat(localPart.length - 1)
            } else {
                localPart.first() + "*".repeat(localPart.length - 2) + localPart.last()
            }

        return maskedLocal + domain
    }
}

enum class AuditAction(
    val level: AuditLevel,
) {
    LOGIN_SUCCESS(AuditLevel.INFO),
    LOGIN_FAILURE(AuditLevel.WARN),
    LOGOUT(AuditLevel.INFO),
    SIGN_UP(AuditLevel.INFO),
    PASSWORD_CHANGE(AuditLevel.INFO),
    PASSWORD_RESET_REQUEST(AuditLevel.INFO),
    ACCOUNT_LOCKED(AuditLevel.WARN),
    ACCOUNT_UNLOCKED(AuditLevel.INFO),
    TOKEN_REFRESH(AuditLevel.INFO),
    ACCESS_DENIED(AuditLevel.WARN),
}

enum class AuditLevel {
    INFO,
    WARN,
    ERROR,
}
