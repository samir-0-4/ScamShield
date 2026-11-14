package com.phantomtrace.dto.campaign;

import java.util.List;

public record CampaignReconstructionResponse(
        List<SharedIndicatorDto> sharedIndicators,
        List<CampaignSignalDto> campaignSignals,
        List<ConnectedInvestigationDto> connectedInvestigations,
        String campaignClassification,
        String attributionSummary,
        int totalConnectedInvestigations,
        int totalLinkedEvidenceItems
) {
    public CampaignReconstructionResponse(
            List<SharedIndicatorDto> sharedIndicators,
            List<CampaignSignalDto> campaignSignals) {
        this(
                sharedIndicators,
                campaignSignals,
                List.of(),
                sharedIndicators != null && !sharedIndicators.isEmpty() ? "POSSIBLE" : "ISOLATED_INCIDENT",
                "Heuristic campaign analysis completed.",
                0,
                sharedIndicators != null ? sharedIndicators.stream().mapToInt(SharedIndicatorDto::count).sum() : 0
        );
    }
}
