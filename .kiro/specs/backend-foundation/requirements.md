# Specification: Phase 2 — Backend Foundation & Database

> **Spec ID:** `backend-foundation`  
> **Phase:** 2 (Backend Core & Investigation Management)  
> **Status:** Draft — Awaiting User Approval  
> **Target:** Database Schema, Persistence Layer, and Investigation CRUD API  

---

## 1. Overview & Objective
Build the backend persistence layer, database schema, and investigation management APIs for **PhantomTrace** (user-facing: **ScamShield**). This establishes the core investigative lifecycle: creating, querying, updating, and deleting case dossiers while enforcing strict multi-tenant user ownership and robust validation.

---

## 2. Scope

### In Scope
1. **Flyway Database Migration (`V2__investigations_and_evidence.sql`)**:
   - `investigations` table linked to `users(id)` via Foreign Key with `ON DELETE CASCADE`.
   - `evidence` table linked to `investigations(id)` via Foreign Key with `ON DELETE CASCADE`.
   - Appropriate indexes for user lookups, status filtering, and chronological sorting.
   - Database constraints (foreign keys, nullability, defaults).
2. **Domain Entities**:
   - `Investigation`: Represents an investigation case dossier.
   - `Evidence`: Represents an evidence item attached to an investigation.
   - `InvestigationCategory` & `InvestigationStatus` enums.
   - `RiskLevel` / `SeverityLevel` enum.
3. **Repository Layer**:
   - `InvestigationRepository`: User-scoped paginated queries (`findByUserId`), single-item ownership lookups (`findByIdAndUserId`).
   - `EvidenceRepository`: Scoped queries by investigation.
4. **Service & DTO Layer**:
   - DTOs: `CreateInvestigationRequest`, `UpdateInvestigationRequest`, `InvestigationResponse`, `PageResponse<T>`.
   - `InvestigationService` interface and `InvestigationServiceImpl`.
   - Strict User Ownership Enforcement: Analysts can strictly access and mutate only their own investigations. Cross-user access returns `403 Forbidden` (or `404 Not Found`).
5. **REST API Endpoints**:
   - `POST /api/v1/investigations`: Create new investigation.
   - `GET /api/v1/investigations`: List current user's investigations (with pagination and sorting).
   - `GET /api/v1/investigations/{id}`: Get single investigation by ID.
   - `PATCH /api/v1/investigations/{id}`: Partial update (title, description, category, status).
   - `DELETE /api/v1/investigations/{id}`: Delete an investigation.
6. **Testing**:
   - Unit tests with JUnit 5 & Mockito for `InvestigationService`.
   - Integration tests with MockMvc for `InvestigationController`.
   - Cross-user authorization tests, validation failure tests, pagination tests.
7. **Documentation Updates**:
   - Update `docs/database/database-design.md`, `docs/architecture/backend-architecture.md`, `docs/api/api-overview.md`.

### Out of Scope (Deferred to Future Phases)
- Multi-modal file upload and parsing pipelines (Phase 3).
- Groq AI detective agents and threat intelligence feeds (Phase 4).
- Interactive evidence relationship graph rendering (Phase 5).
- Final synthesized report generation (Phase 6).

---

## 3. Data Schema & Requirements

### 3.1 `investigations` Table
- `id` (UUID, Primary Key, application-generated UUIDv4)
- `user_id` (UUID, Foreign Key -> `users(id)` ON DELETE CASCADE, NOT NULL)
- `title` (VARCHAR(255), NOT NULL)
- `description` (TEXT, NULLABLE)
- `category` (VARCHAR(50), NOT NULL)
  - Allowed: `JOB_SCAM`, `PAYMENT_SCAM`, `PHISHING`, `INVESTMENT_SCAM`, `SHOPPING_SCAM`, `IDENTITY_FRAUD`, `SUSPICIOUS_COMMUNICATION`, `OTHER`
