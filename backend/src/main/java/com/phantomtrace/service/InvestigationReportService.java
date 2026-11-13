package com.phantomtrace.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.dto.graph.InvestigationGraphResponse;
import com.phantomtrace.dto.report.InvestigationReportResponse;
import com.phantomtrace.dto.risk.RiskAssessmentResponse;
import com.phantomtrace.entity.AgentExecution;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.InvestigationReport;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.AgentExecutionRepository;
import com.phantomtrace.repository.EvidenceRelationshipRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationReportRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class InvestigationReportService {

    private final InvestigationRepository investigationRepository;
    private final EvidenceRepository evidenceRepository;
    private final ExtractedIndicatorRepository indicatorRepository;
    private final ThreatLookupRepository lookupRepository;
    private final AgentExecutionRepository executionRepository;
    private final EvidenceRelationshipRepository relationshipRepository;
    private final RiskAssessmentService riskAssessmentService;
    private final CorrelationOrchestrator correlationOrchestrator;
    private final InvestigationReportRepository reportRepository;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    public InvestigationReportService(InvestigationRepository investigationRepository,
            EvidenceRepository evidenceRepository, ExtractedIndicatorRepository indicatorRepository,
            ThreatLookupRepository lookupRepository, AgentExecutionRepository executionRepository,
            EvidenceRelationshipRepository relationshipRepository, RiskAssessmentService riskAssessmentService,
            CorrelationOrchestrator correlationOrchestrator, InvestigationReportRepository reportRepository,
            ObjectMapper objectMapper, SecurityUtils securityUtils) {
        this.investigationRepository = investigationRepository;
        this.evidenceRepository = evidenceRepository;
        this.indicatorRepository = indicatorRepository;
        this.lookupRepository = lookupRepository;
        this.executionRepository = executionRepository;
        this.relationshipRepository = relationshipRepository;
        this.riskAssessmentService = riskAssessmentService;
        this.correlationOrchestrator = correlationOrchestrator;
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
        this.securityUtils = securityUtils;
    }

    public InvestigationReportResponse create(UUID investigationId) {
        Investigation investigation = ownedInvestigation(investigationId);
        List<Evidence> evidence = evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId);
        RiskAssessmentResponse risk;
        try {
            risk = riskAssessmentService.latest(investigationId);
        } catch (ApiException ex) {
            if (ex.getStatus() != HttpStatus.NOT_FOUND) throw ex;
            risk = null;
        }
        InvestigationGraphResponse graph = correlationOrchestrator.getGraph(investigationId);

        List<Map<String, Object>> evidenceData = evidence.stream().map(item -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("id", item.getId());
            value.put("type", item.getEvidenceType());
            value.put("description", item.getDescription());
            value.put("createdAt", item.getCreatedAt());
            value.put("indicators", indicatorRepository.findByEvidenceIdOrderByExtractedAtAsc(item.getId()).stream()
                    .map(indicator -> nullableMap(
                            "id", indicator.getId(), "type", indicator.getIndicatorType(),
                            "originalValue", indicator.getOriginalValue(),
                            "normalizedValue", indicator.getNormalizedValue(),
                            "extractionMethod", indicator.getExtractionMethod(),
                            "extractedAt", indicator.getExtractedAt())).toList());
            value.put("threatIntelligence", lookupRepository.findByIndicatorEvidenceIdOrderByLookupTimestampDesc(item.getId())
                    .stream().map(lookup -> nullableMap(
                            "id", lookup.getId(), "provider", lookup.getProvider(),
                            "indicator", lookup.getIndicatorValue(),
                            "lookupStatus", lookup.getLookupStatus(),
                            "threatStatus", lookup.getThreatStatus(),
                            "resultDetails", lookup.getResultDetails(),
                            "providerReference", lookup.getProviderReference(),
                            "errorMessage", lookup.getErrorMessage(),
                            "lookupTimestamp", lookup.getLookupTimestamp())).toList());
            value.put("analysis", executionRepository.findByEvidenceIdOrderByCompletedAtDesc(item.getId())
                    .stream().map(ExecutionView::from).toList());
            return value;
        }).toList();

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("investigation", Map.of(
                "id", investigation.getId(), "title", investigation.getTitle(),
                "category", investigation.getCategory(), "createdAt", investigation.getCreatedAt()));
        content.put("evidence", evidenceData);
        content.put("riskAssessment", risk);
        content.put("relationships", graph == null ? List.of() : graph.edges());
        content.put("limitations", List.of(
                "Missing provider data is shown as unavailable and is not treated as clean.",
                "Heuristic risk scoring is not a mathematically proven probability of fraud.",
                "AI summaries are limited to persisted structured analysis results."));
        content.put("disclaimer", "This report supports investigation and verification. It does not establish fraud as a legal conclusion.");
        content.put("generatedAt", Instant.now());
        String reportJson = write(content);
        return InvestigationReportResponse.fromEntity(reportRepository.save(
                new InvestigationReport(investigation, reportJson, Instant.now())));
    }

    @Transactional(readOnly = true)
    public List<InvestigationReportResponse> list(UUID investigationId) {
        ownedInvestigation(investigationId);
        return reportRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId).stream()
                .map(InvestigationReportResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public InvestigationReportResponse get(UUID reportId) {
        InvestigationReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));
        ownedInvestigation(report.getInvestigation().getId());
        return InvestigationReportResponse.fromEntity(report);
    }

    private Investigation ownedInvestigation(UUID id) {
        Investigation investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));
        if (!investigation.getUser().getId().equals(securityUtils.getCurrentUserId())) {
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }
        return investigation;
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ApiException("Report could not be generated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> nullableMap(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            result.put(String.valueOf(values[i]), values[i + 1]);
        }
        return result;
    }

    private record ExecutionView(String id, String agentName, String status, String outputJson,
            String errorMessage, Instant completedAt) {
        static ExecutionView from(AgentExecution execution) {
            return new ExecutionView(execution.getId().toString(), execution.getAgentName(),
                    execution.getExecutionStatus(), execution.getOutputJson(),
                    execution.getErrorMessage(), execution.getCompletedAt());
        }
    }
}
