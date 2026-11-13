package com.phantomtrace.dto.evidence;

import com.phantomtrace.enums.EvidenceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CreateEvidenceRequest(
        @NotNull EvidenceType evidenceType,
        String textContent,
        String url,
        @Email String senderAddress,
        String subject,
        String emailBody,
        String emailHeaders,
        String description) {

    public CreateEvidenceRequest {
        textContent = trimToNull(textContent);
        url = trimToNull(url);
        senderAddress = trimToNull(senderAddress);
        subject = trimToNull(subject);
        emailBody = trimToNull(emailBody);
        emailHeaders = trimToNull(emailHeaders);
        description = trimToNull(description);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
