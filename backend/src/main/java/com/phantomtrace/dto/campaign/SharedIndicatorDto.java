package com.phantomtrace.dto.campaign;

import java.util.List;

public record SharedIndicatorDto(
        String value,
        String type,
        List<String> evidenceIds,
        int count,
        List<String> investigationIds,
        List<String> investigationTitles,
        boolean crossInvestigation,
        String verificationStatus
) {
    public SharedIndicatorDto(String value, String type, List<String> evidenceIds, int count) {
        this(value, type, evidenceIds, count, List.of(), List.of(), false, "DETERMINISTIC");
    }
}
