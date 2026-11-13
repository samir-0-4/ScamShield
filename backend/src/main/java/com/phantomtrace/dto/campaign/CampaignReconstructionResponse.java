package com.phantomtrace.dto.campaign;

import java.util.List;

public record CampaignReconstructionResponse(
        List<SharedIndicatorDto> sharedIndicators,
        List<CampaignSignalDto> campaignSignals
) {}
