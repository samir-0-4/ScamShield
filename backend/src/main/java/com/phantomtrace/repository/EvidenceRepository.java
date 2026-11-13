package com.phantomtrace.repository;

import com.phantomtrace.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {

    List<Evidence> findByInvestigationIdOrderByCreatedAtDesc(UUID investigationId);

    Optional<Evidence> findByIdAndInvestigationId(UUID id, UUID investigationId);

    long countByInvestigationId(UUID investigationId);

    boolean existsByInvestigationIdAndContentHash(UUID investigationId, String contentHash);
}
