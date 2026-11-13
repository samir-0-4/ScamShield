package com.phantomtrace.dto.investigation;

import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import jakarta.validation.constraints.Size;

public record UpdateInvestigationRequest(
                @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters") String title,

                @Size(max = 4000, message = "Description must not exceed 4000 characters") String description,

                InvestigationCategory category,

                InvestigationStatus status) {
}
