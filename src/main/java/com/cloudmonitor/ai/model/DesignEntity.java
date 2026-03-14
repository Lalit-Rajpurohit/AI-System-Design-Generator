package com.cloudmonitor.ai.model;

import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for persisting generated system designs.
 */
@Entity
@Table(name = "designs", indexes = {
        @Index(name = "idx_designs_created_at", columnList = "created_at"),
        @Index(name = "idx_designs_status", columnList = "status"),
        @Index(name = "idx_designs_user_id", columnList = "user_id"),
        @Index(name = "idx_designs_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String description;

    /**
     * JSON serialized preferences.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String preferencesJson;

    /**
     * Full architecture explanation text.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String architectureText;

    /**
     * JSON serialized list of services.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String servicesJson;

    /**
     * JSON serialized tech stack.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String techStackJson;

    /**
     * JSON serialized infrastructure config.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String infrastructureJson;

    /**
     * Mermaid diagram source code.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String diagramMermaid;

    /**
     * Terraform snippet (HCL code).
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String terraformSnippet;

    /**
     * JSON serialized cost estimate.
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String costEstimateJson;

    @Column(length = 50)
    private String llmProvider;

    @Column
    private Integer tokensUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DesignStatus status;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String errorMessage;

    /**
     * Optional user ID for tracking.
     */
    @Column(name = "user_id", length = 100)
    private String userId;

    /**
     * Hash of title + description + preferences for idempotency.
     */
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    /**
     * Original prompt sent to LLM (for debugging).
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String promptUsed;

    /**
     * Raw LLM response (for debugging).
     */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawLlmResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
