package com.cloudmonitor.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating system architecture designs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    @Valid
    private DesignPreferences preferences;

    /**
     * Optional user ID for audit and tracking purposes.
     */
    @Size(max = 100, message = "User ID must not exceed 100 characters")
    private String userId;

    /**
     * If true, regenerate even if an identical design exists.
     */
    @Builder.Default
    private boolean regenerate = false;

    /**
     * Nested class for design preferences.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignPreferences {

        @Builder.Default
        private CloudProvider cloudProvider = CloudProvider.AWS;

        @Builder.Default
        private ArchitectureStyle architectureStyle = ArchitectureStyle.MICROSERVICES;

        @Builder.Default
        private Long expectedMonthlyActiveUsers = 100000L;

        @Builder.Default
        private DataConsistency dataConsistency = DataConsistency.EVENTUAL;

        @Builder.Default
        private boolean includeTerraform = true;

        @Builder.Default
        private DiagramFormat diagramFormat = DiagramFormat.MERMAID;

        @Builder.Default
        private boolean includeCostEstimate = false;
    }

    public enum CloudProvider {
        AWS, GCP, AZURE, MULTI_CLOUD
    }

    public enum ArchitectureStyle {
        MONOLITH, MICROSERVICES, SERVERLESS
    }

    public enum DataConsistency {
        STRONG, EVENTUAL
    }

    public enum DiagramFormat {
        MERMAID, SVG, DRAWIO
    }
}
