import { Link, useNavigate } from 'react-router-dom';
import { ShieldCheck, Compass, PlusCircle, LogIn, LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export const Navbar = () => {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-sticky backdrop-blur-xl bg-[#08090d]/85 border-b border-white/[0.08] select-none transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link
          to="/"
          className="flex items-center gap-2.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded-lg p-1 group"
        >
          <div className="p-1.5 bg-blue-500/10 border border-blue-500/25 rounded-lg text-blue-400 group-hover:border-blue-500/40 transition-colors shadow-sm">
            <ShieldCheck className="w-5 h-5 shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
          </div>
          <div>
            <span className="font-bold text-base tracking-tight text-white group-hover:text-slate-100 transition-colors">
              Phantom<span className="text-blue-400">Trace</span>
            </span>
          </div>
        </Link>

        <nav className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-300 hover:text-white px-3.5 py-2 rounded-lg hover:bg-white/[0.06] transition-colors"
              >
                <Compass className="w-4 h-4 text-current shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                <span className="hidden sm:inline">Dashboard</span>
              </Link>
              <Link
                to="/investigations/new"
                className="inline-flex items-center gap-1.5 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-3.5 py-2 rounded-lg transition-all shadow-sm hover:shadow-[0_0_16px_rgba(59,130,246,0.25)] border border-blue-400/30"
              >
                <PlusCircle className="w-4 h-4 text-current shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                <span>New Case</span>
              </Link>
              <div className="hidden sm:flex items-center gap-3 pl-3 border-l border-white/[0.08]">
                <span className="text-xs text-slate-400 font-mono truncate max-w-[120px]">
                  {user?.fullName || 'Analyst'}
                </span>
                <button
                  onClick={handleLogout}
                  className="min-w-[36px] min-h-[36px] flex items-center justify-center text-slate-400 hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition-colors"
                  title="Sign out"
                  aria-label="Sign out"
                >
                  <LogOut className="w-4 h-4 text-current shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2.5">
              <Link
                to="/login"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-300 hover:text-white px-3.5 py-2 rounded-lg hover:bg-white/[0.06] transition-colors"
              >
                <LogIn className="w-4 h-4 text-current shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                <span>Sign In</span>
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg shadow-sm hover:shadow-[0_0_16px_rgba(59,130,246,0.25)] transition-all border border-blue-400/30"
              >
                Get Started
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Navbar;
