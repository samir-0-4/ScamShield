# PhantomTrace — Development Workflow

## Day-to-Day Practices

### 1. Monorepo Organization
- `backend/` contains all Spring Boot Java services.
- `frontend/` contains all React/Vite client application files.
- `docs/` is the single source of truth for architectural and schema designs.
- `.kiro/` maintains specifications and automation hooks.

### 2. Branching & Review Strategy
- Create focused feature branches named `feature/<short-desc>` or `fix/<short-desc>`.
- Keep pull requests small and verified with automated test suites (`mvn test` and `npm run build`).

### 3. Adding New AI Agents
1. Define the agent's purpose, input prompts, and output schema in `backend/src/main/java/com/phantomtrace/ai/prompt/`.
2. Implement the `AiAgent` interface in `backend/src/main/java/com/phantomtrace/ai/agent/`.
3. Register the agent inside `AiAgentOrchestrator`.
4. Wrap any external LLM calls with input/output validation via `SecurityManager`.

### 4. Database Migrations
- Schema changes must be written as versioned Flyway scripts under `backend/src/main/resources/db/migration/` (e.g. `V1__init_schema.sql`, `V2__add_index.sql`).
- Never edit an existing migration script once committed.

