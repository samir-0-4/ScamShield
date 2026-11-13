package com.phantomtrace.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_executions")
public class AgentExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @Column(nullable = false, length = 80)
    private String agentName;
    @Column(nullable = false, length = 120)
    private String model;
    @Column(nullable = false, length = 40)
    private String executionStatus;
    @Column(nullable = false)
    private long durationMilliseconds;
    @Column(columnDefinition = "TEXT")
    private String outputJson;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    @Column(nullable = false)
    private Instant completedAt;

    protected AgentExecution() {
    }

    public AgentExecution(Evidence evidence, String agentName, String model, String executionStatus,
            long durationMilliseconds, String outputJson, String errorMessage, Instant completedAt) {
        this.evidence = evidence;
        this.agentName = agentName;
        this.model = model;
        this.executionStatus = executionStatus;
        this.durationMilliseconds = durationMilliseconds;
        this.outputJson = outputJson;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public Evidence getEvidence() { return evidence; }
    public String getAgentName() { return agentName; }
    public String getModel() { return model; }
    public String getExecutionStatus() { return executionStatus; }
    public long getDurationMilliseconds() { return durationMilliseconds; }
    public String getOutputJson() { return outputJson; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCompletedAt() { return completedAt; }
}
