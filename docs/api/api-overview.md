# PhantomTrace — REST API Specification

## 1. Overview
The PhantomTrace REST API is served over HTTP/JSON under the base path `/api/v1`.

### Standard Response Envelope
All API endpoints return JSON conforming to the standard structure:
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2026-11-09T18:30:00Z"
}
```

---

## 2. Health & Diagnostics

### `GET /api/v1/health`
Basic service availability check.
- **Auth**: Public
- **Response**:
```json
{
  "status": "UP",
  "application": "PhantomTrace"
}
```

---

## 3. Authentication Endpoints

- `POST /api/v1/auth/register` — Create a new analyst account.
- `POST /api/v1/auth/login` — Authenticate and receive a JWT Bearer token.
- `GET /api/v1/auth/me` — Retrieve the current authenticated user profile.

---

## 4. Investigation Endpoints

- `GET /api/v1/investigations` — List investigations for the authenticated user.
- `POST /api/v1/investigations` — Initialize a new investigation.
- `GET /api/v1/investigations/{id}` — Fetch details and metadata for an investigation.
- `PATCH /api/v1/investigations/{id}` — Update title, description, or status.
- `DELETE /api/v1/investigations/{id}` — Soft delete or archive an investigation.

---

## 5. Evidence Ingestion Endpoints

- `POST /api/v1/investigations/{id}/evidence` (`application/json`) — Submit text, URL, or email evidence.
- `POST /api/v1/investigations/{id}/evidence` (`multipart/form-data`) — Submit image or PDF evidence.
- `GET /api/v1/investigations/{id}/evidence` — List all evidence items for an investigation.
- `GET /api/v1/evidence/{evidenceId}` — Retrieve one evidence item.
- `DELETE /api/v1/evidence/{evidenceId}` — Delete an evidence item.
- `GET /api/v1/evidence/{evidenceId}/indicators` — Retrieve deterministic indicators.
- `POST /api/v1/evidence/{evidenceId}/analysis` — Run applicable AI agents.
- `GET /api/v1/evidence/{evidenceId}/analysis` — Retrieve persisted agent executions.
- `POST /api/v1/evidence/{evidenceId}/threat-lookups` — Run configured threat lookups.
- `GET /api/v1/evidence/{evidenceId}/threat-lookups` — Retrieve threat lookup results.

File evidence is processed synchronously after private storage. PDF selectable
text is extracted with PDFBox; image QR payloads are decoded with ZXing; OCR is
attempted only when the Tesseract runtime is installed. Extracted content is
returned as `extractedContent` and is never treated as a visited URL.

---

## 6. Threat Graph Endpoints

- `POST /api/v1/investigations/{id}/graph/generate` — Generate or update persisted deterministic relationships.
- `GET /api/v1/investigations/{id}/graph` — Read the persisted node-and-edge graph projection formatted for React Flow.
- Graph retrieval is read-only and does not invoke threat intelligence, AI analysis, or persistence.

---

## 7. Reports Endpoints

- `POST /api/v1/investigations/{id}/risk-assessment` — Generate a transparent heuristic risk assessment.
- `GET /api/v1/investigations/{id}/risk-assessment` — Retrieve the latest risk assessment.
- `POST /api/v1/investigations/{id}/reports` — Persist a report snapshot from current investigation data.
- `GET /api/v1/investigations/{id}/reports` — List report snapshots.
- `GET /api/v1/reports/{reportId}` — Retrieve one report snapshot.
