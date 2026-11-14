package com.phantomtrace.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.AiCompletionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GroqAiProviderTest {

    @Test
    void reportsMissingCredentialsAsUnavailableWithoutCallingTheNetwork() {
        AIProviderConfig config = new AIProviderConfig();
        config.setApiKey("");
        GroqAiProvider provider = new GroqAiProvider(config, new ObjectMapper());

        AiProviderResult result = provider.complete(new AiCompletionRequest(
                null, List.of(AiCompletionRequest.Message.user("untrusted")), 0.1, 100,
                Map.of("type", "json_object")));

        assertThat(result.status()).isEqualTo(AiProviderResult.Status.UNAVAILABLE);
        assertThat(result.content()).isNull();
    }

    @Test
    void parsesStructuredCompletionAndRejectsMalformedJson() {
        AIProviderConfig config = baseConfig();
        RestClient.Builder builder = RestClient.builder().baseUrl(config.getBaseUrl());
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        GroqAiProvider provider = new GroqAiProvider(config, new ObjectMapper(), builder.build());

        String mockResponseBody = """
                {
                  "model": "test-model",
                  "choices": [
                    { "message": { "content": "{\\"findings\\":[]}" } }
                  ],
                  "usage": { "prompt_tokens": 10, "completion_tokens": 20 }
                }
                """;

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(mockResponseBody, MediaType.APPLICATION_JSON));

        AiProviderResult result = provider.complete(request());
        mockServer.verify();

        assertThat(result.status()).isEqualTo(AiProviderResult.Status.COMPLETED);
        assertThat(result.content()).isEqualTo("{\"findings\":[]}");
        assertThat(result.promptTokens()).isEqualTo(10);
        assertThat(result.completionTokens()).isEqualTo(20);
    }

    @Test
    void mapsRateLimitAfterRetry() {
        AIProviderConfig config = baseConfig();
        RestClient.Builder builder = RestClient.builder().baseUrl(config.getBaseUrl());
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        GroqAiProvider provider = new GroqAiProvider(config, new ObjectMapper(), builder.build());

        // GroqAiProvider retries once on 429; so 2 consecutive 429s result in RATE_LIMITED status
        mockServer.expect(ExpectedCount.twice(), requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        AiProviderResult result = provider.complete(request());
        mockServer.verify();

        assertThat(result.status()).isEqualTo(AiProviderResult.Status.RATE_LIMITED);
    }

    @Test
    void mapsSocketTimeoutAccurately() {
        AIProviderConfig config = baseConfig();
        RestClient.Builder builder = RestClient.builder().baseUrl(config.getBaseUrl());
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        GroqAiProvider provider = new GroqAiProvider(config, new ObjectMapper(), builder.build());

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out"));
                });

        AiProviderResult result = provider.complete(request());
        mockServer.verify();

        assertThat(result.status()).isEqualTo(AiProviderResult.Status.TIMEOUT);
    }

    private AIProviderConfig baseConfig() {
        AIProviderConfig config = new AIProviderConfig();
        config.setApiKey("test-key");
        config.setBaseUrl("https://api.groq.com/openai/v1");
        config.setTimeoutSeconds(2);
        config.setModel("test-model");
        return config;
    }

    private AiCompletionRequest request() {
        return new AiCompletionRequest(null, List.of(AiCompletionRequest.Message.user("untrusted")),
                0.1, 100, Map.of("type", "json_object"));
    }
}
