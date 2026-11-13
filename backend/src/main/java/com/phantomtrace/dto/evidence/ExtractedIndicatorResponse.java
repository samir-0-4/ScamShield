package com.phantomtrace.dto.evidence;

import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.enums.IndicatorType;

import java.time.Instant;
import java.util.UUID;

public record ExtractedIndicatorResponse(
        UUID id,
        UUID evidenceId,
        IndicatorType indicatorType,
        String originalValue,
        String normalizedValue,
        String extractionMethod,
        Instant extractedAt) {

    public static ExtractedIndicatorResponse fromEntity(ExtractedIndicator indicator) {
        return new ExtractedIndicatorResponse(
                indicator.getId(),
                indicator.getEvidence().getId(),
                indicator.getIndicatorType(),
                indicator.getOriginalValue(),
                indicator.getNormalizedValue(),
                indicator.getExtractionMethod(),
                indicator.getExtractedAt());
    }
}
