import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  FileText,
  Image,
  Link2,
  Mail,
  Paperclip,
  Trash2,
  Upload,
  AlertCircle,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Textarea from '../components/ui/Textarea';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';
import { EVIDENCE_TYPES, EVIDENCE_TYPE_OPTIONS } from '../constants';
import { getApiErrorMessage } from '../services/api/apiClient';
import evidenceService from '../services/evidence/evidenceService';
import { investigationService } from '../services/investigation/investigationService';
import { formatRelative } from '../utils/format';

const MAX_FILE_SIZE = 25 * 1024 * 1024;
const FILE_ACCEPT = {
  IMAGE: 'image/png,image/jpeg,image/webp',
  PDF: 'application/pdf',
};

const initialFields = {
  evidenceType: EVIDENCE_TYPES.TEXT_MESSAGE,
  textContent: '',
  url: '',
  senderAddress: '',
  subject: '',
  emailBody: '',
  emailHeaders: '',
  description: '',
};

const validateUrl = (value) => {
  try {
    const parsed = new URL(value);
    return ['http:', 'https:'].includes(parsed.protocol) && Boolean(parsed.hostname);
  } catch {
    return false;
  }
};

const validateFields = (fields) => {
  const errors = {};
  if (fields.evidenceType === EVIDENCE_TYPES.TEXT_MESSAGE && !fields.textContent.trim()) {
    errors.textContent = 'Message content is required';
  }
  if (fields.evidenceType === EVIDENCE_TYPES.URL) {
    if (!fields.url.trim()) errors.url = 'URL is required';
    else if (!validateUrl(fields.url.trim())) errors.url = 'Enter a valid HTTP or HTTPS URL';
  }
  if (fields.evidenceType === EVIDENCE_TYPES.EMAIL) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(fields.senderAddress.trim())) {
      errors.senderAddress = 'Enter a valid sender address';
    }
    if (!fields.subject.trim()) errors.subject = 'Subject is required';
    if (!fields.emailBody.trim()) errors.emailBody = 'Email body is required';
  }
  return errors;
};

const typeIcon = {
  TEXT_MESSAGE: FileText,
  URL: Link2,
  EMAIL: Mail,
  IMAGE: Image,
  PDF: Paperclip,
};

const EvidenceRow = ({ item, onRemove, removing }) => {
  const Icon = typeIcon[item.evidenceType] || Paperclip;
  const contentDisplay =
    item.originalFilename || item.url || item.subject || item.textContent || item.extractedContent;

  return (
    <li className="flex items-start justify-between gap-3 px-5 sm:px-6 py-4.5 hover:bg-white/[0.04] transition-colors group">
      <div className="flex min-w-0 flex-1 items-start gap-3.5">
        <div className="mt-0.5 shrink-0 rounded-xl border border-cyan-500/25 bg-cyan-500/10 p-2.5 text-cyan-400 shadow-sm group-hover:scale-105 transition-transform">
          <Icon className="h-4 w-4" />
        </div>
        <div className="min-w-0 flex-1 space-y-1">
          <div className="flex items-center gap-2">
            <p className="text-xs font-semibold text-slate-100">
              {item.evidenceType?.replace('_', ' ')}
            </p>
            {item.status && (
              <span className="text-2xs font-mono uppercase px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-white/[0.08]">
                {item.status}
              </span>
            )}
          </div>
          <p className="truncate text-xs text-slate-400 font-mono" title={contentDisplay}>
            {contentDisplay}
          </p>
          <p className="text-2xs text-slate-400 font-mono">
            {formatRelative(item.createdAt)}
          </p>
        </div>
      </div>
      <Button
        variant="ghost"
        size="xs"
        onClick={() => onRemove(item.id)}
        loading={removing === item.id}
        aria-label={`Remove ${item.evidenceType}`}
        className="shrink-0 text-slate-400 hover:text-rose-400 p-2 rounded-lg"
      >
        <Trash2 className="h-4 w-4" />
      </Button>
    </li>
  );
};

