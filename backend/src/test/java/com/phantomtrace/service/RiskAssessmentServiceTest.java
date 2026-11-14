package com.phantomtrace.service;

import com.phantomtrace.ai.config.RiskAssessmentProperties;
import com.phantomtrace.entity.*;
import com.phantomtrace.enums.*;
import com.phantomtrace.repository.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {
    @Mock InvestigationRepository investigationRepository;
    @Mock EvidenceRepository evidenceRepository;
    @Mock ExtractedIndicatorRepository extractedRepository;
    @Mock ThreatIndicatorRepository threatIndicatorRepository;
    @Mock ThreatLookupRepository lookupRepository;
    @Mock RiskAssessmentRepository assessmentRepository;
    @Mock RiskContributionRepository contributionRepository;
    @Mock SecurityUtils securityUtils;

    private UUID userId;
    private UUID investigationId;
    private Investigation investigation;
    private RiskAssessmentService service;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        investigationId = UUID.randomUUID();
        User user = new User("risk@example.com", "hash", "Risk", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);
        investigation = new Investigation(user, "Risk case", "test", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(investigation, "id", investigationId);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        when(assessmentRepository.save(any())).thenAnswer(invocation -> {
            RiskAssessment value = invocation.getArgument(0);
            ReflectionTestUtils.setField(value, "id", UUID.randomUUID());
            return value;
        });
        lenient().when(contributionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(threatIndicatorRepository.findByInvestigationId(investigationId)).thenReturn(List.of());
        lenient().when(lookupRepository.findByIndicatorId(any())).thenReturn(List.of());
        service = new RiskAssessmentService(investigationRepository, evidenceRepository, extractedRepository,
                threatIndicatorRepository, lookupRepository, assessmentRepository, contributionRepository,
                securityUtils, new RiskAssessmentProperties());
    }

    @Test
    void returnsInconclusiveWhenNoEvidenceExists() {
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of());
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of());

        var result = service.assess(investigationId);

        assertThat(result.category()).isEqualTo(RiskCategory.INCONCLUSIVE);
        assertThat(result.score()).isZero();
        assertThat(result.confidence()).isZero();
    }

    @Test
    void calculatesModerateRiskAndDeduplicatesRepeatedPaymentSignals() {
        Evidence evidence = evidence("Pay the registration fee now");
        ExtractedIndicator payment = indicator(evidence, IndicatorType.PAYMENT_TERM, "fee");
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(evidence));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(payment, payment));

        var result = service.assess(investigationId);

        assertThat(result.score()).isEqualTo(20);
        assertThat(result.category()).isEqualTo(RiskCategory.MODERATE);
        assertThat(result.contributingSignals()).hasSize(1);
        assertThat(result.confidence()).isGreaterThan(0).isLessThan(1);
    }

    @Test
    void capsCriticalScoreAndKeepsConfidenceSeparate() {
        Evidence evidence = evidence("Urgent pay fee and share OTP password");
        ExtractedIndicator payment = indicator(evidence, IndicatorType.PAYMENT_TERM, "payment");
        ExtractedIndicator otp = indicator(evidence, IndicatorType.OTP_TERM, "otp");
        ExtractedIndicator urgency = indicator(evidence, IndicatorType.URGENCY_INDICATOR, "urgent");
        ExtractedIndicator impersonation = indicator(evidence, IndicatorType.MESSAGE_SIGNAL, "IMPERSONATION_INDICATOR");
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(evidence));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId))
                .thenReturn(List.of(payment, otp, urgency, impersonation));

        var result = service.assess(investigationId);

        assertThat(result.score()).isEqualTo(70);
        assertThat(result.category()).isEqualTo(RiskCategory.CRITICAL);
        assertThat(result.confidence()).isNotEqualTo(result.score() / 100.0);
    }

    @Test
    void verifiesAllDocumentedRiskWeightsAndCategoryTiers() {
        // Test LOW tier (0-19): single urgency signal (+10)
        Evidence evUrgency = evidence("Act immediately");
        ExtractedIndicator urgency = indicator(evUrgency, IndicatorType.URGENCY_INDICATOR, "immediately");
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(evUrgency));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(urgency));

        var lowResult = service.assess(investigationId);
        assertThat(lowResult.score()).isEqualTo(10);
        assertThat(lowResult.category()).isEqualTo(RiskCategory.LOW);
        assertThat(lowResult.contributingSignals()).hasSize(1);
        assertThat(lowResult.contributingSignals().get(0).weight()).isEqualTo(10); // Urgency: +10

        // Test MODERATE tier (20-39): Payment (+20) + Unusual URL (+10) = 30
        Evidence evMod = evidence("Transfer fee to http://198.51.100.42/pay");
        ExtractedIndicator payment = indicator(evMod, IndicatorType.PAYMENT_TERM, "transfer");
        ExtractedIndicator unusualUrl = indicator(evMod, IndicatorType.URL, "http://198.51.100.42/pay");
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(evMod));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(payment, unusualUrl));

        var modResult = service.assess(investigationId);
        assertThat(modResult.score()).isEqualTo(30);
        assertThat(modResult.category()).isEqualTo(RiskCategory.MODERATE);

        // Test HIGH tier (40-69): Credential/OTP (+25) + Impersonation (+15) + Urgency (+10) = 50
        Evidence evHigh = evidence("Security alert from bank compliance: submit OTP immediately");
        ExtractedIndicator otp = indicator(evHigh, IndicatorType.OTP_TERM, "otp");
        ExtractedIndicator imp = indicator(evHigh, IndicatorType.MESSAGE_SIGNAL, "impersonation_signal");
        ExtractedIndicator urg = indicator(evHigh, IndicatorType.URGENCY_INDICATOR, "immediately");
        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(evHigh));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(otp, imp, urg));

        var highResult = service.assess(investigationId);
        assertThat(highResult.score()).isEqualTo(50);
        assertThat(highResult.category()).isEqualTo(RiskCategory.HIGH);

        // Test Verified Malicious (+40) + Credential/OTP (+25) + Payment (+20) = 85 -> CRITICAL (70-100)
        ThreatIndicator maliciousThreat = new ThreatIndicator(investigation, IndicatorType.DOMAIN,
                "evil.com", "evil.com", evHigh, "VERIFIED_MALICIOUS", Instant.now());
        when(threatIndicatorRepository.findByInvestigationId(investigationId)).thenReturn(List.of(maliciousThreat));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(otp, payment));

        var critResult = service.assess(investigationId);
        assertThat(critResult.score()).isEqualTo(85); // 40 + 25 + 20
        assertThat(critResult.category()).isEqualTo(RiskCategory.CRITICAL);
        assertThat(critResult.contributingSignals()).anyMatch(s -> s.weight() == 40 && s.signalKey().startsWith("MALICIOUS"));
    }

    @Test
    void capsScoreAtOneHundredWhenCumulativeWeightsExceedMax() {
        Evidence ev = evidence("Critical mega phish");
        ExtractedIndicator otp = indicator(ev, IndicatorType.OTP_TERM, "otp"); // 25
        ExtractedIndicator payment = indicator(ev, IndicatorType.PAYMENT_TERM, "payment"); // 20
        ExtractedIndicator imp = indicator(ev, IndicatorType.MESSAGE_SIGNAL, "impersonation"); // 15
        ExtractedIndicator urg = indicator(ev, IndicatorType.URGENCY_INDICATOR, "urgency"); // 10
        ExtractedIndicator url = indicator(ev, IndicatorType.URL, "http://192.0.2.1/login"); // 10

        ThreatIndicator threat1 = new ThreatIndicator(investigation, IndicatorType.DOMAIN,
                "evil1.com", "evil1.com", ev, "VERIFIED_MALICIOUS", Instant.now()); // 40
        ThreatIndicator threat2 = new ThreatIndicator(investigation, IndicatorType.DOMAIN,
                "evil2.com", "evil2.com", ev, "VERIFIED_MALICIOUS", Instant.now()); // 40

        when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId)).thenReturn(List.of(ev));
        when(extractedRepository.findByEvidenceInvestigationId(investigationId)).thenReturn(List.of(otp, payment, imp, urg, url));
        when(threatIndicatorRepository.findByInvestigationId(investigationId)).thenReturn(List.of(threat1, threat2));

        var result = service.assess(investigationId);

        // Sum = 25 + 20 + 15 + 10 + 10 + 40 + 40 = 160 -> capped at 100
        assertThat(result.score()).isEqualTo(100);
        assertThat(result.category()).isEqualTo(RiskCategory.CRITICAL);
    }

    private Evidence evidence(String text) {
        Evidence value = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, null);
        ReflectionTestUtils.setField(value, "id", UUID.randomUUID());
        value.setTextContent(text);
        return value;
    }

    private ExtractedIndicator indicator(Evidence evidence, IndicatorType type, String value) {
        ExtractedIndicator indicator = new ExtractedIndicator(evidence, type, value, value,
                "test", Instant.now());
        ReflectionTestUtils.setField(indicator, "id", UUID.randomUUID());
        return indicator;
    }
}
