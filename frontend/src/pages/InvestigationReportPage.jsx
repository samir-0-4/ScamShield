import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, FileText, Printer, AlertCircle } from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import EmptyState from '../components/ui/EmptyState';
import { FullPageSpinner } from '../components/ui/Spinner';
import { getApiErrorMessage } from '../services/api/apiClient';
import { investigationService } from '../services/investigation/investigationService';
import { formatDate, formatDateTime, titleCase } from '../utils/format';

const parse = (value) => {
  try {
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
};

const ReportFinding = ({ finding }) => (
  <article className="report-card rounded-2xl border border-white/[0.08] bg-[#0c101c] p-5 space-y-2.5 shadow-sm">
    <div className="flex items-start justify-between gap-3">
      <div className="min-w-0 flex-1">
        <h3 className="font-bold text-slate-100 text-sm">
          {finding.title || 'Unknown finding'}
        </h3>
        <p className="mt-1 text-xs text-slate-400">
          {finding.description || 'No description available.'}
        </p>
      </div>
      <Badge status={finding.severity}>{titleCase(finding.severity || 'UNKNOWN')}</Badge>
    </div>
    <p className="mt-2 text-2xs leading-relaxed text-slate-400">
      {finding.explanation || 'No explanation available.'}
    </p>
    {finding.recommendedAction && (
      <p className="text-2xs text-blue-300 font-semibold bg-blue-500/10 p-2 rounded-lg border border-blue-500/20">
        Action: {finding.recommendedAction}
      </p>
    )}
  </article>
);

export const InvestigationReportPage = () => {
  const { id } = useParams();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    const reports = await investigationService.listReports(id);
    setReport(reports[0] || null);
  }, [id]);

  useEffect(() => {
    load()
      .catch((err) => setError(getApiErrorMessage(err, 'Could not load reports.')))
      .finally(() => setLoading(false));
  }, [load]);

  const generate = async () => {
    setGenerating(true);
    setError('');
    try {
      const created = await investigationService.createReport(id);
      setReport(created);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Report could not be generated.'));
    } finally {
      setGenerating(false);
    }
  };

  if (loading) return <FullPageSpinner label="Loading investigation report…" />;
  const content = parse(report?.contentJson);
  const investigation = content?.investigation;
  const risk = content?.riskAssessment;
  const evidence = content?.evidence || [];
  const findings = evidence.flatMap((item) =>
    (item.analysis || []).flatMap((execution) => parse(execution.outputJson)?.findings || [])
  );
  const summaries = evidence
    .flatMap((item) => (item.analysis || []).map((execution) => parse(execution.outputJson)))
    .filter((item) => item?.summary);
  const relationships = content?.relationships || [];

  return (
    <div className="report-page mx-auto max-w-5xl space-y-6 min-w-0">
      <div className="no-print flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/[0.08] pb-6 min-w-0">
        <div>
          <Link
            to={`/investigations/${id}`}
            className="mb-2 inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white font-mono transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Back to investigation
          </Link>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            Investigation report
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-slate-400 font-mono">
            Persisted forensic report snapshot · browser print &amp; PDF export supported
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2.5">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => window.print()}
            disabled={!report}
            className="font-semibold inline-flex items-center gap-2 shadow-sm"
          >
            <Printer className="h-4 w-4" /> <span>Print / Save PDF</span>
          </Button>
          <Button
            onClick={generate}
            loading={generating}
            size="sm"
            className="font-semibold inline-flex items-center gap-2 shadow-md"
          >
            <FileText className="h-4 w-4" />
            <span>{report ? 'Regenerate report' : 'Generate report'}</span>
          </Button>
        </div>
      </div>

      {error && (
        <div
          role="alert"
          className="no-print rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-xs sm:text-sm text-rose-300 flex items-start gap-2.5 shadow-sm"
        >
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {!report ? (
        <Card className="shadow-card">
          <EmptyState
            icon={FileText}
            title="No report generated"
            message="Generate a report after submitting and analyzing evidence."
            action={
              <Button onClick={generate} loading={generating} size="sm" className="shadow-md font-semibold">
                Generate report
              </Button>
            }
          />
        </Card>
      ) : (
        <article className="space-y-6 print:bg-white print:text-black">
          <header className="report-card border-b border-white/[0.08] pb-6 print:border-slate-300">
            <p className="text-2xs font-mono font-bold uppercase tracking-[0.2em] text-blue-400 print:text-slate-600">
              PhantomTrace Digital Forensics Dossier
            </p>
            <h1 className="mt-2 text-2xl sm:text-3xl font-extrabold text-white print:text-black tracking-tight">
              {investigation?.title || 'Untitled investigation'}
            </h1>
            <p className="mt-2 text-xs sm:text-sm text-slate-400 print:text-slate-600 font-mono">
              ID: {investigation?.id || id} · Created: {formatDate(investigation?.createdAt)} ·
              Category: {titleCase(investigation?.category || 'UNKNOWN')}
            </p>
            <p className="mt-1 text-2xs text-slate-400 print:text-slate-600 font-mono">
              Report generated {formatDateTime(report.createdAt)} · Epistemic Truth Standard
            </p>
          </header>

          <Card title="Executive summary" className="shadow-card">
            {summaries[0]?.summary ? (
              <p className="text-xs sm:text-sm leading-relaxed text-slate-200 print:text-slate-800 whitespace-pre-wrap font-sans">
                {summaries[0].summary}
              </p>
            ) : (
              <p className="text-xs sm:text-sm text-slate-400">
                Unknown — no structured executive summary was persisted.
              </p>
            )}
          </Card>

          <Card title="Overall risk assessment" className="shadow-card">
            {risk ? (
              <div className="space-y-4">
                <div className="flex flex-wrap items-center gap-4">
                  <span className="text-3xl sm:text-4xl font-mono font-extrabold text-white print:text-black">
                    {risk.score}/100
                  </span>
                  <Badge status={risk.category}>{titleCase(risk.category)}</Badge>
                  <span className="text-xs font-mono text-slate-400">
                    Confidence: {Math.round((risk.confidence || 0) * 100)}%
                  </span>
                </div>
                <p className="text-xs sm:text-sm leading-relaxed text-slate-200 print:text-slate-800">
                  {risk.explanation}
                </p>
                {risk.disclaimer && (
                  <p className="text-2xs font-mono text-amber-300 print:text-slate-600 bg-amber-950/20 p-3 rounded-xl border border-amber-900/40">
                    {risk.disclaimer}
                  </p>
                )}
              </div>
            ) : (
              <p className="text-xs sm:text-sm text-slate-400">
                Unknown — no risk assessment was available when this report was generated.
              </p>
            )}
          </Card>

          <Card title={`Evidence (${evidence.length})`} className="shadow-card">
            {evidence.length ? (
              <div className="space-y-3">
                {evidence.map((item) => (
                  <div
                    key={item.id}
                    className="report-card rounded-2xl border border-white/[0.08] bg-[#090e1a] p-4 space-y-2"
                  >
                    <h3 className="font-bold text-slate-100 text-sm">
                      {titleCase(item.type)}
                    </h3>
                    <p className="text-xs text-slate-400 font-mono">
                      {item.description || 'No description provided.'}
                    </p>
                    {item.indicators?.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 pt-1">
                        {item.indicators.map((indicator) => (
                          <span
                            key={indicator.id}
                            className="rounded-full bg-[#111728] px-2.5 py-0.5 text-2xs font-mono text-slate-300 border border-white/[0.08]"
                          >
                            {titleCase(indicator.type)}:{' '}
                            {indicator.normalizedValue || indicator.originalValue}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs sm:text-sm text-slate-400">Unknown — no evidence was attached.</p>
            )}
          </Card>

          <Card title={`Findings (${findings.length})`} className="shadow-card">
            {findings.length ? (
              <div className="space-y-3.5">
                {findings.map((finding, index) => (
                  <ReportFinding key={`${finding.title}-${index}`} finding={finding} />
                ))}
              </div>
            ) : (
              <p className="text-xs sm:text-sm text-slate-400">
                Unknown — no structured findings were persisted.
              </p>
            )}
          </Card>

          <Card title={`Evidence relationships (${relationships.length})`} className="shadow-card">
            {relationships.length ? (
              <div className="space-y-3">
                {relationships.map((relationship) => (
                  <div
                    key={relationship.id}
                    className="report-card rounded-2xl border border-white/[0.08] bg-[#090e1a] p-3.5 text-xs space-y-1.5"
                  >
                    <p className="font-bold text-slate-100">
                      {titleCase(relationship.type || relationship.relationshipType)}
                    </p>
                    <p className="text-slate-300">
                      {relationship.explanation || 'No explanation available.'}
                    </p>
                    <p className="text-2xs font-mono text-slate-400">
                      Verification: {titleCase(relationship.verificationStatus || 'UNKNOWN')} ·
                      Confidence:{' '}
                      {relationship.confidence == null
                        ? 'Unknown'
                        : `${Math.round(relationship.confidence * 100)}%`}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs sm:text-sm text-slate-400">
                Unknown — no supported relationships were found.
              </p>
            )}
          </Card>
        </article>
      )}
    </div>
  );
};

export default InvestigationReportPage;
