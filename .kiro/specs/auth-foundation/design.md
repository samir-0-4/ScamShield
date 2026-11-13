# Phase 1 Design — Persistence Foundation & User Authentication

> **Spec ID:** `auth-foundation`

## 1. Architecture Overview

Layered, thin-controller design consistent with the project's clean-architecture steering rules:

```
AuthController (web) ─▶ AuthService (business logic) ─▶ UserRepository (data) ─▶ User (entity)
        │                       │
        │                       ├─▶ PasswordEncoder (BCrypt, existing bean)
        │                       └─▶ JwtService (issue/validate tokens)
        │
        └─▶ ApiResponse<T> envelope (existing)
        └─▶ SecurityUtils (resolve authenticated user id)

JwtAuthenticationFilter ─▶ JwtService.validate ─▶ SecurityContextHolder (principal = userId)
        ▲
SecurityConfig wires the filter before UsernamePasswordAuthenticationFilter; permits health + auth/**; authenticates everything else.
```

## 2. Package Placement (reuses existing empty packages)

| Concern | Package (existing) | New files |
|---|---|---|
| Entity | `com.phantomtrace.entity` | `User.java` |
| Enum | `com.phantomtrace.enums` | `UserRole.java` |
| Repository | `com.phantomtrace.repository` | `UserRepository.java` |
| Service | `com.phantomtrace.service` | `AuthService.java` |
| Security | `com.phantomtrace.security` | `JwtService.java`, `JwtAuthenticationFilter.java`, `SecurityUtils.java`, `DemoUserSeeder.java` |
| Auth DTOs | `com.phantomtrace.auth` | `RegisterRequest.java`, `LoginRequest.java`, `AuthResponse.java`, `UserDto.java` |
| Controller | `com.phantomtrace.controller` | `AuthController.java` |
| Config | `com.phantomtrace.config` | `JpaAuditingConfig.java` |
| Migration | `resources/db/migration` | `V1__init_schema.sql` |

No existing files are deleted. `SecurityConfig.java` is modified in place.

## 3. Data Model

### 3.1 `UserRole` enum
```
ROLE_ANALYST, ROLE_ADMIN
```
Stored as `EnumType.STRING` in the `role` column. New registrations default to `ROLE_ANALYST`.

### 3.2 `User` entity
- `id`: `UUID`, `@GeneratedValue(strategy = GenerationType.UUID)` (Hibernate 6 generates the UUID in Java, so the migration needs no DB-side `uuid-ossp` default).
- `email`: `VARCHAR(255)`, unique, non-null.
- `passwordHash`: `VARCHAR(255)`, non-null. Column name `password_hash`.
- `fullName`: `VARCHAR(150)`, non-null. Column name `full_name`.
- `role`: `UserRole` (string), non-null.
- `createdAt` / `updatedAt`: `Instant`, `@CreatedDate` / `@LastModifiedDate`, non-null, columns `created_at` / `updated_at` (`TIMESTAMPTZ`). Auditing enabled via `JpaAuditingConfig` (`@EnableJpaAuditing`).

