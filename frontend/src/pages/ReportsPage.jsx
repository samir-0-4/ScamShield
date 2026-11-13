import { useEffect, useState } from 'react';
import { FileText, Info, ArrowRight, AlertCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import EmptyState from '../components/ui/EmptyState';
import { FullPageSpinner } from '../components/ui/Spinner';
import { investigationService } from '../services/investigation/investigationService';
import { getApiErrorMessage } from '../services/api/apiClient';
import { formatDate, titleCase } from '../utils/format';

export const ReportsPage = () => {
  const [investigations, setInvestigations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    investigationService
      .list({ size: 50 })
      .then((page) => setInvestigations(page?.content || []))
      .catch((err) => setError(getApiErrorMessage(err, 'Could not load investigations.')))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <FullPageSpinner label="Loading investigations…" />;

  return (
    <div className="mx-auto max-w-4xl space-y-6 min-w-0">
      <div className="border-b border-white/[0.08] pb-6 min-w-0">
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
          Investigation Reports
        </h1>
        <p className="mt-1 text-xs sm:text-sm text-slate-400">
          Generate and print formal forensic reports from each case&apos;s persisted evidence and analysis.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-xs sm:text-sm text-rose-300 flex items-start gap-2.5 shadow-sm"
        >
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {!error && investigations.length === 0 ? (
        <Card className="shadow-card">
          <EmptyState
            icon={FileText}
            title="No investigations yet"
            message="Create an investigation before generating a report."
            action={
              <Link to="/investigations/new">
                <Button variant="outline" size="sm" className="font-semibold shadow-sm">
                  Create Investigation
                </Button>
              </Link>
            }
          />
        </Card>
      ) : (
        <div className="space-y-3.5">
          {investigations.map((investigation) => (
            <Card key={investigation.id} padded={false} interactive className="overflow-hidden shadow-card">
              <Link
                to={`/investigations/${investigation.id}/report`}
                className="flex items-center justify-between gap-4 p-5 hover:bg-white/[0.04] transition-all duration-150 group"
              >
                <div className="min-w-0 flex-1 space-y-1">
                  <h2 className="font-semibold text-slate-100 text-sm sm:text-base group-hover:text-cyan-300 transition-colors truncate">
                    {investigation.title}
                  </h2>
                  <p className="text-xs text-slate-400 font-mono">
                    {titleCase(investigation.category)} · {formatDate(investigation.createdAt)} ·{' '}
                    {investigation.evidenceCount || 0} evidence items
                  </p>
                </div>
                <div className="flex items-center gap-2 text-xs font-semibold text-cyan-400 group-hover:text-cyan-300 shrink-0 font-mono">
                  <span>View report</span>
                  <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </div>
              </Link>
            </Card>
          ))}
        </div>
      )}

      <div className="flex items-start gap-3.5 rounded-2xl border border-cyan-500/25 bg-cyan-500/10 p-4.5 text-slate-300 shadow-sm">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-cyan-400" />
        <p className="text-xs leading-relaxed">
          Reports preserve unknown and unavailable information explicitly according to the
          PhantomTrace Epistemic Standard. Use the report page&apos;s Print / Save PDF action for browser-based export.
        </p>
      </div>
    </div>
  );
};

export default ReportsPage;
