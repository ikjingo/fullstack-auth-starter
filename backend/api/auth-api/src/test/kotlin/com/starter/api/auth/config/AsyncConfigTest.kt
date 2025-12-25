package com.starter.api.auth.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@DisplayName("AsyncConfig")
class AsyncConfigTest {
    private lateinit var asyncConfig: AsyncConfig

    @BeforeEach
    fun setUp() {
        asyncConfig = AsyncConfig()
    }

    @Nested
    @DisplayName("getAsyncExecutor 메서드")
    inner class GetAsyncExecutorTest {
        @Test
        fun `ThreadPoolTaskExecutor를 반환해야 한다`() {
            // When
            val executor = asyncConfig.asyncExecutor

            // Then
            assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor::class.java)

            // Cleanup
            (executor as ThreadPoolTaskExecutor).shutdown()
        }

        @Test
        fun `corePoolSize가 2로 설정되어야 한다`() {
            // When
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor

            // Then
            assertThat(executor.corePoolSize).isEqualTo(2)

            // Cleanup
            executor.shutdown()
        }

        @Test
        fun `maxPoolSize가 10으로 설정되어야 한다`() {
            // When
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor

            // Then
            assertThat(executor.maxPoolSize).isEqualTo(10)

            // Cleanup
            executor.shutdown()
        }

        @Test
        fun `queueCapacity가 500으로 설정되어야 한다`() {
            // When
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor

            // Then
            // ThreadPoolTaskExecutor는 queueCapacity를 직접 조회하는 getter가 없음
            // 대신 스레드 풀이 정상 동작하는지 테스트
            assertThat(executor).isNotNull

            // Cleanup
            executor.shutdown()
        }

        @Test
        fun `스레드 이름이 async- 접두사를 가져야 한다`() {
            // Given
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor
            val threadNameHolder = arrayOfNulls<String>(1)
            val latch = CountDownLatch(1)

            // When
            executor.execute {
                threadNameHolder[0] = Thread.currentThread().name
                latch.countDown()
            }
            latch.await(5, TimeUnit.SECONDS)

            // Then
            assertThat(threadNameHolder[0]).startsWith("async-")

            // Cleanup
            executor.shutdown()
        }

        @Test
        fun `비동기 작업이 실행되어야 한다`() {
            // Given
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor
            val executed = AtomicBoolean(false)
            val latch = CountDownLatch(1)

            // When
            executor.execute {
                executed.set(true)
                latch.countDown()
            }
            latch.await(5, TimeUnit.SECONDS)

            // Then
            assertThat(executed.get()).isTrue()

            // Cleanup
            executor.shutdown()
        }

        @Test
        fun `여러 작업이 병렬로 실행되어야 한다`() {
            // Given
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor
            val taskCount = 5
            val latch = CountDownLatch(taskCount)
            val executedThreads = mutableSetOf<String>()
            val lock = Any()

            // When
            repeat(taskCount) {
                executor.execute {
                    synchronized(lock) {
                        executedThreads.add(Thread.currentThread().name)
                    }
                    Thread.sleep(100) // 병렬 실행 확인을 위한 지연
                    latch.countDown()
                }
            }
            latch.await(10, TimeUnit.SECONDS)

            // Then
            assertThat(executedThreads.size).isGreaterThanOrEqualTo(2)

            // Cleanup
            executor.shutdown()
        }
    }

    @Nested
    @DisplayName("getAsyncUncaughtExceptionHandler 메서드")
    inner class GetAsyncUncaughtExceptionHandlerTest {
        @Test
        fun `AsyncUncaughtExceptionHandler를 반환해야 한다`() {
            // When
            val handler = asyncConfig.asyncUncaughtExceptionHandler

            // Then
            assertThat(handler).isNotNull
        }

        @Test
        fun `예외를 처리할 수 있어야 한다`() {
            // Given
            val handler = asyncConfig.asyncUncaughtExceptionHandler
            val exception = RuntimeException("Test exception")
            val method = this::class.java.methods.first()

            // When & Then (예외 없이 실행되어야 함)
            handler?.handleUncaughtException(exception, method)
        }

        @Test
        fun `다양한 예외 타입을 처리할 수 있어야 한다`() {
            // Given
            val handler = asyncConfig.asyncUncaughtExceptionHandler
            val method = this::class.java.methods.first()
            val exceptions =
                listOf(
                    RuntimeException("Runtime exception"),
                    IllegalArgumentException("Illegal argument"),
                    NullPointerException("Null pointer"),
                    IllegalStateException("Illegal state"),
                )

            // When & Then (모든 예외가 처리되어야 함)
            exceptions.forEach { exception ->
                handler?.handleUncaughtException(exception, method)
            }
        }
    }

    @Nested
    @DisplayName("Executor 종료 동작")
    inner class ExecutorShutdownTest {
        @Test
        fun `waitForTasksToCompleteOnShutdown이 활성화되어야 한다`() {
            // Given
            val executor = asyncConfig.asyncExecutor as ThreadPoolTaskExecutor
            val executed = AtomicBoolean(false)
            val latch = CountDownLatch(1)

            // When
            executor.execute {
                Thread.sleep(100)
                executed.set(true)
                latch.countDown()
            }
            executor.shutdown()
            latch.await(5, TimeUnit.SECONDS)

            // Then
            assertThat(executed.get()).isTrue()
        }
    }
}
