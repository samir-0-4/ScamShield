package com.phantomtrace.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String application,
        String version,
        boolean dbConnected,
        boolean groqConfigured,
        boolean virusTotalConfigured,
        boolean urlhausConfigured,
        Instant timestamp
) {
    public static HealthResponse up(boolean dbConnected, boolean groqConfigured,
                                     boolean virusTotalConfigured, boolean urlhausConfigured) {
        return new HealthResponse("UP", "PhantomTrace", "0.1.0",
                dbConnected, groqConfigured, virusTotalConfigured, urlhausConfigured,
                Instant.now());
    }

    public static HealthResponse degraded(boolean dbConnected, boolean groqConfigured,
                                           boolean virusTotalConfigured, boolean urlhausConfigured) {
        return new HealthResponse("DEGRADED", "PhantomTrace", "0.1.0",
                dbConnected, groqConfigured, virusTotalConfigured, urlhausConfigured,
                Instant.now());
    }
}

