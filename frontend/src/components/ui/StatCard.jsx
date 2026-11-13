import { cn } from '../../utils/cn';

const ACCENTS = {
  accent: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30 shadow-[0_0_14px_rgba(59,130,246,0.25)]',
  emerald: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30 shadow-[0_0_14px_rgba(16,185,129,0.25)]',
  amber: 'text-amber-400 bg-amber-500/10 border-amber-500/30 shadow-[0_0_14px_rgba(245,158,11,0.25)]',
  rose: 'text-rose-400 bg-rose-500/10 border-rose-500/30 shadow-[0_0_14px_rgba(244,63,94,0.25)]',
  cyan: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30 shadow-[0_0_14px_rgba(6,182,212,0.25)]',
  slate: 'text-slate-400 bg-slate-500/10 border-slate-500/30 shadow-[0_0_14px_rgba(148,163,184,0.15)]',
};

export const StatCard = ({ icon: Icon, label, value, accent = 'accent', sub, className = '' }) => (
  <div
    className={cn(
      'relative overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0c101c]/90 p-5 shadow-[0_4px_24px_-4px_rgba(0,0,0,0.6),inset_0_1px_0_0_rgba(255,255,255,0.08)] flex items-start justify-between gap-4 min-w-0 transition-all duration-200 hover:border-white/[0.18] hover:bg-[#101628] hover:shadow-[0_12px_36px_-6px_rgba(0,0,0,0.7),inset_0_1px_0_0_rgba(255,255,255,0.12)] group',
      className
    )}
  >
    <div className="space-y-1.5 min-w-0 flex-1">
      <p className="text-2xs font-semibold uppercase tracking-wider text-slate-400 truncate">
        {label}
      </p>
      <p className="text-2xl sm:text-3xl font-extrabold font-mono tracking-tight text-white tabular-nums">
        {value}
      </p>
      {sub && <p className="text-xs text-slate-400 truncate leading-relaxed">{sub}</p>}
    </div>
    {Icon && (
      <div
        className={cn(
          'p-3 rounded-xl border shrink-0 transition-all duration-200 group-hover:scale-105',
          ACCENTS[accent] || ACCENTS.accent
        )}
      >
        <Icon className="w-5 h-5" />
      </div>
    )}
  </div>
);

export default StatCard;
