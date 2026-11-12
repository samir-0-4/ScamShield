import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

export const PublicLayout = ({ children }) => (
  <div className="min-h-dvh flex flex-col bg-[#08090d] text-slate-100 antialiased selection:bg-blue-500/30 selection:text-white">
    <Navbar />
    <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12 min-w-0">
      {children || <Outlet />}
    </main>
    <footer className="border-t border-white/[0.08] bg-[#06070a] py-8 text-center text-xs text-slate-400 select-none">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-4">
        <p className="font-mono text-2xs text-slate-400">
          PhantomTrace &copy; 2026 — AI-Powered Digital Threat &amp; Scam Investigation Engine
        </p>
        <div className="flex items-center gap-4 text-slate-500 text-2xs font-mono">
          <span className="hover:text-slate-300 transition-colors">Zero Plaintext Storage</span>
          <span>•</span>
          <span className="hover:text-slate-300 transition-colors">Epistemic Transparency</span>
          <span>•</span>
          <span className="hover:text-slate-300 transition-colors">Sandboxed Execution</span>
        </div>
      </div>
    </footer>
  </div>
);

export default PublicLayout;
