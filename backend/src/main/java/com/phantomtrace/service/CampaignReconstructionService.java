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
import com.phantomtrace.util.SyntheticIndicatorDetector;
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

    public static final String CLASSIFICATION_SYNTHETIC_DEMO = "SYNTHETIC_DEMO_CAMPAIGN";
    public static final String CLASSIFICATION_CONFIRMED = "CONFIRMED";
    public static final String CLASSIFICATION_PROBABLE = "PROBABLE";
    public static final String CLASSIFICATION_POSSIBLE = "POSSIBLE";
    public static final String CLASSIFICATION_ISOLATED = "ISOLATED_INCIDENT";

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
                String verificationStatus = checkVerificationStatus(tracker.indicatorEntityIds(), val);
                if ("VERIFIED_MALICIOUS".equals(verificationStatus) && !isSyntheticIndicator(val)) {
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

        boolean isCurrentDemo = isSyntheticInvestigation(currentInvestigation);

        List<ConnectedInvestigationDto> connectedInvestigations = new ArrayList<>();
        int genuineCrossCaseCount = 0;
        int syntheticDossierLinkCount = 0;

        for (Map.Entry<UUID, List<String>> entry : invToSharedIndicators.entrySet()) {
            UUID linkedId = entry.getKey();
            List<String> matchedValues = entry.getValue();
            Investigation linkedInv = otherInvestigationCache.get(linkedId);
            boolean isLinkedDemo = isSyntheticInvestigation(linkedInv);

            String title = linkedInv != null ? linkedInv.getTitle() : "Linked Dossier #" + linkedId.toString().substring(0, 8);
            String category = linkedInv != null && linkedInv.getCategory() != null ? linkedInv.getCategory().name() : "PHISHING";
            String status = linkedInv != null && linkedInv.getStatus() != null ? linkedInv.getStatus().name() : "UNDER_INVESTIGATION";

            String linkConfidence;
            String explanation;

            if (isCurrentDemo) {
                // Current investigation is synthetic demo: relationships to other demo cases are SYNTHETIC_DEMO
                linkConfidence = "SYNTHETIC_DEMO";
                explanation = "[DEMO] Shares " + matchedValues.size() + " synthetic demonstration indicator(s) ["
                        + String.join(", ", matchedValues.stream().limit(3).toList())
                        + (matchedValues.size() > 3 ? "..." : "") + "] with " + currentInvestigation.getTitle() + ".";
                syntheticDossierLinkCount++;
            } else if (isLinkedDemo) {
                // Current investigation is genuine, but linked dossier is a synthetic demo case.
                // Exclude synthetic dossier from real-world attribution calculations.
                linkConfidence = "SYNTHETIC_DEMO";
                explanation = "[DEMO EXCLUDED] Shares " + matchedValues.size() + " demonstration indicator(s) ["
                        + String.join(", ", matchedValues.stream().limit(3).toList())
                        + (matchedValues.size() > 3 ? "..." : "") + "] with synthetic demo dossier " + title + ". Excluded from real-world attribution.";
                syntheticDossierLinkCount++;
            } else {
                // Genuine-to-genuine investigation link
                List<String> genuineMatched = matchedValues.stream()
                        .filter(val -> !SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(val))
                        .toList();

                if (hasVerifiedMaliciousThreatHit) {
                    linkConfidence = "CONFIRMED";
                } else {
                    linkConfidence = genuineMatched.size() >= 2 ? "PROBABLE" : "POSSIBLE";
                }
                explanation = "Shares " + matchedValues.size() + " infrastructure indicator(s) ["
                        + String.join(", ", matchedValues.stream().limit(3).toList())
                        + (matchedValues.size() > 3 ? "..." : "") + "] with " + currentInvestigation.getTitle() + ".";
                genuineCrossCaseCount++;
            }

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
        List<SharedIndicatorDto> genuineSharedIndicators = sharedIndicators.stream()
                .filter(si -> !SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(si.value()))
                .toList();

        if (isCurrentDemo) {
            // Synthetic investigations are ALWAYS classified as SYNTHETIC_DEMO_CAMPAIGN or ISOLATED_INCIDENT
            classification = sharedIndicators.isEmpty() ? CLASSIFICATION_ISOLATED : CLASSIFICATION_SYNTHETIC_DEMO;
        } else {
            // Current investigation is GENUINE:
            // Real-world attribution is strictly derived from genuine evidence and genuine connected dossiers.
            long genuineCrossCaseSharedCount = genuineSharedIndicators.stream()
                    .filter(SharedIndicatorDto::crossInvestigation)
                    .count();

            boolean hasGenuineConnectedCase = genuineCrossCaseCount > 0;
            boolean genuineHasIndependentCorroboration = hasGenuineConnectedCase || genuineSharedIndicators.size() >= 2;

            if (genuineSharedIndicators.isEmpty()) {
                classification = CLASSIFICATION_ISOLATED;
            } else if (hasVerifiedMaliciousThreatHit && genuineHasIndependentCorroboration) {
                classification = CLASSIFICATION_CONFIRMED;
            } else if (genuineCrossCaseSharedCount >= 2 || genuineSharedIndicators.size() >= 3) {
                classification = CLASSIFICATION_PROBABLE;
            } else {
                classification = CLASSIFICATION_POSSIBLE;
            }
        }

        List<CampaignSignalDto> campaignSignals = new ArrayList<>();
        if (!sharedIndicators.isEmpty()) {
            int sharedCount = isCurrentDemo ? sharedIndicators.size() : genuineSharedIndicators.size();
            Set<String> allSupportingEvidenceIds = new LinkedHashSet<>();
            Set<String> allSupportingInvIds = new LinkedHashSet<>();
            (isCurrentDemo ? sharedIndicators : genuineSharedIndicators).forEach(si -> {
                allSupportingEvidenceIds.addAll(si.evidenceIds());
                if (si.investigationIds() != null) allSupportingInvIds.addAll(si.investigationIds());
            });

            String signalDescription;
            if (isCurrentDemo) {
                signalDescription = sharedCount + " synthetic demonstration indicator(s) cross-correlated across "
                        + (connectedInvestigations.size() + 1) + " demonstration cases (RFC 2606/5737 test fixtures)";
            } else if (genuineSharedIndicators.isEmpty()) {
                signalDescription = "Zero genuine shared indicators observed; incident exhibits isolated characteristics";
            } else if (hasVerifiedMaliciousThreatHit && (genuineCrossCaseCount > 0 || genuineSharedIndicators.size() >= 2)) {
                signalDescription = sharedCount + " shared indicator(s) confirmed malicious by threat intelligence feeds — confirmed coordinated campaign";
            } else if (genuineCrossCaseCount >= 1 && sharedCount >= 2) {
                signalDescription = sharedCount + " shared indicator(s) cross-correlated across "
                        + (genuineCrossCaseCount + 1) + " investigations — multi-case attack campaign detected";
            } else if (genuineCrossCaseCount >= 1) {
                signalDescription = sharedCount + " shared indicator(s) cross-correlated across "
                        + (genuineCrossCaseCount + 1) + " investigations — possible multi-case correlation";
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

            // If genuine case observed synthetic demo fixtures in workspace, add explicit isolation signal
            if (!isCurrentDemo && syntheticDossierLinkCount > 0) {
                campaignSignals.add(new CampaignSignalDto(
                        syntheticDossierLinkCount + " synthetic demonstration dossier(s) detected in tenant workspace and strictly isolated from real-world attribution",
                        "SYNTHETIC_DEMO_ISOLATION",
                        List.of(),
                        connectedInvestigations.stream()
                                .filter(ci -> "SYNTHETIC_DEMO".equals(ci.confidence()))
                                .map(ci -> ci.id().toString())
                                .toList(),
                        "SYNTHETIC_DEMO_ISOLATION"
                ));
            }
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

    public static boolean isSyntheticIndicator(String value) {
        return SyntheticIndicatorDetector.isSyntheticIndicator(value);
    }

    public static boolean isSyntheticInvestigation(Investigation inv) {
        if (inv == null) return false;
        if (inv.isSyntheticDemo()) return true;
        return inv.getTitle() != null && inv.getTitle().startsWith("[DEMO]");
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

    private String checkVerificationStatus(List<UUID> indicatorIds, String indicatorValue) {
        if (isSyntheticIndicator(indicatorValue)) {
            return "SYNTHETIC_TEST_INDICATOR";
        }
        if (lookupRepository == null || indicatorIds == null || indicatorIds.isEmpty()) {
            return "DETERMINISTIC";
        }
        for (UUID id : indicatorIds) {
            try {
                List<ThreatLookup> lookups = lookupRepository.findByIndicatorId(id);
                for (ThreatLookup l : lookups) {
                    if (isSyntheticLookup(l)) {
                        continue; // Synthetic lookups never count as external threat intel
                    }
                    if (l.getThreatStatus() == ThreatStatus.MALICIOUS) {
                        return "VERIFIED_MALICIOUS";
                    }
                    if (l.getThreatStatus() == ThreatStatus.SUSPICIOUS) {
                        return "VERIFIED_SUSPICIOUS";
                    }
                }
            } catch (Exception ignored) {}
        }
        return "DETERMINISTIC";
    }

    private boolean isSyntheticLookup(ThreatLookup l) {
        if (l == null) return false;
        String provider = l.getProvider() != null ? l.getProvider().toLowerCase(Locale.ROOT) : "";
        if (provider.contains("synthetic") || provider.contains("demo")) return true;
        if (l.getResultDetails() != null && (l.getResultDetails().contains("RFC-2606") || l.getResultDetails().contains("Synthetic") || l.getResultDetails().contains("demo"))) return true;
        if (l.getProviderReference() != null && l.getProviderReference().contains("RFC-2606")) return true;
        return isSyntheticIndicator(l.getIndicatorValue());
    }

    private String buildAttributionSummary(Investigation current, List<ConnectedInvestigationDto> connected,
                                           List<SharedIndicatorDto> shared, String classification) {
        if (shared.isEmpty()) {
            return "No cross-evidence or cross-investigation indicator matches were identified. "
                    + "This incident currently exhibits isolated characteristics.";
        }
        if (CLASSIFICATION_SYNTHETIC_DEMO.equals(classification)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Synthetic demonstration campaign footprint detected (SYNTHETIC_DEMO_CAMPAIGN). ");
            sb.append("Correlated ").append(connected.size()).append(" demonstration case(s) [")
                    .append(String.join(", ", connected.stream().map(ConnectedInvestigationDto::title).toList()))
                    .append("] via ").append(shared.size()).append(" shared RFC 2606 (.test) and RFC 5737 (TEST-NET) indicators. ");
            sb.append("Demonstration fixtures illustrate cross-case correlation mechanics and cannot produce real-world CONFIRMED threat attribution.");
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Campaign footprint analysis classified this activity as ").append(classification).append(". ");

        List<ConnectedInvestigationDto> genuineConnected = connected.stream()
                .filter(c -> !"SYNTHETIC_DEMO".equals(c.confidence()))
                .toList();
        List<ConnectedInvestigationDto> syntheticConnected = connected.stream()
                .filter(c -> "SYNTHETIC_DEMO".equals(c.confidence()))
                .toList();

        if (!genuineConnected.isEmpty()) {
            sb.append("This case links directly to ").append(genuineConnected.size())
                    .append(" other investigation(s) in your organization [")
                    .append(String.join(", ", genuineConnected.stream().map(ConnectedInvestigationDto::title).toList()))
                    .append("] through ")
                    .append(shared.size()).append(" shared indicator(s). ");
        } else if (!syntheticConnected.isEmpty()) {
            sb.append("No genuine cross-case links identified. ");
        } else {
            sb.append("Multiple distinct evidence artifacts within this case converge on ").append(shared.size())
                    .append(" shared infrastructure point(s). ");
        }
        sb.append("Shared infrastructure establishes relationship provenance; threat-actor attribution remains an analytical hypothesis.");
        if (!syntheticConnected.isEmpty()) {
            sb.append(" Note: ").append(syntheticConnected.size())
                    .append(" synthetic demonstration dossier(s) were observed in tenant workspace and strictly isolated from real-world attribution.");
        }
        return sb.toString();
    }
}
