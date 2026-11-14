package com.phantomtrace.repository;

import com.phantomtrace.entity.Investigation;
import com.phantomtrace.enums.InvestigationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, UUID> {

    Page<Investigation> findByUserId(UUID userId, Pageable pageable);

    Page<Investigation> findByUserIdAndStatus(UUID userId, InvestigationStatus status, Pageable pageable);

    java.util.List<Investigation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Investigation> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
