package com.phantomtrace.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.AiCompletionRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GroqAiProvider implements AiProvider {
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final AIProviderConfig config;

    public GroqAiProvider(AIProviderConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = Math.max(1, config.getTimeoutSeconds()) * 1000;
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.client = RestClient.builder().baseUrl(config.getBaseUrl()).requestFactory(factory).build();
    }

    @Override
    public String getProviderName() {
        return "GROQ";
    }

    @Override
    public AiProviderResult complete(AiCompletionRequest request) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            return failure(AiProviderResult.Status.UNAVAILABLE, "AI provider credentials are not configured");
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                String body = client.post().uri("/chat/completions")
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .body(request).retrieve().body(String.class);
                return parse(body);
            } catch (ResourceAccessException ex) {
                return failure(AiProviderResult.Status.TIMEOUT, "AI provider request timed out: " + ex.getMessage());
            } catch (RestClientResponseException ex) {
                if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                    return failure(AiProviderResult.Status.AUTHENTICATION_ERROR, "AI provider credentials were rejected");
                }
                if (ex.getStatusCode().value() == 429 && attempt == 0) {
                    continue;
                }
                if (ex.getStatusCode().value() == 429) {
                    return failure(AiProviderResult.Status.RATE_LIMITED, "AI provider rate limit reached");
                }
                return failure(AiProviderResult.Status.ERROR, "AI provider request failed");
            } catch (RuntimeException ex) {
                return failure(AiProviderResult.Status.ERROR, "AI provider request failed");
            }
        }
        return failure(AiProviderResult.Status.RATE_LIMITED, "AI provider rate limit reached");
    }

    private AiProviderResult parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode message = root.path("choices").path(0).path("message");
            String content = message.path("content").asText(null);
            if (content == null || content.isBlank()) {
                return failure(AiProviderResult.Status.MALFORMED_RESPONSE, "AI provider returned empty content");
            }
            JsonNode usage = root.path("usage");
            return new AiProviderResult(AiProviderResult.Status.COMPLETED,
                    root.path("model").asText(config.getModel()), content, null,
                    usage.path("prompt_tokens").asInt(0), usage.path("completion_tokens").asInt(0));
        } catch (Exception ex) {
            return failure(AiProviderResult.Status.MALFORMED_RESPONSE, "AI provider response was malformed");
        }
    }

    private AiProviderResult failure(AiProviderResult.Status status, String message) {
        return new AiProviderResult(status, config.getModel(), null, message, 0, 0);
    }
}
