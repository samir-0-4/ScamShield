# PhantomTrace — Database Design & Schema Specification

## 1. Overview
PhantomTrace uses **PostgreSQL 16** as its primary relational store. Database migrations are managed via **Flyway**. Cryptographic `UUIDv4` identifiers are used across all primary and foreign key references to avoid predictable sequence harvesting.

---

## 2. Entity Specifications

### `users`
Represents an analyst, investigator, or authenticated user.
- `id` (UUID, Primary Key)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL)
- `full_name` (VARCHAR(150), NOT NULL)
- `role` (VARCHAR(50), NOT NULL, e.g. `ROLE_ANALYST`, `ROLE_ADMIN`)
- `created_at` (TIMESTAMPTZ, NOT NULL)
- `updated_at` (TIMESTAMPTZ, NOT NULL)

### `investigations`
Represents an overarching case dossier.
- `id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key -> `users.id`, ON DELETE CASCADE)
- `title` (VARCHAR(255), NOT NULL)
- `description` (TEXT)
- `status` (VARCHAR(50), NOT NULL: `CREATED`, `IN_PROGRESS`, `ANALYZING`, `COMPLETED`, `ARCHIVED`)
- `severity` (VARCHAR(50): `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `UNKNOWN`)
- `created_at` (TIMESTAMPTZ, NOT NULL)
- `updated_at` (TIMESTAMPTZ, NOT NULL)

### `evidence`
Represents an individual item submitted into an investigation.
- `id` (UUID, Primary Key)
- `investigation_id` (UUID, Foreign Key -> `investigations.id`, ON DELETE CASCADE)
- `evidence_type` (VARCHAR(50), NOT NULL: `WHATSAPP_MESSAGE`, `EMAIL`, `URL`, `PAYMENT_SCREENSHOT`, `QR_CODE`, `PDF_DOCUMENT`, `RECRUITMENT_MESSAGE`, `OTHER`)
- `title` (VARCHAR(255), NOT NULL)
- `raw_content` (TEXT)
- `file_path` (VARCHAR(512), NULLABLE)
- `file_hash_sha256` (VARCHAR(64), NULLABLE)
- `file_size_bytes` (BIGINT, NULLABLE)
- `mime_type` (VARCHAR(100), NULLABLE)
- `status` (VARCHAR(50), NOT NULL: `PENDING`, `PARSED`, `ANALYZED`, `FAILED`)
- `created_at` (TIMESTAMPTZ, NOT NULL)
- `updated_at` (TIMESTAMPTZ, NOT NULL)

### `evidence_analysis`
Detailed parsing and analytical findings for a single evidence item.
- `id` (UUID, Primary Key)
- `evidence_id` (UUID, Foreign Key -> `evidence.id`, ON DELETE CASCADE)
- `agent_name` (VARCHAR(100), NOT NULL)
- `summary` (TEXT, NOT NULL)
- `confidence_score` (DECIMAL(3, 2), NOT NULL) -- 0.00 to 1.00
- `epistemic_status` (VARCHAR(50), NOT NULL: `VERIFIED`, `AI_INFERENCE`, `UNKNOWN`)
- `findings_json` (JSONB, NOT NULL)
- `created_at` (TIMESTAMPTZ, NOT NULL)

### `threat_indicators`
Atomic observables extracted from evidence (domains, IPs, hashes, phone numbers, crypto addresses).
- `id` (UUID, Primary Key)
- `investigation_id` (UUID, Foreign Key -> `investigations.id`, ON DELETE CASCADE)
- `indicator_type` (VARCHAR(50), NOT NULL: `DOMAIN`, `URL`, `IP_ADDRESS`, `SHA256`, `EMAIL_ADDRESS`, `PHONE_NUMBER`, `CRYPTO_WALLET`, `BANK_ACCOUNT`)
- `indicator_value` (VARCHAR(512), NOT NULL)
- `reputation_score` (INT, NULLABLE) -- e.g. VirusTotal score
- `reputation_source` (VARCHAR(100), NULLABLE) -- e.g. `VIRUSTOTAL`, `URLHAUS`
- `epistemic_status` (VARCHAR(50), NOT NULL: `VERIFIED`, `AI_INFERENCE`, `UNKNOWN`)
- `created_at` (TIMESTAMPTZ, NOT NULL)

### `evidence_relationships`
Nodes and links representing relationships between evidence items and indicators for graph projection.
- `id` (UUID, Primary Key)
- `investigation_id` (UUID, Foreign Key -> `investigations.id`, ON DELETE CASCADE)
- `source_id` (UUID, NOT NULL) -- Evidence or Indicator ID
- `source_type` (VARCHAR(50), NOT NULL: `EVIDENCE`, `INDICATOR`)
- `target_id` (UUID, NOT NULL) -- Evidence or Indicator ID
- `target_type` (VARCHAR(50), NOT NULL: `EVIDENCE`, `INDICATOR`)
- `relationship_type` (VARCHAR(100), NOT NULL, e.g. `CONTAINS_LINK`, `RESOLVES_TO_IP`, `MATCHES_PHONE`, `SIMILAR_CONTENT`)
- `epistemic_status` (VARCHAR(50), NOT NULL: `VERIFIED`, `AI_INFERENCE`, `UNKNOWN`)
- `confidence_score` (DECIMAL(3, 2), NOT NULL)
- `created_at` (TIMESTAMPTZ, NOT NULL)

### `investigation_reports`
Synthesized final reports generated for an investigation.
- `id` (UUID, Primary Key)
- `investigation_id` (UUID, Foreign Key -> `investigations.id`, ON DELETE CASCADE)
- `executive_summary` (TEXT, NOT NULL)
- `threat_level` (VARCHAR(50), NOT NULL: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `verified_facts` (JSONB, NOT NULL)
- `ai_inferences` (JSONB, NOT NULL)
- `unknown_gaps` (JSONB, NOT NULL)
- `recommendations` (JSONB, NOT NULL)
- `created_at` (TIMESTAMPTZ, NOT NULL)

### `agent_executions`
Audit log of all AI agent invocations.
- `id` (UUID, Primary Key)
- `investigation_id` (UUID, Foreign Key -> `investigations.id`, ON DELETE CASCADE)
- `agent_name` (VARCHAR(100), NOT NULL)
- `prompt_tokens` (INT)
- `completion_tokens` (INT)
- `latency_ms` (BIGINT)
- `status` (VARCHAR(50), NOT NULL: `SUCCESS`, `FAILURE`, `REJECTED_BY_SECURITY`)
- `created_at` (TIMESTAMPTZ, NOT NULL)

---

## 3. Important Indexes
- `idx_investigations_user_id`: Optimize fetching investigations by user.
- `idx_evidence_investigation_id`: Optimize fetching all evidence for a case.
- `idx_evidence_sha256`: Hash lookup for deduplication and VirusTotal matching.
- `idx_threat_indicators_value`: Exact lookup on indicator values.
- `idx_evidence_relationships_inv`: Fast retrieval of all edges for graph rendering.

---

## 4. Lifecycles

### Evidence Lifecycle
`UPLOADED` -> `PARSED` -> `ANALYZING` -> `ANALYZED` (or `FAILED`)

### Investigation Lifecycle
`CREATED` -> `IN_PROGRESS` -> `ANALYZING` -> `COMPLETED` -> `ARCHIVED`

