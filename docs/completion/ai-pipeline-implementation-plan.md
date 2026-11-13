# AI Agent Orchestration and Evidence Intelligence Implementation Plan

## Current architecture

- `AiAgentOrchestrator` currently runs text, URL, and email agents based on
  evidence type and deterministic URL indicators.
- `FileAnalystAgent`, `IntelligenceCorrelationAgent`, and
  `InvestigationReportAgent` exist but are not fully connected to the primary
  workflow.
- Deterministic indicators are persisted in `extracted_indicators`.
- AI executions are persisted as structured JSON in `agent_executions`.
- `CorrelationOrchestrator` persists deterministic graph entities through the
  explicit generation endpoint; graph GET retrieval is read-only.
- Reports currently consume persisted evidence, indicators, executions, risk,
  threat lookups, and graph data.
- Uploaded image/PDF evidence is validated and stored privately, but file
  content is not extracted.

## Implementation scope

1. Add a bounded file-content extraction service for PDF text, image OCR, and
   QR payloads.
2. Persist extracted file analysis metadata without exposing storage paths.
3. Route file evidence to `FILE_ANALYST` and preserve existing specialist
   routing for text, URL, and email evidence.
4. Run deterministic extraction over extracted file text and decoded QR URLs.
5. Trigger safe graph generation after successful evidence processing while
   keeping graph GET read-only.
6. Expose processing status and extracted content summaries through existing
   evidence responses where compatible.
7. Expand report content from persisted agent executions and actual evidence
   results.
8. Add focused backend and frontend tests and document runtime dependencies.

## Dependency changes

- PDFBox for safe PDF text extraction.
- ZXing core plus a JavaSE bridge for QR decoding.
- Tess4J for optional OCR, requiring a Tesseract runtime in deployments.

OCR must fail visibly as unavailable when the native runtime is not installed;
the application must not claim OCR success.

## Processing model

The initial implementation remains synchronous to avoid introducing an
incomplete queue infrastructure. Processing status is updated as each stage
completes and failures are persisted as `FAILED` with a safe error message.
Long-running production deployments should move OCR and AI work to background
jobs in a later phase.

## Testing strategy

- Unit tests for routing and unsupported types.
- PDF extraction tests for valid, empty, corrupt, and password-protected files.
- QR tests for URL, plain text, and unreadable payloads.
- OCR tests that are skipped or marked unavailable when Tesseract is absent.
- Integration tests for ownership, processing status, graph generation, and
  read-only graph retrieval.
- Frontend tests for processing states, extraction summaries, and failures.

## Risks and limitations

- OCR quality depends on the installed Tesseract language data and native
  runtime.
- WEBP decoding depends on the selected ImageIO provider.
- Scanned PDFs without an OCR pass remain unavailable rather than being
  represented as extracted text.
- Synchronous processing can hold a request open for large files.
- Existing API contracts should be preserved; new response fields must remain
  additive.
