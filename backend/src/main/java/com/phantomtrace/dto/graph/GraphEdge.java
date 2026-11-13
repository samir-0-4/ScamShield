package com.phantomtrace.dto.graph;

public record GraphEdge(String id, String source, String target, String type, String verificationStatus,
        Double confidence, String explanation, String sourceReference) {
}
