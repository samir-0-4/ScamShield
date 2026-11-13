import { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { X } from 'lucide-react';
import Sidebar from '../navigation/Sidebar';
import Topbar from '../navigation/Topbar';
import { cn } from '../../utils/cn';

export const DashboardLayout = () => {
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    if (!mobileOpen) return undefined;
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') setMobileOpen(false);
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mobileOpen]);

  return (
    <div className="min-h-dvh bg-[#05070c] text-slate-100 flex flex-col antialiased selection:bg-blue-500/30 selection:text-white relative bg-cyber-grid">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 z-modal px-4 py-2 bg-blue-600 text-white font-medium rounded-xl shadow-lg focus:outline-none"
      >
        Skip to main content
      </a>

      <aside className="hidden lg:flex lg:fixed lg:inset-y-0 lg:w-64 lg:flex-col bg-[#080d18]/95 backdrop-blur-xl border-r border-white/[0.08] z-sticky shadow-2xl">
        <Sidebar />
      </aside>

      <div
        className={cn(
          'lg:hidden fixed inset-0 z-modal transition-opacity duration-fast ease-out',
          mobileOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        )}
        aria-hidden={!mobileOpen}
      >
        <div
          className="fixed inset-0 bg-black/80 backdrop-blur-md"
          onClick={() => setMobileOpen(false)}
        />
        <aside
          role="dialog"
          aria-modal="true"
          aria-label="Mobile Navigation Drawer"
          className={cn(
            'fixed inset-y-0 left-0 w-72 max-w-[85vw] bg-[#080d18] border-r border-white/[0.08] shadow-2xl transition-transform duration-fast ease-out flex flex-col',
            mobileOpen ? 'translate-x-0' : '-translate-x-full'
          )}
        >
          <div className="absolute top-4 right-4 z-10">
            <button
              type="button"
              onClick={() => setMobileOpen(false)}
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/[0.08] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
              aria-label="Close navigation"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
          <Sidebar onNavigate={() => setMobileOpen(false)} />
        </aside>
      </div>

      <div className="lg:pl-64 min-w-0 flex flex-col flex-1">
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <main
          id="main-content"
          tabIndex={-1}
          className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 min-w-0 flex-1 focus:outline-none"
        >
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
