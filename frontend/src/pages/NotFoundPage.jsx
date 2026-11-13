import { Link } from 'react-router-dom';
import { SearchX, Home, LayoutDashboard } from 'lucide-react';
import Button from '../components/common/Button';

export const NotFoundPage = () => (
  <div className="text-center py-20 sm:py-28 space-y-7 max-w-md mx-auto min-w-0">
    <div className="mx-auto w-18 h-18 rounded-3xl bg-gradient-to-br from-cyan-500/20 to-violet-500/20 border border-cyan-400/30 flex items-center justify-center text-cyan-400 shadow-[0_0_24px_rgba(59,130,246,0.35)]">
      <SearchX className="w-9 h-9 text-cyan-400" />
    </div>
    <div className="space-y-2">
      <p className="font-mono text-xs font-bold uppercase tracking-widest text-cyan-400">
        Status 404
      </p>
      <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
        Investigation target not found
      </h1>
      <p className="text-xs sm:text-sm text-slate-400 leading-relaxed max-w-sm mx-auto">
        The route, dossier, or analysis output you requested does not exist or has been moved.
      </p>
    </div>
    <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
      <Link to="/" className="w-full sm:w-auto">
        <Button variant="secondary" className="w-full sm:w-auto font-semibold shadow-sm">
          <Home className="w-4 h-4 mr-1.5" /> Return to Home
        </Button>
      </Link>
      <Link to="/dashboard" className="w-full sm:w-auto">
        <Button className="w-full sm:w-auto font-semibold shadow-md">
          <LayoutDashboard className="w-4 h-4 mr-1.5" /> Open Dashboard
        </Button>
      </Link>
    </div>
  </div>
);

export default NotFoundPage;
