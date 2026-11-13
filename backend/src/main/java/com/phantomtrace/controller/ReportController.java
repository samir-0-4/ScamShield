package com.phantomtrace.controller;

import com.phantomtrace.dto.ApiResponse;
import com.phantomtrace.dto.report.InvestigationReportResponse;
import com.phantomtrace.service.InvestigationReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final InvestigationReportService reportService;

    public ReportController(InvestigationReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/investigations/{id}/reports")
    public ResponseEntity<ApiResponse<InvestigationReportResponse>> create(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                reportService.create(id), "Investigation report generated successfully"));
    }

    @GetMapping("/investigations/{id}/reports")
    public ResponseEntity<ApiResponse<List<InvestigationReportResponse>>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.list(id), "Investigation reports retrieved successfully"));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<InvestigationReportResponse>> get(@PathVariable UUID reportId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.get(reportId), "Investigation report retrieved successfully"));
    }
}
