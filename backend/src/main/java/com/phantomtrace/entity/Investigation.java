package com.phantomtrace.entity;

import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.enums.SeverityLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an investigation dossier managed by an authenticated analyst.
 */
@Entity
@Table(name = "investigations")
@EntityListeners(AuditingEntityListener.class)
public class Investigation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InvestigationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvestigationStatus status = InvestigationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk_level")
    private SeverityLevel overallRiskLevel = SeverityLevel.UNKNOWN;

    @Column(name = "risk_score")
    private Double riskScore = 0.0;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "investigation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidenceList = new ArrayList<>();

    protected Investigation() {
        // JPA constructor
    }

    public Investigation(User user, String title, String description, InvestigationCategory category) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = InvestigationStatus.DRAFT;
        this.overallRiskLevel = SeverityLevel.UNKNOWN;
        this.riskScore = 0.0;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InvestigationCategory getCategory() {
        return category;
    }

    public void setCategory(InvestigationCategory category) {
        this.category = category;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public void setStatus(InvestigationStatus status) {
        this.status = status;
        if (status == InvestigationStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = Instant.now();
        }
    }

    public SeverityLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(SeverityLevel overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<Evidence> getEvidenceList() {
        return evidenceList;
    }

    public void addEvidence(Evidence evidence) {
        evidenceList.add(evidence);
        evidence.setInvestigation(this);
    }

    public void removeEvidence(Evidence evidence) {
        evidenceList.remove(evidence);
        evidence.setInvestigation(null);
    }
}
