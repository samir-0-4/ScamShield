package com.phantomtrace.service;

import com.phantomtrace.dto.campaign.CampaignReconstructionResponse;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignReconstructionServiceTest {

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private ExtractedIndicatorRepository indicatorRepository;

    @Mock
    private SecurityUtils securityUtils;

    private CampaignReconstructionService campaignService;

    private UUID userId;
    private UUID investigationId;
    private Investigation investigation;
    private Evidence evidence1;
    private Evidence evidence2;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignReconstructionService(investigationRepository, indicatorRepository, securityUtils);

        userId = UUID.randomUUID();
        investigationId = UUID.randomUUID();

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        investigation = new Investigation(user, "Operation Ghost", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(investigation, "id", investigationId);

        evidence1 = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, "SMS text");
        ReflectionTestUtils.setField(evidence1, "id", UUID.randomUUID());

        evidence2 = new Evidence(investigation, EvidenceType.EMAIL, "Phish email");
        ReflectionTestUtils.setField(evidence2, "id", UUID.randomUUID());
    }

    @Test
    void rejectsAccessWhenUserDoesNotOwnInvestigation() {
        when(securityUtils.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        assertThatThrownBy(() -> campaignService.reconstruct(investigationId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void returnsEmptySignalsWhenNoSharedIndicators() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        ExtractedIndicator ind1 = new ExtractedIndicator(evidence1, IndicatorType.DOMAIN, "domain1.com", "domain1.com", "REGEX", Instant.now());
        ExtractedIndicator ind2 = new ExtractedIndicator(evidence2, IndicatorType.DOMAIN, "domain2.com", "domain2.com", "REGEX", Instant.now());
        when(indicatorRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(ind1, ind2));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        assertThat(response.sharedIndicators()).isEmpty();
        assertThat(response.campaignSignals()).isEmpty();
    }

    @Test
    void reconstructsSharedIndicatorsAndSignalsAcrossEvidence() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        ExtractedIndicator ind1 = new ExtractedIndicator(evidence1, IndicatorType.DOMAIN, "evil-bank.tk", "evil-bank.tk", "REGEX", Instant.now());
        ExtractedIndicator ind2 = new ExtractedIndicator(evidence2, IndicatorType.DOMAIN, "evil-bank.tk", "evil-bank.tk", "REGEX", Instant.now());
        ExtractedIndicator ind3 = new ExtractedIndicator(evidence1, IndicatorType.IP_ADDRESS, "198.51.100.5", "198.51.100.5", "REGEX", Instant.now());
        ExtractedIndicator ind4 = new ExtractedIndicator(evidence2, IndicatorType.IP_ADDRESS, "198.51.100.5", "198.51.100.5", "REGEX", Instant.now());

        when(indicatorRepository.findByEvidenceInvestigationId(investigationId))
                .thenReturn(List.of(ind1, ind2, ind3, ind4));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        assertThat(response.sharedIndicators()).hasSize(2);
        assertThat(response.campaignSignals()).hasSize(1);
        assertThat(response.campaignSignals().get(0).confidence()).isEqualTo("POSSIBLE");
        assertThat(response.campaignSignals().get(0).description()).contains("2 shared indicator(s)");
    }

    @Test
    void reconstructsCrossInvestigationCampaignSignals() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID secondInvId = UUID.randomUUID();
        Investigation secondInv = new Investigation(user, "Phishing Attack Wave 2", "Context 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(secondInv, "id", secondInvId);

        Evidence evidenceInSecondCase = new Evidence(secondInv, EvidenceType.EMAIL, "Phish email 2");
        ReflectionTestUtils.setField(evidenceInSecondCase, "id", UUID.randomUUID());

        ExtractedIndicator case1Ind = new ExtractedIndicator(evidence1, IndicatorType.DOMAIN, "evil-phish.net", "evil-phish.net", "REGEX", Instant.now());
        ExtractedIndicator case2Ind = new ExtractedIndicator(evidenceInSecondCase, IndicatorType.DOMAIN, "evil-phish.net", "evil-phish.net", "REGEX", Instant.now());

        when(indicatorRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(case1Ind));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(case1Ind, case2Ind));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).title()).isEqualTo("Phishing Attack Wave 2");
        assertThat(response.connectedInvestigations().get(0).sharedIndicatorCount()).isEqualTo(1);
        assertThat(response.campaignClassification()).isEqualTo("PROBABLE");
        assertThat(response.attributionSummary()).contains("Phishing Attack Wave 2");
    }

    @Test
    void filtersGenericBenignDomainsFromCampaignConnections() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID secondInvId = UUID.randomUUID();
        Investigation secondInv = new Investigation(user, "Case 2", "Context 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(secondInv, "id", secondInvId);

        Evidence ev2 = new Evidence(secondInv, EvidenceType.EMAIL, "Email 2");
        ReflectionTestUtils.setField(ev2, "id", UUID.randomUUID());

        // Both cases happen to mention google.com (generic domain)
        ExtractedIndicator generic1 = new ExtractedIndicator(evidence1, IndicatorType.DOMAIN, "google.com", "google.com", "REGEX", Instant.now());
        ExtractedIndicator generic2 = new ExtractedIndicator(ev2, IndicatorType.DOMAIN, "google.com", "google.com", "REGEX", Instant.now());

        when(indicatorRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(generic1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(generic1, generic2));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        assertThat(response.connectedInvestigations()).isEmpty();
        assertThat(response.campaignClassification()).isEqualTo("ISOLATED_INCIDENT");
    }
}
