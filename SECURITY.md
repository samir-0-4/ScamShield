# Security Policy

PhantomTrace processes sensitive digital threat indicators, suspicious emails, uploaded files, and potentially malicious URLs. Protecting the integrity of user data and execution environments is paramount.

---

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

---

## Reporting a Vulnerability

If you discover a potential security vulnerability within PhantomTrace, please do **not** open a public issue.

Instead, report the vulnerability responsibly by following these steps:

1. Draft a summary of the issue, including reproduction steps, affected component, and potential impact.
2. Send your report directly to the project security maintainers at `security@phantomtrace.local` (or through private advisory on GitHub once repository is created).
3. Allow the team reasonable time to evaluate, remediate, and publish a patch before disclosing publicly.

---

## Security Architecture Principles

- **Safe Handling of Malicious Artefacts**: All uploaded evidence files (PDFs, suspicious attachments) must be quarantined and never executed directly on the host machine.
- **Defensive Prompt Engineering**: AI prompts are structured with boundary delimiters and anti-jailbreak sanitizers to prevent indirect prompt injection from parsed phishing emails or suspicious URLs.
- **Zero-Trust AI Epistemics**: AI agent outputs are treated as untrusted inferences until corroborated by deterministic indicators (such as hash matching on VirusTotal or URL verification on URLhaus).
- **Environment Isolation**: Production deployments must run backend analysis routines in sandboxed worker containers with restricted network egress.

