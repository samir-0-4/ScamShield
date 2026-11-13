# PhantomTrace — System Architecture

## Overview

PhantomTrace is a web-based cyber-threat investigation platform. Investigators upload digital evidence (text messages, emails, screenshots, documents), and the system extracts threat indicators, enriches them against external intelligence feeds, correlates them across submissions, and produces an auditable risk assessment.

---

## High-Level Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        BROWSER (React 18 SPA)                          │
│                                                                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
│  │  Auth Pages  │  │ Investigation│  │  Graph View  │  │  Report  │  │
│  │  Login/Reg   │  │  Dashboard   │  │ (React Flow) │  │  Export  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └────┬─────┘  │
│         │                 │                 │               │        │
│         └─────────────────┴─────────────────┴───────────────┘        │
│                                   │                                   │
│                           apiClient.js (axios)                        │
└───────────────────────────────────┼───────────────────────────────────┘
                                    │ HTTPS / REST + JWT
┌───────────────────────────────────▼───────────────────────────────────┐
│                     SPRING BOOT 3.4.2 (Java 21)                       │
│                                                                        │
│  ┌─────────────────┐    ┌────────────────┐    ┌────────────────────┐  │
│  │  REST Controllers│    │  Service Layer │    │  Repository Layer  │  │
│  │  (HTTP only,    │───▶│  (all business │───▶│  (JPA/Hibernate,   │  │
│  │  no logic)      │    │   logic here)  │    │  Spring Data)      │  │
│  └─────────────────┘    └───────┬────────┘    └────────────────────┘  │
│                                 │                        │            │
│           ┌─────────────────────┼──────────────────┐    │            │
│           │                     │                  │    │            │
│  ┌────────▼──────┐  ┌───────────▼──────┐  ┌───────▼────▼──────────┐ │
│  │ AI Agent Layer│  │ Threat Intel      │  │  PostgreSQL 16        │ │
│  │               │  │ Adapters          │  │  (Flyway migrations)  │ │
│  │ AiAgent iface │  │                  │  │                       │ │
│  │ AbstractAiAgent│  │ VirusTotal v3    │  │  Tables:              │ │
│  │               │  │ URLhaus          │  │  users, investigations│ │
│  │  Groq LLaMA   │  │                  │  │  evidence, indicators │ │
│  │  3.3-70B via  │  │  SSRF-protected  │  │  correlations,        │ │
│  │  HTTP API     │  │  (SecurityMgr)   │  │  reports              │ │
│  └───────┬───────┘  └──────────────────┘  └───────────────────────┘ │
│          │                                                            │
└──────────┼────────────────────────────────────────────────────────────┘
           │ HTTPS
