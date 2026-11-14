package com.phantomtrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.integration.urlhaus.UrlhausClient;
import com.phantomtrace.integration.virustotal.VirusTotalClient;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreatIntelligenceServiceTest {
    @Mock ExtractedIndicatorRepository indicatorRepository;
    @Mock ThreatLookupRepository lookupRepository;
    @Mock VirusTotalClient virusTotalClient;
    @Mock UrlhausClient urlhausClient;

    @Test
    void normalizesProviderResultsWithoutTreatingUnavailableAsClean() {
        UUID evidenceId = UUID.randomUUID();
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        Investigation investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        Evidence evidence = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, null);
        ReflectionTestUtils.setField(evidence, "id", evidenceId);
        ExtractedIndicator indicator = new ExtractedIndicator(evidence, IndicatorType.URL,
                "https://suspicious-bank-login.com/auth", "https://suspicious-bank-login.com/auth", "test", java.time.Instant.now());
        ReflectionTestUtils.setField(indicator, "id", UUID.randomUUID());

        when(indicatorRepository.findByEvidenceIdAndIndicatorType(evidenceId, IndicatorType.URL))
                .thenReturn(List.of(indicator));
        when(lookupRepository.findByIndicatorIdAndLookupTimestampAfter(any(), any())).thenReturn(List.of());
        when(lookupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(virusTotalClient.lookup(any())).thenReturn(new VirusTotalClient.ProviderLookup(
                indicator.getNormalizedValue(), "COMPLETED", "MALICIOUS", "{\"malicious\":1}", "vt-id", null));
        when(urlhausClient.lookup(any())).thenReturn(new UrlhausClient.ProviderLookup(
                indicator.getOriginalValue(), "UNAVAILABLE", "UNAVAILABLE", null, null, "credentials missing"));

        var results = new ThreatIntelligenceService(indicatorRepository, lookupRepository,
                virusTotalClient, urlhausClient, Duration.ofHours(1)).lookupEvidence(evidenceId);

        assertThat(results).extracting("threatStatus")
                .containsExactlyInAnyOrder(com.phantomtrace.enums.ThreatStatus.MALICIOUS,
                        com.phantomtrace.enums.ThreatStatus.UNAVAILABLE);
    }

    @Test
    void syntheticIndicatorsNeverQueryExternalFeedsAndReturnUnknownWithSyntheticReason() {
        UUID evidenceId = UUID.randomUUID();
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        Investigation investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        Evidence evidence = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, null);
        ReflectionTestUtils.setField(evidence, "id", evidenceId);

        // RFC 2606 test domain and RFC 5737 test IP URLs
        ExtractedIndicator ind1 = new ExtractedIndicator(evidence, IndicatorType.URL,
                "https://secure-netbanking-verify.internal-demo.test/login?case=49102",
                "https://secure-netbanking-verify.internal-demo.test/login?case=49102",
                "test", java.time.Instant.now());
        ReflectionTestUtils.setField(ind1, "id", UUID.randomUUID());

        ExtractedIndicator ind2 = new ExtractedIndicator(evidence, IndicatorType.URL,
                "http://198.51.100.42/login", "http://198.51.100.42/login",
                "test", java.time.Instant.now());
        ReflectionTestUtils.setField(ind2, "id", UUID.randomUUID());

        when(indicatorRepository.findByEvidenceIdAndIndicatorType(evidenceId, IndicatorType.URL))
                .thenReturn(List.of(ind1, ind2));
        when(lookupRepository.findByIndicatorIdAndLookupTimestampAfter(any(), any())).thenReturn(List.of());
        when(lookupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var results = new ThreatIntelligenceService(indicatorRepository, lookupRepository,
                virusTotalClient, urlhausClient, Duration.ofHours(1)).lookupEvidence(evidenceId);

        // External clients must NEVER be invoked for synthetic test fixtures
        verify(virusTotalClient, never()).lookup(any());
        verify(urlhausClient, never()).lookup(any());

        // Must return UNKNOWN with explicit SYNTHETIC_TEST_INDICATOR reason
        assertThat(results).hasSize(4); // 2 providers x 2 indicators
        assertThat(results).allMatch(r -> r.threatStatus() == com.phantomtrace.enums.ThreatStatus.UNKNOWN);
        assertThat(results).allMatch(r -> "SYNTHETIC_TEST_INDICATOR".equals(r.providerReference()));
        assertThat(results).allMatch(r -> r.resultDetails().contains("SYNTHETIC_TEST_INDICATOR"));
    }

    @Test
    void providerClientsReportMissingCredentialsAsUnavailable() {
        var virusTotal = new com.phantomtrace.integration.virustotal.VirusTotalClientImpl(
                new ObjectMapper(), "https://invalid.example", "");
        var urlhaus = new com.phantomtrace.integration.urlhaus.UrlhausClientImpl(
                new ObjectMapper(), "https://invalid.example", "");

        assertThat(virusTotal.lookup("https://example.test").lookupStatus()).isEqualTo("UNAVAILABLE");
        assertThat(urlhaus.lookup("https://example.test").threatStatus()).isEqualTo("UNAVAILABLE");
    }
}
