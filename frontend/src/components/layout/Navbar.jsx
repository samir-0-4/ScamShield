import { Link, useNavigate } from 'react-router-dom';
import { ShieldCheck, Compass, PlusCircle, LogIn, LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import Button from '../common/Button';

export const Navbar = () => {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-sticky backdrop-blur-xl bg-[#060911]/85 border-b border-white/[0.08] select-none transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link
          to="/"
          className="flex items-center gap-3 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-500 rounded-xl p-1 group"
        >
          <div className="p-2 bg-gradient-to-br from-cyan-500/20 to-violet-500/20 border border-cyan-400/30 rounded-xl text-cyan-400 group-hover:border-cyan-400/60 group-hover:shadow-[0_0_16px_rgba(59,130,246,0.35)] transition-all shadow-sm">
            <ShieldCheck className="w-5 h-5 shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
          </div>
          <div>
            <span className="font-extrabold text-base tracking-tight text-white group-hover:text-slate-100 transition-colors">
              Phantom<span className="bg-gradient-to-r from-cyan-400 to-violet-400 bg-clip-text text-transparent">Trace</span>
            </span>
            <span className="block text-[9px] uppercase tracking-widest text-slate-400 font-mono font-semibold">
              Threat Forensics
            </span>
          </div>
        </Link>

        <nav className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className="inline-flex items-center gap-2 text-xs font-semibold text-slate-300 hover:text-white px-3.5 py-2 rounded-xl hover:bg-white/[0.06] transition-colors"
              >
                <Compass className="w-4 h-4 text-cyan-400 shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                <span className="hidden sm:inline">Dashboard</span>
              </Link>
              <Link to="/investigations/new">
                <Button size="sm" className="font-semibold shadow-md">
                  <PlusCircle className="w-4 h-4 shrink-0" aria-hidden="true" />
                  <span>New Case</span>
                </Button>
              </Link>
              <div className="hidden sm:flex items-center gap-3 pl-3 border-l border-white/[0.08]">
                <span className="text-xs text-slate-300 font-mono font-medium truncate max-w-[120px]">
                  {user?.fullName || 'Analyst'}
                </span>
                <button
                  onClick={handleLogout}
                  className="min-w-[36px] min-h-[36px] flex items-center justify-center text-slate-400 hover:text-rose-400 rounded-xl hover:bg-rose-500/10 transition-colors"
                  title="Sign out"
                  aria-label="Sign out"
                >
                  <LogOut className="w-4 h-4 shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2.5">
              <Link
                to="/login"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-300 hover:text-white px-3.5 py-2 rounded-xl hover:bg-white/[0.06] transition-colors"
              >
                <LogIn className="w-4 h-4 text-slate-400 shrink-0 transform-gpu translate-y-[0.5px]" aria-hidden="true" />
                <span>Sign In</span>
              </Link>
              <Link to="/register">
                <Button size="sm" className="font-semibold shadow-md">
                  Get Started
                </Button>
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Navbar;
