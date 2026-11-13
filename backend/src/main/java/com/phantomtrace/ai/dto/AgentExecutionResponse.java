package com.phantomtrace.ai.dto;

import com.phantomtrace.entity.AgentExecution;

import java.time.Instant;
import java.util.UUID;

public record AgentExecutionResponse(
        UUID id, UUID evidenceId, String agentName, String model, String executionStatus,
        long durationMilliseconds, String outputJson, String errorMessage, Instant completedAt) {
    public static AgentExecutionResponse fromEntity(AgentExecution value) {
        return new AgentExecutionResponse(value.getId(), value.getEvidence().getId(), value.getAgentName(),
                value.getModel(), value.getExecutionStatus(), value.getDurationMilliseconds(),
                value.getOutputJson(), value.getErrorMessage(), value.getCompletedAt());
    }
}
