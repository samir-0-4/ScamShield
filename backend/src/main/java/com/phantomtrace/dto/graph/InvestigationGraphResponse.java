package com.phantomtrace.dto.graph;

import java.util.List;

public record InvestigationGraphResponse(String investigationId, List<GraphNode> nodes, List<GraphEdge> edges) {
}
