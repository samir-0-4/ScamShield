package com.phantomtrace.dto.risk;

import com.phantomtrace.entity.RiskContribution;
import java.util.UUID;

public record RiskContributionResponse(UUID id, String signalKey, String signal, int weight,
        String evidenceReferences, String explanation) {
    public static RiskContributionResponse fromEntity(RiskContribution value) {
        return new RiskContributionResponse(value.getId(), value.getSignalKey(), value.getSignal(),
                value.getWeight(), value.getEvidenceReferences(), value.getExplanation());
    }
}
