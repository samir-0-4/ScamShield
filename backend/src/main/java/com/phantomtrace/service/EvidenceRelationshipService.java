package com.phantomtrace.service;

import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.EvidenceRelationship;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.ThreatIndicator;
import com.phantomtrace.enums.RelationshipType;
import com.phantomtrace.enums.RelationshipVerificationStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EvidenceRelationshipService {
    public EvidenceRelationship create(Investigation investigation, ThreatIndicator sourceIndicator,
            ThreatIndicator targetIndicator, Evidence sourceEvidence, Evidence targetEvidence,
            RelationshipType type, RelationshipVerificationStatus status, double confidence,
            String explanation, String sourceReference) {
        if (investigation == null || sourceEvidence == null || targetEvidence == null
                || type == null || status == null || explanation == null || explanation.isBlank()
                || confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Invalid evidence relationship data");
        }
        return new EvidenceRelationship(investigation, sourceIndicator, targetIndicator,
                sourceEvidence, targetEvidence, type, status, confidence, explanation,
                sourceReference, Instant.now());
    }
}
