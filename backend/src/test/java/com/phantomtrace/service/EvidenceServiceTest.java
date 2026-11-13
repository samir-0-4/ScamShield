package com.phantomtrace.service;

import com.phantomtrace.dto.evidence.CreateEvidenceRequest;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.security.SecurityUtils;
import com.phantomtrace.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DeterministicExtractionService deterministicExtractionService;

    @Mock
    private FileEvidenceProcessingService fileEvidenceProcessingService;

    private EvidenceServiceImpl evidenceService;

    private UUID userId;
    private UUID otherUserId;
    private UUID investigationId;
    private UUID evidenceId;
    private Investigation investigation;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        investigationId = UUID.randomUUID();
        evidenceId = UUID.randomUUID();
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);
        investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(investigation, "id", investigationId);
        evidenceService = new EvidenceServiceImpl(evidenceRepository, investigationRepository, securityUtils,
                fileStorageService, deterministicExtractionService, fileEvidenceProcessingService,
                25L * 1024 * 1024);
    }

    @Test
    void createsMultipleStructuredEvidenceTypesForOneInvestigation() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        when(evidenceRepository.existsByInvestigationIdAndContentHash(any(), any())).thenReturn(false);
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(invocation -> {
            Evidence evidence = invocation.getArgument(0);
            ReflectionTestUtils.setField(evidence, "id", UUID.randomUUID());
            return evidence;
        });

        var message = evidenceService.create(investigationId,
                new CreateEvidenceRequest(EvidenceType.TEXT_MESSAGE, "Pay now", null, null, null, null, null, null));
        var url = evidenceService.create(investigationId,
                new CreateEvidenceRequest(EvidenceType.URL, null, "https://example.com", null, null, null, null, null));
        var email = evidenceService.create(investigationId,
                new CreateEvidenceRequest(EvidenceType.EMAIL, null, null, "sender@example.com",
                        "Urgent", "Please pay", null, null));

        assertThat(message.evidenceType()).isEqualTo(EvidenceType.TEXT_MESSAGE);
        assertThat(url.url()).isEqualTo("https://example.com");
        assertThat(email.senderAddress()).isEqualTo("sender@example.com");
        verify(evidenceRepository, org.mockito.Mockito.times(3)).save(any(Evidence.class));
    }

    @Test
    void rejectsInvalidUrlAndDuplicateEvidence() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        when(evidenceRepository.existsByInvestigationIdAndContentHash(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> evidenceService.create(investigationId,
                new CreateEvidenceRequest(EvidenceType.URL, null, "not-a-url", null, null, null, null, null)))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() -> evidenceService.create(investigationId,
                new CreateEvidenceRequest(EvidenceType.TEXT_MESSAGE, "same", null, null, null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void rejectsInvalidImageSignatureAndCrossUserAccess() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "not-an-image".getBytes());

        assertThatThrownBy(() -> evidenceService.createFile(investigationId,
                new CreateEvidenceRequest(EvidenceType.IMAGE, null, null, null, null, null, null, null), file))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("invalid image");

        when(securityUtils.getCurrentUserId()).thenReturn(otherUserId);
        assertThatThrownBy(() -> evidenceService.list(investigationId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Access denied");
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    void rejectsOversizedFilesBeforeReadingThem() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(26L * 1024 * 1024);

        assertThatThrownBy(() -> evidenceService.createFile(investigationId,
                new CreateEvidenceRequest(EvidenceType.PDF, null, null, null, null, null, null, null), file))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("maximum allowed size");
    }
}
