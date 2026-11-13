# Phase 0 Repository Gap Analysis

**Audit date:** 2026-11-10  
**Scope:** Existing PhantomTrace / ScamShield repository before implementing
the later completion-plan phases.

## Executive summary

The repository contains a functional hackathon MVP. The core workflow is
implemented and verified locally: authentication, investigation creation,
multi-evidence submission, deterministic extraction, risk assessment, graph
generation, and report persistence/rendering.

The largest gaps are multimodal file analysis and production hardening. Image
and PDF files can be validated and stored, but a complete OCR/PDF-text/QR
analysis pipeline is not present. Groq, VirusTotal, and URLhaus integrations are
implemented behind adapters and tested with mocks, but live credentials and
provider behavior were not validated during this audit.

## Verification results

| Check | Result | Evidence |
|---|---|---|
| Backend Maven suite | Passed: 66 tests | `./scripts/verify-all.sh` |
| Frontend Vitest suite | Passed: 25 tests | `./scripts/verify-all.sh` |
| Frontend lint | Passed | `./scripts/verify-all.sh` |
| Frontend production build | Passed | `./scripts/verify-all.sh` |
| Docker Compose configuration | Passed | `docker compose config` |
| PostgreSQL smoke test | Passed when container started | PostgreSQL 16 health check |
| Flyway migration validation | Passed through V9 | Backend startup log |
| Auth/investigation/evidence smoke workflow | Passed | Synthetic API workflow |
| Graph/risk/report smoke workflow | Passed | Synthetic API workflow |
| Live Groq provider | Not validated | No live provider key supplied |
| Live VirusTotal | Not validated | No live provider key supplied |
| Live URLhaus | Not validated | No live provider key supplied |
| Browser end-to-end testing | Not implemented | No Playwright/browser suite found |

## Completed features

### Backend

- JWT registration, login, current-user lookup, and ownership checks.
- Investigation CRUD and pagination.
- JSON and multipart evidence submission.
- Text, URL, email, image, and PDF metadata/file validation.
- Private UUID-based local file storage.
- Deterministic indicator extraction and URL normalization.
- VirusTotal and URLhaus integration abstractions.
- Threat lookup status/error handling and caching.
- Shared Groq provider abstraction and structured-response validation.
- Text, URL, email, file, correlation, and report agent classes.
- Evidence-scoped AI execution persistence.
- Deterministic correlation and graph projection.
- Configurable risk assessment with explanations and confidence.
- Persisted investigation report snapshots.
- Flyway migrations V1 through V9.
- Controller and service tests for authentication, evidence, extraction,
  threat intelligence, risk, and core investigation behavior.

### Frontend

- Public landing page and authenticated dashboard layout.
- Protected routes, login, registration, and logout flow.
- Investigation list, creation, details, and dashboard data.
- Multi-evidence submission workspace.
- Results page with risk, findings, indicators, threat status, and actions.
- React Flow graph with custom nodes, labeled edges, provenance panel, and
  graph states.
- Investigation report page and browser print stylesheet.
- Global report index linked to investigation-specific reports.
- Central Axios and API service modules.
- Loading, empty, error, and unavailable states in major workflows.
- Focused component tests for routing, auth, evidence, results, and graph.

## Partially completed features

### File analysis

Image and PDF uploads are accepted, validated, stored, listed, and deleted.
However:

- PDF text is not extracted.
- Image OCR is not implemented.
- QR decoding and destination extraction are not implemented.
- Page-level PDF provenance is not persisted.
- The File Analyst Agent exists, but the orchestrator currently selects agents
  for text, URL, and email evidence rather than running a complete file-content
  pipeline.

Relevant files:

- `backend/src/main/java/com/phantomtrace/service/EvidenceServiceImpl.java`
- `backend/src/main/java/com/phantomtrace/ai/agent/FileAnalystAgent.java`
- `backend/src/main/java/com/phantomtrace/ai/orchestrator/AiAgentOrchestrator.java`

### AI report generation

`InvestigationReportAgent` exists and produces structured output when invoked
through the AI orchestration path. Persisted investigation reports currently
assemble actual stored evidence, executions, risk, graph, and threat data. A
dedicated investigation-level orchestration call that always invokes the Report
Agent is not yet a separate report-generation workflow.

### Threat intelligence

Provider adapters and mocked tests exist. Live provider credentials, quotas,
rate limits, and production endpoint behavior still need validation in the
target deployment environment.

### Frontend quality

