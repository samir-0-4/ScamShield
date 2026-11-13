import { cn } from '../../utils/cn';

export const EmptyState = ({
  icon: Icon,
  title,
  message,
  action,
  className = '',
}) => (
  <div className={cn('text-center py-12 px-4 sm:px-6 space-y-4 min-w-0', className)}>
    {Icon && (
      <div className="relative mx-auto w-14 h-14 rounded-2xl bg-[#111728] border border-white/[0.1] flex items-center justify-center text-slate-300 shadow-[0_4px_20px_rgba(0,0,0,0.5),inset_0_1px_0_0_rgba(255,255,255,0.1)]">
        <Icon className="w-6 h-6 text-cyan-300" aria-hidden="true" />
      </div>
    )}
    <div className="space-y-1.5 max-w-sm mx-auto">
      {title && <h3 className="text-sm sm:text-base font-bold text-white tracking-tight">{title}</h3>}
      {message && (
        <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
          {message}
        </p>
      )}
    </div>
    {action && <div className="pt-2">{action}</div>}
  </div>
);

export default EmptyState;
