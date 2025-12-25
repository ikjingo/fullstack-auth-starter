package com.starter.api.auth.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor

/**
 * 비동기 처리 설정
 *
 * 사용 예시:
 * - 이메일 발송
 * - 외부 API 호출 (알림 등)
 * - 대용량 데이터 처리
 *
 * 주의: 현재 AuditLogService와 AuthMetricsService는
 * 이미 충분히 빠른 작업이므로 비동기화하지 않음
 */
@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 10
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("async-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler = AsyncExceptionHandler()

    private inner class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
        override fun handleUncaughtException(
            ex: Throwable,
            method: Method,
            vararg params: Any?,
        ) {
            log.error(
                "Async method '{}' threw exception: {}",
                method.name,
                ex.message,
                ex,
            )
        }
    }
}
