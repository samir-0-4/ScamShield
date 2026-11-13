package com.phantomtrace.repository;

import com.phantomtrace.entity.ThreatIndicator;
import com.phantomtrace.enums.IndicatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ThreatIndicatorRepository extends JpaRepository<ThreatIndicator, UUID> {
    List<ThreatIndicator> findByInvestigationId(UUID investigationId);
    java.util.Optional<ThreatIndicator> findByInvestigationIdAndIndicatorTypeAndNormalizedValueAndSourceEvidenceId(
            UUID investigationId, IndicatorType indicatorType, String normalizedValue, UUID sourceEvidenceId);
    void deleteByInvestigationId(UUID investigationId);
}
