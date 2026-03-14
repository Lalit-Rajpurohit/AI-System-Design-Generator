package com.cloudmonitor.ai.util;

import com.cloudmonitor.ai.dto.GenerateRequestDTO;
import com.cloudmonitor.ai.dto.GenerateRequestDTO.DesignPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PromptBuilder.
 */
class PromptBuilderTest {

    private PromptBuilder promptBuilder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptBuilder = new PromptBuilder(objectMapper);
    }

    @Test
    @DisplayName("System prompt should contain required instructions")
    void buildSystemPrompt_containsRequiredInstructions() {
        String systemPrompt = promptBuilder.buildSystemPrompt();

        assertNotNull(systemPrompt);
        assertTrue(systemPrompt.contains("ArchitectGen"));
        assertTrue(systemPrompt.contains("JSON"));
        assertTrue(systemPrompt.contains("architectureText"));
        assertTrue(systemPrompt.contains("services"));
        assertTrue(systemPrompt.contains("techStack"));
        assertTrue(systemPrompt.contains("infrastructure"));
        assertTrue(systemPrompt.contains("diagramMermaid"));
        assertTrue(systemPrompt.contains("terraformSnippet"));
    }

    @Test
    @DisplayName("System prompt should contain few-shot examples")
    void buildSystemPrompt_containsFewShotExamples() {
        String systemPrompt = promptBuilder.buildSystemPrompt();

        assertTrue(systemPrompt.contains("EXAMPLE 1"));
        assertTrue(systemPrompt.contains("EXAMPLE 2"));
        assertTrue(systemPrompt.contains("URL shortener"));
        assertTrue(systemPrompt.contains("chat"));
    }

    @Test
    @DisplayName("User prompt should include title and description")
    void buildUserPrompt_includesTitleAndDescription() {
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test E-commerce Platform")
                .description("A scalable e-commerce system with payments")
                .build();

        String userPrompt = promptBuilder.buildUserPrompt(request);

        assertNotNull(userPrompt);
        assertTrue(userPrompt.contains("Test E-commerce Platform"));
        assertTrue(userPrompt.contains("A scalable e-commerce system with payments"));
    }

    @Test
    @DisplayName("User prompt should include preferences")
    void buildUserPrompt_includesPreferences() {
        DesignPreferences prefs = DesignPreferences.builder()
                .cloudProvider(GenerateRequestDTO.CloudProvider.GCP)
                .architectureStyle(GenerateRequestDTO.ArchitectureStyle.SERVERLESS)
                .includeTerraform(true)
                .includeCostEstimate(true)
                .build();

        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .preferences(prefs)
                .build();

        String userPrompt = promptBuilder.buildUserPrompt(request);

        assertTrue(userPrompt.contains("GCP"));
        assertTrue(userPrompt.contains("SERVERLESS"));
        assertTrue(userPrompt.contains("terraform"));
        assertTrue(userPrompt.contains("cost estimate"));
    }

    @Test
    @DisplayName("User prompt should use default values when preferences are null")
    void buildUserPrompt_usesDefaults_whenPreferencesNull() {
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .preferences(null)
                .build();

        String userPrompt = promptBuilder.buildUserPrompt(request);

        assertTrue(userPrompt.contains("AWS"));
        assertTrue(userPrompt.contains("MICROSERVICES"));
    }

    @Test
    @DisplayName("Idempotency key should be consistent for same input")
    void generateIdempotencyKey_isConsistent() {
        GenerateRequestDTO request1 = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .preferences(DesignPreferences.builder().build())
                .build();

        GenerateRequestDTO request2 = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .preferences(DesignPreferences.builder().build())
                .build();

        String key1 = promptBuilder.generateIdempotencyKey(request1);
        String key2 = promptBuilder.generateIdempotencyKey(request2);

        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("Idempotency key should differ for different inputs")
    void generateIdempotencyKey_differsForDifferentInputs() {
        GenerateRequestDTO request1 = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .build();

        GenerateRequestDTO request2 = GenerateRequestDTO.builder()
                .title("Different App")
                .description("Different description")
                .build();

        String key1 = promptBuilder.generateIdempotencyKey(request1);
        String key2 = promptBuilder.generateIdempotencyKey(request2);

        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Idempotency key should be SHA-256 hash format")
    void generateIdempotencyKey_isSha256Format() {
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .build();

        String key = promptBuilder.generateIdempotencyKey(request);

        assertNotNull(key);
        assertEquals(64, key.length()); // SHA-256 produces 64 hex characters
        assertTrue(key.matches("[a-f0-9]+"));
    }
}
