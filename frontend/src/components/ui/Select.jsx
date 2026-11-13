import { useId } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '../../utils/cn';

export const Select = ({
  label,
  id,
  error,
  hint,
  options = [],
  children,
  className = '',
  required = false,
  disabled = false,
  ...props
}) => {
  const autoId = useId();
  const selectId = id || autoId;
  const errorId = `${selectId}-error`;

  return (
    <div className="w-full">
      {label && (
        <label htmlFor={selectId} className="input-label">
          {label} {required && <span className="text-rose-400">*</span>}
        </label>
      )}
      <div className="relative flex items-center">
        <select
          id={selectId}
          required={required}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          className={cn(
            'input-field appearance-none pr-10 cursor-pointer bg-[#090e1a]',
            error && 'border-rose-500/80 focus:border-rose-500 focus:ring-rose-500/25',
            className
          )}
          {...props}
        >
          {children ||
            options.map((o) => (
              <option key={o.value} value={o.value} className="bg-[#0b101c] text-slate-100 py-1">
                {o.label}
              </option>
            ))}
        </select>
        <ChevronDown className="absolute right-3.5 w-4 h-4 text-slate-400 pointer-events-none transition-transform duration-150" />
      </div>
      {error ? (
        <p id={errorId} className="mt-1.5 text-xs text-rose-400 font-medium flex items-center gap-1.5">
          <span className="w-1 h-1 rounded-full bg-rose-400" />
          {error}
        </p>
      ) : hint ? (
        <p className="mt-1.5 text-xs text-slate-400">{hint}</p>
      ) : null}
    </div>
  );
};

export default Select;
