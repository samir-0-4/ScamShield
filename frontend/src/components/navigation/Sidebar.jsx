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
  <div className="flex h-full flex-col bg-[#0b0e16] select-none">
    {/* Product branding */}
    <div className="flex items-center gap-3 px-5 h-16 border-b border-white/[0.08] shrink-0">
      <div className="p-2 rounded-lg bg-blue-500/10 border border-blue-500/25 text-blue-400 flex items-center justify-center shadow-sm">
        <ShieldCheck className="w-5 h-5" />
      </div>
      <div>
        <span className="font-bold tracking-tight text-white text-base">
          Phantom<span className="text-blue-400">Trace</span>
        </span>
        <span className="block text-[10px] uppercase tracking-widest text-slate-400 font-semibold font-mono">
          Security Forensics
        </span>
      </div>
    </div>

    {/* Primary Navigation */}
    <nav className="flex-1 px-3 py-4 space-y-1.5 overflow-y-auto" aria-label="Main Navigation">
      <div className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-wider text-slate-400 font-mono">
        Platform
      </div>
      {NAV.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
              isActive
                ? 'bg-blue-500/15 text-blue-300 border border-blue-500/30 font-semibold shadow-[0_0_12px_rgba(59,130,246,0.12)]'
                : 'text-slate-400 hover:text-slate-100 hover:bg-white/[0.05] border border-transparent'
            )
          }
        >
          <Icon className="w-[18px] h-[18px] shrink-0" />
          <span className="truncate">{label}</span>
        </NavLink>
      ))}
    </nav>

    {/* Engine status & session telemetry */}
    <div className="p-4 border-t border-white/[0.08] bg-black/20 shrink-0 space-y-2">
      <div className="flex items-center justify-between text-2xs text-slate-400 font-mono">
        <span className="flex items-center gap-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
          Engine Online
        </span>
        <span className="text-slate-400 font-semibold">v1.2</span>
      </div>
      <p className="text-[11px] text-slate-400 leading-tight">
        PhantomTrace AI · Digital Threat Investigation
      </p>
    </div>
  </div>
);

export default Sidebar;
