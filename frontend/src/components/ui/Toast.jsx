import { createContext, useContext, useState, useCallback } from 'react';
import { createPortal } from 'react-dom';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';
import { cn } from '../../utils/cn';

const TOAST_ICONS = {
  success: CheckCircle2,
  error: AlertCircle,
  info: Info,
};

const TOAST_BORDERS = {
  success: 'border-emerald-500/35 bg-[#0a1815]/95 text-emerald-300 shadow-[0_8px_32px_rgba(16,185,129,0.25)]',
  error: 'border-rose-500/35 bg-[#1b0a11]/95 text-rose-300 shadow-[0_8px_32px_rgba(244,63,94,0.25)]',
  info: 'border-cyan-500/35 bg-[#0b101c]/95 text-cyan-300 shadow-[0_8px_32px_rgba(59,130,246,0.25)]',
};

const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, message, type }]);

    if (duration > 0) {
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider
      value={{
        addToast,
        success: (msg) => addToast(msg, 'success'),
        error: (msg) => addToast(msg, 'error'),
      }}
    >
      {children}
      {typeof document !== 'undefined' &&
        createPortal(
          <div
            aria-live="polite"
            className="fixed bottom-4 right-4 z-toast flex flex-col gap-2.5 max-w-sm w-full pointer-events-none p-2"
          >
            {toasts.map((toast) => {
              const Icon = TOAST_ICONS[toast.type] || Info;
              return (
                <div
                  key={toast.id}
                  className={cn(
                    'pointer-events-auto flex items-start gap-3 p-4 rounded-2xl border backdrop-blur-xl transition-all duration-fast shadow-xl',
                    TOAST_BORDERS[toast.type] || TOAST_BORDERS.info
                  )}
                >
                  <Icon className="w-5 h-5 shrink-0 mt-0.5" />
                  <p className="text-xs sm:text-sm font-medium flex-1 leading-snug">{toast.message}</p>
                  <button
                    type="button"
                    onClick={() => removeToast(toast.id)}
                    className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/[0.08] transition-colors"
                    aria-label="Dismiss notification"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              );
            })}
          </div>,
          document.body
        )}
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    return {
      addToast: () => {},
      success: () => {},
      error: () => {},
    };
  }
  return context;
};

export default ToastProvider;
