package com.phantomtrace.entity;

import com.phantomtrace.enums.IndicatorType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "threat_indicators")
public class ThreatIndicator {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private IndicatorType indicatorType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String indicatorValue;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String normalizedValue;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_evidence_id", nullable = false)
    private Evidence sourceEvidence;
    @Column(nullable = false, length = 40)
    private String externalVerificationStatus;
    @Column(nullable = false)
    private Instant createdAt;

    protected ThreatIndicator() {}
    public ThreatIndicator(Investigation investigation, IndicatorType indicatorType, String indicatorValue,
            String normalizedValue, Evidence sourceEvidence, String externalVerificationStatus, Instant createdAt) {
        this.investigation = investigation;
        this.indicatorType = indicatorType;
        this.indicatorValue = indicatorValue;
        this.normalizedValue = normalizedValue;
        this.sourceEvidence = sourceEvidence;
        this.externalVerificationStatus = externalVerificationStatus;
        this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public Investigation getInvestigation() { return investigation; }
    public IndicatorType getIndicatorType() { return indicatorType; }
    public String getIndicatorValue() { return indicatorValue; }
    public String getNormalizedValue() { return normalizedValue; }
    public Evidence getSourceEvidence() { return sourceEvidence; }
    public String getExternalVerificationStatus() { return externalVerificationStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void updateExternalVerificationStatus(String status) {
        this.externalVerificationStatus = status;
    }
}
