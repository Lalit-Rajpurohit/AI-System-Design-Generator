package com.cloudmonitor.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI System Designer Application - Main entry point.
 *
 * This application provides AI-powered system architecture design generation
 * using configurable LLM providers (OpenRouter, Groq, Gemini, HuggingFace).
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class AiSystemDesignerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSystemDesignerApplication.class, args);
    }
}
