import { cn } from '../../utils/cn';

export const Spinner = ({ className = '', label }) => (
  <span className={cn('inline-flex items-center gap-2.5', className)} role="status" aria-live="polite">
    <svg
      className={cn('animate-spin', className || 'w-5 h-5 text-cyan-400')}
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <circle className="opacity-20" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
      <path
        className="opacity-95"
        fill="currentColor"
        d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
      />
    </svg>
    {label && <span className="text-xs sm:text-sm font-medium text-slate-300 font-mono">{label}</span>}
  </span>
);

export const FullPageSpinner = ({ label = 'Loading…' }) => (
  <div className="flex min-h-[18rem] flex-col items-center justify-center py-24 space-y-4" aria-label={label}>
    <div className="relative flex items-center justify-center">
      <div className="absolute w-12 h-12 rounded-full bg-cyan-500/20 blur-md animate-pulse" />
      <Spinner className="w-8 h-8 text-cyan-400" />
    </div>
    <p className="text-xs sm:text-sm text-slate-400 font-mono tracking-wide">{label}</p>
  </div>
);

export default Spinner;