export const EvidenceAnalysisPage = () => {
  const { id } = useParams();
  const fileInputRef = useRef(null);
  const [investigation, setInvestigation] = useState(null);
  const [evidence, setEvidence] = useState([]);
  const [fields, setFields] = useState(initialFields);
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [removing, setRemoving] = useState('');
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [threatResults, setThreatResults] = useState([]);
  const [threatLoading, setThreatLoading] = useState(false);

  const load = useCallback(async () => {
    const [caseData, evidenceData] = await Promise.all([
      investigationService.getById(id),
      evidenceService.list(id),
    ]);
    setInvestigation(caseData);
    setEvidence(evidenceData || []);
  }, [id]);

  useEffect(() => {
    load()
      .catch((err) => setError(getApiErrorMessage(err, 'Could not load this investigation.')))
      .finally(() => setLoading(false));
  }, [load]);

  useEffect(() => {
    if (!file || fields.evidenceType !== EVIDENCE_TYPES.IMAGE) {
      setPreviewUrl('');
      return undefined;
    }
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file, fields.evidenceType]);

  const updateField = (name) => (event) => {
    setFields((current) => ({ ...current, [name]: event.target.value }));
    setFieldErrors((current) => ({ ...current, [name]: '' }));
    setError('');
  };

  const selectFile = (selected) => {
    const next = selected?.[0];
    if (!next) return;
    const allowed =
      fields.evidenceType === EVIDENCE_TYPES.IMAGE
        ? ['image/png', 'image/jpeg', 'image/webp']
        : ['application/pdf'];
    if (!allowed.includes(next.type)) {
      setError('Unsupported file type for the selected evidence type.');
      setFile(null);
      return;
    }
    if (next.size > MAX_FILE_SIZE) {
      setError('Files must be 25 MB or smaller.');
      setFile(null);
      return;
    }
    setError('');
    setFile(next);
  };

  const submit = async (event) => {
    event.preventDefault();
    const nextErrors =
      fields.evidenceType === EVIDENCE_TYPES.IMAGE || fields.evidenceType === EVIDENCE_TYPES.PDF
        ? !file
          ? { file: 'Select a file to upload' }
          : {}
        : validateFields(fields);
    if (Object.keys(nextErrors).length) {
      setFieldErrors(nextErrors);
      return;
    }
    setSubmitting(true);
    setProgress(0);
    setError('');
    try {
      const metadata = { ...fields };
      if (fields.evidenceType === EVIDENCE_TYPES.IMAGE || fields.evidenceType === EVIDENCE_TYPES.PDF) {
        await evidenceService.createFile(id, metadata, file, (e) => {
          if (e.total) setProgress(Math.round((e.loaded * 100) / e.total));
        });
      } else {
        await evidenceService.create(id, metadata);
      }
      await investigationService.generateGraph(id);
      setEvidence(await evidenceService.list(id));
      setFields(initialFields);
      setFile(null);
      setFieldErrors({});
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (err) {
      setError(getApiErrorMessage(err, 'Evidence could not be submitted.'));
    } finally {
      setSubmitting(false);
    }
  };

  const remove = async (evidenceId) => {
    setRemoving(evidenceId);
    setError('');
    try {
      await evidenceService.remove(evidenceId);
      setEvidence((items) => items.filter((item) => item.id !== evidenceId));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Evidence could not be removed.'));
    } finally {
      setRemoving('');
    }
  };

  const runThreatLookups = async () => {
    setThreatLoading(true);
    setError('');
    try {
      const urlEvidence = evidence.filter(
        (item) => item.evidenceType === EVIDENCE_TYPES.URL || item.url
      );
      const results = await Promise.all(
        urlEvidence.map((item) => evidenceService.lookupThreats(item.id))
      );
      setThreatResults(results.flat());
    } catch (err) {
      setError(getApiErrorMessage(err, 'External verification is currently unavailable.'));
    } finally {
      setThreatLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-24 min-w-0">
        <Spinner className="h-8 w-8 text-cyan-400" label="Loading evidence workspace…" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6 min-w-0">
      <div className="border-b border-white/[0.08] pb-6 min-w-0">
        <Link
          to={`/investigations/${id}`}
          className="mb-2.5 inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white font-mono transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" /> Back to investigation
        </Link>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
          Evidence workspace
        </h1>
        <p className="mt-1 text-xs sm:text-sm text-slate-400">
          <span className="text-slate-100 font-semibold">{investigation?.title}</span> · Submit
          multiple evidence items before analysis.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-xs sm:text-sm text-rose-300 flex items-start gap-3 shadow-sm"
        >
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start min-w-0">
        <div className="lg:col-span-7 min-w-0">
          <Card
            title="Add evidence"
            subtitle="One item at a time; all items stay attached to this investigation."
            className="shadow-card"
          >
            <form onSubmit={submit} className="space-y-4.5" noValidate>
              <Select
                label="Evidence type"
                value={fields.evidenceType}
                onChange={(event) => {
                  setFields({ ...initialFields, evidenceType: event.target.value });
                  setFile(null);
                  setPreviewUrl('');
                  setFieldErrors({});
                  setError('');
                }}
                options={EVIDENCE_TYPE_OPTIONS}
              />

              {fields.evidenceType === EVIDENCE_TYPES.TEXT_MESSAGE && (
                <Textarea
                  label="Message content"
                  required
                  rows={6}
                  value={fields.textContent}
                  onChange={updateField('textContent')}
                  error={fieldErrors.textContent}
                  placeholder="Paste a WhatsApp, SMS, job offer, payment request, or social message…"
                />
              )}

              {fields.evidenceType === EVIDENCE_TYPES.URL && (
                <Input
                  label="URL"
                  required
                  type="url"
                  value={fields.url}
                  onChange={updateField('url')}
                  error={fieldErrors.url}
                  placeholder="https://example.com/suspicious-link"
                />
              )}

              {fields.evidenceType === EVIDENCE_TYPES.EMAIL && (
                <div className="space-y-3.5">
                  <Input
                    label="Sender address"
                    required
                    type="email"
                    value={fields.senderAddress}
                    onChange={updateField('senderAddress')}
                    error={fieldErrors.senderAddress}
                    placeholder="sender@example.com"
                  />
                  <Input
                    label="Subject"
                    required
                    value={fields.subject}
                    onChange={updateField('subject')}
                    error={fieldErrors.subject}
                    placeholder="Urgent notice regarding your account"
                  />
                  <Textarea
                    label="Email body"
                    required
                    rows={5}
                    value={fields.emailBody}
                    onChange={updateField('emailBody')}
                    error={fieldErrors.emailBody}
                    placeholder="Paste full raw email body text here…"
                  />
                  <Textarea
                    label="Headers (optional)"
                    rows={3}
                    value={fields.emailHeaders}
                    onChange={updateField('emailHeaders')}
                    placeholder="Received: from mail.example.com..."
                  />
                </div>
              )}

              {(fields.evidenceType === EVIDENCE_TYPES.IMAGE ||
                fields.evidenceType === EVIDENCE_TYPES.PDF) && (
                <div className="space-y-2">
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    onDragOver={(event) => event.preventDefault()}
                    onDrop={(event) => {
                      event.preventDefault();
                      selectFile(event.dataTransfer.files);
                    }}
                    className="w-full rounded-2xl border-2 border-dashed border-white/[0.14] bg-[#090e1a]/90 p-7 text-center hover:border-cyan-400/70 hover:bg-cyan-500/[0.05] transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-500 group shadow-inner"
                  >
                    <div className="mx-auto mb-3 w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-400/25 flex items-center justify-center text-cyan-400 group-hover:scale-105 transition-transform">
                      <Upload className="h-6 w-6" />
                    </div>
                    <span className="block text-xs sm:text-sm font-semibold text-slate-100">
                      {file ? file.name : 'Drop or select a file'}
                    </span>
                    <span className="mt-1 block text-2xs text-slate-400 font-mono">
                      {fields.evidenceType === EVIDENCE_TYPES.PDF
                        ? 'PDF document'
                        : 'PNG, JPG, JPEG, or WEBP'}{' '}
                      · max 25 MB
                    </span>
                    {previewUrl && (
                      <img
                        src={previewUrl}
                        alt="Selected evidence preview"
                        className="mx-auto mt-4 max-h-36 rounded-xl object-contain border border-white/[0.1] shadow-md"
                      />
                    )}
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept={FILE_ACCEPT[fields.evidenceType]}
                      onChange={(event) => selectFile(event.target.files)}
                      className="hidden"
                    />
                  </button>
                  {fieldErrors.file && (
                    <p className="text-xs text-rose-400 font-medium">{fieldErrors.file}</p>
                  )}
                </div>
              )}

              <Textarea
                label="Description (optional)"
                rows={2}
                value={fields.description}
                onChange={updateField('description')}
                placeholder="Context notes on where and how this item was received…"
              />

              {submitting && progress > 0 && (
                <div className="h-2 overflow-hidden rounded-full bg-navy-800">
                  <div
                    className="h-full bg-cyan-500 transition-all duration-micro"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              )}

              <Button type="submit" className="w-full font-semibold shadow-md" loading={submitting}>
                Add evidence
              </Button>
            </form>
          </Card>
        </div>

        <div className="lg:col-span-5 min-w-0">
          <Card
            title={`Evidence submitted (${evidence.length})`}
            subtitle="Only you can access these items."
            padded={false}
            className="min-w-0 shadow-card"
          >
            {evidence.length === 0 ? (
              <div className="p-6">
                <EmptyState
                  icon={Paperclip}
                  title="No evidence yet"
                  message="Submit a message, URL, email, image, or PDF to begin building this case."
                />
              </div>
            ) : (
              <ul className="divide-y divide-white/[0.06] max-h-[560px] overflow-y-auto">
                {evidence.map((item) => (
                  <EvidenceRow
                    key={item.id}
                    item={item}
                    onRemove={remove}
                    removing={removing}
                  />
                ))}
              </ul>
            )}
          </Card>
        </div>
      </div>

      <Card
        title="Threat intelligence"
        subtitle="Optional external verification. Unavailable providers are never treated as clean."
        className="shadow-card"
        headerAction={
          <Button
            variant="secondary"
            size="xs"
            onClick={runThreatLookups}
            loading={threatLoading}
            className="font-mono text-2xs font-semibold"
          >
            Verify URLs
          </Button>
        }
      >
        {threatResults.length === 0 ? (
          <EmptyState
            icon={Link2}
            title="No external results yet"
            message="Run verification for submitted URL evidence when provider credentials are configured."
          />
        ) : (
          <div className="space-y-3">
            {threatResults.map((result) => (
              <div
                key={result.id}
                className="rounded-2xl border border-white/[0.08] bg-[#0c101c] p-4.5 space-y-2 shadow-sm"
              >
                <div className="flex items-center justify-between gap-3">
                  <span className="text-xs font-bold text-slate-100 font-mono">
                    {result.provider}
                  </span>
                  <Badge variant={result.threatStatus}>
                    {result.threatStatus?.replaceAll('_', ' ')}
                  </Badge>
                </div>
                <p className="break-all text-xs font-mono text-slate-300">{result.indicator}</p>
                <p className="text-2xs text-slate-400 leading-relaxed">
                  {result.errorMessage ||
                    result.resultDetails ||
                    'No additional provider details were reported.'}
                </p>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};

export default EvidenceAnalysisPage;
