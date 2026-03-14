package com.cloudmonitor.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for LLM integration.
 *
 * Configure these via application.properties or environment variables:
 * - LLM_PROVIDER: openrouter, groq, gemini, huggingface
 * - LLM_API_KEY: Your API key (REQUIRED - do not hardcode)
 * - LLM_URL: API endpoint URL
 * - LLM_MODEL: Model identifier
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
@Validated
@Getter
@Setter
public class LLMConfig {

    /**
     * LLM provider name: openrouter, groq, gemini, huggingface
     */
    @NotBlank(message = "LLM provider must be specified")
    private String provider = "openrouter";

    /**
     * API key for the LLM provider.
     * IMPORTANT: Set via environment variable LLM_API_KEY, never hardcode!
     */
    @NotBlank(message = "LLM API key must be specified via environment variable LLM_API_KEY")
    private String apiKey;

    /**
     * Base URL for the LLM API endpoint.
     */
    @NotBlank(message = "LLM URL must be specified")
    private String url = "https://openrouter.ai/api/v1/chat/completions";

    /**
     * Model identifier to use for generation.
     */
    @NotBlank(message = "LLM model must be specified")
    private String model = "mistralai/mistral-7b-instruct";

    /**
     * Request timeout in milliseconds.
     */
    @Positive
    private int timeoutMs = 20000;

    /**
     * Maximum tokens to generate.
     */
    @Positive
    private int maxTokens = 4096;

    /**
     * Temperature for generation (0.0 - 2.0).
     */
    private double temperature = 0.7;

    /**
     * Maximum retries on transient failures.
     */
    @Positive
    private int maxRetries = 3;

    /**
     * Whether to include raw prompt in response (for debugging).
     */
    private boolean includePromptInResponse = false;
}
