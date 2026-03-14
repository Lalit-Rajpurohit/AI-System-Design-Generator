package com.cloudmonitor.ai.service.impl;

import com.cloudmonitor.ai.config.LLMConfig;
import com.cloudmonitor.ai.dto.LLMRequestDTO;
import com.cloudmonitor.ai.dto.LLMResponseDTO;
import com.cloudmonitor.ai.exception.LLMException;
import com.cloudmonitor.ai.service.LLMClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LLM client implementation for OpenRouter and OpenAI-compatible APIs.
 *
 * Supports:
 * - OpenRouter (https://openrouter.ai)
 * - Groq (https://api.groq.com)
 * - OpenAI (https://api.openai.com)
 * - Any OpenAI-compatible API
 */
@Service
@Slf4j
public class OpenRouterLLMClient implements LLMClient {

    private final LLMConfig config;
    private final ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    public OpenRouterLLMClient(LLMConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(config.getTimeoutMs()))
                .setResponseTimeout(Timeout.ofMilliseconds(config.getTimeoutMs()))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        log.info("Initialized LLM client for provider: {} with URL: {}",
                config.getProvider(), config.getUrl());
    }

    @PreDestroy
    public void cleanup() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.warn("Error closing HTTP client", e);
            }
        }
    }

    @Override
    @CircuitBreaker(name = "llmClient", fallbackMethod = "fallbackGenerate")
    public LLMResponseDTO generate(LLMRequestDTO request) throws LLMException {
        log.debug("Sending LLM request to {} with model {}", config.getUrl(), request.getModel());

        try {
            HttpPost httpPost = new HttpPost(config.getUrl());
            httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            // Add provider-specific headers
            if (config.getProvider().equalsIgnoreCase("openrouter")) {
                httpPost.setHeader("HTTP-Referer", "https://ai-system-designer.local");
                httpPost.setHeader("X-Title", "AI System Designer");
            }

            // Set model and parameters if not already set
            if (request.getModel() == null) {
                request.setModel(config.getModel());
            }
            if (request.getTemperature() == null) {
                request.setTemperature(config.getTemperature());
            }
            if (request.getMaxTokens() == null) {
                request.setMaxTokens(config.getMaxTokens());
            }

            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            log.debug("LLM request body length: {} chars", requestBody.length());

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {
                    healthy.set(true);
                    LLMResponseDTO llmResponse = objectMapper.readValue(responseBody, LLMResponseDTO.class);
                    log.info("LLM response received. Tokens used: {}", llmResponse.getTotalTokensUsed());
                    return llmResponse;
                } else {
                    healthy.set(false);
                    boolean retryable = statusCode == 429 || statusCode >= 500;
                    String errorMsg = String.format("LLM API error: status=%d, body=%s",
                            statusCode, truncate(responseBody, 500));
                    log.error(errorMsg);
                    throw new LLMException(errorMsg, config.getProvider(), retryable, statusCode);
                }
            });

        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            healthy.set(false);
            log.error("LLM request failed", e);
            throw new LLMException("LLM request failed: " + e.getMessage(), e, config.getProvider(), true);
        }
    }

    @Override
    @CircuitBreaker(name = "llmClient", fallbackMethod = "fallbackGenerateSimple")
    public LLMResponseDTO generate(String systemMessage, String userMessage) throws LLMException {
        LLMRequestDTO request = LLMRequestDTO.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .messages(List.of(
                        LLMRequestDTO.Message.builder()
                                .role("system")
                                .content(systemMessage)
                                .build(),
                        LLMRequestDTO.Message.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build();

        return generate(request);
    }

    @Override
    public String getProviderName() {
        return config.getProvider();
    }

    @Override
    public boolean isHealthy() {
        return healthy.get();
    }

    /**
     * Fallback method when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    private LLMResponseDTO fallbackGenerate(LLMRequestDTO request, Throwable t) {
        log.error("Circuit breaker fallback triggered for LLM request", t);
        throw new LLMException("LLM service temporarily unavailable (circuit breaker open)",
                config.getProvider(), true);
    }

    /**
     * Fallback method when circuit breaker is open (simple version).
     */
    @SuppressWarnings("unused")
    private LLMResponseDTO fallbackGenerateSimple(String systemMessage, String userMessage, Throwable t) {
        log.error("Circuit breaker fallback triggered for LLM request", t);
        throw new LLMException("LLM service temporarily unavailable (circuit breaker open)",
                config.getProvider(), true);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
