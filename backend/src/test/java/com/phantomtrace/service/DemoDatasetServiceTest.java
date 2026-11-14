package com.phantomtrace.service;

import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.RiskContribution;
import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.enums.ThreatStatus;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.never;
import com.phantomtrace.repository.EvidenceRelationshipRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.RiskAssessmentRepository;
import com.phantomtrace.repository.RiskContributionRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDatasetServiceTest {

    @Mock private InvestigationRepository investigationRepository;
    @Mock private EvidenceRepository evidenceRepository;
    @Mock private ExtractedIndicatorRepository indicatorRepository;
    @Mock private ThreatLookupRepository lookupRepository;
    @Mock private RiskAssessmentRepository assessmentRepository;
    @Mock private RiskContributionRepository contributionRepository;
    @Mock private EvidenceRelationshipRepository relationshipRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityUtils securityUtils;

    private DemoDatasetService demoDatasetService;
    private UUID userId;
    private User analyst;

    @BeforeEach
    void setUp() {
        demoDatasetService = new DemoDatasetService(
                investigationRepository, evidenceRepository, indicatorRepository,
                lookupRepository, assessmentRepository, contributionRepository,
                relationshipRepository, userRepository, securityUtils
        );

        userId = UUID.randomUUID();
        analyst = new User("analyst@example.com", "hash", "Analyst", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(analyst, "id", userId);
    }

    @Test
    void seedsThreeInterconnectedDemoCases() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(analyst));
        when(investigationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        when(investigationRepository.save(any(Investigation.class))).thenAnswer(inv -> {
            Investigation i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
            return i;
        });
        when(evidenceRepository.save(any())).thenAnswer(inv -> {
            com.phantomtrace.entity.Evidence e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", UUID.randomUUID());
            return e;
        });
        when(indicatorRepository.save(any())).thenAnswer(inv -> {
            com.phantomtrace.entity.ExtractedIndicator ind = inv.getArgument(0);
            ReflectionTestUtils.setField(ind, "id", UUID.randomUUID());
            return ind;
        });
        when(assessmentRepository.save(any())).thenAnswer(inv -> {
            com.phantomtrace.entity.RiskAssessment r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", UUID.randomUUID());
            return r;
        });

        ArgumentCaptor<ExtractedIndicator> indicatorCaptor = ArgumentCaptor.forClass(ExtractedIndicator.class);
        ArgumentCaptor<RiskContribution> contributionCaptor = ArgumentCaptor.forClass(RiskContribution.class);
        ArgumentCaptor<ThreatLookup> lookupCaptor = ArgumentCaptor.forClass(ThreatLookup.class);

        List<Investigation> seeded = demoDatasetService.seedDemoCampaign();

        assertThat(seeded).hasSize(3);
        assertThat(seeded).allMatch(Investigation::isSyntheticDemo);
        assertThat(seeded).allMatch(inv -> inv.getTitle().startsWith(DemoDatasetService.DEMO_PREFIX));
        assertThat(seeded.get(0).getTitle()).contains("Apex Campaign");
        assertThat(seeded.get(1).getTitle()).contains("KYC Lure");
        assertThat(seeded.get(2).getTitle()).contains("Refund");

        // Verify synthetic IP is 198.51.100.42
        verify(indicatorRepository, org.mockito.Mockito.atLeastOnce()).save(indicatorCaptor.capture());
        List<String> indicatorValues = indicatorCaptor.getAllValues().stream()
                .map(ExtractedIndicator::getNormalizedValue)
                .toList();
        assertThat(indicatorValues).contains("198.51.100.42");
        assertThat(indicatorValues).doesNotContain("185.220.101.5");
        assertThat(indicatorValues).contains("secure-netbanking-verify.internal-demo.test");

        // Verify synthetic indicators do not receive real-world verified threat-intelligence points
        verify(contributionRepository, org.mockito.Mockito.atLeastOnce()).save(contributionCaptor.capture());
        List<RiskContribution> contributions = contributionCaptor.getAllValues();
        assertThat(contributions).noneMatch(c -> "MALICIOUS_THREAT_INTEL".equals(c.getSignalKey()));
        assertThat(contributions).noneMatch(c -> c.getWeight() == 40);

        // Verify threat lookups are marked UNKNOWN with SYNTHETIC_TEST_INDICATOR for synthetic fixtures
        verify(lookupRepository, org.mockito.Mockito.atLeastOnce()).save(lookupCaptor.capture());
        List<ThreatLookup> lookups = lookupCaptor.getAllValues();
        assertThat(lookups).allMatch(l -> l.getThreatStatus() == ThreatStatus.UNKNOWN);
        assertThat(lookups).allMatch(l -> "SYNTHETIC_TEST_INDICATOR".equals(l.getProviderReference()));
    }

    @Test
    void resetsDemoCampaignOnlyForDemoCasesWithSyntheticDemoMarker() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        Investigation realCase = new Investigation(analyst, "Real Fraud Case", "Notes", InvestigationCategory.PHISHING);
        realCase.setSyntheticDemo(false);

        // A user case that happens to have [DEMO] in title but is NOT a synthetic demo
        Investigation userCaseWithDemoPrefix = new Investigation(analyst, "[DEMO] Genuine Enterprise Incident", "Notes", InvestigationCategory.PHISHING);
        userCaseWithDemoPrefix.setSyntheticDemo(false);

        Investigation demoCase = new Investigation(analyst, "[DEMO] Apex Campaign Case", "Demo", InvestigationCategory.PHISHING);
        demoCase.setSyntheticDemo(true);

        when(investigationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(realCase, userCaseWithDemoPrefix, demoCase));

        demoDatasetService.resetDemoCampaign();

        verify(investigationRepository).delete(demoCase);
        verify(investigationRepository, never()).delete(realCase);
        verify(investigationRepository, never()).delete(userCaseWithDemoPrefix);
    }
}
