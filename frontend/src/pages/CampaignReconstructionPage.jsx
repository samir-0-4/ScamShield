import { useState, useEffect, useCallback } from 'react';
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
  FolderSearch,
  ExternalLink,
  Fingerprint,
  Sparkles,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import { Spinner } from '../components/ui/Spinner';
import { ErrorState } from '../components/ui/ErrorState';
import { EmptyState } from '../components/ui/EmptyState';
import { StatusBadge, CategoryBadge } from '../components/ui/StatusBadge';
import investigationService from '../services/investigation/investigationService';

const classificationStyles = {
  SYNTHETIC_DEMO_CAMPAIGN: {
    border: 'border-purple-500/40 bg-gradient-to-r from-purple-950/50 via-cyan-950/30 to-[#0c101c]',
    badgeVariant: 'purple',
    badgeText: 'SYNTHETIC DEMO CAMPAIGN',
    iconColor: 'text-purple-400',
    description: 'Simulated demonstration campaign using RFC 2606 (.test) and RFC 5737 (TEST-NET) test fixtures. Demonstrates multi-case correlation without real-world threat attribution.',
  },
  CONFIRMED: {
    border: 'border-rose-500/40 bg-gradient-to-r from-rose-950/50 via-purple-950/30 to-[#0c101c]',
    badgeVariant: 'danger',
    badgeText: 'CONFIRMED CAMPAIGN',
    iconColor: 'text-rose-400',
    description: 'Shared infrastructure is confirmed malicious by external threat intelligence or identical cryptographic payload matches.',
  },
  PROBABLE: {
    border: 'border-amber-500/40 bg-gradient-to-r from-amber-950/40 via-purple-950/25 to-[#0c101c]',
    badgeVariant: 'warning',
    badgeText: 'PROBABLE CAMPAIGN',
    iconColor: 'text-amber-400',
    description: 'Multiple distinct indicators (e.g. domain, IP, sender identity) converge across independent evidence items or investigations.',
  },
  POSSIBLE: {
    border: 'border-cyan-500/30 bg-gradient-to-r from-cyan-950/35 via-slate-900/40 to-[#0c101c]',
    badgeVariant: 'cyan',
    badgeText: 'POSSIBLE CAMPAIGN',
    iconColor: 'text-cyan-400',
    description: 'Single indicator overlap or heuristic correlation observed across artifacts; further evidence required for attribution.',
  },
  ISOLATED_INCIDENT: {
    border: 'border-white/[0.08] bg-[#0c101c]/90',
    badgeVariant: 'secondary',
    badgeText: 'ISOLATED INCIDENT',
    iconColor: 'text-slate-400',
    description: 'No cross-evidence or cross-investigation footprint identified. The submitted evidence appears isolated.',
  },
};

