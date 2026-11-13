# Design — Deterministic Evidence Extraction

## Components

- `DeterministicExtractionService`: extracts indicators from an `Evidence` entity.
- `UrlNormalizationService`: validates and normalizes HTTP(S) URLs without visiting
  them.
- `ExtractedIndicator`: immutable persisted result linked to its source evidence.
- `ExtractedIndicatorRepository`: owner-independent persistence used only after the
  parent evidence has passed ownership checks.

## Extraction flow

1. Evidence ownership is validated by the existing evidence service.
2. Structured evidence fields are assembled in stable source order.
3. Regex-based extractors find candidate values and deterministic phrase rules.
4. URL candidates are parsed and normalized; invalid candidates are not persisted.
5. Results are saved with the evidence ID, original value, normalized value, method,
   and UTC extraction timestamp.
6. No URL, domain, email, or IP is resolved or fetched.

The flow is idempotent for a newly-created evidence row because indicators are
deleted and regenerated only by an explicit re-extraction operation in a future
phase; initial creation writes one result set.

## Indicator semantics

`URL`, `DOMAIN`, `EMAIL_ADDRESS`, `IP_ADDRESS`, and `PHONE_NUMBER` represent extracted
observables. `PAYMENT_TERM`, `CREDENTIAL_TERM`, `OTP_TERM`, `SUSPICIOUS_PHRASE`, and
`URGENCY_INDICATOR` represent matched language. `MESSAGE_SIGNAL` represents a named
rule such as `UPFRONT_PAYMENT_REQUEST` or `IMPERSONATION_INDICATOR`.

The normalized value is populated for canonicalizable observables and normalized
terms. The original value is always retained.
