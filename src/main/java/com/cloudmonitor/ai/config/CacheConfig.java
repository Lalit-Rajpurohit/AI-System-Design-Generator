package com.cloudmonitor.ai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for caching generated designs.
 *
 * Cache settings:
 * - Maximum 1000 entries
 * - Expire after 1 hour of no access
 * - Record stats for monitoring
 */
@Configuration
public class CacheConfig {

    public static final String DESIGNS_CACHE = "designs";
    public static final String LLM_RESPONSES_CACHE = "llmResponses";
    public static final String IDEMPOTENCY_CACHE = "idempotency";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats());
        cacheManager.setCacheNames(java.util.List.of(
                DESIGNS_CACHE,
                LLM_RESPONSES_CACHE,
                IDEMPOTENCY_CACHE
        ));
        return cacheManager;
    }
}
