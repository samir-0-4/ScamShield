package com.phantomtrace.controller;

import com.phantomtrace.dto.ApiResponse;
import com.phantomtrace.dto.PageResponse;
import com.phantomtrace.dto.investigation.CreateInvestigationRequest;
import com.phantomtrace.dto.investigation.InvestigationResponse;
import com.phantomtrace.dto.investigation.UpdateInvestigationRequest;
import com.phantomtrace.dto.evidence.CreateEvidenceRequest;
import com.phantomtrace.dto.evidence.EvidenceResponse;
import com.phantomtrace.service.EvidenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.service.InvestigationService;
import com.phantomtrace.service.CorrelationOrchestrator;
import com.phantomtrace.dto.graph.InvestigationGraphResponse;
import com.phantomtrace.dto.risk.RiskAssessmentResponse;
import com.phantomtrace.service.RiskAssessmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;
    private final EvidenceService evidenceService;
    private final ObjectMapper objectMapper;
    private final CorrelationOrchestrator correlationOrchestrator;
    private final RiskAssessmentService riskAssessmentService;

    public InvestigationController(InvestigationService investigationService,
            EvidenceService evidenceService, ObjectMapper objectMapper,
            CorrelationOrchestrator correlationOrchestrator, RiskAssessmentService riskAssessmentService) {
        this.investigationService = investigationService;
        this.evidenceService = evidenceService;
        this.objectMapper = objectMapper;
        this.correlationOrchestrator = correlationOrchestrator;
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvestigationResponse>> createInvestigation(
            @Valid @RequestBody CreateInvestigationRequest request) {
        InvestigationResponse response = investigationService.createInvestigation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Investigation created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InvestigationResponse>>> listInvestigations(
            @RequestParam(required = false) InvestigationStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<InvestigationResponse> response = investigationService.listInvestigations(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Investigations retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestigationResponse>> getInvestigationById(@PathVariable UUID id) {
        InvestigationResponse response = investigationService.getInvestigationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Investigation retrieved successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestigationResponse>> updateInvestigation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestigationRequest request) {
        InvestigationResponse response = investigationService.updateInvestigation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Investigation updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvestigation(@PathVariable UUID id) {
        investigationService.deleteInvestigation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Investigation deleted successfully"));
    }

    @PostMapping(value = "/{id}/evidence", consumes = "application/json")
    public ResponseEntity<ApiResponse<EvidenceResponse>> createEvidence(
            @PathVariable UUID id, @Valid @RequestBody CreateEvidenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(evidenceService.create(id, request), "Evidence created successfully"));
    }

    @PostMapping(value = "/{id}/evidence", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<EvidenceResponse>> createFileEvidence(
            @PathVariable UUID id,
            @RequestPart("metadata") String metadata,
            @RequestPart("file") MultipartFile file) {
        try {
            CreateEvidenceRequest request = objectMapper.readValue(metadata, CreateEvidenceRequest.class);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(evidenceService.createFile(id, request, file),
                            "Evidence created successfully"));
        } catch (JsonProcessingException ex) {
            throw new com.phantomtrace.exception.ApiException("Evidence metadata is invalid");
        }
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<java.util.List<EvidenceResponse>>> listEvidence(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(evidenceService.list(id), "Evidence retrieved successfully"));
    }

    @GetMapping("/{id}/graph")
    public ResponseEntity<ApiResponse<InvestigationGraphResponse>> getGraph(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                correlationOrchestrator.getGraph(id), "Investigation graph retrieved successfully"));
    }

    @PostMapping("/{id}/graph/generate")
    public ResponseEntity<ApiResponse<InvestigationGraphResponse>> generateGraph(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                correlationOrchestrator.generateGraph(id), "Investigation graph generated successfully"));
    }

    @PostMapping("/{id}/risk-assessment")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> assessRisk(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                riskAssessmentService.assess(id), "Risk assessment generated successfully"));
    }

    @GetMapping("/{id}/risk-assessment")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> getRiskAssessment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                riskAssessmentService.latest(id), "Risk assessment retrieved successfully"));
    }
}
