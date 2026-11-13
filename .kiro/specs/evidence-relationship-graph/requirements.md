# Phase 11 Spec — Evidence Relationship Graph

> **Spec ID:** `evidence-relationship-graph`
> **Phase:** 11

## Goal

Visualize the authenticated investigation graph returned by
`GET /api/v1/investigations/{id}/graph` using the already-installed React Flow
package. The graph must contain only backend nodes and relationships.

## Requirements

- Render investigation, evidence, and extracted indicator nodes with reusable
  cybersecurity-styled custom node components.
- Render relationship labels, verification state, confidence, explanation, and
  source reference from the backend edge response.
- Support zoom, pan, fit view, node selection, edge selection, and a responsive
  detail panel.
- Show explicit loading, empty, disconnected, and failed-request states.
- Never infer, fabricate, or silently add graph entities or relationships.