┌──────────▼────────────────────────────────────────────────────────────┐
│                    EXTERNAL SERVICES                                   │
│                                                                        │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐  │
│  │  Groq Cloud  │   │  VirusTotal  │   │  URLhaus (abuse.ch)      │  │
│  │  LLaMA-3.3-  │   │  API v3      │   │  Domain/URL blocklist    │  │
│  │  70B-versatile│  │  File/URL/IP │   │  lookup                  │  │
│  └──────────────┘   └──────────────┘   └──────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
```

---

## Component Descriptions

### Frontend (React 18 SPA)

Built with Vite 6 for fast development and production builds. All pages are client-side rendered with React Router. The global API abstraction (`src/services/apiClient.js`) manages auth headers, base URL, and error handling. No component makes direct `fetch` or `axios` calls outside this abstraction.

**Key UI modules:**
- **Dashboard** — Active investigations list, status summaries
- **Evidence Submission** — Multi-type upload (text, file, URL paste)
- **Analysis Results** — Tabbed view: extracted indicators, threat intel hits, AI assessment
- **Graph View** — Interactive `@xyflow/react` canvas showing evidence nodes and indicator edges
- **Risk Report** — Structured summary with risk score gauge (recharts) and PDF export

### Backend (Spring Boot 3.4.2)

Follows strict Controller → Service → Repository layering. No business logic lives in `@RestController` classes.

**Key packages:**
- `controller/` — HTTP surface only; validates input, delegates to services
- `service/` — All orchestration logic, calling agents and repositories
- `integration/` — VirusTotal, URLhaus adapters (isolated, SSRF-protected)
- `ai/` — `AiAgent` interface, `AbstractAiAgent` base class, concrete Groq agents
- `model/` — JPA entities
- `repository/` — Spring Data JPA repositories
- `security/` — JWT filter, `SecurityManager.java` (SSRF guard), CORS config

### AI Agent Orchestration

Each AI analysis task is a concrete class implementing `AiAgent` and extending `AbstractAiAgent`. The base class handles:
- Groq HTTP client configuration
- Prompt template loading
- Response parsing via `StructuredAiResponseParser`
- Epistemic labelling (all LLM output tagged `AI_INFERENCE`)

**Active agents:**
- `PhishingDetectionAgent` — Analyses text for phishing patterns
- `ThreatNarrativeAgent` — Generates a human-readable threat summary
- `CampaignCorrelationAgent` — Identifies shared indicators across investigations
- `RiskScoringAgent` — Weights indicators and produces a 0-100 risk score

### Database (PostgreSQL 16)

Schema managed exclusively via **Flyway** numbered migrations (`V1__` through `V9__`). No schema changes are applied by Hibernate's DDL auto. Migrations run on application startup.

### Threat Intelligence Adapters

Both adapters live in `integration/`:
- **VirusTotal v3** — URL, domain, IP, and file hash reputation lookups
- **URLhaus** — Domain and URL blocklist membership checks

All outbound HTTP calls pass through `SecurityManager.java`, which rejects requests targeting private IP ranges (RFC 1918), localhost, and cloud metadata endpoints (169.254.169.254, etc.) to prevent SSRF.

### Evidence Correlation Graph

The graph data model (stored in the database, rendered with `@xyflow/react`) represents:
- **Nodes**: evidence items, URLs, domains, emails, phone numbers, IPs, file hashes
- **Edges**: `extracted-from`, `shared-indicator`, `correlates-with`

The layout algorithm positions evidence nodes centrally and indicator nodes radially. Clicking any node opens a detail panel.

### Risk Scoring Engine

Implemented in `RiskScoringService`. The score (0–100) is computed from:
1. VirusTotal positives for each extracted indicator (weighted ×3)
2. URLhaus blocklist hits (weighted ×2)
3. AI-inferred confidence score from `RiskScoringAgent` (weighted ×1, labelled `AI_INFERENCE`)

Final score buckets: **LOW** (0-33), **MEDIUM** (34-66), **HIGH** (67-100).

---

## Data Flow — Evidence Submission

```
1. User uploads evidence (text/file/image)
        │
2. Backend validates type, size ≤ 25MB
        │
3. FileParserService extracts raw text
   (PDFBox for PDF, Tess4j OCR for images, plain text for SMS/email)
        │
4. DeterministicExtractorService pulls indicators via regex
   (URLs, IPs, emails, phone numbers, domains, hashes)
        │
5. AI agents called in parallel via Groq API
   → PhishingDetectionAgent
   → ThreatNarrativeAgent
        │
6. Threat intel adapters called for each indicator
   → VirusTotal v3
   → URLhaus
        │
7. All results persisted to PostgreSQL
        │
8. CampaignCorrelationAgent checks for cross-investigation matches
        │
9. RiskScoringService computes final 0-100 score
        │
10. Frontend polls /api/evidence/{id}/status → renders results
```

---

## Deployment

All services are containerised with Docker and orchestrated via `docker-compose`. The compose file starts:
- `db` — PostgreSQL 16
- `backend` — Spring Boot JAR (port 8080)
- `frontend` — Vite production build served by nginx (port 3000)

Environment variables are injected from `.env` files; secrets are never baked into images.
