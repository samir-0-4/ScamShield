import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  FolderSearch,
  CheckCircle2,
  ShieldAlert,
  FileStack,
  PlusCircle,
  ChevronRight,
  Clock,
} from 'lucide-react';
import StatCard from '../components/ui/StatCard';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import { StatusBadge, RiskBadge, CategoryBadge } from '../components/ui/StatusBadge';
import { EmptyState } from '../components/ui/EmptyState';
import { ErrorState } from '../components/ui/ErrorState';
import { Skeleton } from '../components/ui/Skeleton';
import { investigationService } from '../services/investigation/investigationService';
import { formatRelative } from '../utils/format';
import { useAuth } from '../hooks/useAuth';

const RISK_HIGH = new Set(['HIGH', 'CRITICAL']);

const deriveStats = (content, totalElements) => ({
  total: totalElements,
  completed: content.filter((i) => i.status === 'COMPLETED').length,
  highRisk: content.filter((i) => RISK_HIGH.has(i.overallRiskLevel)).length,
  evidenceTotal: content.reduce((sum, i) => sum + (i.evidenceCount || 0), 0),
});

export const DashboardPage = () => {
  const { user } = useAuth();
  const [page, setPage] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchDashboard = useCallback(() => {
    setLoading(true);
    investigationService
      .list({ page: 0, size: 100, sort: 'createdAt,desc' })
      .then((res) => {
        setPage(res);
        setError('');
      })
      .catch(() => setError('Could not load investigations. Check the backend is running.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  if (loading) {
    return (
      <div className="space-y-8 min-w-0">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/[0.08] pb-6">
          <div className="space-y-2">
            <Skeleton className="h-8 w-72 rounded-xl" />
            <Skeleton className="h-4 w-48 rounded-lg" />
          </div>
          <Skeleton className="h-11 w-44 rounded-xl" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
          {Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              className="rounded-2xl border border-white/[0.08] bg-[#0c101c]/90 p-5 space-y-3.5 shadow-card"
            >
              <Skeleton className="h-3 w-28 rounded-md" />
              <Skeleton className="h-9 w-16 rounded-lg" />
              <Skeleton className="h-3 w-32 rounded-md" />
            </div>
          ))}
        </div>
        <Card title="Recent Investigations">
          <div className="space-y-3 py-2">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between py-4 border-b border-white/[0.06] last:border-0">
                <div className="space-y-1.5">
                  <Skeleton className="h-4.5 w-56 rounded-md" />
                  <Skeleton className="h-3 w-28 rounded-md" />
                </div>
                <Skeleton className="h-6 w-24 rounded-full" />
              </div>
            ))}
          </div>
        </Card>
      </div>
    );
  }

  const content = page?.content || [];
  const totalElements = page?.totalElements ?? 0;
  const stats = deriveStats(content, totalElements);
  const recent = content.slice(0, 8);

  return (
    <div className="space-y-8 min-w-0">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-5 border-b border-white/[0.08] pb-6 min-w-0">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            Welcome back,{' '}
            <span className="bg-gradient-to-r from-blue-400 to-indigo-300 bg-clip-text text-transparent">
              {user?.fullName?.split(' ')[0] || 'Analyst'}
            </span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Digital threat intelligence overview and real-time active cases
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/investigations/new">
            <Button className="flex items-center gap-2 font-semibold shadow-lg">
              <PlusCircle className="w-4 h-4" />
              <span>New Investigation</span>
            </Button>
          </Link>
        </div>
      </div>

      {error && (
        <ErrorState
          title="Failed to load cases"
          message={error}
          onRetry={fetchDashboard}
        />
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
        <StatCard
          icon={FolderSearch}
          label="Total Investigations"
          value={stats.total}
          accent="accent"
          sub={`${stats.completed} closed dossier${stats.completed === 1 ? '' : 's'}`}
        />
        <StatCard
          icon={CheckCircle2}
          label="Completed"
          value={stats.completed}
          accent="emerald"
          sub={stats.total ? `${Math.round((stats.completed / (stats.total || 1)) * 100)}% resolution rate` : 'No cases yet'}
        />
        <StatCard
          icon={ShieldAlert}
          label="High-Risk Cases"
          value={stats.highRisk}
          accent="rose"
          sub={stats.highRisk === 0 ? 'No active high-risk alerts' : 'Critical review recommended'}
        />
        <StatCard
          icon={FileStack}
          label="Evidence Items"
          value={stats.evidenceTotal}
          accent="cyan"
          sub="Analyzed indicators & payloads"
        />
      </div>

      <Card
        title="Recent Investigations"
        subtitle="Chronological list of all active and completed threat cases"
        headerAction={
          totalElements > 0 && (
            <Link
              to="/investigations"
              className="text-xs font-semibold text-blue-400 hover:text-blue-300 inline-flex items-center gap-1 transition-colors group"
            >
              <span>View all cases</span>
              <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          )
        }
      >
        {recent.length === 0 ? (
          <EmptyState
            icon={FolderSearch}
            title="No investigations yet"
            message="Create your first case to start analysing suspicious communications, links, and documents."
            action={
              <Link to="/investigations/new">
                <Button variant="outline" size="sm" className="font-semibold shadow-sm">
                  Create Investigation
                </Button>
              </Link>
            }
          />
        ) : (
          <div className="divide-y divide-white/[0.06] -mx-5 sm:-mx-6">
            {recent.map((inv) => (
              <Link
                key={inv.id}
                to={`/investigations/${inv.id}`}
                className="flex items-center justify-between gap-4 px-5 sm:px-6 py-4.5 hover:bg-white/[0.04] transition-all duration-150 group"
              >
                <div className="min-w-0 flex-1 space-y-1">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <p className="text-sm font-semibold text-slate-100 truncate group-hover:text-blue-300 transition-colors">
                      {inv.title}
                    </p>
                    {inv.category && <CategoryBadge category={inv.category} />}
                  </div>
                  <div className="flex items-center gap-3 text-xs text-slate-400 font-mono">
                    <span className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      {formatRelative(inv.createdAt)}
                    </span>
                    <span className="text-slate-600">•</span>
                    <span>{inv.evidenceCount || 0} evidence item{inv.evidenceCount === 1 ? '' : 's'}</span>
                  </div>
                </div>

                <div className="flex items-center gap-3 shrink-0">
                  <StatusBadge status={inv.status} />
                  {inv.overallRiskLevel && inv.overallRiskLevel !== 'UNKNOWN' && (
                    <RiskBadge level={inv.overallRiskLevel} className="hidden sm:inline-flex" />
                  )}
                  <ChevronRight className="w-4 h-4 text-slate-400 group-hover:text-slate-200 group-hover:translate-x-1 transition-all" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};

export default DashboardPage;
