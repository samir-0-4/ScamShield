import { useNavigate } from 'react-router-dom';
import { Menu, LogOut, User, Activity } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export const Topbar = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-sticky h-16 backdrop-blur-xl bg-[#0b0e16]/85 border-b border-white/[0.08] select-none transition-all">
      <div className="h-full flex items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onMenuClick}
            className="lg:hidden p-2 -ml-2 rounded-lg text-slate-300 hover:text-white hover:bg-white/[0.06] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
            aria-label="Open mobile navigation menu"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-lg bg-blue-500/10 border border-blue-500/25 text-2xs text-blue-300 font-mono shadow-sm">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
              <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500" />
            </span>
            <Activity className="w-3.5 h-3.5 text-blue-400 shrink-0" />
            <span>AI Threat Analysis Pipeline Active</span>
          </div>
        </div>

        <div className="flex items-center gap-3 sm:gap-4">
          <div className="flex items-center gap-2.5 text-xs text-slate-300">
            <div className="w-8 h-8 rounded-lg bg-white/[0.06] border border-white/[0.08] text-slate-300 flex items-center justify-center font-mono text-xs font-semibold shadow-sm">
              <User className="w-4 h-4 text-blue-400" />
            </div>
            <div className="hidden sm:block text-left">
              <p className="font-semibold text-slate-200 leading-none truncate max-w-[140px]">
                {user?.fullName || 'Analyst'}
              </p>
              <p className="text-[10px] text-slate-400 font-mono mt-1 leading-none">
                {user?.role?.replace('ROLE_', '') || 'ANALYST'}
              </p>
            </div>
          </div>

          <div className="h-4 w-px bg-white/[0.08]" />

          <button
            type="button"
            onClick={handleLogout}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/25 transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500"
            aria-label="Sign out"
            title="Sign out of PhantomTrace"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden md:inline">Sign out</span>
          </button>
        </div>
      </div>
    </header>
  );
};

export default Topbar;
