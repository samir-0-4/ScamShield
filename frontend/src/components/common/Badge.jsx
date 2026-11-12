import { EPISTEMIC_STATUS } from '../../constants';
import { cn } from '../../utils/cn';

export const Badge = ({ status, children, variant, dot = false, className = '' }) => {
  const effectiveKey = (variant || status || '').toUpperCase();

  const variants = {
    [EPISTEMIC_STATUS.VERIFIED]: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
    VERIFIED: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
    [EPISTEMIC_STATUS.AI_INFERENCE]: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    AI_INFERENCE: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    INFERENCE: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    [EPISTEMIC_STATUS.UNKNOWN]: 'bg-slate-800/80 text-slate-300 border-slate-700/80',
    UNKNOWN: 'bg-slate-800/80 text-slate-300 border-slate-700/80',

    // Risk levels & severities
    LOW: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
    MEDIUM: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    MODERATE: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    HIGH: 'bg-orange-500/10 text-orange-300 border-orange-500/30',
    CRITICAL: 'bg-rose-500/10 text-rose-300 border-rose-500/30',
    INCONCLUSIVE: 'bg-slate-800/80 text-slate-300 border-slate-700/80',

    // Statuses
    COMPLETED: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
    PROCESSING: 'bg-sky-500/10 text-sky-300 border-sky-500/30',
    PENDING: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    DRAFT: 'bg-slate-800/80 text-slate-300 border-slate-700/80',
    FAILED: 'bg-rose-500/10 text-rose-300 border-rose-500/30',

    // General
    ACCENT: 'bg-blue-500/10 text-blue-300 border-blue-500/30',
    DEFAULT: 'bg-navy-800/90 text-slate-300 border-white/[0.08]',
  };

  const dotColors = {
    LOW: 'bg-emerald-400',
    VERIFIED: 'bg-emerald-400',
    MEDIUM: 'bg-amber-400',
    MODERATE: 'bg-amber-400',
    AI_INFERENCE: 'bg-amber-400',
    HIGH: 'bg-orange-400',
    CRITICAL: 'bg-rose-400',
    FAILED: 'bg-rose-400',
    PROCESSING: 'bg-sky-400',
    PENDING: 'bg-amber-400',
    UNKNOWN: 'bg-slate-400',
    DRAFT: 'bg-slate-400',
  };

  const colorClass = variants[effectiveKey] || variants.DEFAULT;
  const dotColorClass = dotColors[effectiveKey] || 'bg-slate-400';

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-2xs font-medium tracking-wide uppercase border select-none',
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
