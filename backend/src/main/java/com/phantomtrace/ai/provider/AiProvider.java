package com.phantomtrace.ai.provider;

import com.phantomtrace.ai.dto.AiCompletionRequest;
import com.phantomtrace.ai.dto.AiCompletionResponse;

public interface AiProvider {

    String getProviderName();

    AiProviderResult complete(AiCompletionRequest request);
}
