# PhantomTrace — Coding Standards

These standards are enforced by Kiro steering rules and the pre-commit hook. All contributors must follow them. Deviations require an explicit ADR (Architecture Decision Record) in `docs/decisions/`.

---

## Backend (Java / Spring Boot)

### Layering

```
Controller  →  Service  →  Repository
    ↓               ↓
  (HTTP)       (AI Agents, Integrations)
```

**Rules:**
1. `@RestController` classes handle HTTP **only**: parameter extraction, response serialisation, and delegation to a service. Zero business logic.
2. All business logic lives in `@Service` classes.
3. `@Repository` interfaces extend Spring Data's `JpaRepository` or `CrudRepository`. No query logic in services — push it into the repository.
4. Cross-cutting concerns (logging, security checks) belong in `@Aspect` or Spring Security filters, not scattered through services.

### AI Agent Pattern

Every AI task **must**:
1. Implement the `AiAgent` interface
2. Extend `AbstractAiAgent` (which handles Groq HTTP calls, prompt loading, error handling)
3. Parse all LLM responses via `StructuredAiResponseParser`
4. Tag every field derived from the LLM as `EpistemicLabel.AI_INFERENCE`

```java
// ✅ Correct
public class PhishingDetectionAgent extends AbstractAiAgent implements AiAgent {
    @Override
    public AnalysisResult analyse(EvidenceContext ctx) {
        String raw = callGroq(buildPrompt(ctx));
        return parser.parse(raw, EpistemicLabel.AI_INFERENCE);
    }
}

// ❌ Wrong — raw Groq call outside AbstractAiAgent
@Service
public class SomeService {
    public String callGroqDirectly(String prompt) { ... }
}
```

### Naming

- Classes: `PascalCase`
- Methods and fields: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase.dot.separated`
- Database columns: `snake_case` (enforced by Hibernate naming strategy)

### Error Handling

- Throw typed application exceptions (`InvestigationNotFoundException`, `SsrfBlockedException`, etc.) from services
- Let the global `@ControllerAdvice` map them to HTTP status codes
- Never swallow exceptions silently — if you catch, you must log or rethrow

### Environment Variables

- Inject via `@Value("${property.name}")` or `@ConfigurationProperties`
- Every variable used by the application must have a corresponding entry in `backend/.env.example` with a description
- Never use `System.getenv()` directly — always go through Spring's property abstraction

---

## Frontend (React / JavaScript)

### Directory Structure

```
src/
  components/      # Reusable UI components (stateless or lightly stateful)
  pages/           # Route-level page components (one per route)
  services/        # API calls and external data access
  hooks/           # Custom React hooks
  utils/           # Pure utility functions
  assets/          # Images, fonts, static files
```

### API Calls

- **All** HTTP calls go through `src/services/apiClient.js` (the axios instance with base URL, auth headers, and error interceptors)
- No component imports `axios` directly
- No `fetch()` calls anywhere in component code

```js
// ✅ Correct
import { apiClient } from '../services/apiClient';
const data = await apiClient.get('/api/investigations');

// ❌ Wrong
import axios from 'axios';
const data = await axios.get('http://localhost:8080/api/investigations');
```

### Icons

- Use **only** `lucide-react` icons
- Do not add other icon libraries (`react-icons`, FontAwesome, Hero Icons, etc.)
- If a needed icon is not in lucide-react, raise the question before adding a dependency

### Styling

- **Tailwind CSS utilities only** for all styling
- No inline `style={{ }}` props except where values are **truly dynamic** (e.g., computed pixel widths for graph layouts)
- No CSS modules or separate `.css` files unless absolutely unavoidable (document the reason)
- Colour palette tokens (obsidian, cyan, violet, risk-red) are defined in `tailwind.config.js` and used by name, not by raw hex

### Component Guidelines

- Prefer functional components with hooks; no class components
- Keep components small and single-purpose; extract sub-components when JSX exceeds ~100 lines
- Prop types: document with JSDoc comments (`/** @param {string} title */`) or add PropTypes if the codebase adopts them consistently
- Side effects only in `useEffect`; no side effects during render

### Environment Variables

- All frontend env vars prefixed with `VITE_`
- Documented in `frontend/.env.example`
- Never put secrets or API keys in frontend env vars — they are visible to end users

---

## Git & Commit Hygiene

- Commit messages follow Conventional Commits: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`
- The pre-commit hook (`.kiro/hooks/pre-commit-check.sh`) blocks:
  - Commits of `.env` files
  - `TODO` comments in production code paths
- Branch naming: `feat/<slug>`, `fix/<slug>`, `docs/<slug>`

---

## Documentation

- Every public Spring Boot `@Service` method has a Javadoc comment
- Every React component exported from `src/components/` has a JSDoc comment describing its props and purpose
- Architecture decisions are recorded as ADRs in `docs/decisions/`
- All environment variables documented in `.env.example` files at root, `backend/`, and `frontend/`
