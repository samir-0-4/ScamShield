import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  CheckCircle2,
  FileText,
  Info,
  Link2,
  Mail,
  Play,
  ShieldAlert,
  ShieldCheck,
  Sparkles,
  AlertCircle,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import EmptyState from '../components/ui/EmptyState';
import { FullPageSpinner } from '../components/ui/Spinner';
import { getApiErrorMessage } from '../services/api/apiClient';
import evidenceService from '../services/evidence/evidenceService';
import { investigationService } from '../services/investigation/investigationService';
import { formatDate, formatDateTime, titleCase } from '../utils/format';

const riskStyles = {
  LOW: 'border-emerald-500/35 bg-[#081814]/90 text-emerald-300 shadow-[0_4px_24px_rgba(16,185,129,0.15)]',
  MODERATE: 'border-amber-500/35 bg-[#1b1509]/90 text-amber-300 shadow-[0_4px_24px_rgba(245,158,11,0.15)]',
  MEDIUM: 'border-amber-500/35 bg-[#1b1509]/90 text-amber-300 shadow-[0_4px_24px_rgba(245,158,11,0.15)]',
  HIGH: 'border-orange-500/35 bg-[#201009]/90 text-orange-300 shadow-[0_4px_24px_rgba(249,115,22,0.15)]',
  CRITICAL: 'border-rose-500/35 bg-[#200911]/90 text-rose-300 shadow-[0_4px_24px_rgba(244,63,94,0.2)]',
  INCONCLUSIVE: 'border-white/[0.1] bg-[#0c101c]/90 text-slate-300',
};

const evidenceIcon = {
  TEXT_MESSAGE: FileText,
  EMAIL: Mail,
  URL: Link2,
};

const safeJson = (value) => {
  if (!value) return null;
  try {
    return typeof value === 'string' ? JSON.parse(value) : value;
  } catch {
    return null;
  }
};

const findingVerification = (finding) => {
  const value = String(finding?.verificationStatus || '').toUpperCase();
  if (value.includes('EXTERNAL') || value.includes('VERIFIED')) return 'Verified by external source';
  if (value.includes('EXTRACT')) return 'Extracted from submitted evidence';
  if (value.includes('AI')) return 'AI assessment';
  if (value.includes('UNVERIFIED')) return 'Unverified';
  return 'Unknown';
};

const FindingCard = ({ finding }) => (
  <article className="rounded-2xl border border-white/[0.08] bg-[#0c101c] p-5 sm:p-6 space-y-4 shadow-sm hover:border-white/[0.16] transition-all">
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0 flex-1">
        <h3 className="font-bold text-slate-100 text-sm sm:text-base tracking-tight">
          {finding.title}
        </h3>
        <p className="mt-1 text-xs sm:text-sm leading-relaxed text-slate-400">
          {finding.description}
        </p>
      </div>
      <Badge status={finding.severity}>{titleCase(finding.severity)}</Badge>
    </div>

    <div className="grid gap-2 text-2xs font-mono text-slate-400 sm:grid-cols-2 bg-black/40 p-3.5 rounded-xl border border-white/[0.06]">
      <p>
        <span className="text-slate-500 font-sans">Confidence:</span>{' '}
        <span className="text-slate-100 font-semibold">{Math.round((finding.confidence || 0) * 100)}%</span>
      </p>
      <p>
        <span className="text-slate-500 font-sans">Verification:</span>{' '}
        <span className="text-slate-100 font-semibold">{findingVerification(finding)}</span>
      </p>
      <p className="truncate">
        <span className="text-slate-500 font-sans">Evidence ref:</span>{' '}
        <span className="text-slate-100 font-semibold">{finding.evidenceReference || 'Unknown'}</span>
      </p>
      <p>
        <span className="text-slate-500 font-sans">Source engine:</span>{' '}
        <span className="text-slate-100 font-semibold">{finding.sourceType || 'Unknown'}</span>
      </p>
    </div>

    <div className="border-t border-white/[0.08] pt-3 text-xs sm:text-sm space-y-2">
      <p className="text-slate-300 leading-relaxed">{finding.explanation}</p>
      {finding.recommendedAction && (
        <p className="text-cyan-300 font-semibold bg-cyan-500/10 p-2.5 rounded-xl border border-cyan-500/25">
          <span className="text-slate-400 font-normal">Recommended action:</span>{' '}
          {finding.recommendedAction}
        </p>
      )}
    </div>
  </article>
);

