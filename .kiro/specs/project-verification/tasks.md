# Tasks: Phase 1 — Project Verification

> **Spec ID:** `project-verification`  
> **Status:** Awaiting User Approval  

## Validation & Stabilization Tasks

- [ ] **T1. Surefire Plugin Configuration**: Add `maven-surefire-plugin` with `-XX:+EnableDynamicAgentLoading` to `backend/pom.xml`.
- [ ] **T2. Port Alignment in Config Fallbacks**: Update `application-dev.yml` and `docker-compose.yml` defaults to port `5434` to prevent native PostgreSQL host collisions.
- [ ] **T3. Verify Monorepo Script**: Update `scripts/verify-all.sh` to ensure both frontend and backend tests run end-to-end smoothly.
- [ ] **T4. Execute Test Suites**:
  - Run `cd frontend && npm test` and `npm run build`.
  - Run `cd backend && mvn clean test`.
- [ ] **T5. End-to-End Startup & Health Verification**:
  - Start PostgreSQL container via `docker compose up -d postgres`.
  - Start backend via `mvn spring-boot:run`.
  - Query `GET http://localhost:8080/api/v1/health` and verify HTTP 200 response.
  - Start frontend via `npm run dev` and verify HTTP 200 response on `http://localhost:5173`.
- [ ] **T6. Status & Spec Completion Report**: Present the final validation report per Master Instruction §8.

