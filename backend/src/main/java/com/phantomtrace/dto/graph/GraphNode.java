package com.phantomtrace.dto.graph;

public record GraphNode(String id, String type, String label, String evidenceId, String indicatorType,
        String verificationStatus) {
}
