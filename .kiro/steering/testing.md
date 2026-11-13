# PhantomTrace — Testing Standards

## Required checks

- Frontend build: `cd frontend && npm run build`
- Frontend tests: `cd frontend && npm test`
- Frontend lint: `cd frontend && npm run lint`
- Backend tests: `cd backend && mvn test`
- Full verification: `./scripts/verify-all.sh`

## Test principles

- Preserve the existing test suite and add regression coverage for every behavior
  change.
- Security controls require both allow and deny cases, including malformed input,
  ownership violations, and upstream failure handling.
- Tests must use synthetic evidence only; never commit credentials, live tokens,
  or third-party API responses.
- Integration tests must assert tenant isolation and provenance on relationships.
- A failing baseline test must be recorded before it is fixed so regressions are
  distinguishable from pre-existing failures.

## Frontend testing

- Prefer Testing Library user-facing assertions.
- Mock service boundaries, not implementation details.
- Graph tests must render the graph inside the provider required by the graph
  library.
- Responsive behavior is covered by semantic DOM assertions and manual browser
  verification where jsdom cannot model layout.

## Backend testing

- Use JUnit 5 and Mockito for focused service/security tests.
- Use Spring Boot integration tests for request authorization, filters, and
  controller error responses.
- Keep external intelligence clients mocked in unit tests and assert that
  upstream failures become explicit `UNKNOWN` results.
