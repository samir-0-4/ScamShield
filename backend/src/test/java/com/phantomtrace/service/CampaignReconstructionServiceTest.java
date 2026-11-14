package com.phantomtrace.service;

import com.phantomtrace.dto.campaign.CampaignReconstructionResponse;
import com.phantomtrace.dto.campaign.CampaignSignalDto;
import com.phantomtrace.dto.campaign.ConnectedInvestigationDto;
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

import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.LookupStatus;
import com.phantomtrace.enums.ThreatStatus;
import com.phantomtrace.repository.ThreatLookupRepository;

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

    @Mock
    private ThreatLookupRepository lookupRepository;

    private CampaignReconstructionService campaignService;

    private UUID userId;
    private UUID investigationId;
    private Investigation investigation;
    private Evidence evidence1;
    private Evidence evidence2;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignReconstructionService(investigationRepository, indicatorRepository, securityUtils, lookupRepository);

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
        ExtractedIndicator ind3 = new ExtractedIndicator(evidence1, IndicatorType.IP_ADDRESS, "185.220.101.5", "185.220.101.5", "REGEX", Instant.now());
        ExtractedIndicator ind4 = new ExtractedIndicator(evidence2, IndicatorType.IP_ADDRESS, "185.220.101.5", "185.220.101.5", "REGEX", Instant.now());

        when(indicatorRepository.findByEvidenceInvestigationId(investigationId))
                .thenReturn(List.of(ind1, ind2, ind3, ind4));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        assertThat(response.sharedIndicators()).hasSize(2);
        assertThat(response.campaignSignals()).hasSize(1);
        assertThat(response.campaignSignals().get(0).confidence()).isEqualTo("POSSIBLE");
        assertThat(response.campaignSignals().get(0).description()).contains("2 shared indicator(s)");
    }

    @Test
    void reconstructsCrossInvestigationCampaignSignalsSingleIndicatorYieldsPossible() {
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

        // A single unverified shared indicator must NOT establish PROBABLE; it establishes POSSIBLE
        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).title()).isEqualTo("Phishing Attack Wave 2");
        assertThat(response.connectedInvestigations().get(0).sharedIndicatorCount()).isEqualTo(1);
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("POSSIBLE");
        assertThat(response.campaignClassification()).isEqualTo("POSSIBLE");
        assertThat(response.attributionSummary()).contains("Phishing Attack Wave 2");
    }

    @Test
    void reconstructsCrossInvestigationCampaignSignalsMultipleIndicatorsYieldsProbable() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID secondInvId = UUID.randomUUID();
        Investigation secondInv = new Investigation(user, "Phishing Attack Wave 2", "Context 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(secondInv, "id", secondInvId);

        Evidence evidenceInSecondCase = new Evidence(secondInv, EvidenceType.EMAIL, "Phish email 2");
        ReflectionTestUtils.setField(evidenceInSecondCase, "id", UUID.randomUUID());

        ExtractedIndicator case1Domain = new ExtractedIndicator(evidence1, IndicatorType.DOMAIN, "evil-phish.net", "evil-phish.net", "REGEX", Instant.now());
        ExtractedIndicator case2Domain = new ExtractedIndicator(evidenceInSecondCase, IndicatorType.DOMAIN, "evil-phish.net", "evil-phish.net", "REGEX", Instant.now());
        ExtractedIndicator case1Ip = new ExtractedIndicator(evidence1, IndicatorType.IP_ADDRESS, "185.220.101.5", "185.220.101.5", "REGEX", Instant.now());
        ExtractedIndicator case2Ip = new ExtractedIndicator(evidenceInSecondCase, IndicatorType.IP_ADDRESS, "185.220.101.5", "185.220.101.5", "REGEX", Instant.now());

        when(indicatorRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(case1Domain, case1Ip));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(case1Domain, case2Domain, case1Ip, case2Ip));

        CampaignReconstructionResponse response = campaignService.reconstruct(investigationId);

        // Multiple distinct infrastructure indicators converge across cases -> PROBABLE
        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).title()).isEqualTo("Phishing Attack Wave 2");
        assertThat(response.connectedInvestigations().get(0).sharedIndicatorCount()).isEqualTo(2);
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("PROBABLE");
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

    @Test
    void classifiesSyntheticDemoCampaignAsSyntheticDemoCampaign() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID demoInvId = UUID.randomUUID();
        Investigation demoInv1 = new Investigation(user, "[DEMO] Apex Campaign — SMS Banking Alert", "Context", InvestigationCategory.PHISHING);
        demoInv1.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv1, "id", demoInvId);

        Evidence demoEv1 = new Evidence(demoInv1, EvidenceType.TEXT_MESSAGE, "SMS text");
        ReflectionTestUtils.setField(demoEv1, "id", UUID.randomUUID());

        UUID demoInv2Id = UUID.randomUUID();
        Investigation demoInv2 = new Investigation(user, "[DEMO] Apex Campaign — Spear-Phishing Email", "Context 2", InvestigationCategory.PHISHING);
        demoInv2.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv2, "id", demoInv2Id);

        Evidence demoEv2 = new Evidence(demoInv2, EvidenceType.EMAIL, "Phish email");
        ReflectionTestUtils.setField(demoEv2, "id", UUID.randomUUID());

        ExtractedIndicator ind1 = new ExtractedIndicator(demoEv1, IndicatorType.DOMAIN,
                "secure-netbanking-verify.internal-demo.test", "secure-netbanking-verify.internal-demo.test", "REGEX", Instant.now());
        ExtractedIndicator ind2 = new ExtractedIndicator(demoEv2, IndicatorType.DOMAIN,
                "secure-netbanking-verify.internal-demo.test", "secure-netbanking-verify.internal-demo.test", "REGEX", Instant.now());
        ExtractedIndicator indIp1 = new ExtractedIndicator(demoEv1, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());
        ExtractedIndicator indIp2 = new ExtractedIndicator(demoEv2, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());

        when(investigationRepository.findById(demoInvId)).thenReturn(Optional.of(demoInv1));
        when(indicatorRepository.findByEvidenceInvestigationId(demoInvId)).thenReturn(List.of(ind1, indIp1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(ind1, ind2, indIp1, indIp2));

        CampaignReconstructionResponse response = campaignService.reconstruct(demoInvId);

        assertThat(response.campaignClassification()).isEqualTo(CampaignReconstructionService.CLASSIFICATION_SYNTHETIC_DEMO);
        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("SYNTHETIC_DEMO");
        assertThat(response.connectedInvestigations().get(0).title()).contains("[DEMO]");
        assertThat(response.attributionSummary()).contains("SYNTHETIC_DEMO_CAMPAIGN");
        assertThat(response.attributionSummary()).contains("RFC 2606");
    }

    @Test
    void syntheticEvidenceCannotProduceConfirmedCampaignEvenWithMaliciousLookup() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID demoInvId = UUID.randomUUID();
        Investigation demoInv1 = new Investigation(user, "[DEMO] Attack Case 1", "Context", InvestigationCategory.PHISHING);
        demoInv1.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv1, "id", demoInvId);

        Evidence demoEv1 = new Evidence(demoInv1, EvidenceType.TEXT_MESSAGE, "SMS text");
        ReflectionTestUtils.setField(demoEv1, "id", UUID.randomUUID());

        UUID demoInv2Id = UUID.randomUUID();
        Investigation demoInv2 = new Investigation(user, "[DEMO] Attack Case 2", "Context 2", InvestigationCategory.PHISHING);
        demoInv2.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv2, "id", demoInv2Id);

        Evidence demoEv2 = new Evidence(demoInv2, EvidenceType.EMAIL, "Email");
        ReflectionTestUtils.setField(demoEv2, "id", UUID.randomUUID());

        UUID ind1Id = UUID.randomUUID();
        ExtractedIndicator ind1 = new ExtractedIndicator(demoEv1, IndicatorType.DOMAIN,
                "secure-netbanking-verify.internal-demo.test", "secure-netbanking-verify.internal-demo.test", "REGEX", Instant.now());
        ReflectionTestUtils.setField(ind1, "id", ind1Id);

        UUID ind2Id = UUID.randomUUID();
        ExtractedIndicator ind2 = new ExtractedIndicator(demoEv2, IndicatorType.DOMAIN,
                "secure-netbanking-verify.internal-demo.test", "secure-netbanking-verify.internal-demo.test", "REGEX", Instant.now());
        ReflectionTestUtils.setField(ind2, "id", ind2Id);

        // Even if an external lookup mock has ThreatStatus.MALICIOUS on a .test domain:
        ThreatLookup simulatedLookup = new ThreatLookup(
                ind1, "VirusTotal (Synthetic Demonstration)",
                "secure-netbanking-verify.internal-demo.test",
                LookupStatus.COMPLETED, ThreatStatus.MALICIOUS, "{}", "REF", null, Instant.now()
        );
        org.mockito.Mockito.lenient().when(lookupRepository.findByIndicatorId(ind1Id)).thenReturn(List.of(simulatedLookup));

        when(investigationRepository.findById(demoInvId)).thenReturn(Optional.of(demoInv1));
        when(indicatorRepository.findByEvidenceInvestigationId(demoInvId)).thenReturn(List.of(ind1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(ind1, ind2));

        CampaignReconstructionResponse response = campaignService.reconstruct(demoInvId);

        // MUST NOT be CONFIRMED!
        assertThat(response.campaignClassification()).isNotEqualTo(CampaignReconstructionService.CLASSIFICATION_CONFIRMED);
        assertThat(response.campaignClassification()).isEqualTo(CampaignReconstructionService.CLASSIFICATION_SYNTHETIC_DEMO);
        assertThat(response.connectedInvestigations().get(0).confidence()).isNotEqualTo("CONFIRMED");
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("SYNTHETIC_DEMO");
    }

    @Test
    void classifiesRealCampaignWithVerifiedMaliciousIntelAsConfirmed() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID realInv1Id = UUID.randomUUID();
        Investigation realInv1 = new Investigation(user, "Executive Spear-Phish Incident", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(realInv1, "id", realInv1Id);

        Evidence realEv1 = new Evidence(realInv1, EvidenceType.EMAIL, "Real phishing email");
        ReflectionTestUtils.setField(realEv1, "id", UUID.randomUUID());

        UUID realInv2Id = UUID.randomUUID();
        Investigation realInv2 = new Investigation(user, "Accounting Wire Fraud Incident", "Context 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(realInv2, "id", realInv2Id);

        Evidence realEv2 = new Evidence(realInv2, EvidenceType.EMAIL, "Wire fraud email");
        ReflectionTestUtils.setField(realEv2, "id", UUID.randomUUID());

        UUID ind1Id = UUID.randomUUID();
        ExtractedIndicator realInd1 = new ExtractedIndicator(realEv1, IndicatorType.DOMAIN,
                "real-malicious-c2.org", "real-malicious-c2.org", "REGEX", Instant.now());
        ReflectionTestUtils.setField(realInd1, "id", ind1Id);

        UUID ind2Id = UUID.randomUUID();
        ExtractedIndicator realInd2 = new ExtractedIndicator(realEv2, IndicatorType.DOMAIN,
                "real-malicious-c2.org", "real-malicious-c2.org", "REGEX", Instant.now());
        ReflectionTestUtils.setField(realInd2, "id", ind2Id);

        // Real threat lookup from VirusTotal confirming malicious infrastructure
        ThreatLookup realLookup = new ThreatLookup(
                realInd1, "VirusTotal", "real-malicious-c2.org",
                LookupStatus.COMPLETED, ThreatStatus.MALICIOUS, "{\"positives\": 42}", "VT-1234", null, Instant.now()
        );
        when(lookupRepository.findByIndicatorId(ind1Id)).thenReturn(List.of(realLookup));

        when(investigationRepository.findById(realInv1Id)).thenReturn(Optional.of(realInv1));
        when(indicatorRepository.findByEvidenceInvestigationId(realInv1Id)).thenReturn(List.of(realInd1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(realInd1, realInd2));

        CampaignReconstructionResponse response = campaignService.reconstruct(realInv1Id);

        // Real campaign with real malicious intel MUST be CONFIRMED
        assertThat(response.campaignClassification()).isEqualTo(CampaignReconstructionService.CLASSIFICATION_CONFIRMED);
        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("CONFIRMED");
    }

    @Test
    void genuineEvidenceContainingExampleComIsNotTreatedAsSyntheticDemo() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID inv1Id = UUID.randomUUID();
        Investigation inv1 = new Investigation(user, "Genuine Investigation With Example URL", "Notes", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(inv1, "id", inv1Id);

        Evidence ev1 = new Evidence(inv1, EvidenceType.EMAIL, "Email with example.com");
        ReflectionTestUtils.setField(ev1, "id", UUID.randomUUID());

        UUID inv2Id = UUID.randomUUID();
        Investigation inv2 = new Investigation(user, "Second Genuine Investigation", "Notes 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(inv2, "id", inv2Id);

        Evidence ev2 = new Evidence(inv2, EvidenceType.EMAIL, "Email 2 with example.com");
        ReflectionTestUtils.setField(ev2, "id", UUID.randomUUID());

        ExtractedIndicator ind1 = new ExtractedIndicator(ev1, IndicatorType.DOMAIN,
                "example.com", "example.com", "REGEX", Instant.now());
        ExtractedIndicator ind2 = new ExtractedIndicator(ev2, IndicatorType.DOMAIN,
                "example.com", "example.com", "REGEX", Instant.now());

        when(investigationRepository.findById(inv1Id)).thenReturn(Optional.of(inv1));
        when(indicatorRepository.findByEvidenceInvestigationId(inv1Id)).thenReturn(List.of(ind1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(ind1, ind2));

        CampaignReconstructionResponse response = campaignService.reconstruct(inv1Id);

        // example.com is a documentation domain, NOT an explicit synthetic demo fixture:
        assertThat(response.campaignClassification()).isNotEqualTo(CampaignReconstructionService.CLASSIFICATION_SYNTHETIC_DEMO);
        assertThat(response.connectedInvestigations().get(0).confidence()).isNotEqualTo("SYNTHETIC_DEMO");
        assertThat(response.attributionSummary()).doesNotContain("SYNTHETIC_DEMO_CAMPAIGN");
    }

    @Test
    void mixedScenarioSyntheticDemoDoesNotContaminateGenuineConfirmedCampaign() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        // 1. Genuine Investigation 1
        UUID genuineInv1Id = UUID.randomUUID();
        Investigation genuineInv1 = new Investigation(user, "Executive Spear-Phish Incident", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(genuineInv1, "id", genuineInv1Id);
        Evidence genuineEv1 = new Evidence(genuineInv1, EvidenceType.EMAIL, "Phish email");
        ReflectionTestUtils.setField(genuineEv1, "id", UUID.randomUUID());

        // 2. Genuine Investigation 2
        UUID genuineInv2Id = UUID.randomUUID();
        Investigation genuineInv2 = new Investigation(user, "Corporate Wire Fraud Attack", "Context 2", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(genuineInv2, "id", genuineInv2Id);
        Evidence genuineEv2 = new Evidence(genuineInv2, EvidenceType.EMAIL, "Wire email");
        ReflectionTestUtils.setField(genuineEv2, "id", UUID.randomUUID());

        // Genuine shared indicator: verified malicious
        UUID genuineInd1Id = UUID.randomUUID();
        ExtractedIndicator genuineInd1 = new ExtractedIndicator(genuineEv1, IndicatorType.DOMAIN,
                "active-c2-malware.org", "active-c2-malware.org", "REGEX", Instant.now());
        ReflectionTestUtils.setField(genuineInd1, "id", genuineInd1Id);

        UUID genuineInd2Id = UUID.randomUUID();
        ExtractedIndicator genuineInd2 = new ExtractedIndicator(genuineEv2, IndicatorType.DOMAIN,
                "active-c2-malware.org", "active-c2-malware.org", "REGEX", Instant.now());
        ReflectionTestUtils.setField(genuineInd2, "id", genuineInd2Id);

        ThreatLookup realLookup = new ThreatLookup(
                genuineInd1, "VirusTotal", "active-c2-malware.org",
                LookupStatus.COMPLETED, ThreatStatus.MALICIOUS, "{\"positives\": 55}", "VT-9999", null, Instant.now()
        );
        when(lookupRepository.findByIndicatorId(genuineInd1Id)).thenReturn(List.of(realLookup));

        // 3. Synthetic Demo Investigation in the same tenant workspace
        UUID demoInvId = UUID.randomUUID();
        Investigation demoInv = new Investigation(user, "[DEMO] Apex Campaign — SMS Banking Alert", "Demo", InvestigationCategory.PHISHING);
        demoInv.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv, "id", demoInvId);
        Evidence demoEv = new Evidence(demoInv, EvidenceType.TEXT_MESSAGE, "Demo SMS");
        ReflectionTestUtils.setField(demoEv, "id", UUID.randomUUID());

        // Suppose genuineEv1 also happens to contain a synthetic IP
        ExtractedIndicator synthIndInGenuine = new ExtractedIndicator(genuineEv1, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());
        ExtractedIndicator synthIndInDemo = new ExtractedIndicator(demoEv, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());

        when(investigationRepository.findById(genuineInv1Id)).thenReturn(Optional.of(genuineInv1));
        when(indicatorRepository.findByEvidenceInvestigationId(genuineInv1Id))
                .thenReturn(List.of(genuineInd1, synthIndInGenuine));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId))
                .thenReturn(List.of(genuineInd1, genuineInd2, synthIndInGenuine, synthIndInDemo));

        CampaignReconstructionResponse response = campaignService.reconstruct(genuineInv1Id);

        // Crucial verification: The genuine campaign MUST NOT be contaminated or downgraded to SYNTHETIC_DEMO_CAMPAIGN!
        assertThat(response.campaignClassification()).isEqualTo(CampaignReconstructionService.CLASSIFICATION_CONFIRMED);

        // Genuine connected case has CONFIRMED confidence
        ConnectedInvestigationDto genuineConnectedDto = response.connectedInvestigations().stream()
                .filter(ci -> ci.id().equals(genuineInv2Id))
                .findFirst().orElseThrow();
        assertThat(genuineConnectedDto.confidence()).isEqualTo("CONFIRMED");

        // Synthetic demo dossier is isolated with SYNTHETIC_DEMO confidence and exclusion explanation
        ConnectedInvestigationDto demoConnectedDto = response.connectedInvestigations().stream()
                .filter(ci -> ci.id().equals(demoInvId))
                .findFirst().orElseThrow();
        assertThat(demoConnectedDto.confidence()).isEqualTo("SYNTHETIC_DEMO");
        assertThat(demoConnectedDto.relationshipExplanation()).contains("Excluded from real-world attribution");

        // Campaign signals explicitly account for isolation
        assertThat(response.campaignSignals()).anyMatch(s -> "SYNTHETIC_DEMO_ISOLATION".equals(s.signalType()));
        assertThat(response.attributionSummary()).contains("CONFIRMED");
        assertThat(response.attributionSummary()).contains("isolated from real-world attribution");
    }

    @Test
    void mixedScenarioGenuineInvestigationSharingOnlySyntheticIndicatorRemainsIsolatedIncident() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        User user = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        UUID genuineInvId = UUID.randomUUID();
        Investigation genuineInv = new Investigation(user, "Genuine Standalone Incident", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(genuineInv, "id", genuineInvId);
        Evidence genuineEv = new Evidence(genuineInv, EvidenceType.EMAIL, "Email");
        ReflectionTestUtils.setField(genuineEv, "id", UUID.randomUUID());

        UUID demoInvId = UUID.randomUUID();
        Investigation demoInv = new Investigation(user, "[DEMO] Apex Campaign Case", "Demo", InvestigationCategory.PHISHING);
        demoInv.setSyntheticDemo(true);
        ReflectionTestUtils.setField(demoInv, "id", demoInvId);
        Evidence demoEv = new Evidence(demoInv, EvidenceType.TEXT_MESSAGE, "Demo text");
        ReflectionTestUtils.setField(demoEv, "id", UUID.randomUUID());

        // The ONLY link between genuineInv and demoInv is a synthetic RFC 5737 IP:
        ExtractedIndicator ind1 = new ExtractedIndicator(genuineEv, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());
        ExtractedIndicator ind2 = new ExtractedIndicator(demoEv, IndicatorType.IP_ADDRESS,
                "198.51.100.42", "198.51.100.42", "REGEX", Instant.now());

        when(investigationRepository.findById(genuineInvId)).thenReturn(Optional.of(genuineInv));
        when(indicatorRepository.findByEvidenceInvestigationId(genuineInvId)).thenReturn(List.of(ind1));
        when(indicatorRepository.findByEvidenceInvestigationUserId(userId)).thenReturn(List.of(ind1, ind2));

        CampaignReconstructionResponse response = campaignService.reconstruct(genuineInvId);

        // Synthetic indicators CANNOT establish a real-world campaign:
        assertThat(response.campaignClassification()).isEqualTo(CampaignReconstructionService.CLASSIFICATION_ISOLATED);
        assertThat(response.connectedInvestigations()).hasSize(1);
        assertThat(response.connectedInvestigations().get(0).confidence()).isEqualTo("SYNTHETIC_DEMO");
        assertThat(response.connectedInvestigations().get(0).relationshipExplanation()).contains("Excluded from real-world attribution");
        assertThat(response.attributionSummary()).contains("ISOLATED_INCIDENT");
    }
}
