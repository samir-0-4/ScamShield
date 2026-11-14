package com.phantomtrace.dto.campaign;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConnectedInvestigationDto(
                UUID id,
                String title,
                String category,
                String status,
                int sharedIndicatorCount,
                List<String> sharedIndicatorValues,
                String relationshipExplanation,
                String confidence,
                Instant createdAt) {
}
