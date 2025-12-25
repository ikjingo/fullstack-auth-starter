package com.starter.api.auth.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {
    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> =
        MeterRegistryCustomizer { registry ->
            registry.config().commonTags(
                "service",
                "auth-api",
                "version",
                "1.0.0",
            )
        }

    @Bean
    fun bindCacheMetrics(
        cacheManager: CacheManager,
        meterRegistry: MeterRegistry,
    ): Boolean {
        cacheManager.cacheNames.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            if (cache is CaffeineCache) {
                CaffeineCacheMetrics.monitor(
                    meterRegistry,
                    cache.nativeCache,
                    cacheName,
                )
            }
        }
        return true
    }
}
