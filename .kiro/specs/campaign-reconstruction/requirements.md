# Phase 15 Spec — Multi-Case Campaign Reconstruction & Attribution Engine

> **Spec ID:** `campaign-reconstruction`
> **Status:** Active
> **Category:** Frankenstein — Kiroween 2026 Competitive Upgrade

## Goal

Upgrade PhantomTrace from single-evidence indicator matching to full multi-case scam campaign footprint reconstruction. Correlate indicators deterministically across multiple investigations belonging to an analyst's tenant, distinguish direct evidence from inferred relationships, compute honest campaign confidence classifications (`ISOLATED_INCIDENT`, `POSSIBLE`, `PROBABLE`, `CONFIRMED`), explain connected infrastructure, and present an interactive campaign dashboard.

## Requirements

1. **Cross-Investigation Correlation**:
   - Query indicators across all investigations owned by the authenticated user.
   - Detect matching normalized domains, URLs, IPs, emails, phone numbers, crypto addresses, and hashes.
   - Filter or qualify weak/generic common hosts (e.g., generic public email providers) so they do not falsely trigger campaign signals on their own.

2. **Explainable Attribution & Classification**:
   - `ISOLATED_INCIDENT`: No shared indicators detected across evidence or investigations.
   - `POSSIBLE`: Single shared indicator or heuristic link without verified malicious reputation.
   - `PROBABLE`: Two or more distinct shared indicators connecting cases/evidence items.
   - `CONFIRMED`: One or more shared indicators verified as malicious by threat intelligence (VirusTotal/URLhaus) or matching exact cryptographic payload hashes.
   - Every connection must provide an explicit explanation of why the link exists and what evidence supports it.

3. **Connected Case Dossier & Graph Projection**:
   - List connected investigations with titles, IDs, categories, timestamps, and shared indicator counts.
   - Provide visual campaign graph connectivity and campaign footprint metrics.
   - Never create duplicate graph edges or fabricate ungrounded links.

4. **Synthetic Demonstration Dataset**:
   - Provide an automated, idempotent demo seed endpoint (`/api/v1/demo/seed`) and UI trigger.
   - Seed realistic, non-malicious synthetic banking fraud campaign data spanning 3 interconnected cases.
   - Clearly label all synthetic demo data as `DEMO` to maintain epistemic truth standard.

5. **Security & Tenant Isolation**:
   - Strict tenant boundary: Users can only reconstruct campaigns across their own investigations.
   - No data leakage across user accounts.
