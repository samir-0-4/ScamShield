import { cn } from '../../utils/cn';

export const EmptyState = ({
  icon: Icon,
  title,
  message,
  action,
  className = '',
}) => (
  <div className={cn('text-center py-12 px-4 sm:px-6 space-y-3.5 min-w-0', className)}>
    {Icon && (
      <div className="mx-auto w-12 h-12 rounded-xl bg-white/[0.04] border border-white/[0.08] flex items-center justify-center text-slate-400 shadow-sm">
        <Icon className="w-5 h-5 text-slate-300" />
      </div>
    )}
    {title && <h3 className="text-sm sm:text-base font-semibold text-white tracking-tight">{title}</h3>}
    {message && (
      <p className="text-xs sm:text-sm text-slate-400 max-w-sm mx-auto leading-relaxed">
        {message}
      </p>
    )}
    {action && <div className="pt-2">{action}</div>}
  </div>
);

export default EmptyState;
