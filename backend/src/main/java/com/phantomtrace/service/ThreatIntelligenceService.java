package com.phantomtrace.service;

import com.phantomtrace.dto.evidence.ThreatLookupResponse;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.LookupStatus;
import com.phantomtrace.enums.ThreatStatus;
import com.phantomtrace.integration.urlhaus.UrlhausClient;
import com.phantomtrace.integration.virustotal.VirusTotalClient;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ThreatIntelligenceService {
    private final ExtractedIndicatorRepository indicatorRepository;
    private final ThreatLookupRepository lookupRepository;
    private final VirusTotalClient virusTotalClient;
    private final UrlhausClient urlhausClient;
    private final Duration cacheTtl;

    public ThreatIntelligenceService(ExtractedIndicatorRepository indicatorRepository,
            ThreatLookupRepository lookupRepository, VirusTotalClient virusTotalClient,
            UrlhausClient urlhausClient,
            @Value("${phantomtrace.threat-intel.cache-ttl:PT1H}") Duration cacheTtl) {
        this.indicatorRepository = indicatorRepository;
        this.lookupRepository = lookupRepository;
        this.virusTotalClient = virusTotalClient;
        this.urlhausClient = urlhausClient;
        this.cacheTtl = cacheTtl;
    }

    public List<ThreatLookupResponse> lookupEvidence(UUID evidenceId) {
        List<ExtractedIndicator> indicators = indicatorRepository
                .findByEvidenceIdAndIndicatorType(evidenceId, IndicatorType.URL);
        List<ThreatLookupResponse> results = new ArrayList<>();
        for (ExtractedIndicator indicator : indicators) {
            results.add(lookup("VirusTotal", indicator, () -> {
                var vt = virusTotalClient.lookup(indicator.getNormalizedValue());
                return new ProviderResult(vt.indicator(), vt.lookupStatus(), vt.threatStatus(),
                        vt.details(), vt.reference(), vt.error());
            }));
            results.add(lookup("URLhaus", indicator, () -> {
                var uh = urlhausClient.lookup(indicator.getOriginalValue());
                return new ProviderResult(uh.indicator(), uh.lookupStatus(), uh.threatStatus(),
                        uh.details(), uh.reference(), uh.error());
            }));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<ThreatLookupResponse> listEvidence(UUID evidenceId) {
        var all = lookupRepository.findByIndicatorEvidenceIdOrderByLookupTimestampDesc(evidenceId);
        java.util.Map<String, ThreatLookup> latest = new java.util.LinkedHashMap<>();
        for (ThreatLookup item : all) {
            String key = item.getIndicator().getId() + ":" + item.getProvider();
            latest.putIfAbsent(key, item);
        }
        return latest.values().stream().map(ThreatLookupResponse::fromEntity).toList();
    }

    private ThreatLookupResponse lookup(String provider, ExtractedIndicator indicator,
            java.util.function.Supplier<ProviderResult> providerCall) {
        Instant now = Instant.now();
        var existing = lookupRepository.findByIndicatorIdAndLookupTimestampAfter(indicator.getId(),
                now.minus(cacheTtl)).stream().filter(value -> value.getProvider().equals(provider)).findFirst();
        if (existing.isPresent() && existing.get().getLookupStatus() != LookupStatus.UNAVAILABLE
                && existing.get().getLookupStatus() != LookupStatus.AUTHENTICATION_ERROR) {
            return ThreatLookupResponse.fromEntity(existing.get());
        }
        ProviderResult result = providerCall.get();
        ThreatLookup lookup = new ThreatLookup(indicator, provider, result.indicator(),
                status(result.lookupStatus()), threat(result.threatStatus()), result.details(),
                result.reference(), result.error(), now);
        return ThreatLookupResponse.fromEntity(lookupRepository.save(lookup));
    }

    private LookupStatus status(String value) {
        try { return LookupStatus.valueOf(value); }
        catch (Exception ex) { return LookupStatus.ERROR; }
    }

    private ThreatStatus threat(String value) {
        try { return ThreatStatus.valueOf(value); }
        catch (Exception ex) { return ThreatStatus.UNKNOWN; }
    }

    private record ProviderResult(String indicator, String lookupStatus, String threatStatus,
                                  String details, String reference, String error) {
    }
}
