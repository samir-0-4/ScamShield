# PhantomTrace — System Architecture

## High-Level Overview

PhantomTrace is an investigative platform engineered to process, parse, correlate, and visualize multi-modal digital scam evidence. The system is split into two primary applications sharing an asynchronous event and REST communication model:

1. **Frontend Client**: Modern single-page application built on React, Vite, Tailwind CSS, and React Flow, delivering responsive investigation dashboards and interactive node-link evidence graphs.
2. **Backend Server**: Enterprise Spring Boot application running on Java 21, handling evidence ingestion, transactional persistence in PostgreSQL, file storage abstraction, external threat intelligence lookups (VirusTotal, URLhaus), and AI agent orchestration via Groq.

```
                      +---------------------------------------+
                      |            User Browser               |
                      |  React 18 + Vite + React Flow Graph   |
                      +---------------------------------------+
                                          |
                                    REST API (JSON)
                                    JWT Authorization
                                          v
                      +---------------------------------------+
                      |         Spring Boot Backend           |
                      |        (Java 21 / Spring MVC)         |
                      +---------------------------------------+
                           |              |              |
                           v              v              v
                  +------------+   +-------------+   +---------------+
                  | PostgreSQL |   | File Storage|   |  Threat Intel |
                  |  Database  |   | (Local / S3)|   | (VT, URLhaus) |
                  +------------+   +-------------+   +---------------+
                                          |
                                          v
                               +---------------------+
                               | AI Orchestrator     |
                               | (Groq / LLaMA 3.3)  |
                               +---------------------+
```

## Architectural Tenets

### 1. Epistemic Integrity
AI inferences are fundamentally non-deterministic hypotheses. PhantomTrace explicitly categorizes all indicators, links, and summaries into:
- **`VERIFIED`**: Cryptographic evidence, authoritative DNS/WHOIS records, or deterministic malware signatures.
- **`AI_INFERENCE`**: Pattern matching, sentiment analysis, or semantic correlations derived from Groq models.
- **`UNKNOWN`**: Gaps where information cannot be confirmed or ruled out.

### 2. Modularity & Loose Coupling
- External intelligence providers (VirusTotal, URLhaus) implement clean Java interfaces so mocks or alternative providers can be substituted without touching core business logic.
- AI models are accessed via an OpenAI-compatible interface adapter targeted at Groq, allowing seamless migration to local or enterprise LLM providers.
- File storage is behind an abstract `FileStorageService` interface.

### 3. Scalable Monorepo Layout
Both the frontend and backend live in a unified repository with shared documentation, infrastructure specifications, and Kiro orchestration configurations.

