package com.phantomtrace.service;

import com.phantomtrace.dto.campaign.CampaignReconstructionResponse;
import com.phantomtrace.dto.campaign.CampaignSignalDto;
import com.phantomtrace.dto.campaign.SharedIndicatorDto;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CampaignReconstructionService {

    private final InvestigationRepository investigationRepository;
    private final ExtractedIndicatorRepository indicatorRepository;
    private final SecurityUtils securityUtils;

    public CampaignReconstructionService(InvestigationRepository investigationRepository,
                                          ExtractedIndicatorRepository indicatorRepository,
                                          SecurityUtils securityUtils) {
        this.investigationRepository = investigationRepository;
        this.indicatorRepository = indicatorRepository;
        this.securityUtils = securityUtils;
    }

    public CampaignReconstructionResponse reconstruct(UUID investigationId) {
        // Ownership check
        Investigation investigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));
        if (!investigation.getUser().getId().equals(securityUtils.getCurrentUserId())) {
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }

        List<ExtractedIndicator> indicators = indicatorRepository.findByEvidenceInvestigationId(investigationId);

        // Group by normalized value: value -> { type, set of evidenceIds }
        record IndicatorGroup(String type, Set<String> evidenceIds) {}
        Map<String, IndicatorGroup> grouped = new LinkedHashMap<>();

        for (ExtractedIndicator indicator : indicators) {
            String value = indicator.getNormalizedValue() != null
                    ? indicator.getNormalizedValue()
                    : indicator.getOriginalValue();
            String evidenceId = indicator.getEvidence().getId().toString();
            String type = indicator.getIndicatorType().name();

            grouped.compute(value, (k, existing) -> {
                if (existing == null) {
                    Set<String> ids = new LinkedHashSet<>();
                    ids.add(evidenceId);
                    return new IndicatorGroup(type, ids);
                } else {
                    existing.evidenceIds().add(evidenceId);
                    return existing;
                }
            });
        }

        // Shared indicators = appearing in 2+ evidence items
        List<SharedIndicatorDto> sharedIndicators = new ArrayList<>();
        for (Map.Entry<String, IndicatorGroup> entry : grouped.entrySet()) {
            if (entry.getValue().evidenceIds().size() >= 2) {
                List<String> evidenceIdList = new ArrayList<>(entry.getValue().evidenceIds());
                sharedIndicators.add(new SharedIndicatorDto(
                        entry.getKey(),
                        entry.getValue().type(),
                        evidenceIdList,
                        evidenceIdList.size()
                ));
            }
        }

        // Campaign signals
        List<CampaignSignalDto> campaignSignals = new ArrayList<>();
        if (!sharedIndicators.isEmpty()) {
            int sharedCount = sharedIndicators.size();
            String confidence = sharedCount >= 3 ? "PROBABLE" : "POSSIBLE";
            Set<String> allSupportingIds = new LinkedHashSet<>();
            sharedIndicators.forEach(si -> allSupportingIds.addAll(si.evidenceIds()));

            String description = sharedCount >= 3
                    ? sharedCount + " shared indicator(s) detected across evidence — strong campaign signal"
                    : sharedCount + " shared indicator(s) detected across evidence — possible coordinated campaign";

            campaignSignals.add(new CampaignSignalDto(
                    description,
                    confidence,
                    new ArrayList<>(allSupportingIds)
            ));
        }

        return new CampaignReconstructionResponse(sharedIndicators, campaignSignals);
    }
}
