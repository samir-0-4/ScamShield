import { Handle, Position } from '@xyflow/react';
import {
  AtSign,
  FileText,
  Globe2,
  Hash,
  Link2,
  Mail,
  MessageSquare,
  Network,
  QrCode,
  ShieldAlert,
  ShieldCheck,
  Target,
} from 'lucide-react';
import { cn } from '../../utils/cn';
import { titleCase } from '../../utils/format';

const icons = {
  INVESTIGATION: Target,
  MESSAGE: MessageSquare,
  URL: Link2,
  DOMAIN: Globe2,
  EMAIL: Mail,
  EMAIL_ADDRESS: AtSign,
  IP_ADDRESS: Hash,
  FILE: FileText,
  QR_DESTINATION: QrCode,
  THREAT_INDICATOR: ShieldAlert,
};

const colors = {
  INVESTIGATION: 'border-cyan-500/50 bg-[#0a1628]/95 text-cyan-200 shadow-[0_4px_20px_rgba(59,130,246,0.25)]',
  MESSAGE: 'border-sky-500/50 bg-[#081726]/95 text-sky-200 shadow-[0_4px_20px_rgba(14,165,233,0.2)]',
  URL: 'border-cyan-500/50 bg-[#071920]/95 text-cyan-200 shadow-[0_4px_20px_rgba(6,182,212,0.2)]',
  DOMAIN: 'border-violet-500/50 bg-[#0e122b]/95 text-violet-200 shadow-[0_4px_20px_rgba(99,102,241,0.2)]',
  EMAIL: 'border-violet-500/50 bg-[#140e28]/95 text-violet-200 shadow-[0_4px_20px_rgba(139,92,246,0.2)]',
  FILE: 'border-slate-500/40 bg-[#0f1422]/95 text-slate-200 shadow-[0_4px_20px_rgba(0,0,0,0.4)]',
  THREAT_INDICATOR: 'border-rose-500/50 bg-[#220a14]/95 text-rose-200 shadow-[0_4px_20px_rgba(244,63,94,0.3)]',
};

export const InvestigationNode = ({ data, selected }) => {
  const Icon = icons[data.type] || Network;
  const verified = ['DIRECT', 'DIRECTLY_EXTRACTED', 'EXTERNALLY_VERIFIED'].includes(
    data.verificationStatus
  );

  return (
    <div
      data-testid={`graph-node-${data.id}`}
      className={cn(
        'relative min-w-[200px] max-w-[260px] rounded-2xl border p-4 shadow-card backdrop-blur-xl transition-all duration-150 select-none',
        colors[data.type] || 'border-white/[0.12] bg-[#0c101c] text-slate-200',
        selected && 'ring-2 ring-cyan-400 ring-offset-2 ring-offset-[#04060a] shadow-[0_0_28px_rgba(59,130,246,0.45)] scale-[1.02]'
      )}
    >
      <Handle
        type="target"
        position={Position.Left}
        className="!h-3 !w-3 !border-2 !border-[#04060a] !bg-cyan-400 !shadow-[0_0_8px_rgba(59,130,246,0.8)]"
      />
      <div className="flex items-start gap-3">
        <div className="p-2 rounded-xl bg-black/50 border border-white/10 shrink-0 shadow-sm">
          <Icon className="h-4 w-4" aria-hidden="true" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-[10px] font-mono font-semibold uppercase tracking-wider opacity-75">
            {titleCase(data.type)}
          </p>
          <p className="mt-1 break-words font-mono text-xs font-semibold text-slate-100 leading-snug">
            {data.label}
          </p>
        </div>
      </div>
      <div className="mt-3 pt-2.5 border-t border-white/10 flex items-center justify-between gap-1 text-[10px] font-mono opacity-85">
        <span className="flex items-center gap-1.5 font-medium">
          {verified ? (
            <ShieldCheck className="h-3.5 w-3.5 text-emerald-400 shrink-0" />
          ) : (
            <ShieldAlert className="h-3.5 w-3.5 text-amber-400 shrink-0" />
          )}
          {titleCase(data.verificationStatus || 'UNKNOWN')}
        </span>
      </div>
      <Handle
        type="source"
        position={Position.Right}
        className="!h-3 !w-3 !border-2 !border-[#04060a] !bg-cyan-400 !shadow-[0_0_8px_rgba(59,130,246,0.8)]"
      />
    </div>
  );
};

export default InvestigationNode;
