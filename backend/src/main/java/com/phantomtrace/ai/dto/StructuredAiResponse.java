package com.phantomtrace.ai.dto;

import java.util.List;

public record StructuredAiResponse(
        String agentName,
        List<AiFinding> findings,
        List<AiIndicator> indicators,
        String summary,
        List<String> limitations) {
}
