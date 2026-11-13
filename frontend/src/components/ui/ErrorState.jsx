import { AlertTriangle, RefreshCw } from 'lucide-react';
import Button from '../common/Button';
import { cn } from '../../utils/cn';

export const ErrorState = ({
  title = 'Failed to load content',
  message = 'An unexpected error occurred while communicating with the investigation engine.',
  onRetry,
  className = '',
}) => {
  return (
    <div
      role="alert"
      className={cn(
        'text-center py-10 px-4 sm:px-6 space-y-3.5 rounded-2xl border border-rose-500/25 bg-[#170a11]/90 backdrop-blur-md min-w-0 shadow-[0_4px_24px_rgba(244,63,94,0.15)]',
        className
      )}
    >
      <div className="mx-auto w-12 h-12 rounded-xl bg-rose-500/15 border border-rose-500/35 flex items-center justify-center text-rose-400 shadow-sm">
        <AlertTriangle className="w-5 h-5" aria-hidden="true" />
      </div>
      <div className="space-y-1">
        <h3 className="text-sm sm:text-base font-bold text-rose-200">{title}</h3>
        <p className="text-xs sm:text-sm text-rose-300/80 max-w-sm mx-auto leading-relaxed">{message}</p>
      </div>
      {onRetry && (
        <div className="pt-2">
          <Button variant="secondary" size="sm" onClick={onRetry} className="inline-flex items-center gap-2">
            <RefreshCw className="w-3.5 h-3.5" /> Retry
          </Button>
        </div>
      )}
    </div>
  );
};

export default ErrorState;
