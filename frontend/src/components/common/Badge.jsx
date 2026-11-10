import { EPISTEMIC_STATUS } from '../../constants';
import { cn } from '../../utils/cn';

export const Badge = ({ status, children, className = '' }) => {
  const variants = {
    [EPISTEMIC_STATUS.VERIFIED]:
      'bg-emerald-950/80 text-emerald-400 border border-emerald-800',
    [EPISTEMIC_STATUS.AI_INFERENCE]:
      'bg-amber-950/80 text-amber-400 border border-amber-800',
    [EPISTEMIC_STATUS.UNKNOWN]:
      'bg-slate-800 text-slate-400 border border-slate-700',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium tracking-wide uppercase',
        variants[status] || 'bg-accent-500/10 text-accent-400 border border-accent-500/30',
        className
      )}
    >
      {children || status}
    </span>
  );
};

export default Badge;
