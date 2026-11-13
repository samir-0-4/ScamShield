import { statusMeta, riskMeta, categoryLabel } from '../../constants';
import { cn } from '../../utils/cn';

export const StatusBadge = ({ status, className = '' }) => {
  const meta = statusMeta(status);
  const isAnimated = status === 'PROCESSING';

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-2xs font-semibold tracking-wide uppercase border select-none transition-all duration-150',
        meta.badge,
        className
      )}
    >
      <span
        className={cn(
          'w-1.5 h-1.5 rounded-full shrink-0',
          meta.dot,
          isAnimated && 'animate-ping'
        )}
      />
      <span>{meta.label}</span>
    </span>
  );
};

export const RiskBadge = ({ level, className = '' }) => {
  const meta = riskMeta(level);
  const isCritical = level === 'CRITICAL';

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-2xs font-semibold tracking-wide uppercase border select-none transition-all duration-150',
        meta.badge,
        className
      )}
    >
      <span
        className={cn(
          'w-1.5 h-1.5 rounded-full shrink-0',
          meta.dot,
          isCritical && 'animate-pulse'
        )}
      />
      <span>{meta.label} risk</span>
    </span>
  );
};

export const CategoryBadge = ({ category, className = '' }) => (
  <span
    className={cn(
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-2xs font-medium bg-[#111728] text-slate-300 border border-white/[0.09] select-none shadow-xs',
      className
    )}
  >
    {categoryLabel(category)}
  </span>
);

export default StatusBadge;