The frontend has useful semantic labels and accessible controls, but there is no
automated browser accessibility audit or full cross-device browser test suite.
Responsive behavior is implemented through responsive layouts but needs manual
validation on target devices.

## Missing features

1. Complete PDFBox-style text extraction with page provenance.
2. PDF encryption/password detection and OCR-required classification.
3. Image OCR pipeline.
4. QR-code decoding and QR destination extraction.
5. Unified asynchronous file-analysis pipeline.
6. Browser automation/end-to-end tests.
7. Automated accessibility audit and broader responsive test coverage.
8. Complete application-container Compose deployment for frontend/backend.
9. Production deployment pipeline and operational observability.
10. Production object storage integration and retention policies.
11. Account recovery, email verification, and production-grade rate limiting.
12. Formal report versioning and immutable audit history.

## Broken or risky behavior found

No failing tests or broken core routes were found during the audit. The
following behaviors are development limitations or risks rather than current
test failures:

- Backend configuration contains development fallback values for JWT secret and
  PostgreSQL password. Production startup should fail closed when secure values
  are absent.
- Local storage is appropriate for development but not horizontally scaled
  production.
- The root Docker Compose file starts PostgreSQL only; it does not start the
  application services.
- Live external provider failures can only be verified with provider
  credentials.
- The current graph projection represents stored evidence/indicators but does
  not yet represent QR destinations or extracted file entities.

## Mocked or unverified functionality

- Groq provider automated tests use mocked/local HTTP responses.
- VirusTotal and URLhaus automated tests use mocked provider clients.
- Frontend tests mock API service modules.
- No live external API calls were made during this audit.
- No browser-level workflow was executed.
- The presentation fallback fixture is explicitly not wired into the
  application and must not be presented as live data.

## Security concerns and controls

### Existing controls

- JWT authentication and authenticated ownership checks.
- Private generated storage identifiers.
- MIME, size, and signature checks for supported file uploads.
- No arbitrary URL fetching.
- No uploaded-file execution.
- Backend-only provider credentials.
- Structured AI output validation and bounded input/output configuration.
- Sensitive evidence content is not intentionally logged.

### Follow-up security work

1. Remove development secret fallbacks for production profiles.
2. Enforce secure secret validation at startup.
3. Add production rate limiting and abuse controls.
4. Add dependency scanning and container image scanning.
5. Add formal penetration testing and authorization matrix tests.
6. Add object-storage encryption, retention, and audit controls.
7. Review CORS configuration for deployment-specific origins.

## Recommended implementation order

### Priority 1 — Complete multimodal analysis

1. Add safe PDF text extraction and page provenance.
2. Add image OCR with size/time/output limits.
3. Add QR decoding without visiting decoded destinations.
4. Wire extracted file content into deterministic extraction and File Agent.
5. Add integration tests for malformed, encrypted, oversized, and scanned files.

### Priority 2 — End-to-end confidence

1. Add Playwright/browser tests for registration through report generation.
2. Add mobile viewport and accessibility checks.
3. Add live-provider contract tests using opt-in credentials in CI or staging.
4. Add explicit tests for unavailable providers and AI failures in the UI.

### Priority 3 — Production hardening

1. Add backend/frontend application containers.
2. Add production environment validation and secret management.
3. Add object storage and background processing.
4. Add metrics, structured audit events, rate limiting, and operational alerts.
5. Add report versioning and retention controls.

## Files likely to be affected next

### PDF/OCR/QR

- `backend/pom.xml`
- `backend/src/main/java/com/phantomtrace/service/EvidenceServiceImpl.java`
- `backend/src/main/java/com/phantomtrace/ai/orchestrator/AiAgentOrchestrator.java`
- New file-analysis services, DTOs, entities, repositories, and migrations
- `frontend/src/pages/EvidenceAnalysisPage.jsx`

### Browser and accessibility validation

- `frontend/package.json`
- `frontend/tests/`
- New Playwright configuration and browser test files
- Key pages under `frontend/src/pages/`

### Production containers

- `docker-compose.yml`
- `infrastructure/docker/backend.Dockerfile`
- `infrastructure/docker/frontend.Dockerfile`
- Environment and deployment documentation

## Audit conclusion

The existing application is a credible hackathon MVP and its implemented
workflow is testable. It should not yet be described as a complete multimodal
production platform because PDF text extraction, OCR, QR analysis, live
provider validation, browser automation, and production deployment hardening
remain outstanding.
