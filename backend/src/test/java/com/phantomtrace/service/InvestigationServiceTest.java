package com.phantomtrace.service;

import com.phantomtrace.dto.PageResponse;
import com.phantomtrace.dto.investigation.CreateInvestigationRequest;
import com.phantomtrace.dto.investigation.InvestigationResponse;
import com.phantomtrace.dto.investigation.UpdateInvestigationRequest;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigationServiceTest {

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private InvestigationServiceImpl investigationService;

    private UUID userId;
    private UUID otherUserId;
    private User user;
    private User otherUser;
    private Investigation investigation;
    private UUID investigationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        user = new User("analyst@phantomtrace.local", "$2a$10$hash", "Analyst Jane", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(user, "id", userId);

        otherUser = new User("other@phantomtrace.local", "$2a$10$hash", "Other User", UserRole.ROLE_ANALYST);
        ReflectionTestUtils.setField(otherUser, "id", otherUserId);

        investigationId = UUID.randomUUID();
        investigation = new Investigation(user, "Suspicious WhatsApp Job", "Received unsolicited offer",
                InvestigationCategory.JOB_SCAM);
        ReflectionTestUtils.setField(investigation, "id", investigationId);
    }

    @Test
    @DisplayName("createInvestigation: persists and returns investigation for authenticated user")
    void createInvestigation_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(investigationRepository.save(any(Investigation.class))).thenAnswer(invocation -> {
            Investigation saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", investigationId);
            return saved;
        });

        CreateInvestigationRequest request = new CreateInvestigationRequest(
                "Suspicious WhatsApp Job",
                "Received unsolicited offer",
                InvestigationCategory.JOB_SCAM);

        InvestigationResponse response = investigationService.createInvestigation(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(investigationId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.title()).isEqualTo("Suspicious WhatsApp Job");
        assertThat(response.category()).isEqualTo(InvestigationCategory.JOB_SCAM);
        assertThat(response.status()).isEqualTo(InvestigationStatus.DRAFT);
        verify(investigationRepository).save(any(Investigation.class));
    }

    @Test
    @DisplayName("listInvestigations: returns user-scoped paginated investigations")
    void listInvestigations_withoutStatusFilter() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Investigation> page = new PageImpl<>(List.of(investigation), pageable, 1);
        when(investigationRepository.findByUserId(userId, pageable)).thenReturn(page);

        PageResponse<InvestigationResponse> result = investigationService.listInvestigations(null, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().get(0).title()).isEqualTo("Suspicious WhatsApp Job");
        verify(investigationRepository).findByUserId(userId, pageable);
    }

    @Test
    @DisplayName("listInvestigations: with status filter calls findByUserIdAndStatus")
    void listInvestigations_withStatusFilter() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Investigation> page = new PageImpl<>(List.of(investigation), pageable, 1);
        when(investigationRepository.findByUserIdAndStatus(userId, InvestigationStatus.DRAFT, pageable))
                .thenReturn(page);

        PageResponse<InvestigationResponse> result = investigationService.listInvestigations(InvestigationStatus.DRAFT,
                pageable);

        assertThat(result.content()).hasSize(1);
        verify(investigationRepository).findByUserIdAndStatus(userId, InvestigationStatus.DRAFT, pageable);
    }

    @Test
    @DisplayName("getInvestigationById: returns investigation when owned by current user")
    void getInvestigationById_ownerAccess() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        InvestigationResponse response = investigationService.getInvestigationById(investigationId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(investigationId);
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("getInvestigationById: throws 403 Forbidden when accessed by non-owner")
    void getInvestigationById_crossUserAccess_throwsForbidden() {
        when(securityUtils.getCurrentUserId()).thenReturn(otherUserId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        assertThatThrownBy(() -> investigationService.getInvestigationById(investigationId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Access denied")
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("getInvestigationById: throws 404 Not Found when ID does not exist")
    void getInvestigationById_notFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigationService.getInvestigationById(investigationId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found")
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("updateInvestigation: updates fields when owner updates")
    void updateInvestigation_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
        when(investigationRepository.save(any(Investigation.class))).thenReturn(investigation);

        UpdateInvestigationRequest request = new UpdateInvestigationRequest(
                "Updated Job Title",
                "New description",
                InvestigationCategory.PHISHING,
                InvestigationStatus.PROCESSING);

        InvestigationResponse response = investigationService.updateInvestigation(investigationId, request);

        assertThat(response.title()).isEqualTo("Updated Job Title");
        assertThat(response.description()).isEqualTo("New description");
        assertThat(response.category()).isEqualTo(InvestigationCategory.PHISHING);
        assertThat(response.status()).isEqualTo(InvestigationStatus.PROCESSING);
        verify(investigationRepository).save(investigation);
    }

    @Test
    @DisplayName("updateInvestigation: throws 403 Forbidden when non-owner updates")
    void updateInvestigation_nonOwner_throwsForbidden() {
        when(securityUtils.getCurrentUserId()).thenReturn(otherUserId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        UpdateInvestigationRequest request = new UpdateInvestigationRequest(
                "Hacked Title", null, null, null);

        assertThatThrownBy(() -> investigationService.updateInvestigation(investigationId, request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(investigationRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteInvestigation: deletes entity when owner deletes")
    void deleteInvestigation_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        investigationService.deleteInvestigation(investigationId);

        verify(investigationRepository).delete(investigation);
    }

    @Test
    @DisplayName("deleteInvestigation: throws 403 Forbidden when non-owner deletes")
    void deleteInvestigation_nonOwner_throwsForbidden() {
        when(securityUtils.getCurrentUserId()).thenReturn(otherUserId);
        when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));

        assertThatThrownBy(() -> investigationService.deleteInvestigation(investigationId))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(investigationRepository, never()).delete(any());
    }
}
