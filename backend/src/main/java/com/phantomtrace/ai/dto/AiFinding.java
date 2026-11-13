package com.phantomtrace.ai.dto;

import com.phantomtrace.enums.SeverityLevel;

public record AiFinding(
        String title,
        String description,
        SeverityLevel severity,
        double confidence,
        String verificationStatus,
        String evidenceReference,
        String explanation,
        String recommendedAction,
        String sourceType) {
}
