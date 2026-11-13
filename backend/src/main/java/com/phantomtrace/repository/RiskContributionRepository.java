package com.phantomtrace.repository;

import com.phantomtrace.entity.RiskContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RiskContributionRepository extends JpaRepository<RiskContribution, UUID> {
    List<RiskContribution> findByAssessmentIdOrderByWeightDesc(UUID assessmentId);
}
