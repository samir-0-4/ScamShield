# Tasks: Phase 2 — Backend Foundation & Database

> **Spec ID:** `backend-foundation`  
> **Status:** ✅ Complete — All tasks verified  

---

## Task Breakdown

### Part 1: Database Migration & Schema
- [x] **1.1. Flyway Migration `V2`**: Created `V2__investigations_and_evidence.sql` — `investigations` and `evidence` tables, FK cascade deletes, and performance indexes applied successfully.
- [x] **1.2. Schema Validation**: Flyway V2 applied to PostgreSQL 16. Hibernate `ddl-auto=validate` passed.

### Part 2: Enums & Domain Entities
- [x] **2.1. Enums**: Created `InvestigationCategory`; updated `InvestigationStatus` (`DRAFT`, `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`); `SeverityLevel` already has `UNKNOWN`.
- [x] **2.2. Entities**: `Investigation` and `Evidence` JPA entities implemented with relationships, auditing, and lifecycle hooks.
- [x] **2.3. Repositories**: `InvestigationRepository` (user-scoped paginated queries) and `EvidenceRepository` implemented.

### Part 3: DTOs & Mappers
- [x] **3.1. DTO Records**: `CreateInvestigationRequest`, `UpdateInvestigationRequest`, `InvestigationResponse` (with `fromEntity`), and `PageResponse<T>` implemented.

### Part 4: Service Layer & Ownership Enforcement
- [x] **4.1. Service Interface**: `InvestigationService` defined.
- [x] **4.2. Service Implementation**: `InvestigationServiceImpl` with strict ownership enforcement — 404 for missing, 403 for cross-user access, security audit log on violations.

### Part 5: Controller & Exception Handling
- [x] **5.1. Investigation Controller**: `InvestigationController` at `/api/v1/investigations` — `POST`, `GET`, `GET /{id}`, `PATCH /{id}`, `DELETE /{id}`.
- [x] **5.2. Exception Handling**: `GlobalExceptionHandler` updated with `AccessDeniedException` handler.

### Part 6: Automated Testing
- [x] **6.1. Unit Tests (`InvestigationServiceTest`)**: 10 tests — creation, listing, paginated, update, deletion, 404, 403 cross-user access.
- [x] **6.2. Integration Tests (`InvestigationControllerIntegrationTest`)**: 6 tests — 401 unauthenticated, 400 validation, multi-tenant isolation, 403 barrier, patch protection, delete with 404 confirmation.
- [x] **6.3. Monorepo Test Run**: `mvn clean test` — **43 tests, 0 failures, 0 errors, BUILD SUCCESS**.

### Part 7: Documentation & Verification
- [x] **7.1. Documentation**: Kiro spec documents updated.
- [x] **7.2. Spec Completion Report**: Provided in Phase 2 Completion Report.
