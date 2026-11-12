import { cn } from '../../utils/cn';
import { Spinner } from '../ui/Spinner';

export const Button = ({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  onClick,
  ...props
}) => {
  const base =
    'relative inline-flex items-center justify-center gap-2 font-medium transition-all duration-150 ease-out select-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-navy-950 focus-visible:ring-blue-500 disabled:opacity-40 disabled:pointer-events-none active:scale-[0.98]';

  const sizes = {
    xs: 'px-2.5 py-1 text-2xs min-h-[28px] rounded-md',
    sm: 'px-3 py-1.5 text-xs min-h-[32px] rounded-md',
    md: 'px-4 py-2 text-sm min-h-[38px] rounded-lg',
    lg: 'px-5 py-2.5 text-sm sm:text-base min-h-[44px] rounded-lg',
    icon: 'p-2 w-9 h-9 min-h-[36px] rounded-lg',
  };

  const variants = {
    primary:
      'bg-blue-600 hover:bg-blue-500 active:bg-blue-700 text-white border border-blue-400/25 shadow-sm hover:shadow-[0_0_16px_rgba(59,130,246,0.22)]',
    secondary:
      'bg-navy-800/90 hover:bg-navy-750 active:bg-navy-850 text-slate-200 hover:text-white border border-white/[0.08] hover:border-white/[0.16] shadow-sm',
    danger:
      'bg-rose-600 hover:bg-rose-500 active:bg-rose-700 text-white border border-rose-400/30 shadow-sm hover:shadow-[0_0_16px_rgba(244,63,94,0.22)] focus-visible:ring-rose-500',
    ghost:
      'text-slate-300 hover:text-white hover:bg-white/[0.06] active:bg-white/[0.1] border border-transparent',
    outline:
      'border border-white/[0.12] hover:border-blue-500/50 text-slate-200 hover:text-blue-400 bg-navy-900/60 hover:bg-blue-500/[0.08] active:bg-blue-500/15',
  };

  return (
    <button
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={cn(base, sizes[size] || sizes.md, variants[variant] || variants.primary, className)}
      {...props}
    >
      {loading ? <Spinner className="w-3.5 h-3.5 text-current shrink-0" label="Processing…" /> : null}
      <span className={cn('inline-flex items-center gap-1.5', loading && 'opacity-80')}>
        {children}
      </span>
    </button>
  );
};

export default Button;
