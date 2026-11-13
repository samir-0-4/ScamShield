# Technical Design: Phase 2 — Backend Foundation & Database

> **Spec ID:** `backend-foundation`  
> **Phase:** 2 (Backend Core & Investigation Management)  

---

## 1. Architectural Strategy

### 1.1 Layered Architecture
Following clean architecture principles established in Phase 1:
```
Controller Layer (HTTP, DTO validation, Thin delegation)
       │
       ▼
Service Layer (Business rules, User ownership enforcement, Transaction boundary)
       │
       ▼
Repository Layer (Spring Data JPA, User-scoped query derivation)
       │
       ▼
Database Layer (PostgreSQL 16, Flyway migration V2, Foreign Key Constraints)
```

---

## 2. Database Schema (Flyway `V2__investigations_and_evidence.sql`)

```sql
-- Investigations table
CREATE TABLE investigations (
    id                 UUID PRIMARY KEY NOT NULL,
    user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    category           VARCHAR(50) NOT NULL,
    status             VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    overall_risk_level VARCHAR(50) DEFAULT 'UNKNOWN',
    risk_score         DOUBLE PRECISION DEFAULT 0.0,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    completed_at       TIMESTAMPTZ
);

CREATE INDEX idx_investigations_user_id ON investigations(user_id);
CREATE INDEX idx_investigations_status ON investigations(status);
CREATE INDEX idx_investigations_user_created ON investigations(user_id, created_at DESC);

-- Evidence table
CREATE TABLE evidence (
    id                  UUID PRIMARY KEY NOT NULL,
    investigation_id    UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    evidence_type       VARCHAR(50) NOT NULL,
    original_filename   VARCHAR(255),
    storage_key         VARCHAR(512),
    text_content        TEXT,
    normalized_content  TEXT,
    description         TEXT,
    mime_type           VARCHAR(100),
    file_size           BIGINT,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_evidence_investigation_id ON evidence(investigation_id);
CREATE INDEX idx_evidence_status ON evidence(status);
```

---

## 3. Class Design & Domain Modeling

### 3.1 Entities
- **`Investigation`**:
  - `UUID id` (`@GeneratedValue(strategy = GenerationType.UUID)`)
  - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") User user`
  - `String title`, `String description`
  - `@Enumerated(EnumType.STRING) InvestigationCategory category`
  - `@Enumerated(EnumType.STRING) InvestigationStatus status`
  - `@Enumerated(EnumType.STRING) SeverityLevel overallRiskLevel`
  - `Double riskScore`
  - `Instant createdAt`, `Instant updatedAt`, `Instant completedAt`
  - `@OneToMany(mappedBy = "investigation", cascade = CascadeType.ALL, orphanRemoval = true) List<Evidence> evidenceList`
- **`Evidence`**:
  - `UUID id`
  - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "investigation_id") Investigation investigation`
  - `@Enumerated(EnumType.STRING) EvidenceType evidenceType`
  - `String originalFilename`, `String storageKey`, `String textContent`, `String normalizedContent`, `String description`, `String mimeType`, `Long fileSize`
  - `String status`
  - `Instant createdAt`

### 3.2 Enums
- `InvestigationCategory`: `JOB_SCAM`, `PAYMENT_SCAM`, `PHISHING`, `INVESTMENT_SCAM`, `SHOPPING_SCAM`, `IDENTITY_FRAUD`, `SUSPICIOUS_COMMUNICATION`, `OTHER`
- `InvestigationStatus`: `DRAFT`, `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- `SeverityLevel`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `UNKNOWN`

---

## 4. Ownership Enforcement Pattern

Ownership is strictly validated inside `InvestigationServiceImpl` using the authenticated user identity provided by `SecurityUtils.getCurrentUserId()`:

```java
// Listing: Only current user's records
Page<Investigation> page = investigationRepository.findByUserId(currentUserId, pageable);

// Single Item Access & Mutation:
Investigation investigation = investigationRepository.findById(id)
    .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));

if (!investigation.getUser().getId().equals(currentUserId)) {
    log.warn("Security Alert: User {} attempted unauthorized access to investigation {}", currentUserId, id);
    throw new ApiException("Access denied: You do not own this investigation", HttpStatus.FORBIDDEN);
}
```

---

## 5. DTOs & API Contracts

- **`CreateInvestigationRequest`**:
  ```java
  public record CreateInvestigationRequest(
      @NotBlank(message = "Title is required")
      @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
      String title,

      @Size(max = 4000, message = "Description must not exceed 4000 characters")
      String description,

      @NotNull(message = "Category is required")
      InvestigationCategory category
  ) {}
  ```
- **`UpdateInvestigationRequest`**:
  ```java
  public record UpdateInvestigationRequest(
      @Size(min = 3, max = 255) String title,
      @Size(max = 4000) String description,
      InvestigationCategory category,
      InvestigationStatus status
  ) {}
  ```
- **`InvestigationResponse`**:
  ```java
  public record InvestigationResponse(
      UUID id,
      UUID userId,
      String title,
      String description,
      InvestigationCategory category,
      InvestigationStatus status,
      SeverityLevel overallRiskLevel,
      Double riskScore,
      Instant createdAt,
      Instant updatedAt,
      Instant completedAt
  ) {
      public static InvestigationResponse fromEntity(Investigation entity) { ... }
  }
  ```
- **`PageResponse<T>`**:
  ```java
  public record PageResponse<T>(
      List<T> content,
      int pageNumber,
      int pageSize,
      long totalElements,
      int totalPages,
      boolean last
  ) {
      public static <T> PageResponse<T> fromPage(Page<T> page) { ... }
  }
  ```

---

## 6. Testing Strategy

1. **Unit Testing (`InvestigationServiceTest`)**:
   - `createInvestigation`: asserts entity mapped and saved with current user.
   - `getInvestigationById_success`: asserts retrieval when owner matches.
   - `getInvestigationById_forbidden`: asserts 403 thrown when accessed by non-owner.
   - `getInvestigationById_notFound`: asserts 404 when non-existent.
   - `updateInvestigation`: asserts partial field updates and timestamp changes.
   - `deleteInvestigation`: asserts deletion permitted only for owner.
2. **Integration Testing (`InvestigationControllerIntegrationTest`)**:
   - `MockMvc` tests with real Spring Security filter chain and mock user tokens.
   - Verify unauthenticated requests return 401.
   - Verify validation errors return 400 with structured field errors.
   - Verify pagination and sorting parameters.
