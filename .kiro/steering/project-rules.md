# Kiro Steering Rules — PhantomTrace

## Core Directives
1. **Epistemic Truth Principle**: Every piece of data surfaced to the user must be classified as one of:
   - `VERIFIED`: Proven by deterministic external source or cryptographic hash.
   - `AI_INFERENCE`: Generated hypothesis from LLM or heuristic analysis, requiring verification.
   - `UNKNOWN`: Missing or inconclusive information.
2. **Architecture Boundaries**:
   - Backend controllers never contain business logic.
   - Frontend components never make direct unabstracted HTTP calls.
   - External intelligence APIs (VirusTotal, URLhaus) are isolated in `integration/` modules.
   - Groq AI agents share unified provider configuration and prompt schemas.
3. **Dual-Tooling Compatibility**:
   - Respect Antigravity rules and Kiro specifications equally.
   - Do not delete or overwrite files in `.kiro/`.

