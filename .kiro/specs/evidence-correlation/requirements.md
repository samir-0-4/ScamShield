# Phase 8 Spec — Evidence Correlation

> **Spec ID:** `evidence-correlation`
> **Phase:** 8
> **Status:** Approved — implementation complete

## Goal

Build an investigation-scoped deterministic correlation engine over extracted
indicators and threat-intelligence results. The graph is generated only from
persisted evidence and preserves relationship provenance.

## Requirements

- Normalize indicators consistently before matching.
- Create threat-indicator records and evidence relationships with source and target
  evidence references, verification status, confidence, explanation, and source
  reference.
- Detect message-to-URL, URL-to-domain, repeated normalized indicators, and verified
  threat matches.
- Keep investigations isolated through ownership checks and foreign keys.
- Never promote AI suggestions to confirmed relationships.
- Empty investigations return an empty graph without fabricated nodes.

## Acceptance

A job-offer message containing a URL and another evidence item containing that URL or
domain produces supported deterministic relationships in
`GET /api/v1/investigations/{id}/graph`.
