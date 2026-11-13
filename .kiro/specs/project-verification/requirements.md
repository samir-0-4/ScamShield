# Specification: Phase 1 — Project Verification

> **Spec ID:** `project-verification`  
> **Phase:** 1 (Verification & Baseline)  
> **Status:** Draft — Awaiting User Approval  
> **Target:** Monorepo Stability Baseline  

## 1. Overview & Objective
Verify and stabilize the existing monorepo setup (frontend, backend, database containerization, configuration, and health check) prior to constructing domain-specific application features. Establish reproducible build, test, and startup workflows that strictly adhere to project invariants.

## 2. Assessment of Existing Setup

### 2.1 Monorepo & Directory Structure
- Clean separation between `frontend/` (React 18 + Vite), `backend/` (Spring Boot 3 + Java 21/23), `docs/`, `infrastructure/`, and `.kiro/`.
- Root `.gitignore` and `frontend/.gitignore` cleanly ignore `.env`, `node_modules/`, `target/`, and `uploads/`. No credentials or build artifacts are tracked in Git.

### 2.2 Frontend Verification
- **Framework**: React 18.3.1, Vite 6.1.0, Tailwind CSS 3.4.17.
- **Routing & Networking**: React Router 6.28.2, Axios 1.7.9 with centralized client in `apiClient.js` (Bearer token interceptor and unauthenticated 401 token clearing).
- **Visualization Libraries**: `@xyflow/react` (React Flow) and `recharts` installed and ready for graph and analytics phases.
- **Test Runner**: Vitest 3.0.5 + jsdom. Smoke tests pass cleanly. `npm run build` succeeds without warnings or bundle errors.

### 2.3 Backend Verification
- **Framework**: Spring Boot 3.4.2, Java 21 target running on OpenJDK 23.0.2 via Homebrew.
- **Data & Persistence**: Spring Data JPA, Hibernate ORM 6.6.5, Flyway 10.x, PostgreSQL driver 42.7.5.
- **Security & Auth**: Spring Security 6.4.2 with stateless JWT filter chain (`JwtAuthenticationFilter`), BCrypt password encoder (`BCryptPasswordEncoder`), and centralized `CorsConfig`.
- **Database Migrations**: Baseline Flyway script `V1__init_schema.sql` applies extensions (`uuid-ossp`, `pg_trgm`) and the `users` table.

### 2.4 Infrastructure Verification
- **Docker Compose**: Single-service `postgres` on `postgres:16-alpine` with healthcheck and volume persistence.
- **Port Conflict Identified & Resolved**: The developer host runs native PostgreSQL 17 on port `5432`. Container host port is mapped to `5434:5432` with matching `DATABASE_URL` in `.env` and `backend/.env`.

---

## 3. Discovered Issues & Proposed Fixes

1. **JDK 23 ByteBuddy Dynamic Agent Attachment in Maven Surefire**:
   - *Problem*: In JDK 21+ / 23 (JEP 451), Mockito's inline-mock-maker attempts dynamic self-attachment. Without an explicit JVM argument, Surefire tests trigger warnings or fail in isolated environments.
   - *Fix*: Configure `maven-surefire-plugin` in `backend/pom.xml` with `<argLine>-XX:+EnableDynamicAgentLoading</argLine>`.
2. **Spring Boot Dev Datasource Default Fallback**:
   - *Problem*: `application-dev.yml` currently has fallback `jdbc:postgresql://localhost:5432/phantomtrace` if `DATABASE_URL` is omitted. If a developer runs `mvn spring-boot:run` without sourcing `.env`, it attempts connecting to the conflicting host port 5432.
   - *Fix*: Keep `application-dev.yml` fallback aligned with the container host port (`5434`) or document mandatory `.env` sourcing.
3. **Health Check Standard Compliance**:
   - *Current State*: `GET /api/v1/health` returns `{"status":"UP","application":"PhantomTrace"}` with status 200.
   - *Assessment*: Complies with requirement; does not leak internal DB credentials or infrastructure addresses.

---

## 4. Acceptance Criteria
- [ ] **AC-1**: Frontend build (`npm run build`) executes cleanly with zero errors.
- [ ] **AC-2**: Frontend tests (`npm test`) pass completely.
- [ ] **AC-3**: Backend builds and executes all JUnit tests cleanly with `mvn test` without requiring manual JVM flag overrides.
- [ ] **AC-4**: PostgreSQL container starts and is reachable on configured port `5434`.
- [ ] **AC-5**: Spring Boot application starts cleanly with profile `dev` and validates Flyway schema against PostgreSQL.
- [ ] **AC-6**: `GET /api/v1/health` returns HTTP 200 with JSON payload `{"status":"UP","application":"PhantomTrace"}`.
- [ ] **AC-7**: No credentials, tokens, or local secrets exist in Git-tracked files.

