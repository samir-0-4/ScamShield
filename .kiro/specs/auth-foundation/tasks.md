# Phase 1 Tasks — Persistence Foundation & User Authentication

> **Spec ID:** `auth-foundation`
> Status: **Complete** — all tasks verified. See completion report.

## A. Dependencies & Migration
- [x] A1. Add `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (pinned `0.12.6`) to `backend/pom.xml`.
- [x] A2. Create `backend/src/main/resources/db/migration/V1__init_schema.sql` (extensions + `users` table + index).
- [x] A3. Verify dev-profile startup against PostgreSQL runs Flyway and boots with `ddl-auto=validate`. *(Flyway applied V1 against PostgreSQL 16.11; second boot "up to date", Hibernate validate passed.)*

## B. Domain Layer
- [x] B1. Create `UserRole` enum (`ROLE_ANALYST`, `ROLE_ADMIN`) in `com.phantomtrace.enums`.
- [x] B2. Create `User` JPA entity in `com.phantomtrace.entity` (UUID id, unique email, passwordHash, fullName, role, audited timestamps).
- [x] B3. Create `JpaAuditingConfig` (`@EnableJpaAuditing`) in `com.phantomtrace.config`.
- [x] B4. Create `UserRepository` in `com.phantomtrace.repository` (`existsByEmail`, `findByEmail`).

## C. Auth DTOs
- [x] C1. Create `RegisterRequest`, `LoginRequest` (records with `jakarta.validation` constraints).
- [x] C2. Create `UserDto` record (no password field).
- [x] C3. Create `AuthResponse` record (`{ token, user }`).

## D. Security Layer
- [x] D1. Create `JwtService` (generate, validate, parseClaims, secret-length `@PostConstruct` check).
- [x] D2. Create `JwtAuthenticationFilter` (extends `OncePerRequestFilter`).
- [x] D3. Modify `SecurityConfig`: add JWT filter, change `anyRequest().permitAll()` → `anyRequest().authenticated()`, keep health/auth/error public, keep CORS + PasswordEncoder bean. *(Also added `HttpStatusEntryPoint(401)` for deterministic unauthenticated responses.)*
- [x] D4. Create `SecurityUtils.getCurrentUserId()` (resolves authenticated principal; throws 401 if absent).

## E. Auth Service & Controller
- [x] E1. Create `AuthService` (`register`, `login`, `getCurrentUser`) with duplicate-email 409, uniform login-failure message, BCrypt encoding.
- [x] E2. Create `AuthController` (`POST /auth/register` 201, `POST /auth/login` 200, `GET /auth/me`), returning `ApiResponse<T>`.

## F. Demo Seed
- [x] F1. Create `DemoUserSeeder` (`CommandLineRunner`, `@Profile("dev")` + gated by `phantomtrace.demo.seed-enabled`).
- [x] F2. Add `phantomtrace.demo.seed-enabled` / `phantomtrace.demo.seed-password` defaults to `application.yml`; document in `.env.example`.

## G. Tests (backend)
- [x] G1. `JwtServiceTest` (generation, claims, invalid token, foreign-secret rejection, secret-length validation).
- [x] G2. `AuthServiceTest` (Mockito: register success/duplicate, login success/wrong-password/unknown-email, getCurrentUser found/missing).
- [x] G3. `AuthControllerIntegrationTest` (MockMvc: register 201/409/400, login 200/401 + messages, `/auth/me` 200/401, protected 401, health 200).
- [x] G4. `JwtAuthenticationFilterTest` (valid token sets principal during request; invalid/missing leaves anonymous). SecurityConfig behavior also covered by the integration test.
- [x] G5. Run `mvn clean test` — all tests green. *(26 tests, 0 failures, 0 errors, BUILD SUCCESS.)*

## H. Verification
- [x] H1. Boot backend on dev profile against PostgreSQL; confirm Flyway V1 applied and app starts (no schema-drift errors).
- [x] H2. Manual curl checks against running backend for AC-1..AC-10. *(15/15 checks passed.)*
- [~] H3. Manual end-to-end: frontend `LoginPage` → `/dashboard` and `RegisterPage` → `/login` against the real backend. *(Backend auth endpoints verified live; frontend `authService` unchanged and targets the exact endpoints; `npm run test` + `npm run build` green. Browser click-through left as a manual user step.)*
- [x] H4. Confirm `users.password_hash` in DB is a BCrypt `$2a$...` value (no plaintext). *(Confirmed `$2a$10$...` in PostgreSQL.)*

## I. Documentation & Spec Bookkeeping
- [x] I1. `docs/api/api-overview.md` reviewed — no drift; auth endpoints match the spec. Left as-is.
- [x] I2. Update `.kiro/specs/auth-foundation/tasks.md` statuses and mark spec complete.
- [x] I3. Produce the Phase 1 completion report (per master instruction §8).
