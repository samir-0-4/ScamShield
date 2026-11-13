import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { FolderSearch, PlusCircle, Search, ChevronRight, X, Clock } from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import { StatusBadge, RiskBadge, CategoryBadge } from '../components/ui/StatusBadge';
import { EmptyState } from '../components/ui/EmptyState';
import { TableRowSkeleton } from '../components/ui/Skeleton';
import Pagination from '../components/ui/Pagination';
import Select from '../components/ui/Select';
import {
  INVESTIGATION_STATUS_LIST,
  INVESTIGATION_CATEGORY_LIST,
  categoryLabel,
} from '../constants';
import { investigationService } from '../services/investigation/investigationService';
import { formatRelative } from '../utils/format';

const PAGE_SIZE = 10;

const statusOptions = [
  { value: '', label: 'All statuses' },
  ...INVESTIGATION_STATUS_LIST.map((s) => ({
    value: s,
    label: s.charAt(0) + s.slice(1).toLowerCase(),
  })),
];

const categoryOptions = [
  { value: '', label: 'All categories' },
  ...INVESTIGATION_CATEGORY_LIST.map((c) => ({ value: c, label: categoryLabel(c) })),
];

export const InvestigationsPage = () => {
  const [page, setPage] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchPage = useCallback(
    (pageNum = 0, status = '') => {
      setLoading(true);
      investigationService
        .list({
          page: pageNum,
          size: PAGE_SIZE,
          sort: 'createdAt,desc',
          status: status || undefined,
        })
        .then((res) => {
          setPage(res);
          setError('');
        })
        .catch(() => setError('Failed to load investigations.'))
        .finally(() => setLoading(false));
    },
    []
  );

  useEffect(() => {
    fetchPage(currentPage, statusFilter);
  }, [fetchPage, currentPage, statusFilter]);

  const handleStatusChange = (e) => {
    setStatusFilter(e.target.value);
    setCurrentPage(0);
  };

  const handleCategoryChange = (e) => {
    setCategoryFilter(e.target.value);
  };

  const clearFilters = () => {
    setSearch('');
    setStatusFilter('');
    setCategoryFilter('');
    setCurrentPage(0);
  };

  const content = page?.content || [];
  const displayed = content.filter((inv) => {
    if (categoryFilter && inv.category !== categoryFilter) return false;
    if (search && !inv.title.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  return (
    <div className="space-y-6 min-w-0">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/[0.08] pb-6 min-w-0">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            Investigations
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Active and archived digital threat cases across all vectors
          </p>
        </div>
        <Link to="/investigations/new">
          <Button className="flex items-center gap-2 font-semibold shadow-lg">
            <PlusCircle className="w-4 h-4" />
            <span>New Investigation</span>
          </Button>
        </Link>
      </div>

      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <div className="relative flex-1 min-w-0">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
          <input
            type="text"
            placeholder="Search by case title…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="input-field pl-10"
          />
          {search && (
            <button
              type="button"
              onClick={() => setSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-white/[0.08] transition-colors"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-2.5 sm:w-auto">
          <Select
            value={statusFilter}
            onChange={handleStatusChange}
            options={statusOptions}
            className="sm:w-44 text-xs"
          />
          <Select
            value={categoryFilter}
            onChange={handleCategoryChange}
            options={categoryOptions}
            className="sm:w-48 text-xs"
          />
        </div>
      </div>

      <Card padded={false} className="overflow-hidden shadow-card">
        {loading ? (
          <div className="p-6">
            <table className="w-full text-left">
              <tbody>
                <TableRowSkeleton rows={5} />
              </tbody>
            </table>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-sm text-rose-400 font-medium">{error}</div>
        ) : displayed.length === 0 ? (
          <EmptyState
            icon={FolderSearch}
            title={
              search || statusFilter || categoryFilter
                ? 'No matching investigations'
                : 'No investigations yet'
            }
            message={
              search || statusFilter || categoryFilter
                ? 'Try adjusting your search query or clear active filters.'
                : 'Create your first case to start investigating.'
            }
            action={
              search || statusFilter || categoryFilter ? (
                <Button variant="secondary" size="sm" onClick={clearFilters}>
                  Clear all filters
                </Button>
              ) : (
                <Link to="/investigations/new">
                  <Button variant="outline" size="sm" className="font-semibold shadow-sm">
                    Create Investigation
                  </Button>
                </Link>
              )
            }
          />
        ) : (
          <div className="divide-y divide-white/[0.06]">
            {displayed.map((inv) => (
              <Link
                key={inv.id}
                to={`/investigations/${inv.id}`}
                className="flex items-center justify-between gap-4 px-5 sm:px-6 py-4.5 hover:bg-white/[0.04] transition-all duration-150 group"
              >
                <div className="min-w-0 flex-1 space-y-1.5">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <p className="text-sm sm:text-base font-semibold text-slate-100 truncate group-hover:text-blue-300 transition-colors">
                      {inv.title}
                    </p>
                    {inv.category && <CategoryBadge category={inv.category} />}
                  </div>
                  <div className="flex items-center gap-3 text-xs text-slate-400 font-mono">
                    <span className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      {formatRelative(inv.createdAt)}
                    </span>
                    <span className="text-slate-600">•</span>
                    <span>{inv.evidenceCount || 0} evidence items</span>
                    {inv.id && (
                      <>
                        <span className="hidden md:inline text-slate-600">•</span>
                        <span className="hidden md:inline text-2xs text-slate-400 truncate max-w-[140px]" title={inv.id}>
                          {inv.id}
                        </span>
                      </>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-3 shrink-0">
                  <StatusBadge status={inv.status} />
                  {inv.overallRiskLevel && inv.overallRiskLevel !== 'UNKNOWN' && (
                    <RiskBadge level={inv.overallRiskLevel} className="hidden sm:inline-flex" />
                  )}
                  <ChevronRight className="w-4 h-4 text-slate-400 group-hover:text-slate-200 group-hover:translate-x-1 transition-all" />
                </div>
              </Link>
            ))}
          </div>
        )}

        {!loading && page && page.totalPages > 1 && (
          <div className="px-5 sm:px-6 border-t border-white/[0.08] bg-black/30">
            <Pagination
              page={page.pageNumber}
              pageSize={page.pageSize}
              totalElements={page.totalElements}
              totalPages={page.totalPages}
              onPageChange={(p) => setCurrentPage(p)}
            />
          </div>
        )}
      </Card>
    </div>
  );
};

export default InvestigationsPage;
