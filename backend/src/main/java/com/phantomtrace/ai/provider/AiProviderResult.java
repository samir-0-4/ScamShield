package com.phantomtrace.ai.provider;

public record AiProviderResult(
        Status status,
        String model,
        String content,
        String errorMessage,
        int promptTokens,
        int completionTokens) {
    public enum Status {
        COMPLETED, UNAVAILABLE, TIMEOUT, RATE_LIMITED, AUTHENTICATION_ERROR, MALFORMED_RESPONSE, ERROR
    }

    public boolean succeeded() {
        return status == Status.COMPLETED;
    }
}
