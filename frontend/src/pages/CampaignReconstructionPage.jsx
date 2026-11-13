import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Network,
  Share2,
  ShieldAlert,
  ArrowLeft,
  GitBranch,
  FileText,
  Info,
  Layers,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import { Spinner } from '../components/ui/Spinner';
import { ErrorState } from '../components/ui/ErrorState';
import { EmptyState } from '../components/ui/EmptyState';
import { StatusBadge } from '../components/ui/StatusBadge';
import investigationService from '../services/investigation/investigationService';

export const CampaignReconstructionPage = () => {
  const { id } = useParams();
  const [investigation, setInvestigation] = useState(null);
  const [campaignData, setCampaignData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    const fetchCampaign = async () => {
      try {
        setLoading(true);
        setError('');
        const [caseRes, campaignRes] = await Promise.all([
          investigationService.getById(id),
          investigationService.getCampaign(id),
        ]);
        if (mounted) {
          setInvestigation(caseRes);
          setCampaignData(campaignRes);
        }
      } catch (err) {
        if (mounted) {
          setError(err.response?.data?.message || err.message || 'Failed to load campaign reconstruction');
        }
      } finally {
        if (mounted) setLoading(false);
      }
    };
    fetchCampaign();
    return () => {
      mounted = false;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="flex h-96 flex-col items-center justify-center gap-3">
        <Spinner size="lg" />
        <p className="font-mono text-xs uppercase tracking-widest text-slate-400">
          Reconstructing Campaign Signatures…
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <ErrorState
          title="Campaign Reconstruction Unavailable"
          message={error}
          onRetry={() => window.location.reload()}
        />
      </div>
    );
  }

  const sharedIndicators = campaignData?.sharedIndicators || [];
  const campaignSignals = campaignData?.campaignSignals || [];

  return (
    <div className="space-y-8 pb-12">
      {/* Navigation header */}
      <div className="flex flex-col gap-4 border-b border-white/[0.06] pb-6 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Link
            to={`/investigations/${id}`}
            className="flex h-9 w-9 items-center justify-center rounded-xl border border-white/10 bg-[#0e121e] text-slate-400 transition hover:border-cyan-500/40 hover:text-white"
          >
            <ArrowLeft className="h-4 w-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-2xs uppercase tracking-widest text-cyan-400">
                Investigation Dossier
              </span>
              <span className="text-slate-600">/</span>
              <span className="font-mono text-2xs text-slate-400">Case #{id?.slice(0, 8)}</span>
            </div>
            <h1 className="text-xl font-bold tracking-tight text-white sm:text-2xl">
              {investigation?.title || 'Campaign Reconstruction'}
            </h1>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {investigation?.status && <StatusBadge status={investigation.status} />}
          <Link to={`/investigations/${id}/graph`}>
            <Button variant="secondary" size="sm" className="gap-2">
              <GitBranch className="h-3.5 w-3.5" />
              Evidence Graph
            </Button>
          </Link>
          <Link to={`/investigations/${id}/report`}>
            <Button variant="secondary" size="sm" className="gap-2">
              <FileText className="h-3.5 w-3.5" />
              Audit Report
            </Button>
          </Link>
        </div>
      </div>

      {/* Feature banner */}
      <div className="relative overflow-hidden rounded-2xl border border-cyan-500/20 bg-gradient-to-r from-cyan-950/40 via-purple-950/20 to-transparent p-6 shadow-xl">
        <div className="flex items-start gap-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border border-cyan-500/30 bg-cyan-500/10 text-cyan-400 shadow-inner">
            <Network className="h-6 w-6" />
          </div>
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <h2 className="text-base font-bold text-white sm:text-lg">
                Scam Campaign Footprint Reconstruction
              </h2>
              <Badge variant="cyan" size="sm">
                Proprietary Correlation
              </Badge>
            </div>
            <p className="max-w-3xl text-xs leading-relaxed text-slate-300 sm:text-sm">
              PhantomTrace correlates indicators across multiple fragmented evidence artifacts — messages,
              domains, redirects, sender identities, and payment channels — to reconstruct the overarching
              scam campaign and establish attacker attribution with epistemic confidence tags.
            </p>
          </div>
        </div>
      </div>

      {/* Campaign Signals Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <ShieldAlert className="h-4 w-4 text-cyan-400" />
            <h3 className="font-mono text-xs uppercase tracking-wider text-slate-300">
              Campaign Detection Signals ({campaignSignals.length})
            </h3>
          </div>
          <span className="font-mono text-2xs text-slate-500">
            Epistemic: PROBABLE_CORRELATION
          </span>
        </div>

        {campaignSignals.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2">
            {campaignSignals.map((signal, idx) => (
              <Card
                key={idx}
                className="relative flex flex-col justify-between border-white/[0.08] bg-[#0c101c]/90 p-5 shadow-lg"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-2xs font-semibold uppercase tracking-wider text-slate-400">
                      Signal #{idx + 1}
                    </span>
                    <Badge
                      variant={signal.confidence === 'PROBABLE' ? 'danger' : 'warning'}
                      size="sm"
                    >
                      {signal.confidence} CONFIDENCE
                    </Badge>
                  </div>
                  <p className="text-sm font-medium text-slate-200">{signal.description}</p>
                </div>

                <div className="mt-4 flex items-center justify-between border-t border-white/[0.05] pt-3 font-mono text-2xs text-slate-400">
                  <span>Linked Artifacts:</span>
                  <span className="font-semibold text-cyan-400">
                    {signal.supportingEvidenceIds?.length || 0} Evidence Items
                  </span>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="border-white/[0.06] bg-[#0c101c]/50 p-6 text-center">
            <Info className="mx-auto mb-2 h-6 w-6 text-slate-500" />
            <p className="text-sm text-slate-400">
              No aggregated multi-artifact signals identified yet.
            </p>
          </Card>
        )}
      </div>

      {/* Shared Indicators Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Share2 className="h-4 w-4 text-purple-400" />
            <h3 className="font-mono text-xs uppercase tracking-wider text-slate-300">
              Shared Campaign Indicators ({sharedIndicators.length})
            </h3>
          </div>
          <span className="font-mono text-2xs text-slate-500">
            Requires ≥2 Distinct Evidence Items
          </span>
        </div>

        {sharedIndicators.length > 0 ? (
          <div className="overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0c101c]/90 shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left font-sans text-xs">
                <thead>
                  <tr className="border-b border-white/[0.08] bg-white/[0.02] font-mono text-2xs uppercase tracking-wider text-slate-400">
                    <th className="py-3.5 pl-6 pr-4">Indicator Value</th>
                    <th className="px-4 py-3.5">Type</th>
                    <th className="px-4 py-3.5 text-center">Evidence Crossings</th>
                    <th className="py-3.5 pl-4 pr-6 text-right">Verification Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/[0.05]">
                  {sharedIndicators.map((item, idx) => (
                    <tr key={idx} className="transition-colors hover:bg-white/[0.02]">
                      <td className="py-4 pl-6 pr-4 font-mono font-medium text-cyan-300">
                        {item.value}
                      </td>
                      <td className="px-4 py-4">
                        <Badge variant="secondary" size="sm">
                          {item.type}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-center font-mono">
                        <span className="inline-flex items-center gap-1.5 rounded-full border border-purple-500/20 bg-purple-500/10 px-2.5 py-0.5 text-2xs font-semibold text-purple-300">
                          <Layers className="h-3 w-3" />
                          {item.evidenceCount} Items
                        </span>
                      </td>
                      <td className="py-4 pl-4 pr-6 text-right">
                        <Badge variant="cyan" size="sm">
                          DETERMINISTIC
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <EmptyState
            icon={Network}
            title="No Shared Campaign Footprint Yet"
            description="Campaign reconstruction correlates indicators across at least two distinct evidence submissions. Add more evidence items to this investigation to reveal shared attacker infrastructure."
            action={
              <Link to={`/investigations/${id}/analysis`}>
                <Button size="sm" className="gap-2">
                  <Share2 className="h-4 w-4" />
                  Add Evidence to Investigate
                </Button>
              </Link>
            }
          />
        )}
      </div>
    </div>
  );
};

export default CampaignReconstructionPage;
