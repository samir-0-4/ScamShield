# PhantomTrace — Backend Architecture

## Architectural Style

The backend follows a **Layered Architecture** with strict boundary separation and domain packaging.

```
[ HTTP Requests ]
       │
       ▼
[ Security Filter Chain ] ── JWT Authentication & CORS Filters
       │
       ▼
[ Controller Layer ] ───── REST Endpoints, Input Validation (@Valid DTOs)
       │
       ▼
[ Service Layer ] ──────── Core Business Logic, Transactions, Event Publishing
    ├── AI Orchestration (Groq Client, Agents)
    ├── External Intelligence (VirusTotal, URLhaus)
    └── Storage Abstraction (FileStorageService)
       │
       ▼
[ Repository Layer ] ───── Spring Data JPA Repositories
       │
       ▼
[ Database ] ───────────── PostgreSQL 16 with Flyway Migrations
```

## Core Principles

1. **Thin Controllers**: Controllers handle request parsing, invoke service methods, and wrap responses in standardized envelopes (`ApiResponse<T>`).
2. **DTO Layer**: Entity objects are never exposed directly to controllers or JSON serializers. DTOs are mapped via MapStruct or manual conversion methods.
3. **Global Exception Handling**: A centralized `@RestControllerAdvice` translates domain exceptions into standard RFC 7807 problem details or structured error responses.
4. **Security Architecture**: Stateless JWT authentication configured via Spring Security 6 `SecurityFilterChain`. Public endpoints (health, login, register) are white-listed; all investigation endpoints require valid bearer tokens.
5. **Storage Abstraction**: File upload storage is separated behind a `FileStorageService` interface, with an initial `LocalStorageServiceImpl` for local disk operations and future S3 support.

