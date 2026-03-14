package com.cloudmonitor.ai.repository;

import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import com.cloudmonitor.ai.model.DesignEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DesignRepository using H2.
 */
@DataJpaTest
@ActiveProfiles("test")
class DesignRepositoryTest {

    @Autowired
    private DesignRepository repository;

    @Test
    @DisplayName("Save and retrieve design by ID")
    void saveAndFindById() {
        // Arrange
        DesignEntity entity = createTestEntity("Test Design", "test-user");

        // Act
        DesignEntity saved = repository.save(entity);
        Optional<DesignEntity> found = repository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Design", found.get().getTitle());
        assertEquals("test-user", found.get().getUserId());
        assertEquals(DesignStatus.COMPLETED, found.get().getStatus());
    }

    @Test
    @DisplayName("Find design by idempotency key")
    void findByIdempotencyKey() {
        // Arrange
        DesignEntity entity = createTestEntity("Test Design", "user-1");
        entity.setIdempotencyKey("unique-key-123");
        repository.save(entity);

        // Act
        Optional<DesignEntity> found = repository.findByIdempotencyKey("unique-key-123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Design", found.get().getTitle());
    }

    @Test
    @DisplayName("Find designs by user ID with pagination")
    void findByUserIdOrderByCreatedAtDesc() {
        // Arrange
        repository.save(createTestEntity("Design 1", "user-1"));
        repository.save(createTestEntity("Design 2", "user-1"));
        repository.save(createTestEntity("Design 3", "user-2"));

        // Act
        Page<DesignEntity> user1Designs = repository.findByUserIdOrderByCreatedAtDesc(
                "user-1", PageRequest.of(0, 10));
        Page<DesignEntity> user2Designs = repository.findByUserIdOrderByCreatedAtDesc(
                "user-2", PageRequest.of(0, 10));

        // Assert
        assertEquals(2, user1Designs.getTotalElements());
        assertEquals(1, user2Designs.getTotalElements());
    }

    @Test
    @DisplayName("Find designs by status")
    void findByStatusOrderByCreatedAtDesc() {
        // Arrange
        DesignEntity completed = createTestEntity("Completed Design", "user-1");
        completed.setStatus(DesignStatus.COMPLETED);
        repository.save(completed);

        DesignEntity failed = createTestEntity("Failed Design", "user-1");
        failed.setStatus(DesignStatus.FAILED);
        repository.save(failed);

        // Act
        Page<DesignEntity> completedDesigns = repository.findByStatusOrderByCreatedAtDesc(
                DesignStatus.COMPLETED, PageRequest.of(0, 10));
        Page<DesignEntity> failedDesigns = repository.findByStatusOrderByCreatedAtDesc(
                DesignStatus.FAILED, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, completedDesigns.getTotalElements());
        assertEquals(1, failedDesigns.getTotalElements());
    }

    @Test
    @DisplayName("Search designs by title")
    void searchByTitle() {
        // Arrange
        repository.save(createTestEntity("E-commerce Platform", "user-1"));
        repository.save(createTestEntity("Chat Application", "user-1"));
        repository.save(createTestEntity("E-commerce API", "user-2"));

        // Act
        Page<DesignEntity> results = repository.searchByTitle(
                "e-commerce", PageRequest.of(0, 10));

        // Assert
        assertEquals(2, results.getTotalElements());
    }

    @Test
    @DisplayName("Count designs by status")
    void countByStatus() {
        // Arrange
        for (int i = 0; i < 3; i++) {
            DesignEntity entity = createTestEntity("Design " + i, "user-1");
            entity.setStatus(DesignStatus.COMPLETED);
            repository.save(entity);
        }

        DesignEntity failed = createTestEntity("Failed", "user-1");
        failed.setStatus(DesignStatus.FAILED);
        repository.save(failed);

        // Act
        long completedCount = repository.countByStatus(DesignStatus.COMPLETED);
        long failedCount = repository.countByStatus(DesignStatus.FAILED);

        // Assert
        assertEquals(3, completedCount);
        assertEquals(1, failedCount);
    }

    @Test
    @DisplayName("Check idempotency key exists")
    void existsByIdempotencyKey() {
        // Arrange
        DesignEntity entity = createTestEntity("Test", "user-1");
        entity.setIdempotencyKey("existing-key");
        repository.save(entity);

        // Act & Assert
        assertTrue(repository.existsByIdempotencyKey("existing-key"));
        assertFalse(repository.existsByIdempotencyKey("non-existing-key"));
    }

    @Test
    @DisplayName("Find designs created before cutoff time")
    void findByCreatedAtBefore() {
        // Arrange
        DesignEntity oldDesign = createTestEntity("Old Design", "user-1");
        repository.save(oldDesign);

        // Simulate an old design by creating a new one and checking against future cutoff
        Instant futureCutoff = Instant.now().plus(1, ChronoUnit.DAYS);

        // Act
        var oldDesigns = repository.findByCreatedAtBefore(futureCutoff);

        // Assert
        assertFalse(oldDesigns.isEmpty());
    }

    private DesignEntity createTestEntity(String title, String userId) {
        return DesignEntity.builder()
                .title(title)
                .description("Test description for " + title)
                .userId(userId)
                .architectureText("Sample architecture text")
                .status(DesignStatus.COMPLETED)
                .llmProvider("openrouter")
                .tokensUsed(1000)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }
}
