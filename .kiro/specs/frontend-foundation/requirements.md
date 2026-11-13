# Phase 3 Spec — Frontend Foundation & Dashboard

> **Spec ID:** `frontend-foundation`
> **Phase:** 3 (frontend)
> **Status:** Approved — implementation complete; automated verification passed
> **Depends on:** Phase 1 (auth API) ✅ and Phase 2 (investigation CRUD API) ✅ — both implemented in the backend.

## 1. Purpose

Build the complete initial frontend and connect it to the real backend: a premium cybersecurity-SaaS UI, full routing with protected authenticated routes, real authentication, a dashboard driven entirely by backend data, and investigation management UI wired to the live REST API. No simulated/fake data.

## 2. Backend contract the frontend consumes (verified from source)

Auth (Phase 1): `POST /api/v1/auth/register` (201), `POST /api/v1/auth/login` (200), `GET /api/v1/auth/me` (200) — all return `ApiResponse<{ token, user:{ id, email, fullName, role } }>` (me returns just the user). 401 on bad credentials with message `Invalid email or password`.

Investigations (Phase 2): base `/api/v1/investigations`, JWT required:
- `POST` → 201 `ApiResponse<InvestigationResponse>`; body `{ title (3–255), description (≤4000, optional), category (required) }`.
- `GET?status=<InvestigationStatus>&page=&size=&sort=createdAt,desc` → 200 `ApiResponse<PageResponse<InvestigationResponse>>`.
- `GET /{id}` → 200 / 403 (non-owner) / 404 (missing).
- `PATCH /{id}` → 200; body `{ title, description, category, status }` all optional.
- `DELETE /{id}` → 200 `ApiResponse<Void>`.
- `InvestigationResponse`: `{ id, userId, title, description, category, status, overallRiskLevel, riskScore, evidenceCount, createdAt, updatedAt, completedAt }`.
- `PageResponse<T>`: `{ content, pageNumber, pageSize, totalElements, totalPages, last }`.
- `ApiResponse<T>`: `{ success, data, message, timestamp }`. The existing `apiClient` already unwraps to `ApiResponse`, so service calls read `res.data` for the payload and `res.data.content` for a page.
- Enums: `InvestigationStatus` = `DRAFT, PENDING, PROCESSING, COMPLETED, FAILED`; `InvestigationCategory` = `JOB_SCAM, PAYMENT_SCAM, PHISHING, INVESTMENT_SCAM, SHOPPING_SCAM, IDENTITY_FRAUD, SUSPICIOUS_COMMUNICATION, OTHER`; `SeverityLevel` (overallRiskLevel) = `LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN`.

## 3. Scope

### In scope
- Visual design system: dark navy/charcoal theme, electric blue + cyan accents, restrained gradients, clean typography, professional cards, subtle shadows, consistent spacing, responsive layouts, Lucide icons.
- Application structure: public layout, authenticated layout, sidebar nav, top nav, responsive mobile nav, reusable UI kit.
- Routing: all listed routes + protected authenticated routes (auth guard redirect to /login).
- Landing page (polished hero + sections).
- Auth UI: login/register with validation, loading, errors, password visibility toggle, real backend calls, logout.
- Dashboard: real stats from backend data, recent investigations, status indicators, quick actions.
- Investigation management UI: list (search + status/category filters + pagination), create (page), detail navigation, empty states — wired to real API.
- API integration: enhanced central Axios client (base URL, auth interceptor, error interceptor incl. 401 logout/redirect), investigation service module.
- Tests: routing, protected routes, login, register, dashboard data retrieval, investigation creation, empty/error states, mobile-nav behavior.

### Out of scope (deferred — no backend/AI yet)
- Real evidence upload/analysis and the analysis-results content (route/page exists as an honest placeholder).
- Real investigation reports (route/page exists as an honest placeholder).
- The React Flow evidence graph (a later phase; `@xyflow/react` stays installed but unused here).
- Any AI functionality.
- No new npm dependencies (all needed libs already present: react-router-dom, axios, lucide-react, recharts).

## 4. Design direction (per brief)

Premium cybersecurity SaaS. Dark navy/charcoal background; electric blue + cyan accents; restrained gradients; clean typography; professional cards; subtle shadows; consistent spacing; responsive; Lucide icons. Avoid excessive neon, fake-hacker-terminal aesthetics, overloaded dashboards, unnecessary animations.

## 5. Functional requirements

### Layout & navigation
- FR-L1 Two layouts: `PublicLayout` (minimal top nav) for landing/login/register; `DashboardLayout` (sidebar + top bar + responsive mobile nav) for authenticated pages.
- FR-L2 Sidebar with navigation to Dashboard, Investigations, New Investigation, Reports, Settings; highlights the active route; collapses to a mobile drawer.
- FR-L3 Top bar shows the app name/logo, and on authenticated pages the user's name + a logout control.
- FR-L4 Responsive: sidebar becomes a slide-in drawer with a hamburger toggle below a breakpoint (e.g. `lg`).

