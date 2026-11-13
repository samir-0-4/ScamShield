package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UrlDetectiveAgent extends AbstractAiAgent {
    public UrlDetectiveAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "URL_DETECTIVE"; }
    @Override public String getRoleDescription() { return "Analyzes URL syntax, domain structure, and supporting threat intelligence."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Analyze URL syntax, IP hosts, lookalike clues, suspicious paths, and supplied external threat intelligence. "
                + "Do not call a URL and do not claim maliciousness without supporting evidence.", evidence, indicators);
    }
}
