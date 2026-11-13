package com.phantomtrace.ai.dto;

import com.phantomtrace.ai.provider.AiProviderResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgentAnalysisResponse(
        UUID executionId,
        UUID evidenceId,
        String agentName,
        String model,
        AiProviderResult.Status status,
        long durationMilliseconds,
        StructuredAiResponse result,
        String errorMessage,
        Instant completedAt) {
    public static AgentAnalysisResponse failure(UUID evidenceId, String agentName, String model,
            AiProviderResult.Status status, long duration, String error) {
        return new AgentAnalysisResponse(null, evidenceId, agentName, model, status, duration,
                new StructuredAiResponse(agentName, List.of(), List.of(), null, List.of()),
                error, Instant.now());
    }
}
