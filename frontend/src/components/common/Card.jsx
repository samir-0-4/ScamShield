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
        'rounded-xl border border-white/[0.08] bg-[#0e121b]/95 shadow-card min-w-0',
        interactive &&
          'transition-all duration-150 ease-out hover:border-white/[0.16] hover:bg-[#121622] focus-within:border-white/[0.2]',
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
        <div className="border-t border-white/[0.07] px-5 sm:px-6 py-3.5 bg-black/25 rounded-b-xl">
          {footer}
        </div>
      )}
    </div>
  );
};

export default Card;
