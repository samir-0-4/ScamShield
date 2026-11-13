import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { PublicLayout } from '../components/layout/PublicLayout';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { ProtectedRoute, PublicOnlyRoute } from './ProtectedRoute';
import { FullPageSpinner } from '../components/ui/Spinner';

// Static imports for public landing & auth to ensure instant rendering & synchronous test compatibility
import LandingPage from '../pages/LandingPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import NotFoundPage from '../pages/NotFoundPage';

// Lazy-loaded heavy authenticated routes for code splitting
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const InvestigationsPage = lazy(() => import('../pages/InvestigationsPage'));
const NewInvestigationPage = lazy(() => import('../pages/NewInvestigationPage'));
const InvestigationDetailsPage = lazy(() => import('../pages/InvestigationDetailsPage'));
const EvidenceAnalysisPage = lazy(() => import('../pages/EvidenceAnalysisPage'));
const AnalysisResultsPage = lazy(() => import('../pages/AnalysisResultsPage'));
const EvidenceGraphPage = lazy(() => import('../pages/EvidenceGraphPage'));
const CampaignReconstructionPage = lazy(() => import('../pages/CampaignReconstructionPage'));
const InvestigationReportPage = lazy(() => import('../pages/InvestigationReportPage'));
const ReportsPage = lazy(() => import('../pages/ReportsPage'));
const SettingsPage = lazy(() => import('../pages/SettingsPage'));

export const AppRoutes = () => (
  <Suspense fallback={<FullPageSpinner label="Loading PhantomTrace…" />}>
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<LandingPage />} />
        <Route
          path="login"
          element={
            <PublicOnlyRoute>
              <LoginPage />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="register"
          element={
            <PublicOnlyRoute>
              <RegisterPage />
            </PublicOnlyRoute>
          }
        />
      </Route>

      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="investigations/new" element={<NewInvestigationPage />} />
        <Route path="investigations" element={<InvestigationsPage />} />
        <Route path="investigations/:id" element={<InvestigationDetailsPage />} />
        <Route path="investigations/:id/analysis" element={<EvidenceAnalysisPage />} />
        <Route path="investigations/:id/results" element={<AnalysisResultsPage />} />
        <Route path="investigations/:id/graph" element={<EvidenceGraphPage />} />
        <Route path="investigations/:id/campaign" element={<CampaignReconstructionPage />} />
        <Route path="investigations/:id/report" element={<InvestigationReportPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      <Route
        path="*"
        element={
          <PublicLayout>
            <NotFoundPage />
          </PublicLayout>
        }
      />
    </Routes>
  </Suspense>
);

export default AppRoutes;
