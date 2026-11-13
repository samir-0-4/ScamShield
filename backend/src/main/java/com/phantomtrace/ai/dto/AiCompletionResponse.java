package com.phantomtrace.ai.dto;

import java.util.List;

public record AiCompletionResponse(
        String id,
        String model,
        List<Choice> choices,
        Usage usage
) {
    public record Choice(int index, Message message, String finishReason) {
        public record Message(String role, String content) {}
    }

    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}

    public String getFirstContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message() != null) {
            return choices.get(0).message().content();
        }
        return "";
    }
}

