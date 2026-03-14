package com.cloudmonitor.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for LLM API calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequestDTO {

    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
