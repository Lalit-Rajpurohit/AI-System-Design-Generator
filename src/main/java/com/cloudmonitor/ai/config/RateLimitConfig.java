package com.cloudmonitor.ai.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 *
 * Implements token-bucket algorithm per IP address.
 * Default: 10 requests per minute per IP.
 */
@Component
@Slf4j
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.burst-capacity:15}")
    private int burstCapacity;

    /**
     * Get or create a rate limit bucket for the given key (IP or user ID).
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(burstCapacity,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Try to consume a token from the bucket.
     *
     * @param key The bucket key (IP or user ID)
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        boolean consumed = bucket.tryConsume(1);
        if (!consumed) {
            log.warn("Rate limit exceeded for key: {}", key);
        }
        return consumed;
    }

    /**
     * Get remaining tokens for a key.
     */
    public long getAvailableTokens(String key) {
        return resolveBucket(key).getAvailableTokens();
    }

    /**
     * Clear all buckets (for testing or admin reset).
     */
    public void clearAllBuckets() {
        buckets.clear();
        log.info("All rate limit buckets cleared");
    }
}
