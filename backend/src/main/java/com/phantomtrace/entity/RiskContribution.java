package com.phantomtrace.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "risk_contributions")
public class RiskContribution {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private RiskAssessment assessment;
    @Column(nullable = false, length = 80)
    private String signalKey;
    @Column(nullable = false, length = 120)
    private String signal;
    @Column(nullable = false)
    private int weight;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String evidenceReferences;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;
    protected RiskContribution() {}
    public RiskContribution(RiskAssessment assessment, String signalKey, String signal, int weight,
            String evidenceReferences, String explanation) {
        this.assessment = assessment; this.signalKey = signalKey; this.signal = signal;
        this.weight = weight; this.evidenceReferences = evidenceReferences; this.explanation = explanation;
    }
    public UUID getId() { return id; }
    public RiskAssessment getAssessment() { return assessment; }
    public String getSignalKey() { return signalKey; }
    public String getSignal() { return signal; }
    public int getWeight() { return weight; }
    public String getEvidenceReferences() { return evidenceReferences; }
    public String getExplanation() { return explanation; }
}
