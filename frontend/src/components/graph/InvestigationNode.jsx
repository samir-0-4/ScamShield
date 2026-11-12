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
  INVESTIGATION: 'border-blue-500/40 bg-blue-950/70 text-blue-200 shadow-sm',
  MESSAGE: 'border-sky-500/40 bg-sky-950/70 text-sky-200 shadow-sm',
  URL: 'border-cyan-500/40 bg-cyan-950/70 text-cyan-200 shadow-sm',
  DOMAIN: 'border-indigo-500/40 bg-indigo-950/70 text-indigo-200 shadow-sm',
  EMAIL: 'border-violet-500/40 bg-violet-950/70 text-violet-200 shadow-sm',
  FILE: 'border-slate-600/50 bg-slate-900/80 text-slate-200 shadow-sm',
  THREAT_INDICATOR: 'border-rose-500/50 bg-rose-950/70 text-rose-200 shadow-sm',
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
        'relative min-w-[190px] max-w-[250px] rounded-xl border p-3.5 shadow-card backdrop-blur-md transition-all duration-150 select-none',
        colors[data.type] || 'border-white/[0.1] bg-[#0e121b] text-slate-200',
        selected && 'ring-2 ring-blue-400 ring-offset-2 ring-offset-[#08090d] shadow-[0_0_24px_rgba(59,130,246,0.35)]'
      )}
    >
      <Handle
        type="target"
        position={Position.Left}
        className="!h-2.5 !w-2.5 !border-2 !border-[#08090d] !bg-blue-400"
      />
      <div className="flex items-start gap-2.5">
        <div className="p-1.5 rounded-lg bg-black/40 border border-white/10 shrink-0">
          <Icon className="h-3.5 w-3.5" aria-hidden="true" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-[10px] font-mono font-semibold uppercase tracking-wider opacity-75">
            {titleCase(data.type)}
          </p>
          <p className="mt-0.5 break-words font-mono text-xs font-medium text-slate-100 leading-snug">
            {data.label}
          </p>
        </div>
      </div>
      <div className="mt-2.5 pt-2 border-t border-white/10 flex items-center justify-between gap-1 text-[10px] font-mono opacity-80">
        <span className="flex items-center gap-1.5">
          {verified ? (
            <ShieldCheck className="h-3 w-3 text-emerald-400 shrink-0" />
          ) : (
            <ShieldAlert className="h-3 w-3 text-amber-400 shrink-0" />
          )}
          {titleCase(data.verificationStatus || 'UNKNOWN')}
        </span>
      </div>
      <Handle
        type="source"
        position={Position.Right}
        className="!h-2.5 !w-2.5 !border-2 !border-[#08090d] !bg-blue-400"
      />
    </div>
  );
};

export default InvestigationNode;