- `status` (VARCHAR(50), NOT NULL, default `'DRAFT'`)
  - Allowed: `DRAFT`, `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- `overall_risk_level` (VARCHAR(50), default `'UNKNOWN'`)
  - Allowed: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `UNKNOWN`
- `risk_score` (DOUBLE PRECISION, default `0.0`)
- `created_at` (TIMESTAMPTZ, NOT NULL)
- `updated_at` (TIMESTAMPTZ, NOT NULL)
- `completed_at` (TIMESTAMPTZ, NULLABLE)
- **Indexes**:
  - `idx_investigations_user_id` on `user_id`
  - `idx_investigations_status` on `status`
  - `idx_investigations_user_created` on `(user_id, created_at DESC)`

### 3.2 `evidence` Table
- `id` (UUID, Primary Key, application-generated UUIDv4)
- `investigation_id` (UUID, Foreign Key -> `investigations(id)` ON DELETE CASCADE, NOT NULL)
- `evidence_type` (VARCHAR(50), NOT NULL)
- `original_filename` (VARCHAR(255), NULLABLE)
- `storage_key` (VARCHAR(512), NULLABLE)
- `text_content` (TEXT, NULLABLE)
- `normalized_content` (TEXT, NULLABLE)
- `description` (TEXT, NULLABLE)
- `mime_type` (VARCHAR(100), NULLABLE)
- `file_size` (BIGINT, NULLABLE)
- `status` (VARCHAR(50), NOT NULL, default `'PENDING'`)
- `created_at` (TIMESTAMPTZ, NOT NULL)
- **Indexes**:
  - `idx_evidence_investigation_id` on `investigation_id`
  - `idx_evidence_status` on `status`

---

## 4. REST API Contract

### 4.1 `POST /api/v1/investigations`
- **Auth**: Bearer JWT required.
- **Request Body**:
  ```json
  {
    "title": "Suspicious Job Offer on WhatsApp",
    "description": "Received unsolicited high-paying remote job offer from unknown number.",
    "category": "JOB_SCAM"
  }
  ```
- **Validation**:
  - `title`: Not blank, 3 to 255 chars.
  - `description`: Optional, max 4000 chars.
  - `category`: Valid enum value (`JOB_SCAM`, `PAYMENT_SCAM`, etc.).
- **Response (201 Created)**:
  ```json
  {
    "success": true,
    "data": {
      "id": "c1f7a0b3-...",
      "userId": "95185437-...",
      "title": "Suspicious Job Offer on WhatsApp",
      "description": "Received unsolicited high-paying remote job offer...",
      "category": "JOB_SCAM",
      "status": "DRAFT",
      "overallRiskLevel": "UNKNOWN",
      "riskScore": 0.0,
      "createdAt": "2026-11-10T09:00:00Z",
      "updatedAt": "2026-11-10T09:00:00Z",
      "completedAt": null
    },
    "message": "Investigation created successfully",
    "timestamp": "..."
  }
  ```

### 4.2 `GET /api/v1/investigations`
- **Auth**: Bearer JWT required.
- **Query Params**: `page` (default 0), `size` (default 10), `sort` (default `createdAt,desc`), `status` (optional filter).
- **Behavior**: Retrieves only investigations belonging to the authenticated user.
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "content": [ ... ],
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    },
    "message": "Investigations retrieved successfully",
    "timestamp": "..."
  }
  ```

### 4.3 `GET /api/v1/investigations/{id}`
- **Auth**: Bearer JWT required.
- **Behavior**:
  - Returns investigation if owned by current user.
  - Returns `404 Not Found` if id does not exist.
  - Returns `403 Forbidden` if id exists but belongs to another user.

### 4.4 `PATCH /api/v1/investigations/{id}`
- **Auth**: Bearer JWT required.
- **Request Body** (all fields optional):
  ```json
  {
    "title": "Updated Title",
    "description": "Updated Description",
    "category": "PHISHING",
    "status": "PENDING"
  }
  ```
- **Behavior**: Updates specified fields, updates `updated_at`, checks ownership.

### 4.5 `DELETE /api/v1/investigations/{id}`
- **Auth**: Bearer JWT required.
- **Behavior**: Deletes investigation if owned by user. Cascades to attached evidence. Returns 200 OK.

---

## 5. Security & Ownership Rules
1. Every investigation must be explicitly associated with the authenticated user ID (`SecurityUtils.getCurrentUserId()`).
2. An analyst can never see, modify, or delete another analyst's investigations.
3. Requests with missing or invalid JWT tokens are rejected with `401 Unauthorized`.
4. Unauthorized resource access attempts return `403 Forbidden` and log a security audit warning.
5. All input payloads are validated with Jakarta Validation (`@Valid`, `@NotBlank`, `@Size`).

---

## 6. Acceptance Criteria
- [ ] **AC-1**: User can register and log in, receiving a valid JWT.
- [ ] **AC-2**: Protected investigation routes reject unauthenticated requests with `401 Unauthorized`.
- [ ] **AC-3**: Authenticated user can create an investigation (`POST /api/v1/investigations`) with status `DRAFT` and initial risk `UNKNOWN`.
- [ ] **AC-4**: Authenticated user can retrieve their paginated list of investigations (`GET /api/v1/investigations`).
- [ ] **AC-5**: Authenticated user can retrieve, patch, and delete their own investigation.
- [ ] **AC-6**: Attempting to access or mutate another user's investigation is rejected with `403 Forbidden`.
- [ ] **AC-7**: PostgreSQL persists investigations and evidence; Flyway migration `V2` executes without errors.
- [ ] **AC-8**: Comprehensive unit and integration tests (JUnit 5 + Mockito) pass cleanly with `mvn test`.
