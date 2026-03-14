package com.cloudmonitor.ai.controller;

import com.cloudmonitor.ai.config.RateLimitConfig;
import com.cloudmonitor.ai.dto.DesignListDTO;
import com.cloudmonitor.ai.dto.DesignResponseDTO;
import com.cloudmonitor.ai.dto.GenerateRequestDTO;
import com.cloudmonitor.ai.service.DesignService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for design generation and retrieval.
 *
 * Public endpoints (rate-limited):
 * - POST /api/v1/generate - Generate a new design
 * - POST /api/v1/generate-preview - Generate preview (no persist)
 * - GET /api/v1/designs/{id} - Get design by ID
 * - GET /api/v1/designs - List designs
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DesignController {

    private final DesignService designService;
    private final RateLimitConfig rateLimitConfig;

    /**
     * Generate a new system design.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generate(
            @Valid @RequestBody GenerateRequestDTO request,
            HttpServletRequest httpRequest) {

        String clientId = getClientId(request, httpRequest);

        if (!rateLimitConfig.tryConsume(clientId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Remaining", "0")
                    .body(new ErrorResponse("Rate limit exceeded. Please try again later."));
        }

        log.info("Generate request from client: {} for title: {}", clientId, request.getTitle());

        DesignResponseDTO response = designService.generate(request);

        return ResponseEntity.ok()
                .header("X-RateLimit-Remaining",
                        String.valueOf(rateLimitConfig.getAvailableTokens(clientId)))
                .body(response);
    }

    /**
     * Generate a preview without persisting.
     */
    @PostMapping("/generate-preview")
    public ResponseEntity<?> generatePreview(
            @Valid @RequestBody GenerateRequestDTO request,
            HttpServletRequest httpRequest) {

        String clientId = getClientId(request, httpRequest);

        if (!rateLimitConfig.tryConsume(clientId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Remaining", "0")
                    .body(new ErrorResponse("Rate limit exceeded. Please try again later."));
        }

        log.info("Preview request from client: {} for title: {}", clientId, request.getTitle());

        DesignResponseDTO response = designService.generatePreview(request);

        return ResponseEntity.ok()
                .header("X-RateLimit-Remaining",
                        String.valueOf(rateLimitConfig.getAvailableTokens(clientId)))
                .body(response);
    }

    /**
     * Get a design by ID.
     */
    @GetMapping("/designs/{id}")
    public ResponseEntity<DesignResponseDTO> getById(@PathVariable UUID id) {
        log.debug("Get design: {}", id);
        DesignResponseDTO response = designService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List designs with optional filters.
     */
    @GetMapping("/designs")
    public ResponseEntity<DesignListDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        log.debug("List designs: page={}, size={}, userId={}, status={}, search={}",
                page, size, userId, status, search);

        DesignListDTO response = designService.list(page, size, userId, status, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get client ID for rate limiting (userId or IP).
     */
    private String getClientId(GenerateRequestDTO request, HttpServletRequest httpRequest) {
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            return "user:" + request.getUserId();
        }

        // Use X-Forwarded-For header if behind proxy
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }

        return "ip:" + httpRequest.getRemoteAddr();
    }

    /**
     * Simple error response record.
     */
    public record ErrorResponse(String error) {}
}
