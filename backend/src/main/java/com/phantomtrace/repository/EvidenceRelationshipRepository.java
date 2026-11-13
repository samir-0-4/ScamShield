package com.phantomtrace.repository;

import com.phantomtrace.entity.EvidenceRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EvidenceRelationshipRepository extends JpaRepository<EvidenceRelationship, UUID> {
    List<EvidenceRelationship> findByInvestigationId(UUID investigationId);
    void deleteByInvestigationId(UUID investigationId);
}