### 3.3 Migration `V1__init_schema.sql`
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE users (
    id            UUID PRIMARY KEY NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_users_email ON users (email);
```
- Application assigns the UUID (Hibernate `GenerationType.UUID`), so no DB-side default generator is required — keeps the migration portable and avoids reliance on `uuid-ossp` for inserts.
- Resolves the current runtime risk: dev/prod run `ddl-auto=validate` + Flyway enabled with no migrations present.

## 4. DTOs (all records, validation via `jakarta.validation`)

- `RegisterRequest { @NotBlank @Email email; @NotBlank @Size(min=2,max=150) fullName; @NotBlank @Size(min=8,max=100) password }`
- `LoginRequest { @NotBlank @Email email; @NotBlank password }`
- `AuthResponse { String token; UserDto user }`
- `UserDto { UUID id; String email; String fullName; UserRole role }` — never exposes `passwordHash`.

## 5. Security Design

### 5.1 `JwtService`
- `String generateToken(UUID userId, String email, UserRole role)` — HS256, claims `sub`=userId, `email`, `role`, `iat`, `exp` (now + `expiration-ms`).
- `boolean isTokenValid(String token)` — verifies signature + expiry; returns false on any `JwtException`.
- `Claims parseClaims(String token)` — extracts claims for the filter.
- Secret loaded from `phantomtrace.security.jwt.secret` via `@Value`; a `@PostConstruct` validates length >= 32 and fails fast otherwise.

### 5.2 `JwtAuthenticationFilter` (extends `OncePerRequestFilter`)
- Reads `Authorization` header; if missing or not `Bearer `, continues the chain (anonymous) — SecurityConfig decides auth.
- Validates the token via `JwtService`; on success sets `SecurityContextHolder` authentication with principal = `userId` and authority `ROLE_<role>`. Clears the context after the request.
- Does **not** throw on invalid tokens — leaves the request anonymous so `SecurityConfig`'s authorization rules produce a clean `401`/`403`.

### 5.3 `SecurityConfig` (modified)
```
cors -> existing CorsConfig
csrf disabled
session STATELESS
authorizeHttpRequests:
   /api/v1/health      -> permitAll
   /api/v1/auth/**     -> permitAll
   /error              -> permitAll
   anyRequest()        -> authenticated   <-- replaces current permitAll
addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
httpBasic/formLogin disabled
PasswordEncoder bean (kept)
```

### 5.4 `SecurityUtils`
- `UUID getCurrentUserId()` — reads the authenticated principal from `SecurityContextHolder`; throws `ApiException(UNAUTHORIZED)` if absent. This is the single integration point every later service will use for ownership checks.

## 6. Auth Service Logic

- `register(RegisterRequest)`:
  1. `if (userRepository.existsByEmail(email)) throw ApiException(409, "Email already registered")`.
  2. `passwordEncoder.encode(password)` → store as `passwordHash`.
  3. New `User` with `role = ROLE_ANALYST`; `save()`.
  4. `jwtService.generateToken(...)` → `AuthResponse(token, UserDto)`.
- `login(LoginRequest)`:
  1. `userRepository.findByEmail(email)`.
  2. If user absent OR `!passwordEncoder.matches(raw, hash)` → `throw ApiException(401, "Invalid email or password")`. Identical message for both branches (anti-enumeration).
  3. Generate token → `AuthResponse`.
- `getCurrentUser()`: `SecurityUtils.getCurrentUserId()` → `findById` → `UserDto`, or `404` if missing.

## 7. AuthController

Thin; returns `ResponseEntity<ApiResponse<AuthResponse>>` for register (201) / login (200), and `ApiResponse<UserDto>` for `/auth/me`. Delegates entirely to `AuthService`. Validation handled by `@Valid` + existing `GlobalExceptionHandler`.

## 8. Demo Seed

`DemoUserSeeder` implements `CommandLineRunner`, active only under `@Profile("dev")` AND gated by `phantomtrace.demo.seed-enabled` (default false). If enabled and `users` table is empty, creates one `ROLE_ANALYST` (`analyst@phantomtrace.local` / password from `phantomtrace.demo.seed-password`, default `PhantomTrace2026!`). Credentials surfaced only in the completion report and `.env.example`, never hardcoded in Java.

## 9. Frontend Integration Notes

No frontend code changes are required for this phase — the existing `authService` already targets `/auth/login`, `/auth/register`, `/auth/me` and unwraps `ApiResponse.data` to read `token` / `user`. `Navbar` consumes `user.fullName`. We only **verify** the end-to-end flow manually. (Frontend route guards are intentionally deferred.)

## 10. Testing Strategy

| Test | Type | Covers |
|---|---|---|
| `JwtServiceTest` | Unit (pure) | token generation, claim extraction, expiry, invalid-token rejection, secret-length validation |
| `AuthServiceTest` | Unit (Mockito) | register success/duplicate, login success/wrong-password/unknown-email (same message), password encoding |
| `AuthControllerIntegrationTest` | `@SpringBootTest` + MockMvc | register 201/duplicate 409/validation 400, login 200/401, `/auth/me` 200/401, health public |
| `JwtAuthenticationFilterTest` | Unit | valid token sets context, invalid/missing token leaves anonymous |
| `SecurityConfigTest` | MockMvc | protected path → 401 without token; health → 200 without token |

- Tests run on the existing H2 test profile (`Flyway disabled`, `ddl-auto=create-drop`) so entities create the schema in-memory. The Flyway migration itself is verified against PostgreSQL via dev-profile startup (manual/CI step; noted as a limitation — Testcontainers PostgreSQL is intentionally out of scope for the hackathon).
- No test marks pass without executing.

## 11. Security Considerations

- No plaintext passwords anywhere; BCrypt only.
- No user enumeration (uniform login failure message).
- JWT secret validated at startup (>= 32 chars).
- `password_hash` excluded from all DTOs/responses.
- No secrets in logs (`GlobalExceptionHandler` logs messages, not bodies — passwords never appear).
- All endpoints behind `ApiResponse<T>` and centralized exception handling.
- CSRF disabled is correct for a stateless JWT API (no cookies).
- Rate limiting noted as a future hardening item, explicitly out of scope here.

## 12. Risks & Mitigations

- **Risk:** Flyway migration syntax differs between PostgreSQL and H2. **Mitigation:** test profile disables Flyway (create-drop); migration validated on PostgreSQL dev startup. Keep migration portable (no `uuid-ossp` default).
- **Risk:** Changing `SecurityConfig` to `authenticated()` could break the existing health test. **Mitigation:** health path stays `permitAll`; covered by `SecurityConfigTest`.
- **Risk:** JJWT API differences across versions. **Mitigation:** pin `0.12.6` (stable, well-documented); no open ranges.
