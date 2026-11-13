package com.phantomtrace.dto.campaign;

import java.util.List;

public record SharedIndicatorDto(
        String value,
        String type,
        List<String> evidenceIds,
        int count
) {}
