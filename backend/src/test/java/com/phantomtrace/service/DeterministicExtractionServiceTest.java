package com.phantomtrace.service;

import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeterministicExtractionServiceTest {

    @Mock
    private ExtractedIndicatorRepository repository;

    private DeterministicExtractionService service;
    private Evidence evidence;
    private final List<com.phantomtrace.entity.ExtractedIndicator> persisted = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new DeterministicExtractionService(repository, new UrlNormalizationService());
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        Investigation investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        evidence = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, null);
        evidence.setTextContent("""
                Urgent! Your account will be suspended today. Pay a registration fee now.
                Send the OTP and password to https://例え.テスト/login?next=pay#now.
                Contact support@example.com or +1 (555) 123-4567. Backup IP 192.0.2.10.
                """);
        ReflectionTestUtils.setField(evidence, "id", UUID.randomUUID());
        when(repository.save(any())).thenAnswer(invocation -> {
            var indicator = (com.phantomtrace.entity.ExtractedIndicator) invocation.getArgument(0);
            persisted.add(indicator);
            return indicator;
        });
    }

    @Test
    void extractsTraceableObservablesTermsAndSignalsWithoutNetworkAccess() {
        List<?> results = service.extractAndPersist(evidence);

        assertThat(results).isNotEmpty();
        assertThat(results).anySatisfy(result -> {
            var indicator = (com.phantomtrace.dto.evidence.ExtractedIndicatorResponse) result;
            assertThat(indicator.evidenceId()).isEqualTo(evidence.getId());
        });
        assertThat(savedTypes()).contains(
                IndicatorType.URL, IndicatorType.DOMAIN, IndicatorType.EMAIL_ADDRESS,
                IndicatorType.IP_ADDRESS, IndicatorType.PHONE_NUMBER,
                IndicatorType.PAYMENT_TERM, IndicatorType.CREDENTIAL_TERM,
                IndicatorType.OTP_TERM, IndicatorType.URGENCY_INDICATOR,
                IndicatorType.MESSAGE_SIGNAL);
    }

    @Test
    void extractsDirectUrlAndPreservesQueryAndFragment() {
        evidence.setEvidenceType(EvidenceType.URL);
        evidence.setTextContent(null);
        evidence.setUrl("HTTPS://example.com/path?token=a%2Fb&next=https%3A%2F%2Fother.example#section");

        service.extractAndPersist(evidence);

        assertThat(savedValues()).anySatisfy(value ->
                assertThat(value).contains("token=a%2Fb", "next=https%3A%2F%2Fother.example", "#section"));
    }

    @Test
    void ignoresMalformedUrlWithoutDiscardingOtherContent() {
        evidence.setTextContent("Use not-a-url and email@example.com");

        service.extractAndPersist(evidence);

        assertThat(savedTypes()).contains(IndicatorType.EMAIL_ADDRESS);
        assertThat(savedTypes()).doesNotContain(IndicatorType.URL);
    }

    private List<IndicatorType> savedTypes() {
        return saved().stream().map(indicator -> indicator.getIndicatorType()).toList();
    }

    private List<String> savedValues() {
        return saved().stream().map(indicator -> indicator.getNormalizedValue()).toList();
    }

    private List<com.phantomtrace.entity.ExtractedIndicator> saved() {
        return persisted;
    }
}
