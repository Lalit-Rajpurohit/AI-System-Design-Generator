package com.cloudmonitor.ai.dto;

import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for listing designs with pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DesignListDTO {

    private List<DesignSummary> designs;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * Summary view of a design for list display.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignSummary {
        private UUID id;
        private String title;
        private DesignStatus status;
        private String llmProvider;
        private Instant generatedAt;
        private String cloudProvider;
        private String architectureStyle;
    }
}