const ProviderResults = ({ results }) => (
  <div className="space-y-3">
    {results.length === 0 ? (
      <p className="text-xs sm:text-sm text-slate-500">No external lookups have been recorded.</p>
    ) : (
      results.map((result) => (
        <div
          key={result.id}
          className="rounded-2xl border border-white/[0.08] bg-[#0c101c] p-4.5 space-y-2 shadow-sm"
        >
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="font-bold text-slate-200 font-mono text-xs">
              {result.provider}
            </span>
            <Badge status={result.threatStatus}>{titleCase(result.threatStatus)}</Badge>
          </div>
          <p className="break-all font-mono text-xs text-slate-300">{result.indicator}</p>
          <p className="text-2xs text-slate-400 leading-relaxed">
            {result.errorMessage ||
              result.resultDetails ||
              'No additional provider details reported.'}
          </p>
          <p className="text-2xs font-mono text-slate-500">
            {formatDateTime(result.lookupTimestamp)}
            {result.providerReference ? ` · Ref: ${result.providerReference}` : ''}
          </p>
        </div>
      ))
    )}
  </div>
);

export const AnalysisResultsPage = () => {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    const investigation = await investigationService.getById(id);
    const evidence = await evidenceService.list(id);
    const risk = await investigationService.getRiskAssessment(id).catch(() => null);
    const graph = await investigationService.getGraph(id).catch(() => null);
    const details = await Promise.all(
      evidence.map(async (item) => {
        const [indicators, threats, analysis] = await Promise.all([
          evidenceService.listIndicators(item.id).catch(() => []),
          evidenceService.listThreatLookups(item.id).catch(() => []),
          evidenceService.listAnalysis(item.id).catch(() => []),
        ]);
        return { item, indicators, threats, analysis };
      })
    );
    setData({ investigation, evidence, risk, graph, details });
  }, [id]);

  useEffect(() => {
    load()
      .catch((err) =>
        setError(getApiErrorMessage(err, 'Could not load investigation results.'))
      )
      .finally(() => setLoading(false));
  }, [load]);

  const runAnalysis = async () => {
    setProcessing(true);
    setError('');
    try {
      const items = data?.evidence || [];
      await Promise.all(items.map((item) => evidenceService.analyze(item.id)));
      await Promise.all(
        items
          .filter((item) => item.evidenceType === 'URL' || item.url)
          .map((item) => evidenceService.lookupThreats(item.id).catch(() => []))
      );
      await investigationService.assessRisk(id);
      await load();
    } catch (err) {
      setError(
        getApiErrorMessage(
          err,
          'Analysis could not be completed. Existing results remain available.'
        )
      );
    } finally {
      setProcessing(false);
    }
  };

  const findings = useMemo(
    () =>
      (data?.details || []).flatMap(({ analysis }) =>
        analysis.flatMap((execution) => {
          const output = safeJson(execution.outputJson);
          return (output?.findings || []).map((finding) => ({
            ...finding,
            agentName: execution.agentName,
          }));
        })
      ),
    [data]
  );

  const summary = useMemo(
    () =>
      (data?.details || [])
        .map(({ analysis }) => analysis)
        .flat()
        .map((execution) => safeJson(execution.outputJson))
        .find((output) => output?.agentName === 'INVESTIGATION_REPORT' && output.summary)?.summary,
    [data]
  );

  const actions = useMemo(() => {
    const text = findings
      .map((item) => `${item.title} ${item.description}`)
      .join(' ')
      .toLowerCase();
    const result = [];
    if (text.includes('payment') || text.includes('fee') || text.includes('money')) {
      result.push(
        'Do not pay an upfront registration fee before independently verifying the organization.'
      );
      result.push(
        'If financial fraud occurred in India, contact your bank and official cybercrime reporting channels, including 1930 where applicable.'
      );
    }
    if (text.includes('otp') || text.includes('password') || text.includes('credential')) {
      result.push('Never share OTPs, passwords, passcodes, or login credentials.');
    }
    if (text.includes('impersonat') || text.includes('support') || text.includes('bank')) {
      result.push(
        'Contact the organization using contact details from its official website, not the message.'
      );
    }
    if ((data?.evidence || []).some((item) => ['IMAGE', 'PDF'].includes(item.evidenceType))) {
      result.push(
        'Avoid opening suspicious attachments until their source is independently verified.'
      );
    }
    return result;
  }, [findings, data]);

  if (loading) return <FullPageSpinner label="Loading investigation results…" />;
  if (!data) {
    return (
      <div
        role="alert"
        className="rounded-2xl border border-rose-800 bg-rose-950/60 p-5 text-xs sm:text-sm text-rose-300"
      >
        {error}
      </div>
    );
  }

  const { investigation, evidence, risk, details } = data;
  const category = risk?.category || 'INCONCLUSIVE';
  const executions = details.flatMap((item) => item.analysis);
  const failed = executions.filter((item) => item.executionStatus === 'FAILED');
  const hasResults = executions.length > 0 || Boolean(risk);

  return (
    <div className="mx-auto max-w-6xl space-y-6 min-w-0">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/[0.08] pb-6 min-w-0">
        <div>
          <Link
            to={`/investigations/${id}`}
            className="mb-2 inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white font-mono transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Back to investigation
          </Link>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            {investigation.title}
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-slate-400 font-mono">
            Investigation results · created {formatDate(investigation.createdAt)}
          </p>
        </div>
        <Button
          onClick={runAnalysis}
          loading={processing}
          disabled={evidence.length === 0}
          className="font-semibold shadow-lg"
        >
          <Play className="h-4 w-4" />{' '}
          {processing ? 'Processing analysis' : hasResults ? 'Refresh analysis' : 'Run analysis'}
        </Button>
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
      {processing && (
        <div
          className="rounded-2xl border border-cyan-500/30 bg-cyan-500/10 p-4 text-xs sm:text-sm text-cyan-300 shadow-sm flex items-center gap-3"
          role="status"
        >
          <div className="w-2.5 h-2.5 rounded-full bg-cyan-400 animate-ping" />
          <span>Analysis is processing. No progress estimate is shown because the backend does not provide one.</span>
        </div>
      )}

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5 min-w-0">
        <div className={`rounded-2xl border p-5 shadow-card backdrop-blur-xl ${riskStyles[category] || riskStyles.INCONCLUSIVE}`}>
          <p className="text-2xs uppercase tracking-wider font-mono opacity-80 font-bold">Risk assessment</p>
          <p className="mt-2 text-3xl font-extrabold font-mono tracking-tight">{risk ? `${risk.score}/100` : '—'}</p>
          <p className="mt-1 text-xs font-semibold">{titleCase(category)}</p>
        </div>
        {[
          ['Status', titleCase(investigation.status)],
          ['Confidence', risk ? `${Math.round(risk.confidence * 100)}%` : '—'],
          ['Evidence', evidence.length],
          ['Findings', findings.length],
        ].map(([label, value]) => (
          <Card key={label} className="p-5 shadow-card">
            <p className="text-2xs uppercase tracking-wider text-slate-400 font-mono font-bold">{label}</p>
            <p className="mt-2 text-2xl font-extrabold text-white font-mono tracking-tight">{value}</p>
          </Card>
        ))}
      </section>

      {risk && (
        <Card
          title="Overall assessment"
          subtitle="Transparent heuristic assessment, not a mathematically proven probability of fraud."
          className="shadow-card"
        >
          <p className="text-xs sm:text-sm leading-relaxed text-slate-200">{risk.explanation}</p>
          {risk.missingInformation && (
            <p className="mt-3.5 flex items-start gap-2.5 text-xs sm:text-sm text-amber-300 bg-amber-950/30 p-3.5 rounded-xl border border-amber-900/50">
              <Info className="h-4 w-4 shrink-0 mt-0.5 text-amber-400" /> Unknown: {risk.missingInformation}
            </p>
          )}
          {risk.contributingSignals?.length > 0 && (
            <div className="mt-5 space-y-2.5">
              <p className="text-2xs font-bold uppercase font-mono tracking-wider text-slate-400">
                Contributing Signals
              </p>
              {risk.contributingSignals.map((signal) => (
                <div
                  key={signal.id}
                  className="rounded-xl border border-white/[0.08] bg-[#090e1a] p-3.5 space-y-1.5"
                >
                  <div className="flex justify-between items-center gap-3 text-xs sm:text-sm">
                    <span className="text-slate-100 font-medium">{signal.signal}</span>
                    <span className="text-cyan-400 font-mono font-extrabold">+{signal.weight}</span>
                  </div>
                  <p className="text-2xs text-slate-400 font-mono">
                    {signal.explanation} · Evidence: {signal.evidenceReferences}
                  </p>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      <Card
        title="Executive summary"
        subtitle="Based on structured report output when available."
        className="shadow-card"
      >
        {summary ? (
          <p className="text-xs sm:text-sm leading-relaxed text-slate-200">{summary}</p>
        ) : (
          <EmptyState
            icon={Sparkles}
            title="Summary not available"
            message={
              hasResults
                ? 'A report agent summary was not returned.'
                : 'Run analysis to generate a structured summary.'
            }
          />
        )}
      </Card>

      <Card
        title={`Findings (${findings.length})`}
        subtitle="Each finding shows its source, evidence reference, and verification state."
        className="shadow-card"
      >
        {findings.length ? (
          <div className="space-y-4">
            {findings.map((finding, index) => (
              <FindingCard key={`${finding.title}-${index}`} finding={finding} />
            ))}
          </div>
        ) : (
          <EmptyState
            icon={ShieldCheck}
            title="No findings returned"
            message={
              failed.length
                ? 'Analysis failed for one or more agents.'
                : 'No structured findings are available yet.'
            }
          />
        )}
      </Card>

      <Card
        title="Evidence breakdown"
        subtitle={`${evidence.length} submitted item${evidence.length === 1 ? '' : 's'}.`}
        className="shadow-card"
      >
        {evidence.length ? (
          <div className="space-y-4">
            {details.map(({ item, indicators, threats, analysis }) => {
              const Icon = evidenceIcon[item.evidenceType] || FileText;
              return (
                <div key={item.id} className="rounded-2xl border border-white/[0.08] bg-[#090e1a] p-4.5 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3 min-w-0">
                      <Icon className="mt-0.5 h-4 w-4 text-cyan-400 shrink-0" />
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-100 text-xs sm:text-sm">
                          {titleCase(item.evidenceType)}
                        </p>
                        <p className="text-2xs text-slate-400 font-mono truncate" title={item.description || item.url}>
                          {item.description ||
                            item.url ||
                            item.subject ||
                            item.originalFilename ||
                            'Submitted evidence'}
                        </p>
                      </div>
                    </div>
                    <Badge
                      status={
                        analysis.some((entry) => entry.executionStatus === 'FAILED')
                          ? 'UNKNOWN'
                          : analysis.length
                          ? 'VERIFIED'
                          : 'UNKNOWN'
                      }
                    >
                      {analysis.length ? 'Analyzed' : 'Not analyzed'}
                    </Badge>
                  </div>
                  {indicators.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {indicators.map((indicator) => (
                        <span
                          key={indicator.id}
                          className="max-w-full truncate inline-block rounded-full bg-[#111728] px-3 py-0.5 text-2xs font-mono text-slate-300 border border-white/[0.08]"
                          title={`${titleCase(indicator.indicatorType)}: ${indicator.normalizedValue}`}
                        >
                          {titleCase(indicator.indicatorType)}: {indicator.normalizedValue}
                        </span>
                      ))}
                    </div>
                  )}
                  {threats.length > 0 && (
                    <div className="pt-2">
                      <ProviderResults results={threats} />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <EmptyState
            icon={FileText}
            title="No evidence submitted"
            message="Submit evidence before running analysis."
          />
        )}
      </Card>

      <Card
        title="Threat intelligence"
        subtitle="External results are never treated as clean when unavailable."
        className="shadow-card"
      >
        <ProviderResults results={details.flatMap((item) => item.threats)} />
      </Card>

      <Card
        title="Recommended actions"
        subtitle="Actions are shown only when supported by returned findings or evidence."
        className="shadow-card"
      >
        {actions.length ? (
          <ul className="space-y-3">
            {actions.map((action) => (
              <li
                key={action}
                className="flex items-start gap-3.5 text-xs sm:text-sm text-slate-200 bg-[#090e1a] p-4 rounded-2xl border border-white/[0.08] shadow-sm"
              >
                <ShieldAlert className="h-4 w-4 shrink-0 text-cyan-400 mt-0.5" />
                <span className="leading-relaxed">{action}</span>
              </li>
            ))}
          </ul>
        ) : (
          <EmptyState
            icon={CheckCircle2}
            title="No specific actions returned"
            message="Review the unknown information and verify any request independently."
          />
        )}
      </Card>
    </div>
  );
};

export default AnalysisResultsPage;
