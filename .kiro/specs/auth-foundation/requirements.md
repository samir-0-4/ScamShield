# Phase 1 Spec — Persistence Foundation & User Authentication

> **Spec ID:** `auth-foundation`
> **Phase:** 1 (foundation)
> **Status:** Draft — awaiting approval
> **Owner:** Senior Full-Stack Architect

## 1. Purpose

Establish the two foundational layers every subsequent PhantomTrace feature depends on:
1. A Flyway-managed PostgreSQL persistence layer with the first JPA entity (`User`).
2. JWT-based authentication implementing the `/api/v1/auth/*` endpoints the React frontend is already wired to call.

This phase delivers a real, demonstrable register → login → authenticated-request loop on top of a real database, and sets the entity/migration/repository/service/controller pattern for all later domain entities (investigations, evidence, indicators, relationships, reports).

## 2. Scope

### In scope
- Flyway baseline migration `V1` creating the `users` table plus the required PostgreSQL extensions.
- `User` JPA entity + repository (UUID keys, audited timestamps, unique email).
- `UserRole` enum (`ROLE_ANALYST`, `ROLE_ADMIN`).
- JWT issuance/validation (`JwtService`) + `JwtAuthenticationFilter`.
- Tightening `SecurityConfig` from the current `anyRequest().permitAll()` scaffold to real JWT enforcement.
- `AuthController` + `AuthService` implementing `POST /auth/register`, `POST /auth/login`, `GET /auth/me` using the existing `ApiResponse<T>` envelope.
- A `SecurityUtils` helper to resolve the authenticated user id (the foundation for user-ownership enforcement in later phases).
- An optional, dev-profile-gated demo analyst seed for hackathon demonstration.
- Backend unit + integration tests for JWT, auth service, auth controller, and the security filter.

### Out of scope (later phases)
- Investigation, evidence, threat-indicator, relationship, report entities/endpoints.
- AI agent execution, Groq live HTTP integration, prompt templates.
- Threat-intelligence integrations (VirusTotal, URLhaus) implementations.
- React Flow / Recharts visualizations and frontend feature components.
- Frontend route guards (deferred to the phase that consumes auth on protected pages — but login/register wiring is verified here).

## 3. User Stories

- **US-1**: As a new analyst, I can register with email, full name, and password so that I obtain a JWT and can access the platform.
- **US-2**: As a registered analyst, I can log in with email/password to receive a fresh JWT for authenticating subsequent requests.
- **US-3**: As an authenticated analyst, the frontend can call `GET /auth/me` to retrieve my profile (id, email, fullName, role) so the UI can render my identity.
- **US-4**: As the platform, unauthenticated requests to protected endpoints are rejected with `401` so that future resources stay private to their owners.
- **US-5**: As the platform, every protected resource will be attributable to a user (foundation pattern), so that ownership checks can be enforced in later phases.
- **US-6**: As a hackathon demonstrator, a default analyst account can be seeded in dev so the product is demonstrable without manual signup.

## 4. Functional Requirements

- **FR-1** `POST /api/v1/auth/register`
  - Request body: `{ email, fullName, password }`.
  - Email must be a valid, non-blank, unique email. `fullName` non-blank (2–150 chars). `password` non-blank (8–100 chars).
  - On success: HTTP `201`, `ApiResponse` with `success:true`, `data: { token, user: { id, email, fullName, role } }`, where `role` defaults to `ROLE_ANALYST`.
  - On duplicate email: HTTP `409`, `ApiResponse.error("Email already registered")`.
  - On invalid input: HTTP `400` with field-level validation errors (via existing `GlobalExceptionHandler`).

- **FR-2** `POST /api/v1/auth/login`
  - Request body: `{ email, password }`.
  - On success: HTTP `200`, `ApiResponse` with `data: { token, user: { id, email, fullName, role } }`.
  - On invalid credentials (wrong password OR unknown email): HTTP `401`, `ApiResponse.error("Invalid email or password")` — identical message for both cases to prevent user enumeration.

