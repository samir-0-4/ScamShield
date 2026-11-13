# Phase 7 Tasks — AI Analysis Agents

> Mark `[x]` only after verification.

## A. Specification
- [x] A1. Create and review requirements and design for `ai-analysis-agents`.

## B. Provider and schema
- [x] B1. Implement shared provider configuration and Groq client.
- [x] B2. Add strict finding/indicator DTOs and output validation.
- [x] B3. Add security limits and prompt-injection safeguards.

## C. Agents and orchestration
- [x] C1. Implement text, URL, email, correlation, and report agents.
- [x] C2. Add sequential orchestration and execution persistence.
- [x] C3. Add authenticated analysis endpoint.

## D. Verification
- [x] D1. Add mocked provider and schema tests for valid, malformed, empty, rate-limit,
  credential, injection, and validation cases; agent failures are persisted by the
  orchestrator's safe execution path.
- [x] D2. Run backend regression verification.
