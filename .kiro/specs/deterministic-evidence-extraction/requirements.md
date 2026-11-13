# Phase 5 Spec — Deterministic Evidence Extraction

> **Spec ID:** `deterministic-evidence-extraction`
> **Phase:** 5
> **Status:** Approved — deterministic extraction implementation complete

## 1. Purpose

Extract auditable indicators and message signals from submitted evidence without
calling Groq or any other language model. Extraction is deterministic, repeatable,
and never a definitive fraud classification.

## 2. Scope

Supported structured evidence inputs:

- Text messages.
- URLs.
- Emails, including sender, subject, body, and optional headers.

Binary image and PDF evidence remains stored securely. The extraction boundary is
designed so future OCR/document parsers can provide text without changing indicator
storage or ownership rules.

## 3. Indicators

The extractor identifies URLs, domains, email addresses, IP addresses, phone
numbers, payment terms, credential terms, OTP terms, suspicious phrases, urgency
indicators, and deterministic message signals. Each result stores the original
matched value, normalized value where applicable, source evidence, extraction
method, and timestamp.

Signals are evidence, not verdicts. The system must not label content fraudulent
based only on these patterns.

## 4. URL handling

URLs are parsed without network access. HTTP and HTTPS schemes are normalized to
lowercase, hosts are normalized using IDN ASCII conversion where supported, and
path, query, and fragment content is preserved. Invalid URLs are ignored as URL
indicators but do not erase the original evidence.

## 5. Persistence

Flyway creates an `extracted_indicators` table linked to `evidence` with cascade
deletion and indexes for evidence and indicator type. The schema is intentionally
separate from future `analysis_results` and `threat_indicators` tables.

## 6. Verification

Unit tests cover extraction, URL normalization, domains, email/IP/phone parsing,
payment/credential/OTP/urgency patterns, malformed input, and traceability. The
acceptance flow submits a message containing a URL and payment request and receives
stored indicators without an AI API call.
