package com.phantomtrace.entity;

import com.phantomtrace.enums.IndicatorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "extracted_indicators")
public class ExtractedIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "indicator_type", nullable = false, length = 50)
    private IndicatorType indicatorType;

    @Column(name = "original_value", nullable = false, columnDefinition = "TEXT")
    private String originalValue;

    @Column(name = "normalized_value", columnDefinition = "TEXT")
    private String normalizedValue;

    @Column(name = "extraction_method", nullable = false, length = 100)
    private String extractionMethod;

    @Column(name = "extracted_at", nullable = false)
    private Instant extractedAt;

    protected ExtractedIndicator() {
    }

    public ExtractedIndicator(Evidence evidence, IndicatorType indicatorType, String originalValue,
            String normalizedValue, String extractionMethod, Instant extractedAt) {
        this.evidence = evidence;
        this.indicatorType = indicatorType;
        this.originalValue = originalValue;
        this.normalizedValue = normalizedValue;
        this.extractionMethod = extractionMethod;
        this.extractedAt = extractedAt;
    }

    public UUID getId() {
        return id;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public IndicatorType getIndicatorType() {
        return indicatorType;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }
}
