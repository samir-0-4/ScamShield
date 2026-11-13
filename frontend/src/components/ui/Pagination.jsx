import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '../../utils/cn';

export const Pagination = ({ page, pageSize, totalElements, totalPages, onPageChange }) => {
  if (!totalElements || totalPages <= 1) {
    return (
      <div className="flex items-center justify-between py-3.5 text-xs text-slate-500 font-mono">
        <span>
          {totalElements} item{totalElements === 1 ? '' : 's'}
        </span>
      </div>
    );
  }

  const from = page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, totalElements);

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 py-3.5 select-none">
      <span className="text-xs text-slate-400 font-mono">
        Showing <span className="text-slate-100 font-semibold">{from}</span>–
        <span className="text-slate-100 font-semibold">{to}</span> of{' '}
        <span className="text-cyan-400 font-semibold">{totalElements}</span> cases
      </span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className={cn(
            'inline-flex items-center justify-center w-8 h-8 rounded-xl border border-white/[0.08] bg-[#0c101c] text-slate-300',
            'hover:bg-[#151c2e] hover:text-white hover:border-white/[0.18] transition-all duration-150',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-500 disabled:opacity-35 disabled:pointer-events-none'
          )}
          aria-label="Previous page"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <span className="text-xs text-slate-200 font-mono px-2.5 py-1 rounded-lg bg-black/40 border border-white/[0.06] tabular-nums font-semibold">
          {page + 1} / {totalPages}
        </span>
        <button
          type="button"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className={cn(
            'inline-flex items-center justify-center w-8 h-8 rounded-xl border border-white/[0.08] bg-[#0c101c] text-slate-300',
            'hover:bg-[#151c2e] hover:text-white hover:border-white/[0.18] transition-all duration-150',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-500 disabled:opacity-35 disabled:pointer-events-none'
          )}
          aria-label="Next page"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
