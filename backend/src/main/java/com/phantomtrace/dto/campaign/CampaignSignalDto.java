package com.phantomtrace.dto.campaign;

import java.util.List;

public record CampaignSignalDto(
        String description,
        String confidence,
        List<String> supportingEvidenceIds
) {}
