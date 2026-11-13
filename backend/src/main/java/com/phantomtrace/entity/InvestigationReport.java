package com.phantomtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "investigation_reports")
public class InvestigationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(nullable = false)
    private Instant createdAt;

    protected InvestigationReport() {}

    public InvestigationReport(Investigation investigation, String contentJson, Instant createdAt) {
        this.investigation = investigation;
        this.contentJson = contentJson;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Investigation getInvestigation() { return investigation; }
    public String getContentJson() { return contentJson; }
    public Instant getCreatedAt() { return createdAt; }
}
