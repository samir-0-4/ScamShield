package com.phantomtrace.controller;

import com.phantomtrace.dto.HealthResponse;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final EntityManager entityManager;

    @Value("${phantomtrace.ai.api-key:}")
    private String groqApiKey;

    @Value("${phantomtrace.threat-intel.virustotal.api-key:}")
    private String virusTotalApiKey;

    @Value("${phantomtrace.threat-intel.urlhaus.api-key:}")
    private String urlhausApiKey;

    public HealthController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> getHealth() {
        boolean dbConnected = checkDb();
        boolean groqConfigured = groqApiKey != null && !groqApiKey.isBlank();
        boolean virusTotalConfigured = virusTotalApiKey != null && !virusTotalApiKey.isBlank();
        boolean urlhausConfigured = urlhausApiKey != null && !urlhausApiKey.isBlank();

        HealthResponse health = dbConnected
                ? HealthResponse.up(dbConnected, groqConfigured, virusTotalConfigured, urlhausConfigured)
                : HealthResponse.degraded(false, groqConfigured, virusTotalConfigured, urlhausConfigured);

        return ResponseEntity.ok(health);
    }

    private boolean checkDb() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

