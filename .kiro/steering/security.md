# PhantomTrace — Security Model

## Authentication & Session Management

- **Algorithm**: JWT HS256 (HMAC-SHA-256)
- **Library**: JJWT 0.12.6
- **Secret**: 256-bit minimum random value, injected via `JWT_SECRET` environment variable; never hardcoded or committed
- **Token lifetime**: Configurable via `JWT_EXPIRATION_MS` (default 24 hours)
- **Password hashing**: bcrypt with Spring Security's `BCryptPasswordEncoder` (cost factor 10)
- **Storage**: Tokens are stateless; no server-side session store. Clients store JWT in memory or secure HTTP-only cookie — never localStorage.

---

## SSRF Protection

`SecurityManager.java` intercepts all outbound HTTP calls made by threat intelligence adapters before the request is dispatched. It rejects any request whose resolved IP address falls within:

- `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16` (RFC 1918 private ranges)
- `127.0.0.0/8` (loopback)
- `169.254.0.0/16` (link-local / cloud metadata, e.g. AWS IMDSv1 at `169.254.169.254`)
- `::1` and `fc00::/7` (IPv6 loopback and ULA)
- Requests using non-HTTP/HTTPS schemes (file, ftp, ldap, etc.)

If a URL resolves to a blocked address the adapter throws `SsrfBlockedException` and the request never leaves the JVM.

---

## File Upload Security

- **Allowed MIME types**: Explicit allowlist — `text/plain`, `application/pdf`, `image/jpeg`, `image/png`, `image/webp`
- **Size limit**: 25 MB per file, enforced at the Spring `MultipartResolver` layer before processing
- **Extension validation**: Double-extension attacks mitigated by checking MIME type from content (Apache Tika magic bytes), not from filename
- **No execution**: Uploaded files are parsed in memory by PDFBox / Tess4j / ZXing; they are never written to an executable path and never interpreted by the OS
- **Storage**: Uploaded content stored as `bytea` in PostgreSQL, not on the filesystem

---

## API Key Management

- All third-party API keys (`GROQ_API_KEY`, `VIRUSTOTAL_API_KEY`) live exclusively in backend environment variables
- Keys are **never** returned to the frontend in any API response
- Keys are **never** logged (the logging config masks fields named `*key*`, `*secret*`, `*token*`, `*password*`)
- The `.gitignore` excludes `.env`, `*.env`, and `application-local.properties`
- The pre-commit hook (`.kiro/hooks/pre-commit-check.sh`) blocks commits that contain `.env` files

---

## Cross-Origin Resource Sharing (CORS)

- Allowed origin: single value read from `CORS_ALLOWED_ORIGIN` environment variable (e.g. `http://localhost:5173` for dev, production domain for prod)
- No wildcard `*` origins in production
- Allowed methods: `GET, POST, PUT, DELETE, OPTIONS`
- Allowed headers: `Authorization, Content-Type`
- Pre-flight cache: 3600 seconds

---

## SQL Injection Prevention

- All database access goes through **Spring Data JPA** repositories using either:
  - Derived query methods (compile-time safe)
  - `@Query` JPQL with named parameters (`:param`)
- Native SQL is used only where absolutely necessary, always with named parameters via `@Param`
- Hibernate's `ddl-auto=validate` means no dynamic DDL is ever executed at runtime

---

## Error Handling & Information Leakage

- Production profile (`spring.profiles.active=prod`) activates a global `@ControllerAdvice` that:
  - Returns only a generic message and an error code — never a stack trace
  - Logs the full exception server-side with a correlation ID
  - Maps the correlation ID into the response so support can trace logs without exposing internals
- Frontend never displays raw backend error messages to end users

---

## Rate Limiting

`RateLimitingFilter` applies in-memory Bucket4j limits per client IP:

- Registration: 5 requests per minute
- Login: 10 requests per minute
- Investigation and evidence APIs: 20 requests per minute

The filter does not trust a client-supplied `X-Forwarded-For` header. Production
deployments behind a reverse proxy must configure the proxy to preserve the
actual client address at the network boundary. Tests disable the filter through
`phantomtrace.security.rate-limit.enabled=false` and cover the filter separately.

---

## Epistemic Truth Principle (Security Relevance)

All LLM-generated content is tagged `AI_INFERENCE` in the database and UI. This prevents investigators from treating AI hypotheses as confirmed facts, reducing the risk of acting on hallucinated threat intelligence. Only data sourced from VirusTotal or URLhaus — deterministic external lookups — may be tagged `VERIFIED`.

---

## Dependency Security

- Backend dependencies managed via Maven; `mvn dependency:check` can be run to audit for known CVEs
- Frontend dependencies managed via npm; `npm audit` run before each release
- Base Docker images pinned to digest hashes in `docker-compose.yml`

---

## Security Checklist (Pre-Deployment)

- [ ] `JWT_SECRET` is at least 256 bits of entropy
- [ ] `CORS_ALLOWED_ORIGIN` is set to the production domain only
- [ ] All `.env` files are excluded from the repository
- [ ] `spring.profiles.active=prod` is set in the production container
- [ ] HTTPS enforced at the reverse-proxy layer (nginx/load balancer)
- [ ] Rate limiting configured at nginx or application layer
- [ ] `npm audit` and `mvn dependency:check` show no critical CVEs
