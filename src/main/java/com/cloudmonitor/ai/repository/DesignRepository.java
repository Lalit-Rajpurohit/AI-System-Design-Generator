package com.cloudmonitor.ai.repository;

import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import com.cloudmonitor.ai.model.DesignEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for design entities.
 */
@Repository
public interface DesignRepository extends JpaRepository<DesignEntity, UUID> {

    /**
     * Find a design by its idempotency key.
     */
    Optional<DesignEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all designs by user ID.
     */
    Page<DesignEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find all designs by status.
     */
    Page<DesignEntity> findByStatusOrderByCreatedAtDesc(DesignStatus status, Pageable pageable);

    /**
     * Find recent designs ordered by creation time.
     */
    Page<DesignEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Search designs by title containing text (case-insensitive).
     */
    @Query("SELECT d FROM DesignEntity d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY d.createdAt DESC")
    Page<DesignEntity> searchByTitle(@Param("query") String query, Pageable pageable);

    /**
     * Find designs created before a specific time (for purging old records).
     */
    List<DesignEntity> findByCreatedAtBefore(Instant cutoffTime);

    /**
     * Delete designs older than a specified time.
     */
    @Modifying
    @Query("DELETE FROM DesignEntity d WHERE d.createdAt < :cutoffTime")
    int deleteByCreatedAtBefore(@Param("cutoffTime") Instant cutoffTime);

    /**
     * Count designs by status.
     */
    long countByStatus(DesignStatus status);

    /**
     * Find designs by user ID and status.
     */
    Page<DesignEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId,
            DesignStatus status,
            Pageable pageable
    );

    /**
     * Check if a design with idempotency key exists.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
