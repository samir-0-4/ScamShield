import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderSearch,
  PlusCircle,
  FileText,
  Settings,
  ShieldCheck,
} from 'lucide-react';
import { cn } from '../../utils/cn';

const NAV = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/investigations', label: 'Investigations', icon: FolderSearch },
  { to: '/investigations/new', label: 'New Investigation', icon: PlusCircle },
  { to: '/reports', label: 'Reports', icon: FileText },
  { to: '/settings', label: 'Settings', icon: Settings },
];

export const Sidebar = ({ onNavigate }) => (
  <div className="flex h-full flex-col bg-[#070b14] select-none">
    <div className="flex items-center gap-3 px-6 h-18 border-b border-white/[0.08] shrink-0">
      <div className="p-2.5 rounded-xl bg-gradient-to-br from-blue-500/20 to-indigo-500/20 border border-blue-400/30 text-blue-400 flex items-center justify-center shadow-[0_0_16px_rgba(59,130,246,0.25)]">
        <ShieldCheck className="w-5 h-5" />
      </div>
      <div>
        <span className="font-extrabold tracking-tight text-white text-base">
          Phantom<span className="bg-gradient-to-r from-blue-400 to-indigo-400 bg-clip-text text-transparent">Trace</span>
        </span>
        <span className="block text-[9px] uppercase tracking-widest text-slate-400 font-semibold font-mono">
          Security Forensics
        </span>
      </div>
    </div>

    <nav className="flex-1 px-3.5 py-5 space-y-1.5 overflow-y-auto" aria-label="Main Navigation">
      <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-400 font-mono">
        Platform
      </div>
      {NAV.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'group relative flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-150',
              isActive
                ? 'bg-gradient-to-r from-blue-500/20 to-blue-500/5 text-white border border-blue-500/35 font-semibold shadow-[0_2px_12px_rgba(59,130,246,0.15)]'
                : 'text-slate-400 hover:text-slate-100 hover:bg-white/[0.05] border border-transparent'
            )
          }
        >
          {({ isActive }) => (
            <>
              {isActive && (
                <span className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 rounded-r-full bg-blue-400 shadow-[0_0_8px_rgba(59,130,246,0.8)]" />
              )}
              <Icon className={cn('w-[18px] h-[18px] shrink-0 transition-colors', isActive ? 'text-blue-400' : 'text-slate-400 group-hover:text-slate-200')} />
              <span className="truncate">{label}</span>
            </>
          )}
        </NavLink>
      ))}
    </nav>

    <div className="p-4.5 border-t border-white/[0.08] bg-black/30 shrink-0 space-y-2.5">
      <div className="flex items-center justify-between text-2xs text-slate-400 font-mono">
        <span className="flex items-center gap-2 font-medium">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
          AI Engine Online
        </span>
        <span className="text-slate-400 font-semibold px-2 py-0.5 rounded-md bg-white/[0.05] border border-white/[0.06]">v1.2</span>
      </div>
      <p className="text-[11px] text-slate-400 leading-tight">
        PhantomTrace Autonomous Forensics
      </p>
    </div>
  </div>
);

export default Sidebar;
