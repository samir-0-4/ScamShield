import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  FileSearch,
  FileText,
  GitBranch,
  Network,
  ShieldCheck,
  Trash2,
  Calendar,
  Hash,
  Image,
  Link2,
  Mail,
  Paperclip,
  Clock,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import { StatusBadge, RiskBadge, CategoryBadge } from '../components/ui/StatusBadge';
import { FullPageSpinner, Spinner } from '../components/ui/Spinner';
import { getApiErrorMessage } from '../services/api/apiClient';
import evidenceService from '../services/evidence/evidenceService';
import { investigationService } from '../services/investigation/investigationService';
import { formatDateTime, formatRelative } from '../utils/format';

const evidenceIcons = {
  TEXT_MESSAGE: FileText,
  URL: Link2,
  EMAIL: Mail,
  IMAGE: Image,
  PDF: Paperclip,
};

export const InvestigationDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [investigation, setInvestigation] = useState(null);
  const [evidence, setEvidence] = useState([]);
  const [evidenceLoading, setEvidenceLoading] = useState(true);
  const [evidenceError, setEvidenceError] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    setLoading(true);
    investigationService
      .getById(id)
      .then((inv) => {
        setInvestigation(inv);
        setError('');
      })
      .catch((err) => {
        const status = err?.response?.status;
        if (status === 404) setError('Investigation not found.');
        else if (status === 403) setError("You don't have permission to view this investigation.");
        else setError('Failed to load investigation. Please try again.');
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    setEvidenceLoading(true);
    evidenceService
      .list(id)
      .then((items) => {
        setEvidence(items || []);
        setEvidenceError('');
      })
      .catch((err) => setEvidenceError(getApiErrorMessage(err, 'Could not load evidence.')))
      .finally(() => setEvidenceLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('Delete this investigation? This cannot be undone.')) return;
    setDeleting(true);
    try {
      await investigationService.remove(id);
      navigate('/investigations', { replace: true });
    } catch {
      setError('Failed to delete the investigation.');
      setDeleting(false);
    }
  };

  if (loading) return <FullPageSpinner label="Loading investigation dossier…" />;

  if (error) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4 min-w-0">
        <p className="text-slate-300 font-medium">{error}</p>
        <Link to="/investigations">
          <Button variant="secondary" size="sm" className="inline-flex items-center gap-2 mx-auto">
            <ArrowLeft className="w-4 h-4" /> Back to investigations
          </Button>
        </Link>
      </div>
    );
  }

  const inv = investigation;

  return (
    <div className="space-y-6 max-w-5xl mx-auto min-w-0">
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-5 border-b border-white/[0.08] pb-6 min-w-0">
        <div className="min-w-0 flex-1 space-y-2">
          <Link
            to="/investigations"
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white transition-colors font-mono mb-1"
          >
            <ArrowLeft className="w-3.5 h-3.5" /> All investigations
          </Link>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white truncate">
            {inv.title}
          </h1>
          <div className="flex flex-wrap items-center gap-2.5 pt-1">
            <StatusBadge status={inv.status} />
            <RiskBadge level={inv.overallRiskLevel} />
            {inv.category && <CategoryBadge category={inv.category} />}
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5 shrink-0">
          <Link to={`/investigations/${id}/analysis`}>
            <Button variant="secondary" size="sm" className="inline-flex items-center gap-2 font-semibold shadow-sm">
              <FileSearch className="w-4 h-4 text-cyan-400" />
              <span>Add Evidence</span>
            </Button>
          </Link>
          <Link to={`/investigations/${id}/results`}>
            <Button size="sm" className="inline-flex items-center gap-2 font-semibold shadow-md">
              <ShieldCheck className="w-4 h-4" />
              <span>View Results</span>
            </Button>
          </Link>
          <Link to={`/investigations/${id}/graph`}>
            <Button variant="outline" size="sm" className="inline-flex items-center gap-2 font-medium">
              <GitBranch className="w-4 h-4" />
              <span>Graph</span>
            </Button>
          </Link>
          <Link to={`/investigations/${id}/campaign`}>
            <Button variant="outline" size="sm" className="inline-flex items-center gap-2 font-medium text-cyan-300 hover:text-cyan-200">
              <Network className="w-4 h-4 text-cyan-400" />
              <span>Campaign</span>
            </Button>
          </Link>
          <Link to={`/investigations/${id}/report`}>
            <Button variant="outline" size="sm" className="inline-flex items-center gap-2 font-medium">
              <FileText className="w-4 h-4" />
              <span>Report</span>
            </Button>
          </Link>
          <Button
            variant="danger"
            size="sm"
            loading={deleting}
            onClick={handleDelete}
            title="Delete investigation"
            aria-label="Delete investigation"
          >
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start min-w-0">
        <div className="lg:col-span-2 space-y-6 min-w-0">
          {inv.description && (
            <Card title="Case Description & Context" className="shadow-card">
              <p className="text-xs sm:text-sm text-slate-300 leading-relaxed whitespace-pre-wrap font-sans">
                {inv.description}
              </p>
            </Card>
          )}

          <Card
            title={`Evidence dossier${evidence.length ? ` (${evidence.length})` : ''}`}
            subtitle="Submitted digital indicators, messages, and document payloads"
            headerAction={
              <Link to={`/investigations/${id}/analysis`}>
                <Button variant="outline" size="xs" className="text-2xs font-mono font-semibold">
                  + Ingest evidence
                </Button>
              </Link>
            }
          >
            {evidenceLoading ? (
              <div className="flex justify-center py-8">
                <Spinner className="h-6 w-6 text-cyan-400" label="Loading evidence…" />
              </div>
            ) : evidenceError ? (
              <p role="alert" className="py-4 text-xs sm:text-sm text-rose-300 font-medium">
                {evidenceError}
              </p>
            ) : evidence.length === 0 ? (
              <div className="py-10 text-center text-slate-400 space-y-3">
                <div className="mx-auto w-12 h-12 rounded-2xl bg-white/[0.04] border border-white/[0.08] flex items-center justify-center text-slate-400">
                  <FileSearch className="h-6 w-6 text-slate-400" />
                </div>
                <div className="space-y-1">
                  <p className="text-xs sm:text-sm font-semibold text-slate-200">No evidence submitted yet.</p>
                  <p className="text-2xs text-slate-400">Add messages, URLs, or files to begin analysis.</p>
                </div>
                <Link to={`/investigations/${id}/analysis`}>
                  <Button variant="outline" size="xs" className="mt-2 font-semibold">
                    Add first evidence item
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="divide-y divide-white/[0.06] -mx-5 sm:-mx-6">
                {evidence.map((item) => {
                  const Icon = evidenceIcons[item.evidenceType] || Paperclip;
                  const label =
                    item.originalFilename ||
                    item.url ||
                    item.subject ||
                    item.textContent ||
                    item.extractedContent ||
                    'Evidence item';
                  return (
                    <div
                      key={item.id}
                      className="flex items-start gap-3.5 px-5 sm:px-6 py-4.5 hover:bg-white/[0.04] transition-all duration-150 group"
                    >
                      <div className="mt-0.5 rounded-xl border border-cyan-500/25 bg-cyan-500/10 p-2.5 text-cyan-400 shrink-0 shadow-sm group-hover:scale-105 transition-transform">
                        <Icon className="h-4 w-4" />
                      </div>
                      <div className="min-w-0 flex-1 space-y-1">
                        <div className="flex items-center gap-2">
                          <p className="text-xs font-semibold text-slate-200">
                            {item.evidenceType?.replaceAll('_', ' ') || 'Evidence'}
                          </p>
                          {item.status && (
                            <span className="text-2xs font-mono uppercase px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-white/[0.08]">
                              {item.status}
                            </span>
                          )}
                        </div>
                        <p className="truncate text-xs text-slate-400 font-mono" title={label}>
                          {label}
                        </p>
                        <p className="text-2xs text-slate-400 font-mono">
                          {formatRelative(item.createdAt)}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </Card>
        </div>

        <div className="space-y-5 min-w-0">
          <Card title="Case metadata" className="shadow-card">
            <dl className="space-y-3.5 text-xs sm:text-sm">
              <div className="flex justify-between items-center gap-2 py-2 border-b border-white/[0.06]">
                <dt className="text-slate-400 flex items-center gap-1.5 font-medium">
                  <Hash className="w-3.5 h-3.5 text-slate-400" /> ID
                </dt>
                <dd
                  className="font-mono text-2xs text-slate-300 truncate max-w-[140px] px-2 py-0.5 rounded bg-black/40 border border-white/[0.06]"
                  title={inv.id}
                >
                  {inv.id}
                </dd>
              </div>

              <div className="flex justify-between items-center gap-2 py-2 border-b border-white/[0.06]">
                <dt className="text-slate-400 font-medium">Status</dt>
                <dd>
                  <StatusBadge status={inv.status} />
                </dd>
              </div>

              <div className="flex justify-between items-center gap-2 py-2 border-b border-white/[0.06]">
                <dt className="text-slate-400 font-medium">Risk Assessment</dt>
                <dd>
                  <RiskBadge level={inv.overallRiskLevel} />
                </dd>
              </div>

              <div className="flex justify-between items-center gap-2 py-2 border-b border-white/[0.06]">
                <dt className="text-slate-400 font-medium">Risk Score</dt>
                <dd className="font-mono font-bold text-slate-100 text-sm">
                  {inv.riskScore != null ? `${inv.riskScore}/100` : '—'}
                </dd>
              </div>

              <div className="flex justify-between items-center gap-2 py-2 border-b border-white/[0.06]">
                <dt className="text-slate-400 font-medium">Evidence Count</dt>
                <dd className="font-mono font-semibold text-slate-200">{inv.evidenceCount || 0} items</dd>
              </div>

              <div className="flex flex-col gap-1 py-2 border-b border-white/[0.06] font-mono text-xs">
                <dt className="text-slate-400 flex items-center gap-1.5 font-sans font-medium">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" /> Created
                </dt>
                <dd className="text-slate-300">{formatDateTime(inv.createdAt)}</dd>
              </div>

              <div className="flex flex-col gap-1 py-2 font-mono text-xs">
                <dt className="text-slate-400 flex items-center gap-1.5 font-sans font-medium">
                  <Clock className="w-3.5 h-3.5 text-slate-400" /> Last updated
                </dt>
                <dd className="text-slate-300">{formatRelative(inv.updatedAt)}</dd>
              </div>

              {inv.completedAt && (
                <div className="flex flex-col gap-1 py-2 border-t border-white/[0.06] font-mono text-xs">
                  <dt className="text-slate-400 font-sans font-medium">Completed at</dt>
                  <dd className="text-slate-300">{formatDateTime(inv.completedAt)}</dd>
                </div>
              )}
            </dl>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default InvestigationDetailsPage;
