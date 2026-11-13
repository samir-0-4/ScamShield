import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import { cn } from '../../utils/cn';

export const Modal = ({
  open,
  onClose,
  title,
  description,
  children,
  className = '',
  maxWidth = 'max-w-lg',
}) => {
  const overlayRef = useRef(null);
  const previouslyFocusedRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;

    previouslyFocusedRef.current = document.activeElement;

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose?.();
      }
    };

    const originalOverflow = document.body.style.overflow;
    const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
    document.body.style.overflow = 'hidden';
    if (scrollbarWidth > 0) {
      document.body.style.paddingRight = `${scrollbarWidth}px`;
    }

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = originalOverflow;
      document.body.style.paddingRight = '';
      window.removeEventListener('keydown', handleKeyDown);
      if (previouslyFocusedRef.current?.focus) {
        previouslyFocusedRef.current.focus();
      }
    };
  }, [open, onClose]);

  if (!open) return null;

  return createPortal(
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? 'modal-title' : undefined}
      aria-describedby={description ? 'modal-description' : undefined}
      className="fixed inset-0 z-modal flex items-center justify-center p-4 sm:p-6 overflow-y-auto"
    >
      <div
        ref={overlayRef}
        onClick={onClose}
        className="fixed inset-0 bg-black/85 backdrop-blur-md transition-opacity duration-fast ease-out"
      />

      <div
        className={cn(
          'relative w-full rounded-2xl border border-white/[0.12] bg-[#0c101c] p-6 sm:p-7 shadow-[0_24px_64px_-8px_rgba(0,0,0,0.9),inset_0_1px_0_0_rgba(255,255,255,0.12)] z-10 min-w-0 transition-all duration-fast ease-out transform scale-100',
          maxWidth,
          className
        )}
      >
        <button
          type="button"
          onClick={onClose}
          aria-label="Close dialog"
          className="absolute right-4 top-4 p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/[0.08] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
        >
          <X className="w-4 h-4" />
        </button>

        {title && (
          <div className="mb-5 pr-8 space-y-1">
            <h2 id="modal-title" className="text-lg sm:text-xl font-bold text-white tracking-tight">
              {title}
            </h2>
            {description && (
              <p id="modal-description" className="text-xs text-slate-400 leading-relaxed">
                {description}
              </p>
            )}
          </div>
        )}

        <div className="min-w-0">{children}</div>
      </div>
    </div>,
    document.body
  );
};

export default Modal;
