package com.phantomtrace.entity;

import com.phantomtrace.enums.RiskCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @Column(nullable = false)
    private int score;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private RiskCategory category;
    @Column(nullable = false)
    private double confidence;
    @Column(columnDefinition = "TEXT")
    private String missingInformation;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;
    @Column(nullable = false)
    private Instant assessedAt;

    protected RiskAssessment() {}
    public RiskAssessment(Investigation investigation, int score, RiskCategory category, double confidence,
            String missingInformation, String explanation, Instant assessedAt) {
        this.investigation = investigation; this.score = score; this.category = category;
        this.confidence = confidence; this.missingInformation = missingInformation;
        this.explanation = explanation; this.assessedAt = assessedAt;
    }
    public UUID getId() { return id; }
    public Investigation getInvestigation() { return investigation; }
    public int getScore() { return score; }
    public RiskCategory getCategory() { return category; }
    public double getConfidence() { return confidence; }
    public String getMissingInformation() { return missingInformation; }
    public String getExplanation() { return explanation; }
    public Instant getAssessedAt() { return assessedAt; }
}
