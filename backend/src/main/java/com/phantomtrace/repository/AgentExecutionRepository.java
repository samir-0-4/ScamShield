package com.phantomtrace.repository;

import com.phantomtrace.entity.AgentExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentExecutionRepository extends JpaRepository<AgentExecution, UUID> {
    List<AgentExecution> findByEvidenceIdOrderByCompletedAtDesc(UUID evidenceId);
}
