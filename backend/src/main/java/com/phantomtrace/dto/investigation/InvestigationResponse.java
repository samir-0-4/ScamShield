package com.phantomtrace.dto.investigation;

import com.phantomtrace.entity.Investigation;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.enums.SeverityLevel;

import java.time.Instant;
import java.util.UUID;

public record InvestigationResponse(
        UUID id,
        UUID userId,
        String title,
        String description,
        InvestigationCategory category,
        InvestigationStatus status,
        SeverityLevel overallRiskLevel,
        Double riskScore,
        int evidenceCount,
        boolean syntheticDemo,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt) {
    public static InvestigationResponse fromEntity(Investigation entity) {
        return new InvestigationResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getOverallRiskLevel(),
                entity.getRiskScore(),
                entity.getEvidenceList() != null ? entity.getEvidenceList().size() : 0,
                entity.isSyntheticDemo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt());
    }
}
