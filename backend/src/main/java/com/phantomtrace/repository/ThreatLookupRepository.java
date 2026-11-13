package com.phantomtrace.repository;

import com.phantomtrace.entity.ThreatLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ThreatLookupRepository extends JpaRepository<ThreatLookup, UUID> {
    List<ThreatLookup> findByIndicatorId(UUID indicatorId);
    List<ThreatLookup> findByIndicatorEvidenceIdOrderByLookupTimestampDesc(UUID evidenceId);
    List<ThreatLookup> findByIndicatorIdAndLookupTimestampAfter(UUID indicatorId, Instant after);
}
