package com.phantomtrace.controller;

import com.phantomtrace.dto.ApiResponse;
import com.phantomtrace.dto.investigation.InvestigationResponse;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.service.DemoDatasetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    private final DemoDatasetService demoDatasetService;

    public DemoController(DemoDatasetService demoDatasetService) {
        this.demoDatasetService = demoDatasetService;
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<List<InvestigationResponse>>> seedDemoCampaign() {
        List<Investigation> created = demoDatasetService.seedDemoCampaign();
        List<InvestigationResponse> response = created.stream().map(InvestigationResponse::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Synthetic demo campaign 'Apex Banking Fraud Campaign' seeded successfully across 3 interconnected investigations."));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetDemoCampaign() {
        demoDatasetService.resetDemoCampaign();
        return ResponseEntity.ok(ApiResponse.success(null, "Synthetic demo investigations removed successfully."));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getDemoStatus() {
        boolean active = demoDatasetService.hasDemoCampaign();
        return ResponseEntity.ok(ApiResponse.success(Map.of("hasDemoData", active), "Demo status retrieved."));
    }
}
