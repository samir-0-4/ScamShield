# Phase 4 Spec — Multi-Evidence Submission

> **Spec ID:** `evidence-submission`
> **Phase:** 4
> **Status:** Approved — implementation complete; automated verification passed
> **Depends on:** Phase 2 investigation CRUD and Phase 3 frontend foundation

## 1. Purpose

Allow an authenticated user to attach multiple evidence items to one investigation,
starting with complete text-message, URL, and email workflows and adding secure image
and PDF upload storage. Evidence is persisted with investigation ownership enforced;
AI analysis remains out of scope.

## 2. Evidence model

Supported types:

- `TEXT_MESSAGE`: WhatsApp, SMS, job offers, payment requests, and social messages.
- `URL`: validated URL plus optional description.
- `EMAIL`: sender address, subject, body, and optional headers.
- `IMAGE`: PNG, JPG, JPEG, or WEBP upload.
- `PDF`: PDF upload.

Each evidence item stores its investigation, type, description, created timestamp, and
type-specific metadata. File evidence stores only a generated private storage key,
safe original filename metadata, MIME type, and byte size in PostgreSQL.

## 3. Backend API contract

All endpoints require JWT authentication and ownership of the investigation/evidence:

- `POST /api/v1/investigations/{id}/evidence`
  - JSON body for `TEXT_MESSAGE`, `URL`, or `EMAIL`.
  - Multipart body with `metadata` JSON and `file` for `IMAGE` or `PDF`.
- `GET /api/v1/investigations/{id}/evidence`
  - Returns the owner's evidence list without sensitive file contents.
- `GET /api/v1/evidence/{evidenceId}`
  - Returns metadata for the owner's evidence item.
- `DELETE /api/v1/evidence/{evidenceId}`
  - Deletes the database record and any private stored file.

Validation rejects empty content, malformed URLs, invalid email fields, unsupported
types, oversized files, mismatched MIME/signature, and duplicate submissions for the
same investigation/type/content identity where applicable. Uploaded files are never
executed or placed in frontend/public directories.

## 4. Frontend requirements

The investigation analysis route becomes an evidence workspace with:

- Evidence type selector.
- Text-message, URL, and email forms.
- Image/PDF drag-and-drop or file picker.
- Client-side validation and upload progress.
- Evidence previews, list refresh, empty state, and remove actions.
- Multiple evidence items in one investigation.
- Analysis remains unavailable until at least one valid evidence item exists; no AI
  analysis is implemented in this phase.

## 5. Security and storage

Storage uses the existing `FileStorageService` abstraction. Generated UUID-based keys
must prevent path traversal; storage reads/deletes are ownership-gated. File limits
are configured server-side and enforced before persistence. Sensitive content is not
written to logs.

## 6. Testing

Backend tests cover creation/retrieval/deletion, multiple evidence types, invalid MIME
and size, unauthorized evidence access, and cross-user investigation access. Frontend
tests cover multiple submissions, retrieval, deletion, validation errors, unsupported
files, and upload failures.

## 7. Acceptance criteria

An authenticated user can create an investigation, submit a message, URL, and email
to that same investigation, retrieve all three, and delete any item. All records have
correct metadata and are isolated from other users. AI analysis is not started.
