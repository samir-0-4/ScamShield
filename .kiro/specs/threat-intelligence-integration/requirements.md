# Phase 6 Spec — Threat Intelligence Integration

> **Spec ID:** `threat-intelligence-integration`
> **Phase:** 6
> **Status:** Approved — implementation complete

## Goal

Query VirusTotal and URLhaus for extracted URL indicators through isolated,
provider-specific clients. Normalize every response into auditable records without
visiting URLs or exposing credentials to the frontend.

## Behavior

- Missing credentials produce `UNAVAILABLE`, not a clean result.
- Provider rate limits, authentication failures, timeouts, malformed responses, and
  not-found responses remain distinguishable.
- `MALICIOUS`, `SUSPICIOUS`, `NO_MALICIOUS_RESULT`, `NOT_FOUND`, `UNAVAILABLE`, and
  `UNKNOWN` are normalized threat statuses.
- Results retain provider, indicator, status, details, timestamp, provider reference,
  and safe error information.
- Lookups use only validated URL indicators and never follow redirects.
- A short server-side cache prevents unnecessary repeated provider requests.

## Acceptance

With valid credentials, the backend performs real provider lookups. With missing
credentials or provider failures, the application remains usable and explains that
external verification is unavailable. No result is fabricated or treated as clean
when the provider is unavailable.