- **FR-3** `GET /api/v1/auth/me`
  - Requires a valid `Authorization: Bearer <token>`.
  - On success: HTTP `200`, `ApiResponse` with `data: { id, email, fullName, role }` of the token subject.
  - On missing/invalid token: HTTP `401`.

- **FR-4** `GET /api/v1/health` remains public (no auth), unchanged from current behavior.

- **FR-5** JWT characteristics:
  - Algorithm: HS256, signed with `phantomtrace.security.jwt.secret`.
  - Claims: `sub` (userId), `email`, `role`, `iat`, `exp`.
  - Expiry: `phantomtrace.security.jwt.expiration-ms` (default 86400000 = 24h).
  - Secret must be validated at startup to be at least 32 characters; otherwise the app fails fast with a clear error.

- **FR-6** Password storage:
  - Stored only as a BCrypt hash (`BCryptPasswordEncoder` bean already exists). Plaintext password is never persisted, logged, or returned in any response.

- **FR-7** Demo seed (optional, gated):
  - A `CommandLineRunner` active only under a gated flag (e.g. dev profile + `phantomtrace.demo.seed-enabled=true`) creates a default `ROLE_ANALYST` account if none exists. Default credentials documented in the completion report.

## 5. Acceptance Criteria (measurable)

- **AC-1**: `POST /api/v1/auth/register` with valid body returns `201` and a response whose `data.token` is a non-empty JWT and `data.user.role` equals `ROLE_ANALYST`. ✅
- **AC-2**: Registering the same email twice returns `409` with `success:false`. ✅
- **AC-3**: Registering with a blank/invalid email or a password shorter than 8 chars returns `400` with a field-error message. ✅
- **AC-4**: `POST /api/v1/auth/login` with valid credentials returns `200` and `data.token` is a non-empty JWT. ✅
- **AC-5**: `POST /api/v1/auth/login` with a wrong password returns `401` with message exactly `Invalid email or password`. ✅
- **AC-6**: `POST /api/v1/auth/login` with a non-existent email returns `401` with the same message as AC-5. ✅
- **AC-7**: `GET /api/v1/auth/me` with a valid Bearer token returns `200` and `data.email` matches the registered user. ✅
- **AC-8**: `GET /api/v1/auth/me` with no/invalid token returns `401`. ✅
- **AC-9**: `GET /api/v1/health` returns `200` with no token (unchanged). ✅
- **AC-10**: A request to any non-public path (e.g. a probe path) without a token returns `401` (i.e. `anyRequest().authenticated()` is in effect, not `permitAll`). ✅
- **AC-11**: The Flyway `V1` migration runs cleanly on startup against PostgreSQL (dev profile) and the app boots with `ddl-auto=validate` (no schema drift). ✅
- **AC-12**: Passwords are persisted only as BCrypt hashes; querying `users.password_hash` returns a `$2a$...` value, never plaintext. ✅
- **AC-13**: All backend tests pass: `JwtServiceTest`, `AuthServiceTest`, `AuthControllerIntegrationTest`, `JwtAuthenticationFilterTest` (or equivalent SecurityConfig test). ✅
- **AC-14**: No credentials/API keys are hardcoded in source; all secrets come from configuration/env. ✅
- **AC-15**: The frontend `LoginPage` → `/dashboard` and `RegisterPage` → `/login` flows work end-to-end against the real backend (manual verification step). ✅

## 6. Dependencies & Constraints

- **New Maven dependency (justified):** `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` at pinned version `0.12.6`. This is the standard JWT library for Spring Security; no lighter standard alternative exists in the current stack.
- **Reuse existing:** `ApiResponse<T>`, `ApiException`, `GlobalExceptionHandler`, `CorsConfig`, `PasswordEncoder` bean, `phantomtrace.security.jwt.*` config keys, test H2 profile.
- **Do not rename** existing packages; new classes go into the existing empty `com.phantomtrace.auth`, `com.phantomtrace.entity`, `com.phantomtrace.repository`, `com.phantomtrace.service` packages.
- **No fake data:** real JWT, real DB rows, real BCrypt hashing. No mock auth.
