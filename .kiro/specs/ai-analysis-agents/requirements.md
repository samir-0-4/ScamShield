# Phase 7 Spec — AI Analysis Agents

> **Spec ID:** `ai-analysis-agents`
> **Phase:** 7
> **Status:** Approved — implementation complete

## Goal

Analyze submitted structured evidence using specialized Java agents backed by one
replaceable Groq provider. Deterministic extraction and threat intelligence remain
authoritative for observables; AI adds bounded, explainable findings only.

## Requirements

- One shared provider configuration and API key is used by all agents.
- Groq uses its OpenAI-compatible chat-completions API and JSON response mode.
- Model output is parsed and validated before it is returned or persisted.
- Evidence is untrusted input, bounded by size, clearly delimited, and cannot invoke
  tools, SQL, shell commands, or network actions.
- Text, URL, email, correlation, and report agents have separate responsibilities.
- SPF/DKIM/DMARC claims require supplied verified authentication headers.
- URL findings cannot claim maliciousness without supporting deterministic or provider
  evidence.
- Agent failures are recorded and returned as safe unavailable/error states.
- Execution records include agent, model, status, duration, and safe error text.

## Acceptance

A submitted message can be analyzed by the Text Detective Agent and receive strict
structured findings through an authenticated backend endpoint. All logical agents
share the same provider bean and configuration. Missing credentials, malformed JSON,
timeouts, rate limits, and invalid outputs do not crash the application or fabricate
findings.
