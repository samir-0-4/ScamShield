import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { PlusCircle, ArrowLeft, Shield, Info, AlertCircle } from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Textarea from '../components/ui/Textarea';
import { INVESTIGATION_CATEGORY_LIST, categoryLabel } from '../constants';
import { investigationService } from '../services/investigation/investigationService';
import { getApiErrorMessage } from '../services/api/apiClient';

const categoryOptions = [
  { value: '', label: 'Select a category…' },
  ...INVESTIGATION_CATEGORY_LIST.map((c) => ({ value: c, label: categoryLabel(c) })),
];

const categoryDescriptions = {
  JOB_SCAM: 'Fake recruitment, remote task commissions, interview deposit schemes.',
  PAYMENT_SCAM: 'Fraudulent wire requests, QR scams, fake invoices, peer-to-peer demands.',
  PHISHING: 'Credential harvesting, lookalike domains, SMS / WhatsApp spoofing.',
  INVESTMENT_SCAM: 'Pig butchering, crypto trading yield schemes, fraudulent brokers.',
  SHOPPING_SCAM: 'Counterfeit storefronts, non-delivery of goods, payment redirect.',
  IDENTITY_FRAUD: 'Impersonation of authorities, KYC theft, unauthorized account access.',
  SUSPICIOUS_COMMUNICATION: 'Unsolicited threats, extortion emails, cold social contact.',
  OTHER: 'General digital threat or emerging fraudulent vectors.',
};

const validate = ({ title, category }) => {
  const errors = {};
  if (!title || title.trim().length < 3) errors.title = 'Title must be at least 3 characters';
  if (title && title.trim().length > 255) errors.title = 'Title must be under 255 characters';
  if (!category) errors.category = 'Category is required';
  return errors;
};

export const NewInvestigationPage = () => {
  const [fields, setFields] = useState({ title: '', description: '', category: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const set = (k) => (e) => {
    setFields((f) => ({ ...f, [k]: e.target.value }));
    setErrors((er) => ({ ...er, [k]: '' }));
    setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate(fields);
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setLoading(true);
    try {
      const created = await investigationService.create({
        title: fields.title.trim(),
        description: fields.description || undefined,
        category: fields.category,
      });
      navigate(`/investigations/${created.id}`, { replace: true });
    } catch (err) {
      setServerError(getApiErrorMessage(err, 'Failed to create the investigation.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 min-w-0">
      <div>
        <Link
          to="/investigations"
          className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white transition-colors mb-2.5 font-mono"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to investigations</span>
        </Link>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white flex items-center gap-3">
          <div className="p-2 rounded-xl bg-cyan-500/10 border border-cyan-400/30 text-cyan-400 shadow-[0_0_16px_rgba(59,130,246,0.2)]">
            <PlusCircle className="w-6 h-6" />
          </div>
          <span>New Investigation</span>
        </h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1.5">
          Initialize a new forensic case container before attaching evidence
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        <Card title="Case details" className="lg:col-span-2 shadow-card">
          {serverError && (
            <div
              role="alert"
              className="mb-5 p-3.5 bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs sm:text-sm rounded-xl flex items-start gap-2.5 shadow-sm"
            >
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{serverError}</span>
            </div>
          )}
          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            <Input
              label="Investigation title"
              required
              value={fields.title}
              onChange={set('title')}
              placeholder="e.g. WhatsApp Recruitment Phishing Campaign"
              error={errors.title}
              hint="Descriptive name identifying the threat actor or scenario"
            />
            <div className="space-y-2">
              <Select
                label="Category"
                required
                value={fields.category}
                onChange={set('category')}
                options={categoryOptions}
                error={errors.category}
              />
              {fields.category && categoryDescriptions[fields.category] && (
                <div className="p-3 rounded-xl bg-cyan-500/[0.08] border border-cyan-500/25 text-2xs text-cyan-300 font-mono mt-2 shadow-sm">
                  <span className="font-bold uppercase tracking-wider text-cyan-400">Scope:</span>{' '}
                  {categoryDescriptions[fields.category]}
                </div>
              )}
            </div>
            <Textarea
              label="Description & context"
              value={fields.description}
              onChange={set('description')}
              rows={4}
              placeholder="Describe background context, initial victim reports, targeted assets, or contact details…"
              hint="Optional contextual notes attached to the case summary"
            />
            <div className="flex items-center justify-end gap-3 pt-5 border-t border-white/[0.08]">
              <Link to="/investigations">
                <Button variant="secondary" size="md">
                  Cancel
                </Button>
              </Link>
              <Button type="submit" loading={loading} size="md" className="font-semibold shadow-md">
                Initialize Case
              </Button>
            </div>
          </form>
        </Card>

        <div className="space-y-4">
          <Card title="Investigation Protocol" className="shadow-card">
            <div className="space-y-3.5 text-xs text-slate-400 leading-relaxed">
              <div className="flex items-start gap-3 p-3 rounded-xl bg-white/[0.02] border border-white/[0.05]">
                <Shield className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <p>
                  Cases start in <strong className="text-white">DRAFT</strong> status until
                  evidence items are submitted.
                </p>
              </div>
              <div className="flex items-start gap-3 p-3 rounded-xl bg-white/[0.02] border border-white/[0.05]">
                <Info className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <p>
                  You can attach screenshots, raw emails, suspicious URLs, and documents immediately
                  after creating the dossier.
                </p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default NewInvestigationPage;
