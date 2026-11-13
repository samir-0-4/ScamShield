package com.phantomtrace.dto.risk;

import com.phantomtrace.entity.RiskAssessment;
import com.phantomtrace.enums.RiskCategory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskAssessmentResponse(UUID id, UUID investigationId, int score, RiskCategory category,
        double confidence, List<RiskContributionResponse> contributingSignals,
        String missingInformation, String explanation, String disclaimer, Instant assessedAt) {
    public static RiskAssessmentResponse fromEntity(RiskAssessment assessment,
            List<RiskContributionResponse> contributions) {
        return new RiskAssessmentResponse(assessment.getId(), assessment.getInvestigation().getId(),
                assessment.getScore(), assessment.getCategory(), assessment.getConfidence(), contributions,
                assessment.getMissingInformation(), assessment.getExplanation(),
                "This is a transparent heuristic assessment, not a mathematically proven probability of fraud.",
                assessment.getAssessedAt());
    }
}
