package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvestigationReportAgent extends AbstractAiAgent {
    public InvestigationReportAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "INVESTIGATION_REPORT"; }
    @Override public String getRoleDescription() { return "Produces bounded summaries from verified structured inputs."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Generate an executive summary, findings summary, evidence breakdown, recommended actions, and limitations "
                + "using only supplied structured evidence and indicators.", evidence, indicators);
    }
}
