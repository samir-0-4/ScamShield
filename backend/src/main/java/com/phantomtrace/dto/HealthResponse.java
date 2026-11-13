package com.phantomtrace.dto;

public record HealthResponse(String status, String application) {
    public static HealthResponse up() {
        return new HealthResponse("UP", "PhantomTrace");
    }
}

