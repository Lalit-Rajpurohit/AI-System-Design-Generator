package com.cloudmonitor.ai.util;

import com.cloudmonitor.ai.dto.GenerateRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds prompts for the LLM to generate system architecture designs.
 *
 * Uses a two-stage prompt approach:
 * 1. System instruction - Fixed formatting and output constraints
 * 2. User prompt - User's description and preferences
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptBuilder {

    private final ObjectMapper objectMapper;

    /**
     * System instruction prompt (fixed).
     */
    public static final String SYSTEM_INSTRUCTION = """
            You are "ArchitectGen", an AI that outputs structured JSON describing system architecture.

            CRITICAL RULES:
            1. ALWAYS respond in JSON only
            2. Wrap the JSON in a single code block (```json ... ```)
            3. The JSON must match the schema exactly with these keys:
               - title (string)
               - architectureText (string - detailed explanation)
               - services (array of objects with name, description, technology, dependencies)
               - techStack (object with frontend, backend, database, cache, messaging, search, monitoring, additional)
               - infrastructure (object with cloud, components, diagramMermaid, scalingStrategy, securityRecommendations)
               - terraformSnippet (string - HCL code or null)
               - costEstimate (object with monthly, yearly, breakdown, assumptions - or null)

            4. Do NOT include commentary outside the JSON
            5. Do NOT escape the JSON beyond what's required
            6. If any field is not available, set it to null or an empty object/array
            7. Keep descriptions concise but complete
            8. For diagrams, use Mermaid syntax in the diagramMermaid field
            9. For terraformSnippet, include only minimal example resources
            10. NEVER include secrets, API keys, or passwords in terraform snippets

            SCHEMA EXAMPLE:
            ```json
            {
              "title": "Example System",
              "architectureText": "Detailed explanation of the architecture...",
              "services": [
                {"name": "API Gateway", "description": "Entry point", "technology": "Kong", "dependencies": ["Auth Service"]}
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
                "components": ["ALB", "EKS", "RDS", "ElastiCache"],
                "diagramMermaid": "graph TD;\\n  User-->ALB;\\n  ALB-->API;",
                "scalingStrategy": {
                  "type": "horizontal",
                  "description": "Auto-scale based on CPU",
                  "keyMetrics": ["cpu", "memory", "request_count"],
                  "thresholds": {"cpu": "70%", "memory": "80%"}
                },
                "securityRecommendations": [
                  {"category": "Network", "recommendation": "Use VPC with private subnets", "priority": "HIGH"}
                ]
              },
              "terraformSnippet": "resource \\"aws_instance\\" \\"example\\" {\\n  ami = \\"ami-xxx\\"\\n}",
              "costEstimate": {
                "monthly": "USD 1,200",
                "yearly": "USD 14,400",
                "breakdown": "Compute: 60%, Database: 25%, Network: 15%",
                "assumptions": "Based on medium traffic"
              }
            }
            ```
            """;

    /**
     * Few-shot examples to guide the model.
     */
    public static final String FEW_SHOT_EXAMPLES = """

            === EXAMPLE 1 ===
            INPUT: "Design a simple URL shortener service"

            OUTPUT:
            ```json
            {
              "title": "URL Shortener Service",
              "architectureText": "A lightweight URL shortening service using a simple monolithic architecture. The service generates short codes using Base62 encoding and stores mappings in a key-value store for fast lookups. Redis provides caching for frequently accessed URLs, reducing database load significantly.",
              "services": [
                {"name": "URL Service", "description": "Handles URL shortening and redirection", "technology": "Node.js/Express", "dependencies": []},
                {"name": "Analytics Service", "description": "Tracks click counts and referrers", "technology": "Node.js", "dependencies": ["URL Service"]}
              ],
              "techStack": {
                "frontend": "React SPA",
                "backend": "Node.js with Express",
                "database": "PostgreSQL",
                "cache": "Redis",
                "messaging": null,
                "search": null,
                "monitoring": "Prometheus",
                "additional": {"cdn": "CloudFlare"}
              },
              "infrastructure": {
                "cloud": "AWS",
                "components": ["CloudFront", "ALB", "ECS Fargate", "RDS", "ElastiCache"],
                "diagramMermaid": "graph TD;\\n  User[User]-->CF[CloudFront];\\n  CF-->ALB[Load Balancer];\\n  ALB-->ECS[ECS Fargate];\\n  ECS-->Redis[ElastiCache];\\n  ECS-->RDS[PostgreSQL RDS];",
                "scalingStrategy": {"type": "horizontal", "description": "Scale ECS tasks based on request count", "keyMetrics": ["request_count", "latency"], "thresholds": {"request_count": "1000/min"}},
                "securityRecommendations": [
                  {"category": "Input", "recommendation": "Validate URL format and block malicious URLs", "priority": "HIGH"},
                  {"category": "Rate Limiting", "recommendation": "Implement rate limiting per IP", "priority": "MEDIUM"}
                ]
              },
              "terraformSnippet": "resource \\"aws_ecs_service\\" \\"url_shortener\\" {\\n  name            = \\"url-shortener\\"\\n  cluster         = aws_ecs_cluster.main.id\\n  task_definition = aws_ecs_task_definition.app.arn\\n  desired_count   = 2\\n}",
              "costEstimate": {"monthly": "USD 150-300", "yearly": "USD 1,800-3,600", "breakdown": "Compute: 40%, Database: 35%, Cache: 15%, Network: 10%", "assumptions": "~1M URLs, ~10M redirects/month"}
            }
            ```

            === EXAMPLE 2 ===
            INPUT: "Design a real-time chat application for 100k concurrent users"

            OUTPUT:
            ```json
            {
              "title": "Real-time Chat Application",
              "architectureText": "A microservices-based real-time chat application designed for 100k concurrent users. Uses WebSocket connections through a scalable gateway layer. Message delivery is handled via Redis Pub/Sub for real-time distribution across server instances. Message persistence uses Cassandra for high write throughput. User presence and typing indicators are managed through a dedicated presence service.",
              "services": [
                {"name": "Gateway Service", "description": "WebSocket connection management and routing", "technology": "Go", "dependencies": []},
                {"name": "Chat Service", "description": "Message processing and delivery", "technology": "Java/Spring Boot", "dependencies": ["Gateway Service", "User Service"]},
                {"name": "User Service", "description": "User authentication and profile management", "technology": "Java/Spring Boot", "dependencies": []},
                {"name": "Presence Service", "description": "Online status and typing indicators", "technology": "Go", "dependencies": ["Gateway Service"]},
                {"name": "Notification Service", "description": "Push notifications for offline users", "technology": "Node.js", "dependencies": ["Chat Service"]}
              ],
              "techStack": {
                "frontend": "React with Socket.io client",
                "backend": "Go, Java Spring Boot, Node.js",
                "database": "Cassandra (messages), PostgreSQL (users)",
                "cache": "Redis",
                "messaging": "Redis Pub/Sub, Kafka for async",
                "search": "Elasticsearch (message search)",
                "monitoring": "Prometheus + Grafana + Jaeger",
                "additional": {"push": "Firebase Cloud Messaging"}
              },
              "infrastructure": {
                "cloud": "AWS",
                "components": ["NLB", "EKS", "Cassandra (Keyspaces)", "ElastiCache", "MSK", "OpenSearch"],
                "diagramMermaid": "graph TD;\\n  Client[Mobile/Web Client]-->NLB[Network Load Balancer];\\n  NLB-->GW[Gateway Service Pods];\\n  GW-->Redis[Redis Pub/Sub];\\n  GW-->Chat[Chat Service];\\n  Chat-->Kafka[Kafka/MSK];\\n  Chat-->Cassandra[Cassandra];\\n  Kafka-->Notif[Notification Service];",
                "scalingStrategy": {"type": "horizontal", "description": "Scale gateway pods based on connection count, chat service based on message throughput", "keyMetrics": ["ws_connections", "messages_per_sec", "cpu"], "thresholds": {"ws_connections": "10000/pod", "cpu": "70%"}},
                "securityRecommendations": [
                  {"category": "Authentication", "recommendation": "Use JWT with short expiry and refresh tokens", "priority": "HIGH"},
                  {"category": "Encryption", "recommendation": "TLS 1.3 for all connections, E2E encryption for DMs", "priority": "HIGH"},
                  {"category": "Rate Limiting", "recommendation": "Rate limit messages per user to prevent spam", "priority": "HIGH"}
                ]
              },
              "terraformSnippet": "module \\"eks\\" {\\n  source  = \\"terraform-aws-modules/eks/aws\\"\\n  version = \\"~> 19.0\\"\\n  cluster_name = \\"chat-cluster\\"\\n  cluster_version = \\"1.28\\"\\n  vpc_id = module.vpc.vpc_id\\n  subnet_ids = module.vpc.private_subnets\\n}",
              "costEstimate": {"monthly": "USD 8,000-15,000", "yearly": "USD 96,000-180,000", "breakdown": "Compute (EKS): 45%, Database (Cassandra): 25%, Cache/Messaging: 20%, Network: 10%", "assumptions": "100k concurrent, 500k registered users, 10M messages/day"}
            }
            ```
            """;

    /**
     * Build the complete system prompt.
     */
    public String buildSystemPrompt() {
        return SYSTEM_INSTRUCTION + FEW_SHOT_EXAMPLES;
    }

    /**
     * Build the user prompt from the request.
     */
    public String buildUserPrompt(GenerateRequestDTO request) {
        StringBuilder sb = new StringBuilder();

        sb.append("Title: ").append(request.getTitle()).append("\n\n");
        sb.append("Description: ").append(request.getDescription()).append("\n\n");

        if (request.getPreferences() != null) {
            GenerateRequestDTO.DesignPreferences prefs = request.getPreferences();
            sb.append("Preferences:\n");
            try {
                sb.append(objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(prefs));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize preferences", e);
                sb.append("- Cloud Provider: ").append(prefs.getCloudProvider()).append("\n");
                sb.append("- Architecture Style: ").append(prefs.getArchitectureStyle()).append("\n");
                sb.append("- Expected MAU: ").append(prefs.getExpectedMonthlyActiveUsers()).append("\n");
            }
            sb.append("\n\n");
        }

        sb.append("Guidelines:\n");
        sb.append("1) Use cloud provider: ")
                .append(request.getPreferences() != null ? request.getPreferences().getCloudProvider() : "AWS")
                .append("\n");
        sb.append("2) Use architecture style: ")
                .append(request.getPreferences() != null ? request.getPreferences().getArchitectureStyle() : "MICROSERVICES")
                .append("\n");
        sb.append("3) Output must be JSON only in the exact schema specified in system instructions\n");
        sb.append("4) Group services with brief 1-2 sentence descriptions\n");
        sb.append("5) Provide a mermaid diagram illustrating major components and connections\n");

        if (request.getPreferences() != null && request.getPreferences().isIncludeTerraform()) {
            sb.append("6) Provide a minimal terraform snippet (only resources and minimal config, under 200 lines)\n");
        } else {
            sb.append("6) Set terraformSnippet to null\n");
        }

        if (request.getPreferences() != null && request.getPreferences().isIncludeCostEstimate()) {
            sb.append("7) Provide a rough monthly cost estimate in USD with explanation\n");
        } else {
            sb.append("7) Set costEstimate to null\n");
        }

        sb.append("8) If unable to provide any piece, set it to null\n");
        sb.append("9) Ensure the mermaid diagram is valid and can be rendered\n");
        sb.append("10) End of instructions.\n");

        return sb.toString();
    }

    /**
     * Generate an idempotency key from the request.
     * Creates a hash of title + description + preferences for caching.
     */
    public String generateIdempotencyKey(GenerateRequestDTO request) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(request.getTitle() != null ? request.getTitle().trim().toLowerCase() : "");
            sb.append("|");
            sb.append(request.getDescription() != null ? request.getDescription().trim().toLowerCase() : "");
            sb.append("|");
            if (request.getPreferences() != null) {
                sb.append(objectMapper.writeValueAsString(request.getPreferences()));
            }

            // Use SHA-256 hash
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            log.error("Failed to generate idempotency key", e);
            // Fallback to simple concatenation
            return String.valueOf((request.getTitle() + request.getDescription()).hashCode());
        }
    }
}
