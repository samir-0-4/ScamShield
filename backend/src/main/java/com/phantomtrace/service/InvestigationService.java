package com.phantomtrace.service;

import com.phantomtrace.dto.PageResponse;
import com.phantomtrace.dto.investigation.CreateInvestigationRequest;
import com.phantomtrace.dto.investigation.InvestigationResponse;
import com.phantomtrace.dto.investigation.UpdateInvestigationRequest;
import com.phantomtrace.enums.InvestigationStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InvestigationService {

    InvestigationResponse createInvestigation(CreateInvestigationRequest request);

    PageResponse<InvestigationResponse> listInvestigations(InvestigationStatus status, Pageable pageable);

    InvestigationResponse getInvestigationById(UUID id);

    InvestigationResponse updateInvestigation(UUID id, UpdateInvestigationRequest request);

    void deleteInvestigation(UUID id);
}
