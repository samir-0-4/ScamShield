package com.phantomtrace.entity;

import com.phantomtrace.enums.RelationshipType;
import com.phantomtrace.enums.RelationshipVerificationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence_relationships")
public class EvidenceRelationship {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_indicator_id")
    private ThreatIndicator sourceIndicator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_indicator_id")
    private ThreatIndicator targetIndicator;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_evidence_id", nullable = false)
    private Evidence sourceEvidence;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_evidence_id", nullable = false)
    private Evidence targetEvidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private RelationshipType relationshipType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private RelationshipVerificationStatus verificationStatus;
    @Column(nullable = false)
    private Double confidence;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;
    @Column(length = 255)
    private String sourceReference;
    @Column(nullable = false)
    private Instant createdAt;

    protected EvidenceRelationship() {}
    public EvidenceRelationship(Investigation investigation, ThreatIndicator sourceIndicator,
            ThreatIndicator targetIndicator, Evidence sourceEvidence, Evidence targetEvidence,
            RelationshipType relationshipType, RelationshipVerificationStatus verificationStatus,
            Double confidence, String explanation, String sourceReference, Instant createdAt) {
        this.investigation = investigation; this.sourceIndicator = sourceIndicator;
        this.targetIndicator = targetIndicator; this.sourceEvidence = sourceEvidence;
        this.targetEvidence = targetEvidence; this.relationshipType = relationshipType;
        this.verificationStatus = verificationStatus; this.confidence = confidence;
        this.explanation = explanation; this.sourceReference = sourceReference; this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public Investigation getInvestigation() { return investigation; }
    public ThreatIndicator getSourceIndicator() { return sourceIndicator; }
    public ThreatIndicator getTargetIndicator() { return targetIndicator; }
    public Evidence getSourceEvidence() { return sourceEvidence; }
    public Evidence getTargetEvidence() { return targetEvidence; }
    public RelationshipType getRelationshipType() { return relationshipType; }
    public RelationshipVerificationStatus getVerificationStatus() { return verificationStatus; }
    public Double getConfidence() { return confidence; }
    public String getExplanation() { return explanation; }
    public String getSourceReference() { return sourceReference; }
    public Instant getCreatedAt() { return createdAt; }
}