export const CampaignReconstructionPage = () => {
  const { id } = useParams();
  const [investigation, setInvestigation] = useState(null);
  const [campaignData, setCampaignData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [seedingDemo, setSeedingDemo] = useState(false);

  const fetchCampaign = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const [caseRes, campaignRes] = await Promise.all([
        investigationService.getById(id),
        investigationService.getCampaign(id),
      ]);
      setInvestigation(caseRes);
      setCampaignData(campaignRes);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load campaign reconstruction');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchCampaign();
  }, [fetchCampaign]);

  const handleSeedDemo = async () => {
    setSeedingDemo(true);
    try {
      await investigationService.seedDemoCampaign();
      await fetchCampaign();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load demo campaign.');
    } finally {
      setSeedingDemo(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 flex-col items-center justify-center gap-3">
        <Spinner size="lg" />
        <p className="font-mono text-xs uppercase tracking-widest text-slate-400">
          Reconstructing Campaign Footprint & Cross-Case Attribution…
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
          onRetry={fetchCampaign}
        />
      </div>
    );
  }

  const sharedIndicators = campaignData?.sharedIndicators || [];
  const campaignSignals = campaignData?.campaignSignals || [];
  const connectedInvestigations = campaignData?.connectedInvestigations || [];
  const classification = campaignData?.campaignClassification || (sharedIndicators.length > 0 ? 'POSSIBLE' : 'ISOLATED_INCIDENT');
  const attributionSummary = campaignData?.attributionSummary || 'Heuristic correlation completed across persisted records.';
  const classStyle = classificationStyles[classification] || classificationStyles.ISOLATED_INCIDENT;

  return (
    <div className="space-y-8 pb-12 min-w-0">
      {/* Navigation header */}
      <div className="flex flex-col gap-4 border-b border-white/[0.08] pb-6 sm:flex-row sm:items-center sm:justify-between min-w-0">
        <div className="flex items-center gap-3 min-w-0">
          <Link
            to={`/investigations/${id}`}
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-[#0e121e] text-slate-400 transition hover:border-cyan-500/40 hover:text-white"
          >
            <ArrowLeft className="h-4 w-4" />
          </Link>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-mono text-2xs uppercase tracking-widest text-cyan-400">
                Investigation Dossier
              </span>
              <span className="text-slate-600">/</span>
              <span className="font-mono text-2xs text-slate-400 truncate">Case #{id?.slice(0, 8)}</span>
            </div>
            <h1 className="text-xl font-bold tracking-tight text-white sm:text-2xl truncate">
              {investigation?.title || 'Campaign Reconstruction'}
            </h1>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {investigation?.status && <StatusBadge status={investigation.status} />}
          <Link to={`/investigations/${id}/graph`}>
            <Button variant="secondary" size="sm" className="gap-2 font-semibold">
              <GitBranch className="h-3.5 w-3.5 text-cyan-400" />
              Evidence Graph
            </Button>
          </Link>
          <Link to={`/investigations/${id}/report`}>
            <Button variant="secondary" size="sm" className="gap-2 font-semibold">
              <FileText className="h-3.5 w-3.5 text-slate-300" />
              Audit Report
            </Button>
          </Link>
        </div>
      </div>

      {/* Signature Campaign Classification Banner */}
      <div className={`relative overflow-hidden rounded-2xl border p-6 shadow-2xl ${classStyle.border}`}>
        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="flex items-start gap-4">
            <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-black/40 ${classStyle.iconColor} shadow-inner`}>
              <Network className="h-6 w-6" />
            </div>
            <div className="space-y-2">
              <div className="flex flex-wrap items-center gap-2.5">
                <h2 className="text-base font-bold text-white sm:text-lg">
                  Scam Campaign Footprint Reconstruction
                </h2>
                <Badge variant={classStyle.badgeVariant} size="sm">
                  {classStyle.badgeText}
                </Badge>
              </div>
              <p className="max-w-3xl text-xs leading-relaxed text-slate-300 sm:text-sm">
                {classStyle.description}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3 shrink-0 rounded-xl bg-black/40 border border-white/[0.08] p-3 font-mono text-xs">
            <div className="text-center px-3 border-r border-white/10">
              <p className="text-2xs text-slate-400 uppercase">Connected Cases</p>
              <p className="text-lg font-bold text-cyan-300">{connectedInvestigations.length}</p>
            </div>
            <div className="text-center px-3 border-r border-white/10">
              <p className="text-2xs text-slate-400 uppercase">Shared Indicators</p>
              <p className="text-lg font-bold text-purple-300">{sharedIndicators.length}</p>
            </div>
            <div className="text-center px-3">
              <p className="text-2xs text-slate-400 uppercase">Verification</p>
              <p className={`text-xs font-bold ${classification === 'CONFIRMED' ? 'text-rose-400' : classification === 'SYNTHETIC_DEMO_CAMPAIGN' ? 'text-purple-400' : 'text-slate-300'}`}>
                {classification === 'CONFIRMED' ? 'EXTERNAL_INTEL' : classification === 'SYNTHETIC_DEMO_CAMPAIGN' ? 'SYNTHETIC_FIXTURE' : 'DETERMINISTIC'}
              </p>
            </div>
          </div>
        </div>

        {/* Forensic Attribution Narrative */}
        <div className="mt-5 border-t border-white/[0.08] pt-4">
          <p className="font-mono text-2xs uppercase tracking-wider text-slate-400 flex items-center gap-1.5 mb-1.5">
            <Fingerprint className="h-3.5 w-3.5 text-cyan-400" />
            Attribution &amp; Relationship Narrative
          </p>
          <p className="text-xs leading-relaxed text-slate-200 font-sans bg-black/30 p-3.5 rounded-xl border border-white/[0.05]">
            {attributionSummary}
          </p>
        </div>
      </div>

      {/* Connected Investigations Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <FolderSearch className="h-4 w-4 text-cyan-400" />
            <h3 className="font-mono text-xs uppercase tracking-wider text-slate-300">
              Cross-Investigation Campaign Dossiers ({connectedInvestigations.length})
            </h3>
          </div>
          <span className="font-mono text-2xs text-slate-500">
            Isolated by Tenant Boundaries
          </span>
        </div>

        {connectedInvestigations.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {connectedInvestigations.map((inv) => (
              <Card
                key={inv.id}
                className="relative flex flex-col justify-between border-white/[0.08] bg-[#0c101c]/90 p-5 shadow-lg hover:border-cyan-500/30 transition-all group"
              >
                <div className="space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <CategoryBadge category={inv.category} />
                      <Badge variant={inv.confidence === 'CONFIRMED' ? 'danger' : inv.confidence === 'SYNTHETIC_DEMO' ? 'purple' : 'warning'} size="xs">
                        {inv.confidence === 'SYNTHETIC_DEMO' ? 'SYNTHETIC DEMO' : inv.confidence}
                      </Badge>
                    </div>
                    <span className="font-mono text-2xs text-slate-500">
                      {inv.sharedIndicatorCount} Shared
                    </span>
                  </div>

                  <div>
                    <h4 className="text-sm font-bold text-white group-hover:text-cyan-300 transition-colors">
                      {inv.title}
                    </h4>
                    <p className="mt-1 text-2xs text-slate-400 line-clamp-2">
                      {inv.relationshipExplanation}
                    </p>
                  </div>

                  {inv.sharedIndicatorValues?.length > 0 && (
                    <div className="flex flex-wrap gap-1 pt-1">
                      {inv.sharedIndicatorValues.slice(0, 3).map((val, idx) => (
                        <span
                          key={idx}
                          className="font-mono text-2xs px-2 py-0.5 rounded-md bg-purple-500/10 text-purple-300 border border-purple-500/20 truncate max-w-[200px]"
                        >
                          {val}
                        </span>
                      ))}
                      {inv.sharedIndicatorValues.length > 3 && (
                        <span className="font-mono text-2xs text-slate-500 px-1">
                          +{inv.sharedIndicatorValues.length - 3} more
                        </span>
                      )}
                    </div>
                  )}
                </div>

                <div className="mt-4 border-t border-white/[0.06] pt-3 flex items-center justify-between">
                  <span className="font-mono text-2xs text-slate-500">
                    Status: {inv.status}
                  </span>
                  <Link
                    to={`/investigations/${inv.id}`}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-cyan-400 hover:text-cyan-300 transition-colors"
                  >
                    <span>Inspect Case</span>
                    <ExternalLink className="h-3 w-3" />
                  </Link>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="border-white/[0.06] bg-[#0c101c]/50 p-6 text-center">
            <Info className="mx-auto mb-2 h-6 w-6 text-slate-500" />
            <p className="text-sm text-slate-400">
              No other investigations in your organization share indicators with this case.
            </p>
            <p className="mt-1 text-2xs text-slate-500">
              Submit related phishing emails or SMS alerts, or load the synthetic demo dataset to evaluate multi-case campaign correlation.
            </p>
          </Card>
        )}
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
            Epistemic: DETERMINISTIC_CORRELATION
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
                      variant={signal.confidence === 'CONFIRMED' ? 'danger' : signal.confidence === 'SYNTHETIC_DEMO_CAMPAIGN' ? 'purple' : signal.confidence === 'PROBABLE' ? 'warning' : 'cyan'}
                      size="sm"
                    >
                      {signal.confidence.replace(/_/g, ' ')}
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
            Requires Evidence or Cross-Case Convergence
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
                    <th className="px-4 py-3.5">Scope</th>
                    <th className="px-4 py-3.5 text-center">Crossings</th>
                    <th className="py-3.5 pl-4 pr-6 text-right">Verification Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/[0.05]">
                  {sharedIndicators.map((item, idx) => (
                    <tr key={idx} className="transition-colors hover:bg-white/[0.02]">
                      <td className="py-4 pl-6 pr-4 font-mono font-medium text-cyan-300 break-all max-w-xs">
                        {item.value}
                      </td>
                      <td className="px-4 py-4">
                        <Badge variant="secondary" size="sm">
                          {item.type}
                        </Badge>
                      </td>
                      <td className="px-4 py-4">
                        <span className={`inline-flex items-center gap-1 font-mono text-2xs px-2 py-0.5 rounded-full border ${item.crossInvestigation ? 'border-rose-500/30 bg-rose-500/10 text-rose-300' : 'border-cyan-500/20 bg-cyan-500/10 text-cyan-300'}`}>
                          {item.crossInvestigation ? 'Multi-Case Campaign' : 'Single-Case Evidence'}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-center font-mono">
                        <span className="inline-flex items-center gap-1.5 rounded-full border border-purple-500/20 bg-purple-500/10 px-2.5 py-0.5 text-2xs font-semibold text-purple-300">
                          <Layers className="h-3 w-3" />
                          {item.count} Artifacts
                        </span>
                      </td>
                      <td className="py-4 pl-4 pr-6 text-right">
                        <Badge
                          variant={item.verificationStatus === 'VERIFIED_MALICIOUS' ? 'danger' : item.verificationStatus === 'SYNTHETIC_TEST_INDICATOR' ? 'purple' : 'cyan'}
                          size="sm"
                        >
                          {item.verificationStatus || 'DETERMINISTIC'}
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
            description="Campaign reconstruction correlates indicators across at least two distinct evidence submissions or across separate cases. Add more evidence items to this investigation or seed the synthetic demonstration campaign to evaluate cross-case attribution."
            action={
              <div className="flex flex-wrap items-center justify-center gap-3">
                <Link to={`/investigations/${id}/analysis`}>
                  <Button size="sm" className="gap-2 font-semibold">
                    <Share2 className="h-4 w-4" />
                    Add Evidence
                  </Button>
                </Link>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={handleSeedDemo}
                  loading={seedingDemo}
                  className="gap-2 font-semibold text-cyan-300 border-cyan-500/30"
                >
                  <Sparkles className="h-4 w-4 text-cyan-400" />
                  Load Demo Campaign
                </Button>
              </div>
            }
          />
        )}
      </div>
    </div>
  );
};

export default CampaignReconstructionPage;
