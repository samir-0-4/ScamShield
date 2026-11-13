package com.phantomtrace.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.AiCompletionRequest;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
    void parsesStructuredCompletionAndRejectsMalformedJson() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = "{\"model\":\"test-model\",\"choices\":[{\"message\":{\"content\":\"{}\"}}],\"usage\":{}}"
                    .getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            AIProviderConfig config = config(server, 2);
            GroqAiProvider provider = new GroqAiProvider(config, new ObjectMapper());
            assertThat(provider.complete(request()).status()).isEqualTo(AiProviderResult.Status.COMPLETED);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsRateLimitAfterRetry() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            assertThat(new GroqAiProvider(config(server, 2), new ObjectMapper())
                    .complete(request()).status()).isEqualTo(AiProviderResult.Status.RATE_LIMITED);
        } finally {
            server.stop(0);
        }
    }

    private AIProviderConfig config(HttpServer server, int timeoutSeconds) {
        AIProviderConfig config = new AIProviderConfig();
        config.setApiKey("test-key");
        config.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        config.setTimeoutSeconds(timeoutSeconds);
        config.setModel("test-model");
        return config;
    }

    private AiCompletionRequest request() {
        return new AiCompletionRequest(null, List.of(AiCompletionRequest.Message.user("untrusted")),
                0.1, 100, Map.of("type", "json_object"));
    }
}
