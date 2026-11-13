# PhantomTrace — Technology Stack

## Frontend

| Technology | Version | Purpose |
|---|---|---|
| React | 18.x | UI component framework |
| Vite | 6.x | Build tool and dev server |
| Tailwind CSS | 3.x | Utility-first styling |
| @xyflow/react | latest | Evidence correlation graph canvas |
| lucide-react | latest | Icon set (sole icon library) |
| recharts | latest | Risk score gauges and charts |
| axios | latest | HTTP client (wrapped in apiClient.js) |
| React Router | 6.x | Client-side routing |
| Vitest | latest | Unit test runner |
| @testing-library/react | latest | Component testing utilities |

### Frontend Conventions
- Dev server: `npm run dev` (Vite, port 5173)
- Production build: `npm run build` (output to `dist/`)
- Lint: `npm run lint` (ESLint)
- Tests: `npm test` (Vitest)
- All environment variables prefixed `VITE_` and documented in `frontend/.env.example`

---

## Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 (LTS) | Runtime |
| Spring Boot | 3.4.2 | Application framework |
| Spring Security | (bundled) | JWT filter, CORS, method security |
| Spring Data JPA | (bundled) | Repository abstraction |
| Hibernate | (bundled) | ORM |
| JJWT | 0.12.6 | JWT creation and validation (HS256) |
| Apache PDFBox | 3.0.4 | PDF text extraction |
| ZXing | 3.5.3 | QR code decoding from images |
| Tess4j | 5.13.0 | OCR — text extraction from images |
| Apache Commons | (various) | Utility functions |
| Flyway | (bundled via Spring) | Database migrations |
| Maven | 3.9.x | Build and dependency management |
| JUnit 5 | (bundled) | Unit and integration testing |
| Mockito | (bundled) | Test mocking |

### Backend Conventions
- Build: `mvn package -DskipTests`
- Test: `mvn test`
- Run: `mvn spring-boot:run` or via Docker
- All secrets injected via environment variables; no hardcoded credentials

---

## AI Layer

| Technology | Details |
|---|---|
| Provider | Groq Cloud |
| Model | `llama-3.3-70b-versatile` |
| Protocol | HTTPS REST (Groq Chat Completions API) |
| Auth | `GROQ_API_KEY` environment variable (backend-only) |
| Output contract | All LLM responses labelled `AI_INFERENCE`; parsed by `StructuredAiResponseParser` |

Groq was chosen for its high-throughput, low-latency inference suitable for real-time investigation workflows.

---

## Database

| Technology | Version | Purpose |
|---|---|---|
| PostgreSQL | 16 | Primary data store |
| Flyway | (via Spring Boot) | Schema version control |

- Migrations: `src/main/resources/db/migration/V1__` through `V9__`
- No Hibernate DDL auto (`spring.jpa.hibernate.ddl-auto=validate`)
- Connection pool: HikariCP (Spring Boot default)

---

## Threat Intelligence

| Feed | API Version | Data |
|---|---|---|
| VirusTotal | v3 | URL, domain, IP, file hash reputation |
| URLhaus (abuse.ch) | v1 | URL and domain blocklist |

Both adapters isolated in `backend/src/main/java/.../integration/` and all outbound calls guarded by `SecurityManager.java` (SSRF protection).

---

## Infrastructure & Deployment

| Technology | Purpose |
|---|---|
| Docker | Container images for backend and frontend |
| docker-compose | Multi-service local orchestration |
| nginx | Frontend static file server in production container |
| `.env` files | Runtime configuration injection |

---

## Development Environment Requirements

- Node.js ≥ 20
- Java 21 JDK
- Maven 3.9+
- PostgreSQL 16 (or Docker)
- Docker Desktop (optional, for full-stack compose)
