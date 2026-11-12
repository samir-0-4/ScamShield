package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailDetectiveAgent extends AbstractAiAgent {
    public EmailDetectiveAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "EMAIL_DETECTIVE"; }
    @Override public String getRoleDescription() { return "Analyzes sender, subject, body, links, and supplied headers."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Analyze sender address, display name if supplied, subject, body, links, and headers. "
                + "Never claim SPF, DKIM, or DMARC failure unless verified authentication information is present.", evidence, indicators);
    }
}
