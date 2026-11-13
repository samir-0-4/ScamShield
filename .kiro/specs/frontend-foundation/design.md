# Phase 3 Design — Frontend Foundation & Dashboard

> **Spec ID:** `frontend-foundation`

## 1. Design system & theme

Evolve the Tailwind theme to the premium cybersecurity-SaaS direction (dark navy/charcoal + electric blue/cyan).

`tailwind.config.js` — extend `colors`:
- `bg`/surface scale: `navy` (`#0b1220` base, `#0f172a`, `#111a2e`) and `charcoal`/`slate` shades already present.
- `accent` (electric blue) `#3b82f6`/`#2563eb`; `cyan` `#22d3ee`/`#06b6d4`.
- Keep `epistemic` palette (verified/inference/unknown) for status/risk badges.
- Restrained gradient utility classes (e.g. `.bg-radial-glow`) defined in CSS, not neon.

`src/styles/index.css` — base layer: dark navy background (`#0b1220`), light slate text, system font stack (or Inter via `font-family` stack; no external font fetch to avoid network dep), custom scrollbar, focus-visible rings in accent, `.card`, `.input`, `.btn` base utility classes for consistency.

Reusable UI kit in `src/components/ui/`:
- `Button` (variants: primary/secondary/ghost/danger; sizes; loading; disabled) — evolve existing `common/Button`.
- `Card` (surface, border, subtle shadow, optional title/headerAction) — evolve existing `common/Card`.
- `Badge` + `StatusBadge` (maps status/category/risk → label + color).
- `Input` (label, error text, password visibility toggle, icon slot), `Select` (native styled), `Textarea`.
- `Spinner` / `LoadingState` (skeleton or spinner).
- `EmptyState` (icon, title, message, optional action).
- `Pagination` (page controls driven by `PageResponse`).
- `StatCard` (icon, label, value, accent).
- `Modal` (if a quick-create modal is desired later; page is primary).

Label/color maps live in `src/constants/` (see §6) so badges stay consistent.

## 2. Layout & navigation

