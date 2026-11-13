# Phase 4 Design — Multi-Evidence Submission

## Backend

Add `EvidenceService` and `EvidenceServiceImpl` around `EvidenceRepository`,
`InvestigationRepository`, `SecurityUtils`, and `FileStorageService`. The service
resolves and verifies the investigation owner before every operation. JSON evidence
is normalized only for duplicate identity checks; raw content is persisted for later
analysis but never logged.

Use two controller mappings on the same path:

- `application/json` accepts `CreateEvidenceRequest`.
- `multipart/form-data` accepts a JSON `metadata` part and a `file` part.

Return `EvidenceResponse` metadata only. File content is not exposed by the metadata
endpoints. `V3__evidence_submission.sql` adds email fields and indexes while keeping
the evidence table relationally attached to investigations.

`LocalStorageServiceImpl` is hardened to use generated UUID keys, normalized paths
under the configured root, and safe load/delete behavior. The configured upload
directory remains outside frontend static assets.

## Frontend

`evidenceService` owns all evidence API calls. `EvidenceAnalysisPage` loads the
investigation and evidence list, selects a type, validates the corresponding form,
submits one item at a time, and refreshes the list. The page supports multiple
submissions in one session and includes file previews using object URLs that are
revoked after use.

The existing investigation detail action points to this workspace. The analysis
placeholder is replaced with honest evidence collection UI; there is still no
analysis trigger or AI result.
