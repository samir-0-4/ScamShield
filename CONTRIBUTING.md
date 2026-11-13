# Contributing to PhantomTrace

Thank you for your interest in contributing to **PhantomTrace**. We follow clean architecture, strict security standards, and pragmatic development practices.

---

## Code Quality Standards

### General Rules
1. **Separation of Concerns**: Keep business logic, presentation, data access, and third-party orchestration strictly partitioned.
2. **Epistemic Truth Principle**: Never represent AI inferences or probabilistic scores as definitive ground truth. UI and API responses must clearly separate `VERIFIED`, `AI_INFERENCE`, and `UNKNOWN`.
3. **No Secrets in Code**: Never commit secrets, tokens, or private keys. Always use environment variables.
4. **Minimal Dependencies**: Do not add third-party libraries without architectural justification.

### Backend (Spring Boot & Java)
- Follow the standard layered architecture (`controller` -> `service` -> `repository`).
- Controllers must remain thin, delegating to services.
- Always use DTOs for client requests and responses; never leak JPA entities over REST endpoints.
- Validate incoming requests with Bean Validation annotations (`@Valid`, `@NotNull`, etc.).
- Handle exceptions using global `@RestControllerAdvice`.
- Write unit tests using JUnit 5 and Mockito.

### Frontend (React & Vite)
- Use functional components and custom React hooks.
- Keep reusable UI components in `components/` and feature-specific logic in `features/`.
- Centralize API requests in `services/api/`. Do not invoke `fetch` or Axios directly inside UI view components.
- Keep React Flow graph components isolated in `features/evidence-graph/` and `components/graph/`.

---

## Branching & Commit Conventions

Follow the Conventional Commits specification:
- `feat: <description>` for new features
- `fix: <description>` for bug fixes
- `docs: <description>` for documentation changes
- `refactor: <description>` for code refactoring
- `test: <description>` for adding or updating tests
- `chore: <description>` for maintenance or dependency updates

