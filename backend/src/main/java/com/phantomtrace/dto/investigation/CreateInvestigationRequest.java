package com.phantomtrace.dto.investigation;

import com.phantomtrace.enums.InvestigationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvestigationRequest(
                @NotBlank(message = "Title is required") @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters") String title,

                @Size(max = 4000, message = "Description must not exceed 4000 characters") String description,

                @NotNull(message = "Category is required") InvestigationCategory category) {
}
