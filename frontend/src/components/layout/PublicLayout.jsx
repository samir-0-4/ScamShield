import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

export const PublicLayout = ({ children }) => (
  <div className="min-h-dvh flex flex-col bg-[#04060a] text-slate-100 antialiased selection:bg-cyan-500/30 selection:text-white relative bg-cyber-grid">
    <div className="fixed top-0 left-1/2 -translate-x-1/2 w-[800px] h-[350px] bg-gradient-to-b from-cyan-600/15 via-violet-600/5 to-transparent blur-3xl pointer-events-none -z-10" />

    <Navbar />
    <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12 min-w-0">
      {children || <Outlet />}
    </main>
    <footer className="border-t border-white/[0.08] bg-[#030509]/90 backdrop-blur-md py-8 text-center text-xs text-slate-400 select-none">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2.5">
          <span className="font-bold text-sm tracking-tight text-white">
            Phantom<span className="text-cyan-400">Trace</span>
          </span>
          <span className="text-slate-600">•</span>
          <p className="font-mono text-2xs text-slate-400">
            &copy; 2026 AI-Powered Threat &amp; Scam Investigation Engine
          </p>
        </div>
        <div className="flex flex-wrap items-center justify-center gap-3 sm:gap-4 text-slate-400 text-2xs font-mono">
          <span className="hover:text-cyan-400 transition-colors">Zero Plaintext Storage</span>
          <span className="text-slate-700">•</span>
          <span className="hover:text-emerald-400 transition-colors">Epistemic Transparency</span>
          <span className="text-slate-700">•</span>
          <span className="hover:text-cyan-400 transition-colors">Sandboxed Execution</span>
        </div>
      </div>
    </footer>
  </div>
);

export default PublicLayout;
