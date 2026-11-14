package com.phantomtrace.service;

import com.phantomtrace.dto.campaign.CampaignReconstructionResponse;
import com.phantomtrace.dto.campaign.CampaignSignalDto;
import com.phantomtrace.dto.campaign.ConnectedInvestigationDto;
import com.phantomtrace.dto.campaign.SharedIndicatorDto;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.ThreatStatus;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CampaignReconstructionService {

    private static final Set<String> GENERIC_DOMAINS = Set.of(
            "google.com", "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "microsoft.com", "apple.com", "bit.ly", "tinyurl.com", "t.co",
            "facebook.com", "twitter.com", "instagram.com", "linkedin.com"
    );

    private final InvestigationRepository investigationRepository;
    private final ExtractedIndicatorRepository indicatorRepository;
    private final SecurityUtils securityUtils;
    private final ThreatLookupRepository lookupRepository;

    @Autowired
    public CampaignReconstructionService(InvestigationRepository investigationRepository,
                                          ExtractedIndicatorRepository indicatorRepository,
                                          SecurityUtils securityUtils,
                                          ThreatLookupRepository lookupRepository) {
        this.investigationRepository = investigationRepository;
        this.indicatorRepository = indicatorRepository;
        this.securityUtils = securityUtils;
        this.lookupRepository = lookupRepository;
    }

    // 3-arg constructor for backward compatibility with unit tests
    public CampaignReconstructionService(InvestigationRepository investigationRepository,
                                          ExtractedIndicatorRepository indicatorRepository,
                                          SecurityUtils securityUtils) {
        this(investigationRepository, indicatorRepository, securityUtils, null);
    }

    public CampaignReconstructionResponse reconstruct(UUID investigationId) {
        // Ownership check
        Investigation currentInvestigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));

        UUID currentUserId = securityUtils.getCurrentUserId();
        if (!currentInvestigation.getUser().getId().equals(currentUserId)) {
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }

        // Indicators for the current case
        List<ExtractedIndicator> currentIndicators = indicatorRepository.findByEvidenceInvestigationId(investigationId);

        // Fetch all indicators owned by this user to discover cross-case campaign footprints
        List<ExtractedIndicator> allUserIndicators;
        try {
            allUserIndicators = indicatorRepository.findByEvidenceInvestigationUserId(currentUserId);
        } catch (Exception ex) {
            allUserIndicators = currentIndicators;
        }

        // Track indicators in the current case: value -> current IndicatorGroup
        record IndicatorTracker(
                String type,
                IndicatorType indicatorType,
                Set<String> evidenceIds,
                Set<UUID> investigationIds,
                Map<UUID, String> investigationTitles,
                List<UUID> indicatorEntityIds
        ) {}

        Map<String, IndicatorTracker> currentCaseMap = new LinkedHashMap<>();
        for (ExtractedIndicator ind : currentIndicators) {
            String value = canonicalValue(ind);
            String evidenceId = ind.getEvidence().getId().toString();
            String type = ind.getIndicatorType().name();

            currentCaseMap.compute(value, (k, existing) -> {
                if (existing == null) {
                    Set<String> evIds = new LinkedHashSet<>();
                    evIds.add(evidenceId);
                    Set<UUID> invIds = new LinkedHashSet<>();
                    invIds.add(investigationId);
                    Map<UUID, String> titles = new LinkedHashMap<>();
                    titles.put(investigationId, currentInvestigation.getTitle());
                    List<UUID> entityIds = new ArrayList<>();
                    if (ind.getId() != null) entityIds.add(ind.getId());
                    return new IndicatorTracker(type, ind.getIndicatorType(), evIds, invIds, titles, entityIds);
                } else {
                    existing.evidenceIds().add(evidenceId);
                    if (ind.getId() != null) existing.indicatorEntityIds().add(ind.getId());
                    return existing;
                }
            });
        }

        // Correlate with all other investigations owned by this tenant
        Map<UUID, Investigation> otherInvestigationCache = new LinkedHashMap<>();
        for (ExtractedIndicator otherInd : allUserIndicators) {
            if (otherInd.getEvidence() == null || otherInd.getEvidence().getInvestigation() == null) continue;
            UUID otherInvId = otherInd.getEvidence().getInvestigation().getId();
            if (otherInvId.equals(investigationId)) continue; // already tracked

            String value = canonicalValue(otherInd);
            IndicatorTracker tracker = currentCaseMap.get(value);
            if (tracker != null && !isGenericBenignDomain(tracker.indicatorType(), value)) {
                tracker.investigationIds().add(otherInvId);
                String otherTitle = otherInd.getEvidence().getInvestigation().getTitle();
                tracker.investigationTitles().put(otherInvId, otherTitle);
                tracker.evidenceIds().add(otherInd.getEvidence().getId().toString());
                if (otherInd.getId() != null) tracker.indicatorEntityIds().add(otherInd.getId());
                otherInvestigationCache.putIfAbsent(otherInvId, otherInd.getEvidence().getInvestigation());
            }
        }

        // Build SharedIndicatorDto list
        List<SharedIndicatorDto> sharedIndicators = new ArrayList<>();
        boolean hasVerifiedMaliciousThreatHit = false;

        for (Map.Entry<String, IndicatorTracker> entry : currentCaseMap.entrySet()) {
            String val = entry.getKey();
            IndicatorTracker tracker = entry.getValue();

            boolean crossEvidence = tracker.evidenceIds().size() >= 2;
            boolean crossCase = tracker.investigationIds().size() >= 2;

            if (crossEvidence || crossCase) {
                String verificationStatus = checkVerificationStatus(tracker.indicatorEntityIds());
                if ("VERIFIED_MALICIOUS".equals(verificationStatus)) {
                    hasVerifiedMaliciousThreatHit = true;
                }

                List<String> evidenceIdList = new ArrayList<>(tracker.evidenceIds());
                List<String> invIdList = tracker.investigationIds().stream().map(UUID::toString).toList();
                List<String> titleList = new ArrayList<>(tracker.investigationTitles().values());

                sharedIndicators.add(new SharedIndicatorDto(
                        val,
                        tracker.type(),
                        evidenceIdList,
                        evidenceIdList.size(),
                        invIdList,
                        titleList,
                        crossCase,
                        verificationStatus
                ));
            }
        }

        // Build Connected Investigations
        Map<UUID, List<String>> invToSharedIndicators = new LinkedHashMap<>();
        for (SharedIndicatorDto shared : sharedIndicators) {
            if (shared.investigationIds() != null) {
                for (String invIdStr : shared.investigationIds()) {
                    UUID invId = UUID.fromString(invIdStr);
                    if (!invId.equals(investigationId)) {
                        invToSharedIndicators.computeIfAbsent(invId, k -> new ArrayList<>()).add(shared.value());
                    }
                }
            }
        }

        List<ConnectedInvestigationDto> connectedInvestigations = new ArrayList<>();
        for (Map.Entry<UUID, List<String>> entry : invToSharedIndicators.entrySet()) {
            UUID linkedId = entry.getKey();
            List<String> matchedValues = entry.getValue();
            Investigation linkedInv = otherInvestigationCache.get(linkedId);
            String title = linkedInv != null ? linkedInv.getTitle() : "Linked Dossier #" + linkedId.toString().substring(0, 8);
            String category = linkedInv != null && linkedInv.getCategory() != null ? linkedInv.getCategory().name() : "PHISHING";
            String status = linkedInv != null && linkedInv.getStatus() != null ? linkedInv.getStatus().name() : "UNDER_INVESTIGATION";

            String linkConfidence = hasVerifiedMaliciousThreatHit
                    ? "CONFIRMED"
                    : (matchedValues.size() >= 2 ? "PROBABLE" : "POSSIBLE");

            String explanation = "Shares " + matchedValues.size() + " infrastructure indicator(s) ["
                    + String.join(", ", matchedValues.stream().limit(3).toList())
                    + (matchedValues.size() > 3 ? "..." : "") + "] with " + currentInvestigation.getTitle() + ".";

            connectedInvestigations.add(new ConnectedInvestigationDto(
                    linkedId,
                    title,
                    category,
                    status,
                    matchedValues.size(),
                    matchedValues,
                    explanation,
                    linkConfidence,
                    linkedInv != null ? linkedInv.getCreatedAt() : null
            ));
        }

        // Campaign Classification & Signals
        String classification;
        if (sharedIndicators.isEmpty()) {
            classification = "ISOLATED_INCIDENT";
        } else if (hasVerifiedMaliciousThreatHit) {
            classification = "CONFIRMED";
        } else if (connectedInvestigations.size() >= 1 || sharedIndicators.size() >= 3) {
            classification = "PROBABLE";
        } else {
            classification = "POSSIBLE";
        }

        List<CampaignSignalDto> campaignSignals = new ArrayList<>();
        if (!sharedIndicators.isEmpty()) {
            int sharedCount = sharedIndicators.size();
            Set<String> allSupportingEvidenceIds = new LinkedHashSet<>();
            Set<String> allSupportingInvIds = new LinkedHashSet<>();
            sharedIndicators.forEach(si -> {
                allSupportingEvidenceIds.addAll(si.evidenceIds());
                if (si.investigationIds() != null) allSupportingInvIds.addAll(si.investigationIds());
            });

            String signalDescription;
            if (hasVerifiedMaliciousThreatHit) {
                signalDescription = sharedCount + " shared indicator(s) confirmed malicious by threat intelligence feeds — confirmed coordinated campaign";
            } else if (connectedInvestigations.size() >= 1) {
                signalDescription = sharedCount + " shared indicator(s) cross-correlated across "
                        + (connectedInvestigations.size() + 1) + " investigations — multi-case attack campaign detected";
            } else {
                signalDescription = sharedCount >= 3
                        ? sharedCount + " shared indicator(s) detected across evidence — strong campaign signal"
                        : sharedCount + " shared indicator(s) detected across evidence — possible coordinated campaign";
            }

            campaignSignals.add(new CampaignSignalDto(
                    signalDescription,
                    classification,
                    new ArrayList<>(allSupportingEvidenceIds),
                    new ArrayList<>(allSupportingInvIds),
                    connectedInvestigations.isEmpty() ? "CROSS_EVIDENCE" : "CROSS_INVESTIGATION_CAMPAIGN"
            ));
        }

        String attributionSummary = buildAttributionSummary(currentInvestigation, connectedInvestigations, sharedIndicators, classification);

        return new CampaignReconstructionResponse(
                sharedIndicators,
                campaignSignals,
                connectedInvestigations,
                classification,
                attributionSummary,
                connectedInvestigations.size(),
                sharedIndicators.stream().mapToInt(SharedIndicatorDto::count).sum()
        );
    }

    private String canonicalValue(ExtractedIndicator ind) {
        String val = ind.getNormalizedValue() != null && !ind.getNormalizedValue().isBlank()
                ? ind.getNormalizedValue()
                : ind.getOriginalValue();
        return val != null ? val.trim().toLowerCase(Locale.ROOT) : "";
    }

    private boolean isGenericBenignDomain(IndicatorType type, String value) {
        if (type == IndicatorType.DOMAIN) {
            return GENERIC_DOMAINS.contains(value.toLowerCase(Locale.ROOT));
        }
        return false;
    }

    private String checkVerificationStatus(List<UUID> indicatorIds) {
        if (lookupRepository == null || indicatorIds == null || indicatorIds.isEmpty()) {
            return "DETERMINISTIC";
        }
        for (UUID id : indicatorIds) {
            try {
                List<ThreatLookup> lookups = lookupRepository.findByIndicatorId(id);
                if (lookups.stream().anyMatch(l -> l.getThreatStatus() == ThreatStatus.MALICIOUS)) {
                    return "VERIFIED_MALICIOUS";
                }
                if (lookups.stream().anyMatch(l -> l.getThreatStatus() == ThreatStatus.SUSPICIOUS)) {
                    return "VERIFIED_SUSPICIOUS";
                }
            } catch (Exception ignored) {}
        }
        return "DETERMINISTIC";
    }

    private String buildAttributionSummary(Investigation current, List<ConnectedInvestigationDto> connected,
                                           List<SharedIndicatorDto> shared, String classification) {
        if (shared.isEmpty()) {
            return "No cross-evidence or cross-investigation indicator matches were identified. "
                    + "This incident currently exhibits isolated characteristics.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Campaign footprint analysis classified this activity as ").append(classification).append(". ");
        if (!connected.isEmpty()) {
            sb.append("This case links directly to ").append(connected.size())
                    .append(" other investigation(s) in your organization [")
                    .append(String.join(", ", connected.stream().map(ConnectedInvestigationDto::title).toList()))
                    .append("] through ")
                    .append(shared.size()).append(" shared indicator(s). ");
        } else {
            sb.append("Multiple distinct evidence artifacts within this case converge on ").append(shared.size())
                    .append(" shared infrastructure point(s). ");
        }
        sb.append("Shared infrastructure establishes relationship provenance; threat-actor attribution remains an analytical hypothesis.");
        return sb.toString();
    }
}
