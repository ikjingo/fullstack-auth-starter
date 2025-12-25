package com.starter.api.auth.service.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AuthMetricsService(
    private val meterRegistry: MeterRegistry,
) {
    private val loginSuccessCounter: Counter =
        Counter
            .builder("auth.login.success")
            .description("Number of successful login attempts")
            .register(meterRegistry)

    private val loginFailureCounter: Counter =
        Counter
            .builder("auth.login.failure")
            .description("Number of failed login attempts")
            .register(meterRegistry)

    private val signUpCounter: Counter =
        Counter
            .builder("auth.signup")
            .description("Number of user registrations")
            .register(meterRegistry)

    private val logoutCounter: Counter =
        Counter
            .builder("auth.logout")
            .description("Number of logout events")
            .register(meterRegistry)

    private val tokenRefreshCounter: Counter =
        Counter
            .builder("auth.token.refresh")
            .description("Number of token refresh events")
            .register(meterRegistry)

    private val accountLockedCounter: Counter =
        Counter
            .builder("auth.account.locked")
            .description("Number of account lockouts")
            .register(meterRegistry)

    private val tokenGenerationTimer: Timer =
        Timer
            .builder("auth.token.generation")
            .description("Time taken to generate tokens")
            .register(meterRegistry)

    fun recordLoginSuccess() {
        loginSuccessCounter.increment()
    }

    fun recordLoginFailure(reason: String) {
        Counter
            .builder("auth.login.failure")
            .tag("reason", reason)
            .register(meterRegistry)
            .increment()
        loginFailureCounter.increment()
    }

    fun recordSignUp() {
        signUpCounter.increment()
    }

    fun recordLogout() {
        logoutCounter.increment()
    }

    fun recordTokenRefresh() {
        tokenRefreshCounter.increment()
    }

    fun recordAccountLocked() {
        accountLockedCounter.increment()
    }

    fun <T> recordTokenGeneration(block: () -> T): T {
        val startTime = System.nanoTime()
        return try {
            block()
        } finally {
            val duration = System.nanoTime() - startTime
            tokenGenerationTimer.record(duration, TimeUnit.NANOSECONDS)
        }
    }

    fun recordRateLimitExceeded(clientIp: String) {
        Counter
            .builder("auth.rate_limit.exceeded")
            .tag("client_ip_prefix", maskIpAddress(clientIp))
            .register(meterRegistry)
            .increment()
    }

    private fun maskIpAddress(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.x.x"
        } else {
            "unknown"
        }
    }
}
