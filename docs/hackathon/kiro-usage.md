# PhantomTrace — Kiro Usage & Integration

## Overview

PhantomTrace is architected for dual-environment workflows, operating seamlessly inside **Google Antigravity** and **Kiro**. The `.kiro/` directory provides project-level configuration, specifications, hooks, and custom agent definitions to ensure consistency across the development lifecycle.

---

## 1. Kiro Steering Documents (`.kiro/steering/`)

- **Location**: `.kiro/steering/project-rules.md`
- **Purpose**: Defines core system invariants that Kiro must enforce during code generation and reasoning passes:
  - Epistemic classification rules (`VERIFIED`, `AI_INFERENCE`, `UNKNOWN`).
  - Architectural constraints (thin controllers, DTO layers, centralized AI providers).
  - Safety constraints against accidental commit of secret credentials.

---

## 2. Kiro Specifications (`.kiro/specs/`)

- **Location**: `.kiro/specs/phantomtrace-spec.md`
- **Purpose**: High-level system requirements detailing:
  - Supported evidence formats (emails, WhatsApp transcripts, payment receipts, PDFs, QR codes).
  - Target tech stack (Spring Boot 3, React 18, PostgreSQL, Groq).
  - Graph correlation objectives.

---

## 3. Implemented Kiro Specifications

Feature work was organized as reviewed Kiro specs under `.kiro/specs/`, including:
`frontend-foundation`, `evidence-submission`, `deterministic-evidence-extraction`,
`threat-intelligence-integration`, `ai-analysis-agents`, `evidence-correlation`,
`risk-assessment`, `investigation-results`, `evidence-relationship-graph`,
`investigation-reports`, and `hackathon-final-delivery`.

Each feature spec contains requirements, design, and task breakdown documents.

---

## 4. Kiro Agent Hooks (`.kiro/hooks/`)

- **Location**: `.kiro/hooks/pre-commit-check.sh`
- **Purpose**: Automated script hook that verifies:
  - No active `.env` files are accidentally staged in Git.
  - Basic file formatting and directory integrity checks.

---

## 5. Kiro Custom Agents (`.kiro/agents/`)

- **Location**: `.kiro/agents/threat-analyst.md`
- **Purpose**: Specialized system persona for:
  - Validating threat indicator extraction regex patterns and schemas.
  - Ensuring AI agent prompts strictly preserve epistemic integrity.
  - Reviewing VirusTotal and URLhaus API integration schemas.

The custom agent and hook are repository artifacts. This project does not claim
that a hook or custom agent was invoked for every change; the durable evidence of
Kiro usage is the checked-in steering file, specs, task breakdowns, and hook/agent
definitions above.
