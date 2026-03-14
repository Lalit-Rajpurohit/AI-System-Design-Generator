package com.cloudmonitor.ai.service;

import com.cloudmonitor.ai.dto.LLMRequestDTO;
import com.cloudmonitor.ai.dto.LLMResponseDTO;
import com.cloudmonitor.ai.exception.LLMException;

/**
 * Interface for LLM client implementations.
 *
 * Allows swapping between different LLM providers:
 * - OpenRouter
 * - Groq
 * - Gemini
 * - HuggingFace
 */
public interface LLMClient {

    /**
     * Generate a response from the LLM.
     *
     * @param request The LLM request containing messages and parameters
     * @return The LLM response with generated content and usage stats
     * @throws LLMException if the LLM call fails
     */
    LLMResponseDTO generate(LLMRequestDTO request) throws LLMException;

    /**
     * Generate a response with a simple system and user message.
     *
     * @param systemMessage The system instructions
     * @param userMessage   The user prompt
     * @return The LLM response
     * @throws LLMException if the LLM call fails
     */
    LLMResponseDTO generate(String systemMessage, String userMessage) throws LLMException;

    /**
     * Get the provider name.
     */
    String getProviderName();

    /**
     * Check if the client is healthy and can accept requests.
     */
    boolean isHealthy();
}
