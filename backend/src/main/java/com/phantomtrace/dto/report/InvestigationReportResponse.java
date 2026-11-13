package com.phantomtrace.dto.report;

import com.phantomtrace.entity.InvestigationReport;
import java.time.Instant;
import java.util.UUID;

public record InvestigationReportResponse(
        UUID id, UUID investigationId, String contentJson, Instant createdAt) {
    public static InvestigationReportResponse fromEntity(InvestigationReport report) {
        return new InvestigationReportResponse(report.getId(), report.getInvestigation().getId(),
                report.getContentJson(), report.getCreatedAt());
    }
}
