# PhantomTrace — Security Guidelines

## 1. Threat Ingestion & Evidence Isolation

- **File Quarantining**: Submitted binaries, PDFs, and scripts must never be executed or decompressed directly on the application host.
- **Upload Constraints**: Restrict uploads by strict MIME validation, extension whitelisting, and a maximum size ceiling (default 25MB).
- **Safe Rendering**: All user-submitted textual evidence rendered in the frontend must be properly escaped by React DOM to prevent Cross-Site Scripting (XSS).

---

## 2. AI Security & Prompt Injection Defense

- **Defensive Delimiters**: Evidence content is wrapped inside explicit delimiters (e.g. `<evidence_content>...</evidence_content>`) within system prompts to mitigate prompt injection attacks.
- **Output Schema Verification**: Never pipe raw LLM string output into execution engines or database operations without parsing through strongly typed DTOs and schema validators.
- **No System Credential Leaks**: Prompts must instruct models never to disclose system instructions or internal architecture secrets.

---

## 3. Epistemic Separation

- The user interface must distinctly color-code and label data:
  - **Verified Facts**: Green badge (`VERIFIED`).
  - **AI Hypotheses**: Amber badge (`AI_INFERENCE`).
  - **Unknown / Inconclusive**: Gray badge (`UNKNOWN`).
- Disclaimers must accompany all AI-generated investigation reports.

## 4. JWT Secret Management

- `JWT_SECRET` must be supplied explicitly in every environment.
- Secrets must be at least 32 bytes and must not be placeholders or predictable
  repeated-character values.
- Production startup fails closed when the secret is missing or weak.
- Generate secrets with a cryptographically secure command such as
  `openssl rand -base64 32`.
- Never log, commit, or expose JWT secrets or bearer tokens.
