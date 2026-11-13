import { useEffect, useState } from 'react';
import { User, Server, Shield, AlertCircle, Cpu, Radio } from 'lucide-react';
import Card from '../components/common/Card';
import { FullPageSpinner } from '../components/ui/Spinner';
import { authService } from '../services/auth/authService';

export const SettingsPage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    authService
      .getCurrentUser()
      .then((res) => setProfile(res.data))
      .catch(() => setError('Could not load your profile. Please try again.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <FullPageSpinner label="Loading settings & configuration…" />;

  return (
    <div className="max-w-3xl mx-auto space-y-6 min-w-0">
      <div className="border-b border-white/[0.08] pb-6 min-w-0">
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">Settings</h1>
        <p className="text-xs sm:text-sm text-slate-400 mt-1">
          Analyst credentials, security preferences, and backend intelligence integrations
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs sm:text-sm flex items-start gap-2.5 shadow-sm"
        >
          <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <Card title="Analyst profile" subtitle="Authenticated identity associated with your investigation cases" className="shadow-card">
        <div className="space-y-3.5 text-xs sm:text-sm">
          {[
            { label: 'Full name', value: profile?.fullName },
            { label: 'Email', value: profile?.email },
            { label: 'Role', value: profile?.role?.replace('ROLE_', '') || 'ANALYST' },
          ].map(({ label, value }) => (
            <div
              key={label}
              className="flex justify-between items-center py-3.5 border-b border-white/[0.06] last:border-0"
            >
              <div className="flex items-center gap-2.5 text-slate-400 font-medium">
                <User className="w-4 h-4 text-slate-400" />
                <span>{label}</span>
              </div>
              <span className="text-slate-100 font-semibold font-mono">{value || '—'}</span>
            </div>
          ))}
        </div>
      </Card>

      <Card
        title="Intelligence feeds & AI engine"
        subtitle="Server-side threat intelligence APIs powering real-time indicator verification"
        className="shadow-card"
      >
        <div className="space-y-3 text-xs sm:text-sm">
          {[
            {
              name: 'VirusTotal API',
              note: 'Real-time threat reputation for suspicious domains, IP addresses, and file payloads',
              status: 'Configured',
              icon: Radio,
            },
            {
              name: 'URLhaus API',
              note: 'Malware distribution detection and weaponized URL cross-referencing',
              status: 'Configured',
              icon: Server,
            },
            {
              name: 'Groq AI (LLaMA 3.3 70B)',
              note: 'Deterministic multi-agent reasoning for social engineering & scam classification',
              status: 'Active',
              icon: Cpu,
            },
          ].map(({ name, note, status, icon: Icon }) => (
            <div
              key={name}
              className="flex flex-col sm:flex-row sm:items-center justify-between py-4 border-b border-white/[0.06] last:border-0 gap-2.5"
            >
              <div className="min-w-0">
                <div className="flex items-center gap-2.5 font-bold text-slate-100">
                  <div className="p-1.5 rounded-lg bg-cyan-500/10 border border-cyan-400/25 text-cyan-400">
                    <Icon className="w-3.5 h-3.5 shrink-0" />
                  </div>
                  <span>{name}</span>
                </div>
                <p className="text-2xs text-slate-400 mt-1 pl-8 leading-relaxed">{note}</p>
              </div>
              <span className="self-start sm:self-center shrink-0 text-2xs font-mono font-semibold px-3 py-1 rounded-full bg-white/[0.04] text-slate-300 border border-white/[0.08]">
                {status} · Server-managed
              </span>
            </div>
          ))}
        </div>
      </Card>

      <div className="flex items-start gap-3.5 p-4.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/25 text-slate-300 text-xs leading-relaxed shadow-sm">
        <Shield className="w-5 h-5 text-emerald-400 mt-0.5 shrink-0" />
        <p>
          API keys and AI credentials are managed exclusively on the Spring Boot backend via server
          environment variables. They are never exposed to browser memory or client scripts by design.
        </p>
      </div>
    </div>
  );
};

export default SettingsPage;
