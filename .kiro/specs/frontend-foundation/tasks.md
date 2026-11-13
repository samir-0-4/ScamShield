# Phase 3 Tasks — Frontend Foundation & Dashboard

> **Spec ID:** `frontend-foundation`
> Mark `[x]` only after verification.

## A. Theme & design system
- [x] A1. Update `tailwind.config.js` (navy/charcoal + electric blue/cyan palette, epistemic kept).
- [x] A2. Update `src/styles/index.css` (dark base, typography, focus rings, scrollbar, `.card`/`.input` utilities, restrained hero glow).
- [x] A3. Build `components/ui/` kit: Button, Card, Badge, StatusBadge, Input (password toggle), Select, Textarea, Spinner, EmptyState, Pagination, StatCard.

## B. Layout & navigation
- [x] B1. `components/layout/PublicLayout.jsx` (public top nav + footer + Outlet).
- [x] B2. `components/layout/DashboardLayout.jsx` (sidebar + topbar + Outlet; mobile drawer state).
- [x] B3. `components/navigation/Sidebar.jsx` (nav links + Lucide icons + active highlight).
- [x] B4. `components/navigation/Topbar.jsx` (user name + logout; hamburger on mobile).
- [x] B5. `routes/ProtectedRoute.jsx` + `PublicOnlyRoute` guard.

## C. State & API
- [x] C1. Enhance `AuthContext.jsx` (validate session on mount via `/auth/me`; `loading`; remove unused import).
- [x] C2. Enhance `apiClient.js` (401 → clear token + redirect to `/login`; normalized error messages).
- [x] C3. Create `services/investigation/investigationService.js` (list/get/create/update/remove).
- [x] C4. Update `constants/index.js` (real statuses, categories, risk levels with labels/colors); add `utils/{format,cn}`.
- [x] C5. Remove dead `InvestigationProvider` from `App.jsx`.

## D. Routing
- [x] D1. Rewrite `routes/AppRoutes.jsx` with PublicLayout / ProtectedRoute+DashboardLayout / not-found; add `/investigations` and `/investigations/:id/analysis`.

## E. Pages
- [x] E1. `LandingPage.jsx` (exact hero copy, how-it-works, features, security/trust, footer).
- [x] E2. `LoginPage.jsx` (validation, loading, errors, password toggle, real login).
- [x] E3. `RegisterPage.jsx` (validation, loading, errors, password toggle, real register).
- [x] E4. `DashboardPage.jsx` (real stats from backend, recent investigations, quick actions, loading/empty).
- [x] E5. `InvestigationsPage.jsx` (list, search, status/category filters, pagination, empty state).
- [x] E6. `NewInvestigationPage.jsx` (validated form, real create, navigate to detail).
- [x] E7. `InvestigationDetailsPage.jsx` (real getById, metadata, risk/status, 403/404 handling).
- [x] E8. `AnalysisResultsPage.jsx` + `ReportsPage.jsx` + `SettingsPage.jsx` (honest placeholders / profile from `/auth/me`).
- [x] E9. Restyle `NotFoundPage.jsx`.

## F. Testing
- [x] F1. Mock service modules; update `App` smoke test for new landing copy.
- [x] F2. `ProtectedRoute`/`PublicOnlyRoute` redirect tests.
- [x] F3. Login & Register tests (validation, submit, error, password toggle).
- [x] F4. Dashboard test (stats from mocked list; empty state).
- [x] F5. Investigations list (empty + rows + pagination) & New Investigation (create → navigate) tests.
- [ ] F6. Investigation detail (render + 404/403) test.
- [ ] F7. Mobile nav drawer toggle test.
- [x] F8. `npm run test` green; `npm run lint` clean; `npm run build` succeeds.

## G. Verification (end-to-end against backend)
- [ ] G1. Backend sanity: curl investigation endpoints with a real JWT (not run to avoid creating test data).
- [ ] G2. Manual flow: register → login → dashboard (real stats) → create investigation → see on dashboard → open details → logout.
- [ ] G3. Confirm responsive behavior (mobile drawer) via manual visual QA.

## H. Spec bookkeeping
- [x] H1. Update `.kiro/specs/frontend-foundation/tasks.md`; mark completed implementation items.
- [x] H2. Record automated verification in the task completion status.
