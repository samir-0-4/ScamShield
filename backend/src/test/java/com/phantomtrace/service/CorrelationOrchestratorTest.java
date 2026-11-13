package com.phantomtrace.service;

import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.EvidenceRelationship;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.RelationshipType;
import com.phantomtrace.enums.RelationshipVerificationStatus;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.repository.EvidenceRelationshipRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.ThreatIndicatorRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationOrchestratorTest {

    @Mock InvestigationRepository investigationRepository;
    @Mock EvidenceRepository evidenceRepository;
    @Mock ExtractedIndicatorRepository extractedRepository;
    @Mock ThreatLookupRepository lookupRepository;
    @Mock ThreatIndicatorRepository threatIndicatorRepository;
    @Mock EvidenceRelationshipRepository relationshipRepository;
    @Mock IndicatorNormalizationService normalizationService;
    @Mock IndicatorMatchingService matchingService;
    @Mock SecurityUtils securityUtils;

    private CorrelationOrchestrator orchestrator;
    private UUID investigationId;
    private Investigation investigation;
    private Evidence evidence;

    @BeforeEach
    void setUp() {
        orchestrator = new CorrelationOrchestrator(investigationRepository, evidenceRepository,
                extractedRepository, lookupRepository, threatIndicatorRepository, relationshipRepository,
                normalizationService, matchingService, securityUtils);
        investigationId = UUID.randomUUID();
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        ReflectionTestUtils.setField(investigation, "id", investigationId);
        evidence = new Evidence(investigation, EvidenceType.TEXT_MESSAGE, "Message");
        ReflectionTestUtils.setField(evidence, "id", UUID.randomUUID());
        when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
        lenient().when(investigationRepository.findById(investigationId))
                .thenReturn(java.util.Optional.of(investigation));
        lenient().when(evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId))
                .thenReturn(List.of(evidence));
    }

    @Test
    void getGraphReturnsPersistedNodesAndEdgesWithoutWriting() {
        EvidenceRelationship relationship = new EvidenceRelationship(investigation, null, null,
                evidence, evidence, RelationshipType.CONTAINS_URL,
                RelationshipVerificationStatus.DIRECTLY_EXTRACTED, 1.0,
                "URL extracted from evidence.", "TEST", Instant.now());
        ReflectionTestUtils.setField(relationship, "id", UUID.randomUUID());
        when(threatIndicatorRepository.findByInvestigationId(investigationId)).thenReturn(List.of());
        when(relationshipRepository.findByInvestigationId(investigationId)).thenReturn(List.of(relationship));

        var graph = orchestrator.getGraph(investigationId);

        assertThat(graph.nodes()).hasSize(2);
        assertThat(graph.edges()).hasSize(1);
        assertThat(graph.edges().getFirst().type()).isEqualTo("CONTAINS_URL");
        verify(relationshipRepository, never()).deleteByInvestigationId(any());
        verify(relationshipRepository, never()).save(any());
        verify(relationshipRepository, never()).saveAll(any());
        verify(threatIndicatorRepository, never()).deleteByInvestigationId(any());
        verify(threatIndicatorRepository, never()).save(any());
        verifyNoInteractions(extractedRepository, lookupRepository, matchingService, normalizationService);
    }

    @Test
    void getGraphRejectsUnauthorizedInvestigation() {
        when(securityUtils.getCurrentUserId()).thenReturn(UUID.randomUUID());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orchestrator.getGraph(investigationId))
                .isInstanceOf(com.phantomtrace.exception.ApiException.class);

        verifyNoInteractions(evidenceRepository, relationshipRepository, threatIndicatorRepository);
    }

    @Test
    void generateGraphDoesNotDeletePersistedDataWhenNothingChanged() {
        when(extractedRepository.findByEvidenceIdOrderByExtractedAtAsc(evidence.getId())).thenReturn(List.of());
        when(threatIndicatorRepository.findByInvestigationId(investigationId)).thenReturn(List.of());
        when(relationshipRepository.findByInvestigationId(investigationId)).thenReturn(List.of());

        var graph = orchestrator.generateGraph(investigationId);

        assertThat(graph.nodes()).hasSize(2);
        verify(relationshipRepository, never()).deleteByInvestigationId(any());
        verify(threatIndicatorRepository, never()).deleteByInvestigationId(any());
        verify(relationshipRepository, never()).saveAll(any());
        verify(threatIndicatorRepository, never()).save(any());
    }
}
