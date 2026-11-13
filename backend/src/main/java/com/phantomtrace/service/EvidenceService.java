package com.phantomtrace.service;

import com.phantomtrace.dto.evidence.CreateEvidenceRequest;
import com.phantomtrace.dto.evidence.EvidenceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EvidenceService {

    EvidenceResponse create(UUID investigationId, CreateEvidenceRequest request);

    EvidenceResponse createFile(UUID investigationId, CreateEvidenceRequest request, MultipartFile file);

    List<EvidenceResponse> list(UUID investigationId);

    EvidenceResponse get(UUID evidenceId);

    void delete(UUID evidenceId);
}
