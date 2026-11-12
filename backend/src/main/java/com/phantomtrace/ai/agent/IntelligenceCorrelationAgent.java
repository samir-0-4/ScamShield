package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntelligenceCorrelationAgent extends AbstractAiAgent {
    public IntelligenceCorrelationAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "INTELLIGENCE_CORRELATION"; }
    @Override public String getRoleDescription() { return "Suggests only evidence-backed relationships between indicators."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Identify repeated domains, email addresses, and URLs only from supplied indicators. "
                + "Explain the exact basis for each suggested relationship and do not invent connections.", evidence, indicators);
    }
}
