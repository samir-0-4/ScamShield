package com.phantomtrace.service;

import com.phantomtrace.dto.graph.GraphEdge;
import com.phantomtrace.dto.graph.GraphNode;
import com.phantomtrace.dto.graph.InvestigationGraphResponse;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.EvidenceRelationship;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.ThreatIndicator;
import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.RelationshipType;
import com.phantomtrace.enums.RelationshipVerificationStatus;
import com.phantomtrace.enums.ThreatStatus;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.EvidenceRelationshipRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.ThreatIndicatorRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CorrelationOrchestrator {

    private final InvestigationRepository investigationRepository;
    private final EvidenceRepository evidenceRepository;
    private final ExtractedIndicatorRepository extractedRepository;
    private final ThreatLookupRepository lookupRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final EvidenceRelationshipRepository relationshipRepository;
    private final IndicatorNormalizationService normalizationService;
    private final IndicatorMatchingService matchingService;
    private final SecurityUtils securityUtils;

    public CorrelationOrchestrator(InvestigationRepository investigationRepository,
            EvidenceRepository evidenceRepository, ExtractedIndicatorRepository extractedRepository,
            ThreatLookupRepository lookupRepository, ThreatIndicatorRepository threatIndicatorRepository,
            EvidenceRelationshipRepository relationshipRepository,
            IndicatorNormalizationService normalizationService, IndicatorMatchingService matchingService,
            SecurityUtils securityUtils) {
        this.investigationRepository = investigationRepository;
        this.evidenceRepository = evidenceRepository;
        this.extractedRepository = extractedRepository;
        this.lookupRepository = lookupRepository;
        this.threatIndicatorRepository = threatIndicatorRepository;
        this.relationshipRepository = relationshipRepository;
        this.normalizationService = normalizationService;
        this.matchingService = matchingService;
        this.securityUtils = securityUtils;
    }

    public InvestigationGraphResponse generateGraph(UUID investigationId) {
        Investigation investigation = ownedInvestigation(investigationId);
        List<Evidence> evidence = evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId);
        List<ExtractedIndicator> extracted = evidence.stream()
                .flatMap(item -> extractedRepository.findByEvidenceIdOrderByExtractedAtAsc(item.getId()).stream())
                .toList();

        Map<ExtractedIndicator, ThreatIndicator> threatIndicators = new LinkedHashMap<>();
        for (ExtractedIndicator item : extracted) {
            String normalized = normalizationService.normalize(item.getIndicatorType(), item.getNormalizedValue());
            String status = externalStatus(item);
            ThreatIndicator saved = threatIndicatorRepository
                    .findByInvestigationIdAndIndicatorTypeAndNormalizedValueAndSourceEvidenceId(
                            investigationId, item.getIndicatorType(), normalized, item.getEvidence().getId())
                    .orElseGet(() -> threatIndicatorRepository.save(new ThreatIndicator(
                            investigation, item.getIndicatorType(), item.getOriginalValue(), normalized,
                            item.getEvidence(), status, Instant.now())));
            saved.updateExternalVerificationStatus(status);
            threatIndicators.put(item, saved);
        }

        List<EvidenceRelationship> relationships = new ArrayList<>();
        for (ExtractedIndicator item : extracted) {
            if (item.getIndicatorType() == IndicatorType.URL) {
                relationships.addAll(urlRelationships(investigation, item, extracted, threatIndicators));
            }
            if (item.getIndicatorType() == IndicatorType.EMAIL_ADDRESS) {
                relationships.addAll(emailRelationships(investigation, item, extracted, threatIndicators));
            }
            if (isExternallyVerified(item)) {
                ThreatIndicator target = threatIndicators.get(item);
                relationships.add(new EvidenceRelationship(investigation, null, target,
                        item.getEvidence(), item.getEvidence(), RelationshipType.MATCHES_THREAT_INDICATOR,
                        RelationshipVerificationStatus.EXTERNALLY_VERIFIED, 1.0,
                        "Provider threat intelligence marked this extracted indicator as malicious or suspicious.",
                        providerReference(item), Instant.now()));
            }
        }
        relationships.addAll(sharedRelationships(investigation, extracted, threatIndicators));
        List<EvidenceRelationship> existing = relationshipRepository.findByInvestigationId(investigationId);
        Map<String, EvidenceRelationship> existingByKey = existing.stream()
                .collect(Collectors.toMap(this::relationshipKey, value -> value, (left, right) -> left,
                        LinkedHashMap::new));
        List<EvidenceRelationship> additions = deduplicate(relationships).stream()
                .filter(value -> !existingByKey.containsKey(relationshipKey(value)))
                .toList();
        if (!additions.isEmpty()) {
            relationshipRepository.saveAll(additions);
            existing = new ArrayList<>(existing);
            existing.addAll(additions);
        }
        return project(investigation, evidence, threatIndicatorRepository.findByInvestigationId(investigationId), existing);
    }

    @Transactional(readOnly = true)
    public InvestigationGraphResponse getGraph(UUID investigationId) {
        Investigation investigation = ownedInvestigation(investigationId);
        List<Evidence> evidence = evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId);
        return project(investigation, evidence, threatIndicatorRepository.findByInvestigationId(investigationId),
                relationshipRepository.findByInvestigationId(investigationId));
    }

    private List<EvidenceRelationship> urlRelationships(Investigation investigation, ExtractedIndicator url,
            List<ExtractedIndicator> all, Map<ExtractedIndicator, ThreatIndicator> map) {
        List<EvidenceRelationship> relationships = new ArrayList<>();
        all.stream().filter(item -> item.getEvidence().getId().equals(url.getEvidence().getId())
                && item.getIndicatorType() == IndicatorType.DOMAIN).findFirst().ifPresent(domain ->
                relationships.add(new EvidenceRelationship(investigation, map.get(url), map.get(domain),
                        url.getEvidence(), domain.getEvidence(), RelationshipType.REFERENCES_DOMAIN,
                        RelationshipVerificationStatus.DIRECTLY_EXTRACTED, 1.0,
                        "The domain was deterministically extracted from the URL.", url.getExtractionMethod(), Instant.now())));
        relationships.add(new EvidenceRelationship(investigation, null, map.get(url), url.getEvidence(), url.getEvidence(),
                RelationshipType.CONTAINS_URL, RelationshipVerificationStatus.DIRECTLY_EXTRACTED, 1.0,
                "The URL was deterministically extracted from this evidence item.", url.getExtractionMethod(), Instant.now()));
        return relationships;
    }

    private List<EvidenceRelationship> emailRelationships(Investigation investigation, ExtractedIndicator email,
            List<ExtractedIndicator> all, Map<ExtractedIndicator, ThreatIndicator> map) {
        return List.of(new EvidenceRelationship(investigation, null, map.get(email), email.getEvidence(), email.getEvidence(),
                RelationshipType.MENTIONS_EMAIL, RelationshipVerificationStatus.DIRECTLY_EXTRACTED, 1.0,
                "The email address was deterministically extracted from this evidence item.",
                email.getExtractionMethod(), Instant.now()));
    }

    private List<EvidenceRelationship> sharedRelationships(Investigation investigation,
            List<ExtractedIndicator> extracted, Map<ExtractedIndicator, ThreatIndicator> map) {
        List<EvidenceRelationship> relationships = new ArrayList<>();
        for (List<ExtractedIndicator> group : matchingService.group(extracted).values()) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    ExtractedIndicator left = group.get(i), right = group.get(j);
                    if (left.getEvidence().getId().equals(right.getEvidence().getId())) continue;
                    relationships.add(new EvidenceRelationship(investigation, map.get(left), map.get(right),
                            left.getEvidence(), right.getEvidence(), RelationshipType.SHARES_INDICATOR,
                            RelationshipVerificationStatus.DETERMINISTICALLY_MATCHED, 1.0,
                            "Both evidence items contain the same normalized " + left.getIndicatorType() + ".",
                            "INDICATOR_NORMALIZATION_V1", Instant.now()));
                }
            }
        }
        return relationships;
    }

    private List<EvidenceRelationship> deduplicate(List<EvidenceRelationship> relationships) {
        Map<String, EvidenceRelationship> unique = new LinkedHashMap<>();
        for (EvidenceRelationship value : relationships) {
            String key = value.getRelationshipType() + "|" + value.getSourceEvidence().getId() + "|"
                    + value.getTargetEvidence().getId() + "|"
                    + (value.getSourceIndicator() == null ? "" : value.getSourceIndicator().getNormalizedValue()) + "|"
                    + (value.getTargetIndicator() == null ? "" : value.getTargetIndicator().getNormalizedValue());
            unique.putIfAbsent(key, value);
        }
        return new ArrayList<>(unique.values());
    }

    private String externalStatus(ExtractedIndicator item) {
        List<ThreatLookup> lookups = lookupRepository.findByIndicatorId(item.getId());
        if (lookups.stream().anyMatch(lookup -> lookup.getThreatStatus() == ThreatStatus.MALICIOUS)) {
            return "VERIFIED_MALICIOUS";
        }
        if (lookups.stream().anyMatch(lookup -> lookup.getThreatStatus() == ThreatStatus.SUSPICIOUS)) {
            return "VERIFIED_SUSPICIOUS";
        }
        if (lookups.stream().anyMatch(lookup -> lookup.getThreatStatus() == ThreatStatus.UNAVAILABLE)) {
            return "UNAVAILABLE";
        }
        return "UNVERIFIED";
    }

    private boolean isExternallyVerified(ExtractedIndicator item) {
        String status = externalStatus(item);
        return "VERIFIED_MALICIOUS".equals(status) || "VERIFIED_SUSPICIOUS".equals(status);
    }

    private String providerReference(ExtractedIndicator item) {
        return lookupRepository.findByIndicatorId(item.getId()).stream()
                .map(ThreatLookup::getProviderReference).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private String relationshipKey(EvidenceRelationship value) {
        return value.getRelationshipType() + "|" + value.getSourceEvidence().getId() + "|"
                + value.getTargetEvidence().getId() + "|"
                + (value.getSourceIndicator() == null ? "" : value.getSourceIndicator().getNormalizedValue()) + "|"
                + (value.getTargetIndicator() == null ? "" : value.getTargetIndicator().getNormalizedValue());
    }

    private Investigation ownedInvestigation(UUID id) {
        Investigation value = investigationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));
        if (!value.getUser().getId().equals(securityUtils.getCurrentUserId())) {
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }
        return value;
    }

    private InvestigationGraphResponse project(Investigation investigation, List<Evidence> evidence,
            Collection<ThreatIndicator> indicators, List<EvidenceRelationship> relationships) {
        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(new GraphNode("investigation:" + investigation.getId(), "INVESTIGATION",
                investigation.getTitle(), null, null, "DIRECT"));
        evidence.forEach(item -> nodes.add(new GraphNode("evidence:" + item.getId(),
                item.getEvidenceType() == EvidenceType.TEXT_MESSAGE ? "MESSAGE"
                        : item.getEvidenceType() == EvidenceType.EMAIL ? "EMAIL" : "FILE",
                item.getOriginalFilename() == null ? item.getEvidenceType().name() : item.getOriginalFilename(),
                item.getId().toString(), null, "DIRECT")));
        indicators.forEach(item -> nodes.add(new GraphNode("indicator:" + item.getId(),
                item.getIndicatorType().name(), item.getNormalizedValue(), item.getSourceEvidence().getId().toString(),
                item.getIndicatorType().name(), item.getExternalVerificationStatus())));
        List<GraphEdge> edges = relationships.stream().map(item -> new GraphEdge("relationship:" + item.getId(),
                "evidence:" + item.getSourceEvidence().getId(),
                item.getTargetIndicator() == null ? "evidence:" + item.getTargetEvidence().getId()
                        : "indicator:" + item.getTargetIndicator().getId(),
                item.getRelationshipType().name(), item.getVerificationStatus().name(), item.getConfidence(),
                item.getExplanation(), item.getSourceReference())).toList();
        return new InvestigationGraphResponse(investigation.getId().toString(), nodes, edges);
    }
}
