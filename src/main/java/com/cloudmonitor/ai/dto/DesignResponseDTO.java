package com.cloudmonitor.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for generated system architecture designs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DesignResponseDTO {

    private UUID id;
    private String title;
    private Instant generatedAt;
    private String architectureText;
    private List<ServiceComponent> services;
    private TechStack techStack;
    private Infrastructure infrastructure;
    private String terraformSnippet;
    private CostEstimate costEstimate;
    private String promptUsed;
    private String llmProvider;
    private Integer tokensUsed;
    private DesignStatus status;
    private String errorMessage;

    /**
     * Represents a service component in the architecture.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceComponent {
        private String name;
        private String description;
        private String technology;
        private List<String> dependencies;
    }

    /**
     * Technology stack recommendations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechStack {
        private String frontend;
        private String backend;
        private String database;
        private String cache;
        private String messaging;
        private String search;
        private String monitoring;
        private Map<String, String> additional;
    }

    /**
     * Infrastructure configuration.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Infrastructure {
        private String cloud;
        private List<String> components;
        private String diagramMermaid;
        private String diagramSvg;
        private ScalingStrategy scalingStrategy;
        private List<SecurityRecommendation> securityRecommendations;
    }

    /**
     * Scaling strategy recommendations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScalingStrategy {
        private String type;
        private String description;
        private List<String> keyMetrics;
        private Map<String, String> thresholds;
    }

    /**
     * Security recommendations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityRecommendation {
        private String category;
        private String recommendation;
        private String priority;
    }

    /**
     * Cost estimate information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostEstimate {
        private String monthly;
        private String yearly;
        private String breakdown;
        private String assumptions;
    }

    /**
     * Design generation status.
     */
    public enum DesignStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
