package com.phantomtrace.dto.evidence;

import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.LookupStatus;
import com.phantomtrace.enums.ThreatStatus;

import java.time.Instant;
import java.util.UUID;

public record ThreatLookupResponse(
        UUID id, UUID evidenceId, String provider, String indicator, LookupStatus lookupStatus,
        ThreatStatus threatStatus, String resultDetails, String providerReference,
        String errorMessage, Instant lookupTimestamp) {
    public static ThreatLookupResponse fromEntity(ThreatLookup value) {
        return new ThreatLookupResponse(value.getId(), value.getIndicator().getEvidence().getId(),
                value.getProvider(), value.getIndicatorValue(), value.getLookupStatus(),
                value.getThreatStatus(), value.getResultDetails(), value.getProviderReference(),
                value.getErrorMessage(), value.getLookupTimestamp());
    }
}
