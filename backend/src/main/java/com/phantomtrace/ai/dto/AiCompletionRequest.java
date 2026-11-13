package com.phantomtrace.ai.dto;

import java.util.List;
import java.util.Map;

public record AiCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Integer maxTokens,
        Map<String, Object> responseFormat
) {
    public record Message(String role, String content) {
        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String content) {
            return new Message("user", content);
        }

        public static Message assistant(String content) {
            return new Message("assistant", content);
        }
    }
}

