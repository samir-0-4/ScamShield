package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextDetectiveAgent extends AbstractAiAgent {
    public TextDetectiveAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "TEXT_DETECTIVE"; }
    @Override public String getRoleDescription() { return "Analyzes deterministic message indicators and scam-language patterns."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Analyze urgency, threats, upfront payments, OTP/password requests, unrealistic promises, impersonation, "
                + "and suspicious contact information. Return signals, not a definitive fraud verdict.", evidence, indicators);
    }
}
