package com.phantomtrace.ai.agent;

import com.phantomtrace.ai.config.AIProviderConfig;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.ai.provider.AiProvider;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileAnalystAgent extends AbstractAiAgent {
    public FileAnalystAgent(AiProvider provider, SecurityManager securityManager,
            StructuredAiResponseParser parser, AIProviderConfig config) {
        super(provider, securityManager, parser, config);
    }

    @Override public String getAgentName() { return "FILE_ANALYST"; }
    @Override public String getRoleDescription() { return "Coordinates safe analysis of supported document metadata."; }
    @Override public StructuredAiResponse analyze(Evidence evidence, List<ExtractedIndicator> indicators) {
        return complete("Analyze only supplied file metadata and extracted text. Do not infer unseen binary contents.", evidence, indicators);
    }
}
