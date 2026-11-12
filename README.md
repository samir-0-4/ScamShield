# PhantomTrace / ScamShield

> **AI-Assisted Scam & Digital-Threat Forensic Investigation Platform**  
> Connect suspicious messages, phishing URLs, fraudulent emails, documents, and screenshot evidence into an explainable relationship graph backed by live threat intelligence and specialized AI agents.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6.4-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Architecture & Workflow](#architecture--workflow)
- [Project Verification Status](#project-verification-status)
- [Core Features](#core-features)
  - [1. Multi-Evidence Ingestion & Workspace](#1-multi-evidence-ingestion--workspace)
  - [2. Deterministic Indicator Extraction](#2-deterministic-indicator-extraction)
  - [3. Live Threat Intelligence Integration](#3-live-threat-intelligence-integration)
  - [4. Multi-Agent AI Forensic Reasoning (Groq)](#4-multi-agent-ai-forensic-reasoning-groq)
  - [5. Interactive Evidence Relationship Graph](#5-interactive-evidence-relationship-graph)
  - [6. Explainable Risk Assessment Engine](#6-explainable-risk-assessment-engine)
  - [7. Forensic Reporting & Print-to-PDF](#7-forensic-reporting--print-to-pdf)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Setup](#environment-setup)
  - [Starting the Services](#starting-the-services)
- [Demo Scenario Execution](#demo-scenario-execution)
- [REST API Reference](#rest-api-reference)
- [Testing & Quality Assurance](#testing--quality-assurance)
- [Security Architecture](#security-architecture)
- [Production Roadmap](#production-roadmap)
- [License](#license)

---

## Overview

Investigating modern scams and social engineering campaigns requires connecting fragmented clues: a WhatsApp urgency message, a short redirect URL, an impersonated executive email, and a payment QR code. Traditional tools analyze these in isolation.

**PhantomTrace** connects all evidence into a single case dossier. It extracts network and linguistic indicators deterministically, queries live threat feeds (VirusTotal and URLhaus), runs multi-agent AI forensic assessments through an OpenAI-compatible Groq backend, maps relationship graphs via React Flow, calculates a transparent 0–100 risk score, and generates court/audit-ready investigation reports.

---

## Architecture & Workflow

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                        React 18 / Tailwind Frontend                        │
│   (Dashboard, Evidence Workspace, React Flow Graph, Live Threat Feeds)     │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ REST / JSON (JWT Auth)
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                      Spring Boot 3.4.2 Core Engine                         │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐  │
│  │ Extraction Service    │ │ Correlation Engine    │ │ Risk Engine      │  │
│  │ (Regex, PDF, ZXing)   │ │ (Graph Node/Edge Gen) │ │ (Heuristic Math) │  │
│  └───────────┬───────────┘ └───────────┬───────────┘ └────────┬─────────┘  │
│              │                         │                      │            │
│  ┌───────────▼─────────────────────────▼──────────────────────▼──────────┐  │
│  │ Threat Intelligence Adapters       Multi-Agent AI Orchestrator        │  │
│  │ (VirusTotal v3 & URLhaus)          (Groq LLaMA 3.3 70B System)        │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ JDBC / Flyway
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                      PostgreSQL 16 Database Storage                         │
│   (Users, Investigations, Evidence, Extracted Indicators, Threat Lookups,  │
│    Relationships, Risk Assessments, Audit Reports)                         │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## Project Verification Status

**Status: Platform Fully Verified & Operational**

| Milestone / Component | Coverage | Status |
|---|:---:|:---|
| Backend Test Suite (`mvn test`) | **75 / 75 passed** | 100% Pass Rate |
| Frontend Test Suite (`vitest run`) | **25 / 25 passed** | 100% Pass Rate |
| Frontend Production Build (`vite build`) | **0 errors** | Passed |
| PostgreSQL 16 & Flyway Migrations | **V1 → V9** | Applied & Healthy |
| Live External Threat Feeds (VirusTotal & URLhaus) | **Verified** | Operational |
| Groq AI Multi-Agent Forensic Pipeline | **Verified** | Operational |
| End-to-End Demo Script (`demo-scenario.sh`) | **Verified** | Passed |
| Zero Secrets Tracked in Git | **Audited** | Clean |

---

## Core Features

### 1. Multi-Evidence Ingestion & Workspace
- **Multi-Format Support**: Ingest Text Messages, URLs, Emails, Screenshots (PNG, JPG, WEBP), and PDF documents in one investigation.
- **Safe Parsing (Non-Executing)**:
  - **PDF Documents**: Parsed using Apache PDFBox for selectable text extraction.
  - **Images & QR Codes**: Decoded using ZXing for embedded URL/text payloads without opening or visiting targets.
  - **OCR Capabilities**: Supported via Tess4J for image-based text analysis.
- **Integrity Validation**: Enforces MIME validation, file size limits (25MB default), duplicate detection, and private storage isolation.

### 2. Deterministic Indicator Extraction
Extracts forensic artifacts without reliance on third-party APIs:
- **Network Indicators**: RFC-compliant URLs, domains, IPv4 addresses, and email addresses.
- **Telecom & Identification**: Phone numbers and user account identifiers.
- **Fraud Markers**: Payment cues (UPI, wire, gift cards), credential harvesting phrases, OTP requests, and urgency language.
- **Indicator Normalization**: Preserves original values while calculating canonical forms for deterministic deduplication.

### 3. Live Threat Intelligence Integration
Decoupled clients query external reputation feeds with zero-trust fallbacks:
- **VirusTotal API v3**:
  - Uses URL-safe unpadded Base64 identifier encoding.
  - Retrieves live engine detection stats (`malicious`, `suspicious`, `undetected`, `harmless`) and reference hashes.
  - Normalizes verdicts to `MALICIOUS`, `SUSPICIOUS`, or `NO_MALICIOUS_RESULT`.
- **URLhaus (Abuse.ch) API**:
  - Cross-references URLs against active malware and command-and-control distribution feeds.
  - Correctly categorizes clean (`NOT_FOUND` / `no_results`) vs. weaponized indicators.
- **Reliability & Cache Architecture**:
  - **Zero-Lock Caching**: 1-hour database cache prevents redundant API calls for completed verdicts.
  - **Automatic Cache Bypass**: If lookups previously failed due to missing credentials, updating keys and re-verifying immediately queries the external API.
  - **Deduplication**: Surfaces the latest provider verdict per indicator while retaining historical audit logs.

### 4. Multi-Agent AI Forensic Reasoning (Groq)
Powered by Groq's high-throughput LLaMA 3.3 70B engine using specialized, isolated system prompts:
- **Text Detective Agent**: Analyzes psychological pressure, scam taxonomies, and manipulative intent.
- **URL Detective Agent**: Unmasks evasive redirects, typosquatting, brand impersonation, and homoglyphs.
- **Email Detective Agent**: Scans headers, deceptive display names, and domain spoofing patterns.
- **File Detective Agent**: Analyzes extracted document text and QR payloads.
- **Correlation Agent**: Discovers hidden links between disparate evidence items.
- **Report Agent**: Synthesizes structured findings, key takeaways, and remediation steps.
- **Zero-Trust Epistemics**: Structured JSON outputs with confidence scoring. If Groq is unavailable, the system displays explicit fallback notices.

### 5. Interactive Evidence Relationship Graph
An interactive forensic relationship graph powered by React Flow (`@xyflow/react`):
- **Graph Nodes**: Investigations, Evidence, URLs, Domains, Emails, IPs, Files, and Threat Feeds.
- **Semantic Edges**: `CONTAINS_URL`, `REFERENCES_DOMAIN`, `MENTIONS_EMAIL`, `SHARES_INDICATOR`, `MATCHES_THREAT_INDICATOR`, and `ASSOCIATED_WITH`.
- **Investigator Controls**: Interactive zoom, pan, minimap, fit-to-view, node filtering, and evidence provenance sidebar.

### 6. Explainable Risk Assessment Engine
Calculates a transparent heuristic risk score from 0 to 100:
- **Risk Tiers**: `LOW` (0–29), `MODERATE` (30–59), `HIGH` (60–84), `CRITICAL` (85–100), and `INCONCLUSIVE`.
- **Configurable Signal Weights**:
  - Verified Malicious Threat Match: **+40**
  - Credential / Password Request: **+25**
  - Upfront Payment / Financial Demand: **+20**
  - Identity Impersonation: **+15**
  - Urgency / Time-Pressure Trigger: **+10**
  - Unusual / Deceptive URL Structure: **+10**
- **Deduplicated Scoring**: Duplicate signals from the same indicator are bounded to avoid score inflation.
- **Separate Confidence Metric**: Confidence is tracked independently from the risk score to reflect evidence completeness.

### 7. Forensic Reporting & Print-to-PDF
- **Comprehensive Case Snapshots**: Generates immutable reports containing executive summaries, forensic timelines, extracted artifacts, external threat proofs, and actionable recommendations.
- **Print & PDF Optimization**: Clean print layouts with print-specific page breaks, high-contrast typography, and hidden navigation bars for easy **Print $\rightarrow$ Save as PDF** export.

---

## Technology Stack

### Frontend
- **Framework**: React 18 with Vite
- **Styling**: Tailwind CSS with custom cyber-threat dark design system
- **State & Routing**: React Router v6, custom context providers
- **Visualization**: React Flow (`@xyflow/react`), Recharts, Lucide React
- **HTTP Client**: Axios with JWT authentication interceptors
- **Testing**: Vitest, React Testing Library, jsdom

### Backend
- **Framework**: Spring Boot 3.4.2 (Java 21)
- **Security**: Spring Security 6 with HS256 HMAC JWT authentication
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL 16
- **Database Migrations**: Flyway (V1 through V9)
- **Document & Media Analysis**: Apache PDFBox 3.0.4, ZXing 3.5.3, Tess4J 5.13.0
- **HTTP & Resilience**: Spring RestClient with custom timeout and error decoders
- **Testing**: JUnit 5, Mockito, AssertJ, H2 (for unit/mock testing)

---

## Getting Started

### Prerequisites
- **Node.js**: v20 or newer
- **JDK**: Java 21 or newer
- **Maven**: 3.9 or newer
- **Docker & Docker Compose**: Installed and running

---

### Environment Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-org/phantomtrace.git
   cd phantomtrace
   ```

2. **Configure Environment Variables**:
   ```bash
   cp .env.example .env
   cp backend/.env.example backend/.env
   cp frontend/.env.example frontend/.env
   ```

3. **Backend Environment Variables (`backend/.env`)**:
   ```env
   # Server & Profiles
   SERVER_PORT=8080
   SPRING_PROFILES_ACTIVE=dev

   # PostgreSQL Database
   POSTGRES_DB=phantomtrace
   POSTGRES_USER=phantomtrace_user
   POSTGRES_PASSWORD=change_me
   DATABASE_URL=jdbc:postgresql://localhost:5434/phantomtrace

   # Security (Generate with: openssl rand -base64 32)
   JWT_SECRET=your_32_character_minimum_base64_secret_here
   JWT_EXPIRATION_MS=86400000

   # AI Provider (Groq API)
   GROQ_API_KEY=your_groq_api_key_here
   GROQ_BASE_URL=https://api.groq.com/openai/v1
   GROQ_DEFAULT_MODEL=llama-3.3-70b-versatile

   # External Threat Intelligence
   VIRUSTOTAL_API_KEY=your_virustotal_api_key_here
   URLHAUS_API_KEY=your_urlhaus_api_key_here

   # Storage
   FILE_STORAGE_TYPE=local
   FILE_STORAGE_UPLOAD_DIR=./uploads
   ```

   > **Zero-Config `.env` Auto-Loading**: `PhantomTraceApplication` automatically parses `.env` and `backend/.env` on startup. You do **not** need to manually export variables in your shell before starting the backend.

4. **Frontend Environment Variables (`frontend/.env`)**:
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   VITE_APP_NAME=PhantomTrace
   ```

---

### Starting the Services

#### Step 1: Start PostgreSQL via Docker Compose
From the project root:
```bash
docker compose up -d postgres
```
Verify the container is healthy:
```bash
docker compose ps
```

#### Step 2: Start the Backend (Spring Boot)
In a new terminal:
```bash
cd backend
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`. Flyway automatically verifies and applies migrations V1–V9.  
Verify backend health:
```bash
curl http://localhost:8080/api/v1/health
# {"status":"UP","application":"PhantomTrace"}
```

#### Step 3: Start the Frontend (React / Vite)
In a new terminal:
```bash
cd frontend
npm install
npm run dev
```
Open **`http://localhost:5173`** in your browser.

---

## Demo Scenario Execution

To run an automated forensic case demonstration:

```bash
./scripts/demo-scenario.sh
```

This script automatically:
1. Registers a temporary analyst account and logs in via JWT.
2. Creates an investigation: `Remote Job Offer Investigation`.
3. Ingests synthetic scam messages and payment URL indicators.
4. Triggers deterministic indicator extraction and live threat feed lookups.
5. Computes heuristic risk assessment and builds correlation graph relationships.
6. Generates a printable forensic report snapshot.

---

## REST API Reference

All endpoints return standard envelopes:
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation description",
  "timestamp": "2026-11-12T12:00:00Z"
}
```

### Authentication
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/auth/register` | Register a new analyst account |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT bearer token |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile |

### Investigations
| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/v1/investigations` | List user's investigations with status/category filters |
| `POST` | `/api/v1/investigations` | Create a new investigation case |
| `GET` | `/api/v1/investigations/{id}` | Retrieve investigation metadata |
| `PATCH` | `/api/v1/investigations/{id}` | Update title, description, or status |
| `DELETE` | `/api/v1/investigations/{id}` | Delete investigation and associated evidence |

### Evidence Management
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/investigations/{id}/evidence` | Submit URL, Text, Email, or File evidence |
| `GET` | `/api/v1/investigations/{id}/evidence` | List all evidence items for a case |
| `GET` | `/api/v1/evidence/{evidenceId}` | Get single evidence item details |
| `DELETE` | `/api/v1/evidence/{evidenceId}` | Delete evidence item |
| `GET` | `/api/v1/evidence/{evidenceId}/indicators` | List extracted deterministic indicators |
| `POST` | `/api/v1/evidence/{evidenceId}/analysis` | Trigger AI agent analysis |
| `POST` | `/api/v1/evidence/{evidenceId}/threat-lookups`| Trigger live VirusTotal & URLhaus verification |
| `GET` | `/api/v1/evidence/{evidenceId}/threat-lookups` | List threat intelligence results |

### Graph & Reports
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/investigations/{id}/graph/generate` | Generate relationship graph nodes and edges |
| `GET` | `/api/v1/investigations/{id}/graph` | Retrieve graph visualization payload |
| `POST` | `/api/v1/investigations/{id}/risk-assessment` | Run deterministic heuristic risk engine |
| `GET` | `/api/v1/investigations/{id}/risk-assessment` | Retrieve latest risk score and signals |
| `POST` | `/api/v1/investigations/{id}/reports` | Compile and persist final investigation report |
| `GET` | `/api/v1/investigations/{id}/reports` | List historical case reports |

---

## Testing & Quality Assurance

Run the unified test runner:
```bash
./scripts/verify-all.sh
```

Or run test suites independently:

```bash
# Backend unit & integration tests (75 tests)
cd backend
mvn clean test

# Frontend component & route tests (25 tests)
cd frontend
npm test -- --run

# Frontend linting
npm run lint

# Production build validation
npm run build
```

---

## Security Architecture

- **Zero-Trust AI Design**: AI outputs are strictly treated as advisory hypotheses and require deterministic corroboration (hashes, URL lookups, regex indicators).
- **Strict Tenant & Ownership Isolation**: Every API endpoint validates ownership to prevent IDOR (Insecure Direct Object Reference) vulnerabilities.
- **Client Credential Isolation**: Backend API keys (`GROQ_API_KEY`, `VIRUSTOTAL_API_KEY`, `URLHAUS_API_KEY`, database credentials, and JWT signing keys) are never exposed to client-side scripts.
- **Safe Execution Boundary**: Uploaded files and submitted URLs are analyzed statically; arbitrary user links are never automatically executed, downloaded, or visited by the backend server.
- **Cryptographic JWT Validation**: Validates minimum 256-bit signing keys and automatically rejects known insecure/placeholder secrets on startup.

---

## Production Roadmap

- [ ] **Object Storage Integration**: Transition file uploads from local volume storage to AWS S3 / Google Cloud Storage.
- [ ] **Asynchronous Task Queuing**: Implement Spring Batch / Celery / Redis Streams for high-volume batch evidence analysis.
- [ ] **Advanced OCR**: Expanded Tesseract multi-language character recognition models for low-contrast document scans.
- [ ] **STIX / TAXII Export**: Export verified threat indicators in standardized STIX 2.1 formats for enterprise SIEM ingestion.
- [ ] **Role-Based Collaboration**: Multi-analyst case sharing and immutable audit trail logging.

---

## License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
