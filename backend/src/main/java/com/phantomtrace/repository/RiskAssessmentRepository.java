package com.phantomtrace.repository;

import com.phantomtrace.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findTopByInvestigationIdOrderByAssessedAtDesc(UUID investigationId);
}
