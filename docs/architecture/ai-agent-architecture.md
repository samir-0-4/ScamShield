# PhantomTrace — AI Agent Architecture

## Architectural Philosophy

PhantomTrace employs a **Federated Multi-Agent Architecture** powered by a unified inference engine (Groq LLaMA 3.3). Instead of provisioning fragmented API keys or uncoordinated AI services, all agents leverage a centralized `AiProvider` client abstraction configured via Spring Boot.

```
                  +-----------------------------------+
                  |      AiAgentOrchestrator          |
                  +-----------------------------------+
                                    │
                                    ▼
                  +-----------------------------------+
                  |          SecurityManager          |
                  | (Input Sanitization & Guardrails) |
                  +-----------------------------------+
                                    │
    ┌────────────────┬──────────────┼──────────────┬────────────────┐
    ▼                ▼              ▼              ▼                ▼
+───────────+ +───────────+ +───────────+ +────────────────+ +─────────────+
|   Email   | |    URL    | |   File    | |  Intelligence  | |Investigation|
| Detective | | Detective | |  Analyst  | |  Correlation   | |   Report    |
|   Agent   | |   Agent   | |   Agent   | |     Agent      | |    Agent    |
+───────────+ +───────────+ +───────────+ +────────────────+ +─────────────+
    │                │              │              │                │
    └────────────────┴──────────────┼──────────────┴────────────────┘
                                    ▼
                  +-----------------------------------+
                  |          SecurityManager          |
                  | (Output Validation & Redaction)   |
                  +-----------------------------------+
                                    │
                                    ▼
                  +-----------------------------------+
                  |    AiProvider (Groq Client)       |
                  |   (OpenAI-Compatible Adapter)     |
                  +-----------------------------------+
```

## Agent Roles & Responsibilities

### 1. Email Detective Agent
- **Purpose**: Deconstructs email content, headers, MIME boundaries, and claimed sender credentials.
- **Outputs**: SPF/DKIM validation status, reply-to anomalies, embedded phishing links, urgent social engineering language cues.

### 2. URL Detective Agent
- **Purpose**: Inspects suspect URLs, hostnames, redirection chains, and typosquatting vectors.
- **Outputs**: Domain age flags, homoglyph detection, URLhaus malicious report status, redirect anomalies.

### 3. File Analyst Agent
- **Purpose**: Coordinates safe inspection of PDF documents, receipts, and invoices.
- **Outputs**: Embedded Javascript/macro alerts, hidden text layers, suspicious metadata, QR payload decoding.

### 4. Intelligence Correlation Agent
- **Purpose**: Consolidates disparate indicators across all evidence items in an investigation.
- **Outputs**: Node-link relationship graph propositions, differentiating `VERIFIED` connections (e.g. identical domain) from `AI_INFERENCE` links (e.g. similar linguistic patterns).

### 5. Investigation Report Agent
- **Purpose**: Synthesizes the full case dossier into a structured, executive-ready investigation report.
- **Outputs**: Threat executive summary, timeline of events, confidence assessments, verified facts, explicit unknown gaps, and defensive recommendations.

### 6. Security Manager
- **Purpose**: Input/output boundary guardrail.
- **Safeguards**:
  - Sanitizes user input against prompt injection attempts.
  - Validates JSON output schemas from LLMs before passing data to downstream services.
  - Prevents hallucinated commands or unauthorized tool invocations.

## Provider Abstraction (`AiProvider`)

All agents implement the common `AiAgent` interface and inject an `AiProvider` interface:

```java
public interface AiProvider {
    AiCompletionResponse generate(AiCompletionRequest request);
}
```

This guarantees that replacing Groq with local Ollama, Azure OpenAI, or another provider requires zero changes to the business logic of individual agents.

