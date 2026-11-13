# Phase 12 Spec — Investigation Reports

## Goal

Generate authenticated, persisted investigation report snapshots from actual
evidence, deterministic indicators, threat intelligence, relationships, risk
assessments, and persisted structured agent executions.

## Requirements

- Support authenticated report creation, listing, and retrieval.
- Preserve unknown and unavailable information explicitly.
- Include report metadata, evidence, analysis, risk, score explanation,
  relationships, external intelligence, actions, limitations, and disclaimer.
- Provide a responsive report page with browser print / Save as PDF support.
- Do not add a server-side PDF generation dependency for the MVP.
