package com.phantomtrace.dto.evidence;

import com.phantomtrace.entity.Evidence;
import com.phantomtrace.enums.EvidenceType;

import java.time.Instant;
import java.util.UUID;

public record EvidenceResponse(
        UUID id,
        UUID investigationId,
        EvidenceType evidenceType,
        String originalFilename,
        String textContent,
        String url,
        String senderAddress,
        String subject,
        String emailBody,
        String emailHeaders,
        String extractedContent,
        String description,
        String mimeType,
        Long fileSize,
        String status,
        Instant createdAt) {

    public static EvidenceResponse fromEntity(Evidence evidence) {
        return new EvidenceResponse(
                evidence.getId(),
                evidence.getInvestigation().getId(),
                evidence.getEvidenceType(),
                evidence.getOriginalFilename(),
                evidence.getTextContent(),
                evidence.getUrl(),
                evidence.getSenderAddress(),
                evidence.getSubject(),
                evidence.getEmailBody(),
                evidence.getEmailHeaders(),
                evidence.getNormalizedContent(),
                evidence.getDescription(),
                evidence.getMimeType(),
                evidence.getFileSize(),
                evidence.getStatus(),
                evidence.getCreatedAt());
    }
}
