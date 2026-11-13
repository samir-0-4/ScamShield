package com.phantomtrace.ai.config;

public record AIModelConfig(String model, double temperature, int maxTokens) {
}
