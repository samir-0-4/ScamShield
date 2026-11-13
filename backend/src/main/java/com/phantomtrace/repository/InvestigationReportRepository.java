package com.phantomtrace.repository;

import com.phantomtrace.entity.InvestigationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestigationReportRepository extends JpaRepository<InvestigationReport, UUID> {
    List<InvestigationReport> findByInvestigationIdOrderByCreatedAtDesc(UUID investigationId);
    Optional<InvestigationReport> findByIdAndInvestigationId(UUID id, UUID investigationId);
}
