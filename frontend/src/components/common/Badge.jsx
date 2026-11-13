import { EPISTEMIC_STATUS } from '../../constants';
import { cn } from '../../utils/cn';

const VARIANTS = {
  [EPISTEMIC_STATUS.VERIFIED]: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30 shadow-[0_0_12px_rgba(16,185,129,0.15)]',
  VERIFIED: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30 shadow-[0_0_12px_rgba(16,185,129,0.15)]',
  [EPISTEMIC_STATUS.AI_INFERENCE]: 'bg-amber-500/10 text-amber-300 border-amber-500/30 shadow-[0_0_12px_rgba(245,158,11,0.15)]',
  AI_INFERENCE: 'bg-amber-500/10 text-amber-300 border-amber-500/30 shadow-[0_0_12px_rgba(245,158,11,0.15)]',
  INFERENCE: 'bg-amber-500/10 text-amber-300 border-amber-500/30 shadow-[0_0_12px_rgba(245,158,11,0.15)]',
  [EPISTEMIC_STATUS.UNKNOWN]: 'bg-slate-800/80 text-slate-300 border-slate-700/80',
  UNKNOWN: 'bg-slate-800/80 text-slate-300 border-slate-700/80',

  LOW: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/35 shadow-[0_0_10px_rgba(16,185,129,0.2)]',
  MEDIUM: 'bg-amber-500/15 text-amber-300 border-amber-500/35 shadow-[0_0_10px_rgba(245,158,11,0.2)]',
  MODERATE: 'bg-amber-500/15 text-amber-300 border-amber-500/35 shadow-[0_0_10px_rgba(245,158,11,0.2)]',
  HIGH: 'bg-orange-500/15 text-orange-300 border-orange-500/35 shadow-[0_0_12px_rgba(249,115,22,0.25)]',
  CRITICAL: 'bg-rose-500/15 text-rose-300 border-rose-500/35 shadow-[0_0_14px_rgba(244,63,94,0.3)]',
  INCONCLUSIVE: 'bg-slate-800/80 text-slate-300 border-slate-700/80',

  COMPLETED: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/35',
  PROCESSING: 'bg-sky-500/15 text-sky-300 border-sky-500/35 shadow-[0_0_10px_rgba(14,165,233,0.2)]',
  PENDING: 'bg-amber-500/15 text-amber-300 border-amber-500/35',
  DRAFT: 'bg-slate-800/80 text-slate-300 border-slate-700/80',
  FAILED: 'bg-rose-500/15 text-rose-300 border-rose-500/35 shadow-[0_0_12px_rgba(244,63,94,0.25)]',

  ACCENT: 'bg-blue-500/15 text-blue-300 border-blue-500/35 shadow-[0_0_12px_rgba(59,130,246,0.2)]',
  DEFAULT: 'bg-[#111728] text-slate-300 border-white/[0.1]',
};

const DOT_COLORS = {
  LOW: 'bg-emerald-400',
  VERIFIED: 'bg-emerald-400',
  MEDIUM: 'bg-amber-400',
  MODERATE: 'bg-amber-400',
  AI_INFERENCE: 'bg-amber-400',
  HIGH: 'bg-orange-400',
  CRITICAL: 'bg-rose-400 animate-pulse',
  FAILED: 'bg-rose-400',
  PROCESSING: 'bg-sky-400 animate-pulse',
  PENDING: 'bg-amber-400',
  UNKNOWN: 'bg-slate-400',
  DRAFT: 'bg-slate-400',
};

export const Badge = ({ status, children, variant, dot = false, className = '' }) => {
  const effectiveKey = (variant || status || '').toUpperCase();
  const colorClass = VARIANTS[effectiveKey] || VARIANTS.DEFAULT;
  const dotColorClass = DOT_COLORS[effectiveKey] || 'bg-slate-400';

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-2xs font-semibold tracking-wide uppercase border select-none transition-colors duration-150',
        colorClass,
        className
      )}
    >
      {dot && <span className={cn('w-1.5 h-1.5 rounded-full shrink-0', dotColorClass)} />}
      <span>{children || status}</span>
    </span>
  );
};

export default Badge;
