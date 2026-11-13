package com.phantomtrace.service;

import com.phantomtrace.dto.PageResponse;
import com.phantomtrace.dto.investigation.CreateInvestigationRequest;
import com.phantomtrace.dto.investigation.InvestigationResponse;
import com.phantomtrace.dto.investigation.UpdateInvestigationRequest;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class InvestigationServiceImpl implements InvestigationService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationServiceImpl.class);

    private final InvestigationRepository investigationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public InvestigationServiceImpl(InvestigationRepository investigationRepository,
            UserRepository userRepository,
            SecurityUtils securityUtils) {
        this.investigationRepository = investigationRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public InvestigationResponse createInvestigation(CreateInvestigationRequest request) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.UNAUTHORIZED));

        Investigation investigation = new Investigation(
                user,
                request.title().trim(),
                request.description(),
                request.category());

        Investigation saved = investigationRepository.save(investigation);
        log.info("Created investigation {} for user {}", saved.getId(), currentUserId);
        return InvestigationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvestigationResponse> listInvestigations(InvestigationStatus status, Pageable pageable) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        Page<Investigation> page;
        if (status != null) {
            page = investigationRepository.findByUserIdAndStatus(currentUserId, status, pageable);
        } else {
            page = investigationRepository.findByUserId(currentUserId, pageable);
        }
        return PageResponse.fromPage(page.map(InvestigationResponse::fromEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public InvestigationResponse getInvestigationById(UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        Investigation investigation = getAndVerifyOwnership(id, currentUserId);
        return InvestigationResponse.fromEntity(investigation);
    }

    @Override
    public InvestigationResponse updateInvestigation(UUID id, UpdateInvestigationRequest request) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        Investigation investigation = getAndVerifyOwnership(id, currentUserId);

        if (request.title() != null && !request.title().isBlank()) {
            investigation.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            investigation.setDescription(request.description());
        }
        if (request.category() != null) {
            investigation.setCategory(request.category());
        }
        if (request.status() != null) {
            investigation.setStatus(request.status());
        }

        Investigation saved = investigationRepository.save(investigation);
        log.info("Updated investigation {} by user {}", id, currentUserId);
        return InvestigationResponse.fromEntity(saved);
    }

    @Override
    public void deleteInvestigation(UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        Investigation investigation = getAndVerifyOwnership(id, currentUserId);
        investigationRepository.delete(investigation);
        log.info("Deleted investigation {} by user {}", id, currentUserId);
    }

    private Investigation getAndVerifyOwnership(UUID id, UUID currentUserId) {
        Investigation investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Investigation not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!investigation.getUser().getId().equals(currentUserId)) {
            log.warn("Security Alert: User {} attempted unauthorized access to investigation {} owned by {}",
                    currentUserId, id, investigation.getUser().getId());
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }

        return investigation;
    }
}
