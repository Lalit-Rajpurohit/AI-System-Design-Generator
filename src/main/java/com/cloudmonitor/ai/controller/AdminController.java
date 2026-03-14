package com.cloudmonitor.ai.controller;

import com.cloudmonitor.ai.config.RateLimitConfig;
import com.cloudmonitor.ai.dto.DesignResponseDTO;
import com.cloudmonitor.ai.service.DesignService;
import com.cloudmonitor.ai.service.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for privileged operations.
 *
 * All endpoints require X-API-KEY header matching APP_ADMIN_KEY environment variable.
 *
 * Endpoints:
 * - POST /api/v1/admin/designs/{id}/regenerate - Regenerate a design
 * - DELETE /api/v1/admin/designs/{id} - Delete a design
 * - POST /api/v1/admin/purge - Purge old designs
 * - POST /api/v1/admin/rate-limit/reset - Reset rate limits
 * - GET /api/v1/admin/health - Health check with details
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final DesignService designService;
    private final RateLimitConfig rateLimitConfig;
    private final LLMClient llmClient;

    /**
     * Regenerate an existing design.
     */
    @PostMapping("/designs/{id}/regenerate")
    public ResponseEntity<DesignResponseDTO> regenerate(@PathVariable UUID id) {
        log.info("Admin: Regenerating design: {}", id);
        DesignResponseDTO response = designService.regenerate(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a design.
     */
    @DeleteMapping("/designs/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        log.info("Admin: Deleting design: {}", id);
        designService.delete(id);
        return ResponseEntity.ok(Map.of(
                "message", "Design deleted successfully",
                "id", id.toString()
        ));
    }

    /**
     * Purge old designs.
     */
    @PostMapping("/purge")
    public ResponseEntity<Map<String, Object>> purge(
            @RequestParam(defaultValue = "30") int olderThanDays) {
        log.info("Admin: Purging designs older than {} days", olderThanDays);
        int deleted = designService.purgeOldDesigns(olderThanDays);
        return ResponseEntity.ok(Map.of(
                "message", "Purge completed",
                "deletedCount", deleted,
                "olderThanDays", olderThanDays
        ));
    }

    /**
     * Reset all rate limit buckets.
     */
    @PostMapping("/rate-limit/reset")
    public ResponseEntity<Map<String, String>> resetRateLimits() {
        log.info("Admin: Resetting all rate limit buckets");
        rateLimitConfig.clearAllBuckets();
        return ResponseEntity.ok(Map.of("message", "Rate limits reset"));
    }

    /**
     * Health check with system details.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "llmProvider", llmClient.getProviderName(),
                "llmHealthy", llmClient.isHealthy(),
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
