package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;

import java.util.List;

public interface AiAgent {

    String getAgentName();

    String getRoleDescription();

    StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators);
}
