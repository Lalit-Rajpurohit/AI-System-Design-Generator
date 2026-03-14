package com.cloudmonitor.ai.service.impl;

import com.cloudmonitor.ai.config.CacheConfig;
import com.cloudmonitor.ai.config.LLMConfig;
import com.cloudmonitor.ai.dto.*;
import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import com.cloudmonitor.ai.exception.DesignNotFoundException;
import com.cloudmonitor.ai.exception.LLMException;
import com.cloudmonitor.ai.model.DesignEntity;
import com.cloudmonitor.ai.repository.DesignRepository;
import com.cloudmonitor.ai.service.DesignService;
import com.cloudmonitor.ai.service.LLMClient;
import com.cloudmonitor.ai.util.PromptBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of DesignService.
 *
 * Handles the complete design generation pipeline:
 * 1. Validate and normalize input
 * 2. Check for existing design (idempotency)
 * 3. Build prompt and call LLM
 * 4. Parse and validate response
 * 5. Persist and return
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DesignServiceImpl implements DesignService {

    private final DesignRepository repository;
    private final LLMClient llmClient;
    private final LLMConfig llmConfig;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    // Pattern to extract JSON from markdown code blocks
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
            "```(?:json)?\\s*([\\s\\S]*?)```",
            Pattern.MULTILINE
    );

    @Override
    @Transactional
    public DesignResponseDTO generate(GenerateRequestDTO request) {
        log.info("Generating design for: {}", request.getTitle());

        // Generate idempotency key
        String idempotencyKey = promptBuilder.generateIdempotencyKey(request);

        // Check for existing design (unless regenerate requested)
        if (!request.isRegenerate()) {
            var existing = repository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Found existing design with idempotency key: {}", idempotencyKey);
                return entityToDto(existing.get());
            }
        }

        // Create initial entity
        DesignEntity entity = DesignEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .preferencesJson(serializePreferences(request.getPreferences()))
                .userId(request.getUserId())
                .idempotencyKey(idempotencyKey)
                .status(DesignStatus.PROCESSING)
                .llmProvider(llmClient.getProviderName())
                .build();

        entity = repository.save(entity);

        try {
            // Build prompts
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(request);

            log.debug("System prompt length: {}, User prompt length: {}",
                    systemPrompt.length(), userPrompt.length());

            // Store prompt if configured
            if (llmConfig.isIncludePromptInResponse()) {
                entity.setPromptUsed(userPrompt);
            }

            // Call LLM
            LLMResponseDTO llmResponse = llmClient.generate(systemPrompt, userPrompt);

            // Parse response
            String rawContent = llmResponse.getContent();
            entity.setRawLlmResponse(rawContent);
            entity.setTokensUsed(llmResponse.getTotalTokensUsed());

            // Extract and parse JSON
            DesignResponseDTO parsed = parseResponse(rawContent, entity.getId(), request.getTitle());

            // Update entity with parsed data
            updateEntityFromParsed(entity, parsed);
            entity.setStatus(DesignStatus.COMPLETED);

            entity = repository.save(entity);
            log.info("Design generated successfully: {}", entity.getId());

            return entityToDto(entity);

        } catch (LLMException e) {
            log.error("LLM error during design generation", e);
            entity.setStatus(DesignStatus.FAILED);
            entity.setErrorMessage("LLM error: " + e.getMessage());
            repository.save(entity);
            throw e;

        } catch (Exception e) {
            log.error("Error during design generation", e);
            entity.setStatus(DesignStatus.FAILED);
            entity.setErrorMessage("Processing error: " + e.getMessage());
            repository.save(entity);
            throw new RuntimeException("Failed to generate design: " + e.getMessage(), e);
        }
    }

    @Override
    public DesignResponseDTO generatePreview(GenerateRequestDTO request) {
        log.info("Generating preview for: {}", request.getTitle());

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(request);

        LLMResponseDTO llmResponse = llmClient.generate(systemPrompt, userPrompt);

        String rawContent = llmResponse.getContent();
        DesignResponseDTO parsed = parseResponse(rawContent, UUID.randomUUID(), request.getTitle());
        parsed.setStatus(DesignStatus.COMPLETED);
        parsed.setLlmProvider(llmClient.getProviderName());
        parsed.setTokensUsed(llmResponse.getTotalTokensUsed());

        return parsed;
    }

    @Override
    @Cacheable(value = CacheConfig.DESIGNS_CACHE, key = "#id")
    public DesignResponseDTO getById(UUID id) {
        log.debug("Fetching design: {}", id);
        DesignEntity entity = repository.findById(id)
                .orElseThrow(() -> new DesignNotFoundException("Design not found: " + id));
        return entityToDto(entity);
    }

    @Override
    public DesignListDTO list(int page, int size, String userId, String status, String searchQuery) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100));
        Page<DesignEntity> results;

        if (searchQuery != null && !searchQuery.isBlank()) {
            results = repository.searchByTitle(searchQuery.trim(), pageRequest);
        } else if (userId != null && !userId.isBlank() && status != null && !status.isBlank()) {
            results = repository.findByUserIdAndStatusOrderByCreatedAtDesc(
                    userId, DesignStatus.valueOf(status.toUpperCase()), pageRequest);
        } else if (userId != null && !userId.isBlank()) {
            results = repository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
        } else if (status != null && !status.isBlank()) {
            results = repository.findByStatusOrderByCreatedAtDesc(
                    DesignStatus.valueOf(status.toUpperCase()), pageRequest);
        } else {
            results = repository.findAllByOrderByCreatedAtDesc(pageRequest);
        }

        List<DesignListDTO.DesignSummary> summaries = results.getContent().stream()
                .map(this::entityToSummary)
                .toList();

        return DesignListDTO.builder()
                .designs(summaries)
                .page(page)
                .size(size)
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .hasNext(results.hasNext())
                .hasPrevious(results.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DESIGNS_CACHE, key = "#id")
    public DesignResponseDTO regenerate(UUID id) {
        log.info("Regenerating design: {}", id);

        DesignEntity existing = repository.findById(id)
                .orElseThrow(() -> new DesignNotFoundException("Design not found: " + id));

        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title(existing.getTitle())
                .description(existing.getDescription())
                .preferences(deserializePreferences(existing.getPreferencesJson()))
                .userId(existing.getUserId())
                .regenerate(true)
                .build();

        // Update idempotency key to allow regeneration
        existing.setIdempotencyKey(promptBuilder.generateIdempotencyKey(request) + "_regen_" + System.currentTimeMillis());
        repository.save(existing);

        return generate(request);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DESIGNS_CACHE, key = "#id")
    public void delete(UUID id) {
        log.info("Deleting design: {}", id);
        if (!repository.existsById(id)) {
            throw new DesignNotFoundException("Design not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public int purgeOldDesigns(int olderThanDays) {
        Instant cutoff = Instant.now().minus(olderThanDays, ChronoUnit.DAYS);
        int deleted = repository.deleteByCreatedAtBefore(cutoff);
        log.info("Purged {} designs older than {} days", deleted, olderThanDays);
        return deleted;
    }

    /**
     * Parse the LLM response content into a DesignResponseDTO.
     */
    private DesignResponseDTO parseResponse(String rawContent, UUID id, String title) {
        String jsonContent = extractJson(rawContent);

        try {
            JsonNode root = objectMapper.readTree(jsonContent);

            DesignResponseDTO response = DesignResponseDTO.builder()
                    .id(id)
                    .title(root.has("title") ? root.get("title").asText() : title)
                    .generatedAt(Instant.now())
                    .build();

            // Parse architecture text
            if (root.has("architectureText")) {
                response.setArchitectureText(root.get("architectureText").asText());
            }

            // Parse services
            if (root.has("services") && root.get("services").isArray()) {
                List<DesignResponseDTO.ServiceComponent> services = objectMapper.convertValue(
                        root.get("services"),
                        new TypeReference<List<DesignResponseDTO.ServiceComponent>>() {}
                );
                response.setServices(services);
            }

            // Parse tech stack
            if (root.has("techStack") && root.get("techStack").isObject()) {
                DesignResponseDTO.TechStack techStack = objectMapper.convertValue(
                        root.get("techStack"),
                        DesignResponseDTO.TechStack.class
                );
                response.setTechStack(techStack);
            }

            // Parse infrastructure
            if (root.has("infrastructure") && root.get("infrastructure").isObject()) {
                DesignResponseDTO.Infrastructure infra = objectMapper.convertValue(
                        root.get("infrastructure"),
                        DesignResponseDTO.Infrastructure.class
                );
                response.setInfrastructure(infra);
            }

            // Parse terraform snippet
            if (root.has("terraformSnippet") && !root.get("terraformSnippet").isNull()) {
                response.setTerraformSnippet(root.get("terraformSnippet").asText());
            }

            // Parse cost estimate
            if (root.has("costEstimate") && !root.get("costEstimate").isNull() && root.get("costEstimate").isObject()) {
                DesignResponseDTO.CostEstimate costEstimate = objectMapper.convertValue(
                        root.get("costEstimate"),
                        DesignResponseDTO.CostEstimate.class
                );
                response.setCostEstimate(costEstimate);
            }

            return response;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM response as JSON: {}", truncate(rawContent, 200), e);
            throw new RuntimeException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }

    /**
     * Extract JSON from raw content (may be wrapped in markdown code blocks).
     */
    private String extractJson(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new RuntimeException("Empty LLM response");
        }

        // Try to extract from code block
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(rawContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // If no code block, try to parse raw content as JSON
        String trimmed = rawContent.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        // Try to find JSON object in the content
        int start = rawContent.indexOf('{');
        int end = rawContent.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return rawContent.substring(start, end + 1);
        }

        log.warn("Could not extract JSON from response: {}", truncate(rawContent, 500));
        throw new RuntimeException("Could not extract JSON from LLM response");
    }

    /**
     * Update entity with parsed response data.
     */
    private void updateEntityFromParsed(DesignEntity entity, DesignResponseDTO parsed) {
        entity.setArchitectureText(parsed.getArchitectureText());

        if (parsed.getServices() != null) {
            entity.setServicesJson(serialize(parsed.getServices()));
        }

        if (parsed.getTechStack() != null) {
            entity.setTechStackJson(serialize(parsed.getTechStack()));
        }

        if (parsed.getInfrastructure() != null) {
            entity.setInfrastructureJson(serialize(parsed.getInfrastructure()));
            if (parsed.getInfrastructure().getDiagramMermaid() != null) {
                entity.setDiagramMermaid(parsed.getInfrastructure().getDiagramMermaid());
            }
        }

        entity.setTerraformSnippet(parsed.getTerraformSnippet());

        if (parsed.getCostEstimate() != null) {
            entity.setCostEstimateJson(serialize(parsed.getCostEstimate()));
        }
    }

    /**
     * Convert entity to DTO.
     */
    private DesignResponseDTO entityToDto(DesignEntity entity) {
        DesignResponseDTO dto = DesignResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .generatedAt(entity.getCreatedAt())
                .architectureText(entity.getArchitectureText())
                .terraformSnippet(entity.getTerraformSnippet())
                .llmProvider(entity.getLlmProvider())
                .tokensUsed(entity.getTokensUsed())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .build();

        // Deserialize JSON fields
        if (entity.getServicesJson() != null) {
            dto.setServices(deserialize(entity.getServicesJson(),
                    new TypeReference<List<DesignResponseDTO.ServiceComponent>>() {}));
        }

        if (entity.getTechStackJson() != null) {
            dto.setTechStack(deserialize(entity.getTechStackJson(), DesignResponseDTO.TechStack.class));
        }

        if (entity.getInfrastructureJson() != null) {
            dto.setInfrastructure(deserialize(entity.getInfrastructureJson(), DesignResponseDTO.Infrastructure.class));
        }

        if (entity.getCostEstimateJson() != null) {
            dto.setCostEstimate(deserialize(entity.getCostEstimateJson(), DesignResponseDTO.CostEstimate.class));
        }

        // Include prompt if configured
        if (llmConfig.isIncludePromptInResponse() && entity.getPromptUsed() != null) {
            dto.setPromptUsed(entity.getPromptUsed());
        }

        return dto;
    }

    /**
     * Convert entity to summary for list display.
     */
    private DesignListDTO.DesignSummary entityToSummary(DesignEntity entity) {
        DesignListDTO.DesignSummary summary = DesignListDTO.DesignSummary.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .llmProvider(entity.getLlmProvider())
                .generatedAt(entity.getCreatedAt())
                .build();

        // Extract cloud provider and architecture style from preferences
        if (entity.getPreferencesJson() != null) {
            try {
                JsonNode prefs = objectMapper.readTree(entity.getPreferencesJson());
                if (prefs.has("cloudProvider")) {
                    summary.setCloudProvider(prefs.get("cloudProvider").asText());
                }
                if (prefs.has("architectureStyle")) {
                    summary.setArchitectureStyle(prefs.get("architectureStyle").asText());
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse preferences JSON", e);
            }
        }

        return summary;
    }

    private String serializePreferences(GenerateRequestDTO.DesignPreferences prefs) {
        if (prefs == null) return null;
        return serialize(prefs);
    }

    private GenerateRequestDTO.DesignPreferences deserializePreferences(String json) {
        if (json == null) return null;
        return deserialize(json, GenerateRequestDTO.DesignPreferences.class);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object", e);
            return null;
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON", e);
            return null;
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON", e);
            return null;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
