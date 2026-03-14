package com.cloudmonitor.ai.service;

import com.cloudmonitor.ai.config.LLMConfig;
import com.cloudmonitor.ai.dto.*;
import com.cloudmonitor.ai.dto.DesignResponseDTO.DesignStatus;
import com.cloudmonitor.ai.model.DesignEntity;
import com.cloudmonitor.ai.repository.DesignRepository;
import com.cloudmonitor.ai.service.impl.DesignServiceImpl;
import com.cloudmonitor.ai.util.PromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DesignServiceImpl with mocked LLM client.
 */
@ExtendWith(MockitoExtension.class)
class DesignServiceTest {

    @Mock
    private DesignRepository repository;

    @Mock
    private LLMClient llmClient;

    @Mock
    private LLMConfig llmConfig;

    private DesignServiceImpl designService;
    private ObjectMapper objectMapper;
    private PromptBuilder promptBuilder;

    private static final String SAMPLE_LLM_RESPONSE = """
            ```json
            {
              "title": "E-commerce Platform",
              "architectureText": "A microservices-based e-commerce platform with separate services for users, products, orders, and payments.",
              "services": [
                {"name": "User Service", "description": "Handles authentication and user profiles", "technology": "Spring Boot", "dependencies": []},
                {"name": "Product Service", "description": "Product catalog management", "technology": "Spring Boot", "dependencies": []},
                {"name": "Order Service", "description": "Order processing", "technology": "Spring Boot", "dependencies": ["User Service", "Product Service"]}
              ],
              "techStack": {
                "frontend": "React",
                "backend": "Spring Boot",
                "database": "PostgreSQL",
                "cache": "Redis",
                "messaging": "Kafka",
                "search": null,
                "monitoring": "Prometheus + Grafana",
                "additional": {}
              },
              "infrastructure": {
                "cloud": "AWS",
                "components": ["ALB", "EKS", "RDS", "ElastiCache", "MSK"],
                "diagramMermaid": "graph TD;\\n  User-->ALB;\\n  ALB-->API;\\n  API-->DB;",
                "scalingStrategy": {"type": "horizontal", "description": "Auto-scale based on CPU", "keyMetrics": ["cpu", "memory"], "thresholds": {"cpu": "70%"}},
                "securityRecommendations": [
                  {"category": "Network", "recommendation": "Use VPC with private subnets", "priority": "HIGH"}
                ]
              },
              "terraformSnippet": "resource \\"aws_eks_cluster\\" \\"main\\" {\\n  name = \\"ecommerce\\"\\n}",
              "costEstimate": {"monthly": "USD 2,500", "yearly": "USD 30,000", "breakdown": "Compute: 50%, Database: 30%, Other: 20%", "assumptions": "Medium traffic"}
            }
            ```
            """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptBuilder = new PromptBuilder(objectMapper);
        designService = new DesignServiceImpl(repository, llmClient, llmConfig, promptBuilder, objectMapper);
    }

    @Test
    @DisplayName("Generate should parse LLM response and persist design")
    void generate_shouldParseAndPersist() {
        // Arrange
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("E-commerce Platform")
                .description("Build a scalable e-commerce platform")
                .preferences(GenerateRequestDTO.DesignPreferences.builder()
                        .cloudProvider(GenerateRequestDTO.CloudProvider.AWS)
                        .architectureStyle(GenerateRequestDTO.ArchitectureStyle.MICROSERVICES)
                        .includeTerraform(true)
                        .includeCostEstimate(true)
                        .build())
                .build();

        LLMResponseDTO llmResponse = LLMResponseDTO.builder()
                .choices(java.util.List.of(
                        LLMResponseDTO.Choice.builder()
                                .message(LLMResponseDTO.Message.builder()
                                        .content(SAMPLE_LLM_RESPONSE)
                                        .build())
                                .build()
                ))
                .usage(LLMResponseDTO.Usage.builder()
                        .totalTokens(1500)
                        .build())
                .build();

        when(llmClient.generate(anyString(), anyString())).thenReturn(llmResponse);
        when(llmClient.getProviderName()).thenReturn("openrouter");
        when(repository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(DesignEntity.class))).thenAnswer(invocation -> {
            DesignEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        // Act
        DesignResponseDTO result = designService.generate(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("E-commerce Platform", result.getTitle());
        assertNotNull(result.getArchitectureText());
        assertTrue(result.getArchitectureText().contains("microservices"));

        // Verify services parsed correctly
        assertNotNull(result.getServices());
        assertEquals(3, result.getServices().size());
        assertEquals("User Service", result.getServices().get(0).getName());

        // Verify tech stack
        assertNotNull(result.getTechStack());
        assertEquals("React", result.getTechStack().getFrontend());
        assertEquals("Spring Boot", result.getTechStack().getBackend());

        // Verify infrastructure
        assertNotNull(result.getInfrastructure());
        assertEquals("AWS", result.getInfrastructure().getCloud());
        assertNotNull(result.getInfrastructure().getDiagramMermaid());

        // Verify terraform
        assertNotNull(result.getTerraformSnippet());
        assertTrue(result.getTerraformSnippet().contains("aws_eks_cluster"));

        // Verify cost estimate
        assertNotNull(result.getCostEstimate());
        assertEquals("USD 2,500", result.getCostEstimate().getMonthly());

        // Verify repository was called
        verify(repository, times(2)).save(any(DesignEntity.class));
    }

    @Test
    @DisplayName("Generate should return cached design for identical request")
    void generate_shouldReturnCachedDesign() {
        // Arrange
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .regenerate(false)
                .build();

        DesignEntity existingEntity = DesignEntity.builder()
                .id(UUID.randomUUID())
                .title("Test App")
                .description("Test description")
                .architectureText("Existing architecture")
                .status(DesignStatus.COMPLETED)
                .llmProvider("openrouter")
                .build();

        when(repository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(existingEntity));

        // Act
        DesignResponseDTO result = designService.generate(request);

        // Assert
        assertNotNull(result);
        assertEquals(existingEntity.getId(), result.getId());
        assertEquals("Test App", result.getTitle());
        assertEquals("Existing architecture", result.getArchitectureText());

        // Verify LLM was NOT called
        verify(llmClient, never()).generate(anyString(), anyString());
    }

    @Test
    @DisplayName("Generate should call LLM when regenerate is true")
    void generate_shouldCallLLM_whenRegenerateTrue() {
        // Arrange
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .regenerate(true)
                .build();

        LLMResponseDTO llmResponse = LLMResponseDTO.builder()
                .choices(java.util.List.of(
                        LLMResponseDTO.Choice.builder()
                                .message(LLMResponseDTO.Message.builder()
                                        .content(SAMPLE_LLM_RESPONSE)
                                        .build())
                                .build()
                ))
                .usage(LLMResponseDTO.Usage.builder().totalTokens(1000).build())
                .build();

        when(llmClient.generate(anyString(), anyString())).thenReturn(llmResponse);
        when(llmClient.getProviderName()).thenReturn("openrouter");
        when(repository.save(any(DesignEntity.class))).thenAnswer(invocation -> {
            DesignEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        // Act
        DesignResponseDTO result = designService.generate(request);

        // Assert
        assertNotNull(result);

        // Verify LLM WAS called even though we didn't check for existing
        verify(llmClient).generate(anyString(), anyString());
    }

    @Test
    @DisplayName("Generate should set status to FAILED on LLM error")
    void generate_shouldSetStatusFailed_onLLMError() {
        // Arrange
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Test App")
                .description("Test description")
                .build();

        when(repository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(llmClient.getProviderName()).thenReturn("openrouter");
        when(llmClient.generate(anyString(), anyString()))
                .thenThrow(new com.cloudmonitor.ai.exception.LLMException("API error"));
        when(repository.save(any(DesignEntity.class))).thenAnswer(invocation -> {
            DesignEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        // Act & Assert
        assertThrows(com.cloudmonitor.ai.exception.LLMException.class, () -> {
            designService.generate(request);
        });

        // Verify entity was saved with FAILED status
        ArgumentCaptor<DesignEntity> captor = ArgumentCaptor.forClass(DesignEntity.class);
        verify(repository, atLeast(2)).save(captor.capture());

        DesignEntity savedEntity = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(DesignStatus.FAILED, savedEntity.getStatus());
        assertNotNull(savedEntity.getErrorMessage());
    }

    @Test
    @DisplayName("Generate preview should not persist")
    void generatePreview_shouldNotPersist() {
        // Arrange
        GenerateRequestDTO request = GenerateRequestDTO.builder()
                .title("Preview Test")
                .description("Test description")
                .build();

        LLMResponseDTO llmResponse = LLMResponseDTO.builder()
                .choices(java.util.List.of(
                        LLMResponseDTO.Choice.builder()
                                .message(LLMResponseDTO.Message.builder()
                                        .content(SAMPLE_LLM_RESPONSE)
                                        .build())
                                .build()
                ))
                .usage(LLMResponseDTO.Usage.builder().totalTokens(1000).build())
                .build();

        when(llmClient.generate(anyString(), anyString())).thenReturn(llmResponse);
        when(llmClient.getProviderName()).thenReturn("openrouter");

        // Act
        DesignResponseDTO result = designService.generatePreview(request);

        // Assert
        assertNotNull(result);
        assertEquals(DesignStatus.COMPLETED, result.getStatus());

        // Verify repository was NOT called
        verify(repository, never()).save(any(DesignEntity.class));
    }
}
