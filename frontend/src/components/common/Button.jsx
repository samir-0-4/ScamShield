import { cn } from '../../utils/cn';
import { Spinner } from '../ui/Spinner';

const BASE_BUTTON_CLASSES =
  'relative inline-flex items-center justify-center gap-2 font-medium tracking-tight transition-all duration-150 ease-out select-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-[#04060a] focus-visible:ring-blue-500 disabled:opacity-45 disabled:pointer-events-none active:scale-[0.98]';

const SIZES = {
  xs: 'px-2.5 py-1 text-2xs min-h-[28px] rounded-lg',
  sm: 'px-3.5 py-1.5 text-xs min-h-[34px] rounded-lg',
  md: 'px-4.5 py-2 text-sm min-h-[40px] rounded-xl',
  lg: 'px-6 py-2.5 text-sm sm:text-base min-h-[46px] rounded-xl font-semibold',
  icon: 'p-2 w-9 h-9 min-h-[36px] rounded-xl',
};

const VARIANTS = {
  primary:
    'bg-gradient-to-b from-blue-500 to-blue-600 hover:from-blue-400 hover:to-blue-500 active:from-blue-600 active:to-blue-700 text-white border border-blue-400/35 shadow-[0_2px_12px_rgba(59,130,246,0.35),inset_0_1px_0_0_rgba(255,255,255,0.25)] hover:shadow-[0_4px_20px_rgba(59,130,246,0.5),inset_0_1px_0_0_rgba(255,255,255,0.3)]',
  secondary:
    'bg-[#0f172a] hover:bg-[#16223d] active:bg-[#0c1322] text-slate-200 hover:text-white border border-white/[0.12] hover:border-white/[0.22] shadow-[0_2px_8px_rgba(0,0,0,0.5),inset_0_1px_0_0_rgba(255,255,255,0.08)]',
  danger:
    'bg-gradient-to-b from-rose-500 to-rose-600 hover:from-rose-400 hover:to-rose-500 active:from-rose-600 active:to-rose-700 text-white border border-rose-400/35 shadow-[0_2px_12px_rgba(244,63,94,0.35),inset_0_1px_0_0_rgba(255,255,255,0.2)] hover:shadow-[0_4px_20px_rgba(244,63,94,0.5)] focus-visible:ring-rose-500',
  ghost:
    'text-slate-300 hover:text-white hover:bg-white/[0.08] active:bg-white/[0.12] border border-transparent',
  outline:
    'border border-white/[0.14] hover:border-blue-500/60 text-slate-200 hover:text-blue-300 bg-[#0a0f1d]/70 hover:bg-blue-500/[0.08] active:bg-blue-500/15 shadow-[0_2px_8px_rgba(0,0,0,0.4)]',
};

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
}) => (
  <button
    type={type}
    disabled={disabled || loading}
    onClick={onClick}
    className={cn(BASE_BUTTON_CLASSES, SIZES[size] || SIZES.md, VARIANTS[variant] || VARIANTS.primary, className)}
    {...props}
  >
    {loading && <Spinner className="w-3.5 h-3.5 text-current shrink-0" label="Processing…" />}
    <span className={cn('inline-flex items-center gap-1.5', loading && 'opacity-80')}>
      {children}
    </span>
  </button>
);

export default Button;
