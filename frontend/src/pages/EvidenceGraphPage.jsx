import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  ChevronRight,
  GitBranch,
  Info,
  ShieldCheck,
  X,
  AlertCircle,
} from 'lucide-react';
import {
  Background,
  Controls,
  MiniMap,
  ReactFlow,
  ReactFlowProvider,
  useEdgesState,
  useNodesState,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import EmptyState from '../components/ui/EmptyState';
import { FullPageSpinner } from '../components/ui/Spinner';
import { getApiErrorMessage } from '../services/api/apiClient';
import evidenceService from '../services/evidence/evidenceService';
import { investigationService } from '../services/investigation/investigationService';
import { titleCase } from '../utils/format';
import InvestigationNode from '../components/graph/InvestigationNode';

const nodeTypes = { investigation: InvestigationNode };

const edgeLabels = {
  CONTAINS_URL: 'contains URL',
  REFERENCES_DOMAIN: 'references domain',
  MENTIONS_EMAIL: 'mentions email',
  SHARES_INDICATOR: 'shares indicator',
  REDIRECTS_TO: 'redirects to',
  MATCHES_THREAT_INDICATOR: 'matches threat',
  ASSOCIATED_WITH: 'associated with',
};

const layoutNodes = (nodes) =>
  nodes.map((node, index) => {
    const column = index % 3;
    const row = Math.floor(index / 3);
    return {
      id: node.id,
      type: 'investigation',
      position: { x: column * 320, y: row * 180 },
      data: { ...node, id: node.id },
    };
  });

const layoutEdges = (edges, nodes) =>
  edges.map((edge) => ({
    id: edge.id,
    source: edge.source,
    target: edge.target,
    type: 'smoothstep',
    label: edgeLabels[edge.type] || titleCase(edge.type),
    data: {
      ...edge,
      sourceEvidenceId: nodes.find((node) => node.id === edge.source)?.evidenceId,
      targetEvidenceId: nodes.find((node) => node.id === edge.target)?.evidenceId,
    },
    animated: false,
    style: { stroke: '#3b82f6', strokeWidth: 1.5, opacity: 0.75 },
    labelStyle: { fill: '#cbd5e1', fontSize: 11, fontWeight: 600, fontFamily: 'monospace' },
    labelBgStyle: { fill: '#0a0f1d', fillOpacity: 0.95, rx: 6, ry: 6 },
    labelBgPadding: [8, 5],
  }));

const DetailPanel = ({ selected, onClose, findings }) => {
  if (!selected) return null;
  const isEdge = selected.kind === 'edge';
  const item = selected.data;

  return (
    <aside className="absolute right-4 top-4 z-dropdown w-[min(380px,calc(100%-2rem))] rounded-2xl border border-white/[0.12] bg-[#0c101c]/95 p-5 sm:p-6 shadow-[0_20px_50px_rgba(0,0,0,0.8),inset_0_1px_0_0_rgba(255,255,255,0.1)] backdrop-blur-2xl min-w-0 transition-all duration-150">
      <div className="flex items-start justify-between gap-3 pb-3.5 border-b border-white/[0.08]">
        <div className="min-w-0 flex-1">
          <p className="text-2xs font-mono font-bold uppercase tracking-wider text-blue-400">
            {isEdge ? 'Relationship Provenance' : titleCase(item.type)}
          </p>
          <h2 className="mt-1 break-words font-mono text-sm font-bold text-slate-100">
            {isEdge ? edgeLabels[item.type] || titleCase(item.type) : item.label}
          </h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="rounded-xl p-1.5 text-slate-400 hover:bg-white/[0.08] hover:text-white transition-colors"
          aria-label="Close graph details"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      {isEdge ? (
        <dl className="mt-4 space-y-3.5 text-xs">
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Why this exists</dt>
            <dd className="mt-1 text-slate-200 leading-relaxed font-sans">
              {item.explanation || 'No explanation was provided.'}
            </dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Verification</dt>
            <dd className="mt-1">
              <Badge status={item.verificationStatus}>
                {titleCase(item.verificationStatus || 'UNKNOWN')}
              </Badge>
            </dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Confidence</dt>
            <dd className="mt-1 font-mono font-bold text-slate-100">
              {item.confidence == null ? 'Unknown' : `${Math.round(item.confidence * 100)}%`}
            </dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Source reference</dt>
            <dd className="mt-1 break-words font-mono text-2xs text-slate-300 bg-black/40 p-2 rounded-lg border border-white/[0.06]">
              {item.sourceReference || 'Not provided'}
            </dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Supporting evidence</dt>
            <dd className="mt-1 font-mono text-2xs text-slate-300 bg-black/40 p-2 rounded-lg border border-white/[0.06]">
              Source: {item.sourceEvidenceId || 'Not provided'} · Target:{' '}
              {item.targetEvidenceId || 'Indicator relationship'}
            </dd>
          </div>
        </dl>
      ) : (
        <dl className="mt-4 space-y-3.5 text-xs">
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Entity value</dt>
            <dd className="mt-1 break-words font-mono text-slate-100 font-semibold bg-black/40 p-2 rounded-lg border border-white/[0.06]">{item.label}</dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Source evidence</dt>
            <dd className="mt-1 break-words font-mono text-2xs text-slate-300">
              {item.evidenceId || 'Investigation-level entity'}
            </dd>
          </div>
          <div>
            <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Verification</dt>
            <dd className="mt-1">
              <Badge status={item.verificationStatus}>
                {titleCase(item.verificationStatus || 'UNKNOWN')}
              </Badge>
            </dd>
          </div>
          {findings.length > 0 && (
            <div>
              <dt className="text-2xs font-mono font-bold uppercase text-slate-400">Related findings</dt>
              <dd className="mt-1 space-y-1.5 text-slate-300">
                {findings.map((finding) => (
                  <p key={finding.title} className="text-xs">• {finding.title}</p>
                ))}
              </dd>
            </div>
          )}
        </dl>
      )}
    </aside>
  );
};

export const EvidenceGraphPage = () => {
  const { id } = useParams();
  const [graph, setGraph] = useState(null);
  const [investigation, setInvestigation] = useState(null);
  const [selected, setSelected] = useState(null);
  const [findingsByEvidence, setFindingsByEvidence] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  const loadGraph = useCallback(async () => {
    const [result, context] = await Promise.all([
      investigationService.getGraph(id),
      investigationService.getById(id),
    ]);
    const evidence = await evidenceService.list(id);
    const analyses = await Promise.all(
      evidence.map(async (item) => ({
        evidenceId: item.id,
        executions: await evidenceService.listAnalysis(item.id).catch(() => []),
      }))
    );
    const relatedFindings = Object.fromEntries(
      analyses.map(({ evidenceId, executions }) => [
        evidenceId,
        executions.flatMap((execution) => {
          try {
            return JSON.parse(execution.outputJson || '{}').findings || [];
          } catch {
            return [];
          }
        }),
      ])
    );
    setGraph(result);
    setInvestigation(context);
    setNodes(layoutNodes(result.nodes || []));
    setEdges(layoutEdges(result.edges || [], result.nodes || []));
    setFindingsByEvidence(relatedFindings);
  }, [id, setEdges, setNodes]);

  useEffect(() => {
    loadGraph()
      .catch((err) =>
        setError(getApiErrorMessage(err, 'Could not load the investigation graph.'))
      )
      .finally(() => setLoading(false));
  }, [loadGraph]);

  const selectedFindings = useMemo(() => {
    if (!selected || selected.kind !== 'node') return [];
    return findingsByEvidence[selected.data.evidenceId] || [];
  }, [findingsByEvidence, selected]);

  if (loading) return <FullPageSpinner label="Loading relationship graph…" />;
  if (error) {
    return (
      <div
        role="alert"
        className="rounded-2xl border border-rose-800 bg-rose-950/60 p-5 text-xs sm:text-sm text-rose-300 flex items-start gap-3"
      >
        <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
        <span>{error}</span>
      </div>
    );
  }

  const hasGraphData = (graph?.nodes?.length || 0) > 0;
  return (
    <div className="space-y-6 min-w-0">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/[0.08] pb-6 min-w-0">
        <div>
          <Link
            to={`/investigations/${id}`}
            className="mb-2 inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white font-mono transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Back to investigation
          </Link>
          <h1 className="flex items-center gap-3 text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            <div className="p-2 rounded-xl bg-blue-500/15 border border-blue-400/30 text-blue-400 shadow-[0_0_16px_rgba(59,130,246,0.25)]">
              <GitBranch className="h-6 w-6" />
            </div>
            <span>Evidence relationship graph</span>
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-slate-400">
            <span className="text-slate-100 font-semibold">{investigation?.title || 'Investigation'}</span>{' '}
            · Real extracted relationships and entity links
          </p>
        </div>
        <div className="flex items-center gap-2.5 text-xs font-mono text-slate-300 bg-[#0c101c] px-4 py-2 rounded-xl border border-white/[0.1] shadow-sm">
          <ShieldCheck className="h-4 w-4 text-emerald-400" />
          <span className="font-semibold">{graph?.edges?.length || 0} relationships</span>
        </div>
      </div>

      {!hasGraphData ? (
        <Card className="shadow-card">
          <EmptyState
            icon={GitBranch}
            title="No relationships yet"
            message="Submit evidence and run deterministic extraction before opening the graph."
          />
        </Card>
      ) : (
        <Card padded={false} className="overflow-hidden border-white/[0.1] shadow-2xl">
          <div
            className="relative h-[min(72vh,720px)] min-h-[480px] w-full"
            data-testid="relationship-graph"
          >
            <ReactFlow
              nodes={nodes}
              edges={edges}
              nodeTypes={nodeTypes}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              onNodeClick={(_, node) => setSelected({ kind: 'node', data: node.data })}
              onEdgeClick={(_, edge) => setSelected({ kind: 'edge', data: edge.data })}
              onPaneClick={() => setSelected(null)}
              fitView
              fitViewOptions={{ padding: 0.2 }}
              minZoom={0.25}
              maxZoom={1.6}
              proOptions={{ hideAttribution: true }}
              className="bg-[#05070c]"
            >
              <Background color="#1e293b" gap={24} size={1} />
              <Controls position="bottom-left" showInteractive={false} />
              <MiniMap
                nodeColor={(node) =>
                  node.data?.type === 'INVESTIGATION' ? '#3b82f6' : '#334155'
                }
                maskColor="rgba(4, 6, 10, 0.88)"
              />
            </ReactFlow>
            <DetailPanel
              selected={selected}
              onClose={() => setSelected(null)}
              findings={selectedFindings}
            />
            {!selected && (
              <div className="pointer-events-none absolute bottom-4 right-4 hidden items-center gap-2 rounded-xl border border-white/[0.08] bg-[#0c101c]/90 px-3.5 py-2 text-2xs font-mono text-slate-400 sm:flex backdrop-blur-md shadow-lg">
                <Info className="h-3.5 w-3.5 text-blue-400" /> Select a node or relationship to
                inspect provenance
              </div>
            )}
          </div>
        </Card>
      )}

      {hasGraphData && graph.edges?.length === 0 && (
        <p className="flex items-center gap-2 text-xs sm:text-sm text-amber-300 font-mono">
          <ChevronRight className="h-4 w-4" /> Nodes exist, but no supported relationships were
          found.
        </p>
      )}
    </div>
  );
};

export default function EvidenceGraphPageWithProvider() {
  return (
    <ReactFlowProvider>
      <EvidenceGraphPage />
    </ReactFlowProvider>
  );
}
