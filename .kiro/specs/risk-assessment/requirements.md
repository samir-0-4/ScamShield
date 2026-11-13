# Phase 9 Spec — Risk Assessment

> **Spec ID:** `risk-assessment`
> **Phase:** 9
> **Status:** Approved — implementation complete

## Goal

Provide a transparent deterministic risk assessment for each investigation. Scores
are heuristic product rules, not statistically validated fraud probabilities.

## Rules

- Verified malicious indicator: +40.
- Credential or OTP request: +25.
- Upfront payment request: +20.
- Strong impersonation indicator: +15.
- Urgency or pressure language: +10.
- Unusual URL characteristic: +10.
- Each signal is counted once per normalized underlying signal.
- Scores are capped at 100.
- Categories: LOW 0–19, MODERATE 20–39, HIGH 40–69, CRITICAL 70–100.
- Insufficient evidence produces `INCONCLUSIVE`, separately from score.
- Confidence is independently derived from evidence completeness and verification.

## Response contract

An assessment returns score, category, confidence, contributing signals and weights,
supporting evidence references, missing information, explanation, and the heuristic
disclaimer. Assessments are owner-scoped and persisted with their contributions.
