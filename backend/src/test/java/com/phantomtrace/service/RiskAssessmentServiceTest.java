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
