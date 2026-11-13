package com.phantomtrace.entity;

import com.phantomtrace.enums.LookupStatus;
import com.phantomtrace.enums.ThreatStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "threat_lookups")
public class ThreatLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "indicator_id", nullable = false)
    private ExtractedIndicator indicator;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String indicatorValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LookupStatus lookupStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ThreatStatus threatStatus;

    @Column(columnDefinition = "TEXT")
    private String resultDetails;

    @Column(length = 255)
    private String providerReference;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Instant lookupTimestamp;

    protected ThreatLookup() {
    }

    public ThreatLookup(ExtractedIndicator indicator, String provider, String indicatorValue,
            LookupStatus lookupStatus, ThreatStatus threatStatus, String resultDetails,
            String providerReference, String errorMessage, Instant lookupTimestamp) {
        this.indicator = indicator;
        this.provider = provider;
        this.indicatorValue = indicatorValue;
        this.lookupStatus = lookupStatus;
        this.threatStatus = threatStatus;
        this.resultDetails = resultDetails;
        this.providerReference = providerReference;
        this.errorMessage = errorMessage;
        this.lookupTimestamp = lookupTimestamp;
    }

    public UUID getId() { return id; }
    public ExtractedIndicator getIndicator() { return indicator; }
    public String getProvider() { return provider; }
    public String getIndicatorValue() { return indicatorValue; }
    public LookupStatus getLookupStatus() { return lookupStatus; }
    public ThreatStatus getThreatStatus() { return threatStatus; }
    public String getResultDetails() { return resultDetails; }
    public String getProviderReference() { return providerReference; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getLookupTimestamp() { return lookupTimestamp; }
}
