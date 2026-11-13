package com.phantomtrace.controller;

import com.phantomtrace.ai.dto.AgentAnalysisResponse;
import com.phantomtrace.ai.dto.AgentExecutionResponse;
import com.phantomtrace.ai.orchestrator.AiAgentOrchestrator;
import com.phantomtrace.dto.ApiResponse;
import com.phantomtrace.dto.evidence.EvidenceResponse;
import com.phantomtrace.dto.evidence.ExtractedIndicatorResponse;
import com.phantomtrace.dto.evidence.ThreatLookupResponse;
import com.phantomtrace.service.CorrelationOrchestrator;
import com.phantomtrace.service.DeterministicExtractionService;
import com.phantomtrace.service.EvidenceService;
import com.phantomtrace.service.ThreatIntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final DeterministicExtractionService deterministicExtractionService;
    private final ThreatIntelligenceService threatIntelligenceService;
    private final AiAgentOrchestrator aiAgentOrchestrator;
    private final CorrelationOrchestrator correlationOrchestrator;

    public EvidenceController(EvidenceService evidenceService,
            DeterministicExtractionService deterministicExtractionService,
            ThreatIntelligenceService threatIntelligenceService,
            AiAgentOrchestrator aiAgentOrchestrator,
            CorrelationOrchestrator correlationOrchestrator) {
        this.evidenceService = evidenceService;
        this.deterministicExtractionService = deterministicExtractionService;
        this.threatIntelligenceService = threatIntelligenceService;
        this.aiAgentOrchestrator = aiAgentOrchestrator;
        this.correlationOrchestrator = correlationOrchestrator;
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> getEvidence(@PathVariable UUID evidenceId) {
        return ResponseEntity.ok(ApiResponse.success(evidenceService.get(evidenceId), "Evidence retrieved successfully"));
    }

    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(@PathVariable UUID evidenceId) {
        evidenceService.delete(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Evidence deleted successfully"));
    }

    @GetMapping("/{evidenceId}/indicators")
    public ResponseEntity<ApiResponse<List<ExtractedIndicatorResponse>>> listIndicators(@PathVariable UUID evidenceId) {
        evidenceService.get(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(
                deterministicExtractionService.list(evidenceId), "Indicators retrieved successfully"));
    }

    @PostMapping("/{evidenceId}/threat-lookups")
    public ResponseEntity<ApiResponse<List<ThreatLookupResponse>>> lookupThreatIntelligence(@PathVariable UUID evidenceId) {
        evidenceService.get(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(
                threatIntelligenceService.lookupEvidence(evidenceId), "Threat intelligence lookup completed"));
    }

    @GetMapping("/{evidenceId}/threat-lookups")
    public ResponseEntity<ApiResponse<List<ThreatLookupResponse>>> listThreatIntelligence(@PathVariable UUID evidenceId) {
        evidenceService.get(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(
                threatIntelligenceService.listEvidence(evidenceId), "Threat intelligence retrieved"));
    }

    @PostMapping("/{evidenceId}/analysis")
    public ResponseEntity<ApiResponse<List<AgentAnalysisResponse>>> analyzeEvidence(@PathVariable UUID evidenceId) {
        List<AgentAnalysisResponse> result = aiAgentOrchestrator.analyze(evidenceId);
        if (result.stream().anyMatch(value -> value.status().name().equals("COMPLETED"))) {
            EvidenceResponse evidence = evidenceService.get(evidenceId);
            correlationOrchestrator.generateGraph(evidence.investigationId());
        }
        return ResponseEntity.ok(ApiResponse.success(result, "AI analysis completed"));
    }

    @GetMapping("/{evidenceId}/analysis")
    public ResponseEntity<ApiResponse<List<AgentExecutionResponse>>> listAnalysis(@PathVariable UUID evidenceId) {
        return ResponseEntity.ok(ApiResponse.success(
                aiAgentOrchestrator.listExecutions(evidenceId), "AI analysis executions retrieved"));
    }
}
