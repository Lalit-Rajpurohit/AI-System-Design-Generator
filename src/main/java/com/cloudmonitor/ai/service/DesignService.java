package com.cloudmonitor.ai.service;

import com.cloudmonitor.ai.dto.DesignListDTO;
import com.cloudmonitor.ai.dto.DesignResponseDTO;
import com.cloudmonitor.ai.dto.GenerateRequestDTO;

import java.util.UUID;

/**
 * Service interface for generating and managing system designs.
 */
public interface DesignService {

    /**
     * Generate a new system design based on the request.
     * Supports idempotency - returns cached result for identical requests.
     *
     * @param request The generation request
     * @return The generated design response
     */
    DesignResponseDTO generate(GenerateRequestDTO request);

    /**
     * Generate a preview without persisting.
     *
     * @param request The generation request
     * @return The generated design response (not persisted)
     */
    DesignResponseDTO generatePreview(GenerateRequestDTO request);

    /**
     * Get a design by ID.
     *
     * @param id The design ID
     * @return The design response
     */
    DesignResponseDTO getById(UUID id);

    /**
     * List designs with pagination and filters.
     *
     * @param page       Page number (0-indexed)
     * @param size       Page size
     * @param userId     Optional user ID filter
     * @param status     Optional status filter
     * @param searchQuery Optional search query for title
     * @return Paginated list of designs
     */
    DesignListDTO list(int page, int size, String userId, String status, String searchQuery);

    /**
     * Regenerate an existing design.
     *
     * @param id The design ID to regenerate
     * @return The regenerated design response
     */
    DesignResponseDTO regenerate(UUID id);

    /**
     * Delete a design by ID.
     *
     * @param id The design ID
     */
    void delete(UUID id);

    /**
     * Purge designs older than the specified number of days.
     *
     * @param olderThanDays Number of days
     * @return Number of designs deleted
     */
    int purgeOldDesigns(int olderThanDays);
}
