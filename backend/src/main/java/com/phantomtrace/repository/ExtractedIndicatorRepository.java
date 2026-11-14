package com.phantomtrace.repository;

import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.phantomtrace.enums.IndicatorType;

public interface ExtractedIndicatorRepository extends JpaRepository<ExtractedIndicator, UUID> {

    List<ExtractedIndicator> findByEvidenceIdOrderByExtractedAtAsc(UUID evidenceId);
    List<ExtractedIndicator> findByEvidenceIdAndIndicatorType(UUID evidenceId, IndicatorType indicatorType);
    List<ExtractedIndicator> findByEvidenceInvestigationId(UUID investigationId);
    List<ExtractedIndicator> findByEvidenceInvestigationUserId(UUID userId);
}
