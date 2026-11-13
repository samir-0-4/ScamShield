import { cn } from '../../utils/cn';

export const Card = ({
  children,
  title,
  subtitle,
  className = '',
  headerAction,
  footer,
  padded = true,
  interactive = false,
  ...props
}) => {
  return (
    <div
      className={cn(
        'rounded-2xl border border-white/[0.08] bg-[#0c101c]/90 backdrop-blur-xl shadow-[0_4px_24px_-4px_rgba(0,0,0,0.6),inset_0_1px_0_0_rgba(255,255,255,0.08)] min-w-0 transition-all duration-200',
        interactive &&
          'hover:border-cyan-400/30 hover:bg-[#101626] hover:shadow-[0_12px_36px_-6px_rgba(0,0,0,0.7),inset_0_1px_0_0_rgba(255,255,255,0.12)] focus-within:border-cyan-500/50',
        className
      )}
      {...props}
    >
      {(title || subtitle || headerAction) && (
        <div className="flex items-start justify-between gap-3 border-b border-white/[0.07] px-5 sm:px-6 py-4">
          <div className="min-w-0 flex-1">
            {title && (
              <h3 className="text-sm sm:text-base font-semibold text-slate-100 tracking-tight truncate">
                {title}
              </h3>
            )}
            {subtitle && (
              <p className="text-xs text-slate-400 mt-0.5 leading-relaxed">
                {subtitle}
              </p>
            )}
          </div>
          {headerAction && <div className="shrink-0">{headerAction}</div>}
        </div>
      )}
      <div className={cn('min-w-0', padded && 'p-5 sm:p-6')}>{children}</div>
      {footer && (
        <div className="border-t border-white/[0.07] px-5 sm:px-6 py-3.5 bg-black/30 rounded-b-2xl">
          {footer}
        </div>
      )}
    </div>
  );
};

export default Card;