- `components/layout/PublicLayout.jsx` — sticky minimal top bar (logo + "Sign in"/"Get started") + `<Outlet/>` + footer; used for `/`, `/login`, `/register`.
- `components/layout/DashboardLayout.jsx` — sidebar + top bar + `<Outlet/>`; wraps authenticated routes; owns mobile-drawer state.
- `components/navigation/Sidebar.jsx` — nav links (Dashboard, Investigations, New Investigation, Reports, Settings) with Lucide icons + active highlight; on mobile, rendered inside a drawer.
- `components/navigation/Topbar.jsx` — (authenticated) app mark + user menu (name, logout); hamburger button on mobile.
- `routes/ProtectedRoute.jsx` — if `!isAuthenticated` (after initial load) redirect to `/login?redirect=<to>`; if authenticated render `<Outlet/>` (or the layout). Also a `PublicOnlyRoute` guard that bounces authenticated users from `/login`/`/register` to `/dashboard`.
- The existing `AppShell.jsx` is replaced by the two layouts; `Navbar.jsx` is repurposed as the public top nav (or replaced by `PublicLayout`'s bar).

## 3. Routing (`src/routes/AppRoutes.jsx`)

```
<Routes>
  <Route element={<PublicLayout/>}>
    <Route index element={<LandingPage/>}/>
    <Route path="login" element={<PublicOnlyRoute><LoginPage/></PublicOnlyRoute>}/>
    <Route path="register" element={<PublicOnlyRoute><RegisterPage/></PublicOnlyRoute>}/>
  </Route>
  <Route element={<ProtectedRoute><DashboardLayout/></ProtectedRoute>}>
    <Route path="dashboard" element={<DashboardPage/>}/>
    <Route path="investigations" element={<InvestigationsPage/>}/>
    <Route path="investigations/new" element={<NewInvestigationPage/>}/>
    <Route path="investigations/:id" element={<InvestigationDetailsPage/>}/>
    <Route path="investigations/:id/analysis" element={<AnalysisResultsPage/>}/>
    <Route path="reports" element={<ReportsPage/>}/>
    <Route path="settings" element={<SettingsPage/>}/>
  </Route>
  <Route path="*" element={<PublicLayout><NotFoundPage/></PublicLayout>}/>
</Routes>
```
React Router v6 rank-based matching keeps `investigations/new` above `investigations/:id`.

## 4. State (`src/context/AuthContext.jsx`, `App.jsx`)

- `AuthContext`: add session validation on mount (`GET /auth/me` with stored token); `loading` true until validated; remove the unused `useEffect` import; keep `login`/`logout`/`user`/`isAuthenticated`.
- `App.jsx`: remove the dead `InvestigationProvider` wrapper; keep `BrowserRouter > AuthProvider > AppRoutes`.

## 5. API layer (`src/services/`)

- `api/apiClient.js` — keep base URL/timeout/request interceptor; enhance response error interceptor: on 401 clear token + navigate to `/login` (via a window location change or a custom event the AuthContext listens to); normalize error messages from `ApiResponse`.
- `auth/authService.js` — unchanged (already correct).
- `investigation/investigationService.js` (NEW):
  - `list({ status, page, size, sort })` → `GET /investigations` → returns `PageResponse<InvestigationResponse>`.
  - `getById(id)` → `GET /investigations/{id}` → `InvestigationResponse`.
  - `create({ title, description, category })` → `POST /investigations` → `InvestigationResponse`.
  - `update(id, { title, description, category, status })` → `PATCH /investigations/{id}`.
  - `remove(id)` → `DELETE /investigations/{id}`.
  All throw on non-`success`/HTTP errors; components read results, never call axios directly.

## 6. Constants & utils

`src/constants/index.js` — update to the real backend enums + display metadata:
- `INVESTIGATION_STATUS` { DRAFT, PENDING, PROCESSING, COMPLETED, FAILED } with `{ label, color, icon }`.
- `INVESTIGATION_CATEGORIES` (8) with labels.
- `RISK_LEVELS` (LOW/MEDIUM/HIGH/CRITICAL/UNKNOWN) with colors.
- Keep `EPISTEMIC_STATUS`, `EVIDENCE_TYPES`.
`src/utils/` — `formatDate`, `formatDateTime`, `formatRelative`, `cn` (classnames join), `titleCase`.

## 7. Pages

- `LandingPage.jsx` (rewrite): public nav; hero (heading "Not Sure If It's a Scam? Investigate Before You Trust.", the exact description); primary CTA ("Start Investigating" → /register or /dashboard); "How it works" (3 steps); features grid; security/trust section; footer. Responsive, Lucide icons, restrained gradient hero glow.
- `LoginPage.jsx` (rewrite): email + password, validation, loading, server error, password toggle, "remember"/forgot link optional; calls `authService.login`; navigate to `redirect` or `/dashboard`.
- `RegisterPage.jsx` (rewrite): fullName/email/password, validation, loading, errors, password toggle; calls `authService.register`; navigate to `/login` (or auto-login).
- `DashboardPage.jsx` (rewrite): on mount fetch investigations (`size=100` via service) and compute stats; `StatCard`s (Total, Completed, High-risk, Evidence Items); recent investigations list (StatusBadge + risk); quick actions; loading skeleton + empty state. (See §9 for stat derivation.)
- `InvestigationsPage.jsx` (NEW): list table/cards with search (title), status filter, category filter, pagination (from `PageResponse`), empty state; row → detail navigation.
- `NewInvestigationPage.jsx` (rewrite): validated form (title, category select, description); `investigationService.create`; navigate to `/investigations/:id`.
- `InvestigationDetailsPage.jsx` (rewrite): `getById`; show all metadata + StatusBadge + risk; actions (Analysis, Reports); handle 403/404 with clear messages.
- `AnalysisResultsPage.jsx` (NEW/rewrite): loads investigation via `getById`; honest placeholder stating analysis is pending (no evidence/AI backend yet); no fake results.
- `ReportsPage.jsx` (rewrite): honest placeholder; investigation context where relevant; states reports are pending.
- `SettingsPage.jsx` (rewrite): show current user (`/auth/me`); note that API-key settings are server-managed (per security model).
- `NotFoundPage.jsx` (restyle): "Return to Safety" → `/`.

## 8. Testing (`tests/`, Vitest + RTL)

Mock the service modules (`vi.mock('../services/...')`) to avoid network. Tests:
- `App` renders landing (existing smoke test updated for new copy).
- `ProtectedRoute` redirects unauthenticated to `/login`; allows authenticated.
- `LoginPage`: renders, validation errors, submits → success navigate, server error display, password toggle.
- `RegisterPage`: renders, validation, submit.
- `DashboardPage`: renders stat cards from mocked `investigationService.list`; empty state when no data.
- `InvestigationsPage`: empty state; renders rows from mocked list; pagination interaction.
- `NewInvestigationPage`: validation + submit → `create` called → navigate to detail.
- `InvestigationDetailsPage`: renders from mocked `getById`; 404/403 error states.
- `MobileNav`/Sidebar: drawer toggles open/closed on hamburger (behavior test).
- jsdom cannot assert CSS breakpoints → visual responsive QA noted as manual.

## 9. Dashboard stat derivation (honest, client-side)

Fetch `GET /investigations?size=100&sort=createdAt,desc` once:
- Total = `page.totalElements`.
- Completed = count `content` where `status === 'COMPLETED'`.
- High-risk = count `content` where `overallRiskLevel` in {`HIGH`, `CRITICAL`}.
- Evidence items = sum of `evidenceCount` across `content`.
- Recent = `content` (already newest-first; show top 5–8).
Limitation: if a user has >100 investigations, Completed/High-risk/Evidence are based on the most recent 100 (Total stays exact via `totalElements`). For the hackathon this is exact; a backend stats endpoint is a noted future optimization.

## 10. Files created / modified (summary)

Created: `components/ui/{Button,Card,Badge,StatusBadge,Input,Select,Textarea,Spinner,EmptyState,Pagination,StatCard}`; `components/layout/{PublicLayout,DashboardLayout}`; `components/navigation/{Sidebar,Topbar}`; `routes/ProtectedRoute`; `pages/{InvestigationsPage,AnalysisResultsPage}`; `services/investigation/investigationService`; `utils/{format,cn}`.
Modified: `tailwind.config.js`, `styles/index.css`, `routes/AppRoutes.jsx`, `context/AuthContext.jsx`, `App.jsx`, `services/api/apiClient.js`, `constants/index.js`, and the pages listed in §7. Existing `common/Button`,`common/Card`,`common/Badge` are evolved/consolidated into `ui/` (re-exports kept where referenced to avoid churn).

## 11. Verification plan

- Backend-first sanity: confirm backend investigation endpoints respond (curl with a real JWT) before wiring UI — guards against building against a broken API.
- `npm run test` green; `npm run build` succeeds; `npm run lint` clean.
- Manual end-to-end: register → login → dashboard (real stats) → create investigation → see it on dashboard → open details → logout, against the running backend.
