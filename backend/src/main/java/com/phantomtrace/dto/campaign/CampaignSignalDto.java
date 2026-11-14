package com.phantomtrace.dto.campaign;

import java.util.List;

public record CampaignSignalDto(
        String description,
        String confidence,
        List<String> supportingEvidenceIds,
        List<String> supportingInvestigationIds,
        String signalType
) {
    public CampaignSignalDto(String description, String confidence, List<String> supportingEvidenceIds) {
        this(description, confidence, supportingEvidenceIds, List.of(), "CROSS_EVIDENCE");
    }
}