### Routing & protection
- FR-R1 Routes: `/` landing, `/login`, `/register`, `/dashboard`, `/investigations` (list), `/investigations/new`, `/investigations/:id`, `/investigations/:id/analysis`, `/reports`, `/settings`, `*` not-found.
- FR-R2 `ProtectedRoute` wraps authenticated routes; unauthenticated users are redirected to `/login` (with a return-to target); authenticated users hitting `/login`/`/register` are redirected to `/dashboard`.

### Auth UI
- FR-A1 Login & register forms: client-side validation (email format, required fields, password length), loading state, server error display, password visibility toggle.
- FR-A2 Connect to real `POST /auth/login` & `POST /auth/register`; store token+user on success; navigate to dashboard.
- FR-A3 Logout clears token/user and navigates to landing/login.
- FR-A4 `AuthContext` validates the persisted session on mount (call `/auth/me`); exposes `user`, `isAuthenticated`, `loading`, `login`, `logout`.

### Dashboard
- FR-D1 Stats computed **from backend data only**: Total investigations, Completed investigations, High-risk investigations, Evidence items.
- FR-D2 Recent investigations list (from the investigations API, newest first) with status indicators and navigation to details.
- FR-D3 Quick actions (e.g. "New Investigation", "View All").
- FR-D4 Loading and empty states; no hardcoded numbers.

### Investigation management
- FR-I1 List page: fetch `GET /investigations` with pagination; client-side search by title; status & category filters (status filter can use the backend `status` param); empty state.
- FR-I2 Create: `POST /investigations` with validated form (title, category required; description optional); on success navigate to the new investigation's detail.
- FR-I3 Detail: `GET /investigations/{id}`; show title, description, category, status, risk level, risk score, evidence count, timestamps; actions to view analysis/report; handle 403/404.
- FR-I4 All investigation calls go through a centralized `investigationService` module — no direct `fetch`/axios in components.

### API integration
- FR-P1 `apiClient`: base URL from `appConfig`, 30s timeout, request interceptor adds `Bearer <token>`, response interceptor unwraps `ApiResponse` and on 401 clears token + redirects to `/login`.
- FR-P2 No backend secrets in frontend config; only public `VITE_*` vars used.

## 6. Acceptance criteria (from the brief)

1. Open the landing page. ✅
2. Register (real `POST /auth/register`). ✅
3. Log in (real `POST /auth/login`). ✅
4. Open the dashboard (real data from backend). ✅
5. Create an investigation (real `POST /investigations`). ✅
6. See that investigation on the dashboard (real `GET /investigations`). ✅
7. Open investigation details (real `GET /investigations/{id}`). ✅
8. Log out (clears session, navigates away). ✅
All data connected to the backend; no hardcoded/fake statistics.

## 7. Constraints & decisions to confirm

1. **Dashboard stats are derived client-side** from the investigations list (the backend has no `/stats` endpoint). Total = `totalElements`; Completed = count `status=COMPLETED`; High-risk = count `overallRiskLevel` in {HIGH, CRITICAL}; Evidence items = sum of `evidenceCount`. **"Evidence items analyzed" is shown as "Evidence Items"** (sum of per-investigation `evidenceCount`), which is **0 until the evidence-ingestion phase** (no evidence API exists yet) — this is honest, not faked. A dedicated backend stats endpoint is a future optimization (out of Phase 3 scope).
2. **High-risk & evidence stats are 0 by default** (new investigations default to `overallRiskLevel=UNKNOWN`, `evidenceCount=0`) until AI/evidence phases set them — honest representation.
3. **Analysis-results and Reports pages are honest placeholders** that load the investigation context where possible and clearly state the feature is pending (no evidence/report/AI backend yet). The routes exist per the brief.
4. **New investigation is a page** (`/investigations/new`), not a modal (accessibility + simplicity); a quick "New Investigation" action links to it.
5. **403 (non-owner) and 404 (missing)** on investigation detail are handled with distinct, clear messages.
6. **No new dependencies**; existing `lucide-react`, `recharts`, `react-router-dom`, `axios` are used. `@xyflow/react` stays installed (unused until the graph phase).
7. The dead `InvestigationProvider` wrapper is removed; pages fetch via the service module (no global investigation store needed yet).
8. Mobile responsiveness is verified via component behavior tests (nav toggle) + manual visual QA; jsdom cannot assert CSS breakpoints, so visual responsive QA is a manual step.
