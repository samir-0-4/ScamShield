# Phase 10 Spec — Investigation Results

> **Spec ID:** `investigation-results`
> **Phase:** 10
> **Status:** Approved — implementation in progress

## Goal

Provide a clear, accessible investigation results interface backed entirely by
investigation, evidence, deterministic extraction, threat-intelligence, risk, and
structured agent APIs.

## Contract

The page displays investigation metadata, risk score/category/confidence,
executive summary, structured findings, evidence breakdown, provider results,
recommendations, and explicit unknown/unavailable states. It never fabricates
progress, findings, threat results, or recommendations.

Analysis may be started explicitly. Processing shows a real loading state; completed
results are rendered from API responses; failed executions remain visibly failed.
Risk confidence is distinct from risk score and the heuristic disclaimer is shown.
