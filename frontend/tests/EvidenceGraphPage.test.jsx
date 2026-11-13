import { describe, expect, it, vi, beforeEach } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('@xyflow/react', async () => {
  const actual = await vi.importActual('@xyflow/react');
  return {
    ...actual,
    Background: () => null,
    Controls: () => null,
    MiniMap: () => null,
    useReactFlow: () => ({ fitView: vi.fn() }),
    ReactFlowProvider: ({ children }) => children,
    ReactFlow: ({ nodes, edges, onNodeClick, onEdgeClick }) => (
      <div data-testid="relationship-graph">
        {nodes.map((node) => <button key={node.id} type="button" onClick={() => onNodeClick({}, node)}>{node.data.label}</button>)}
        {edges.map((edge) => <button key={edge.id} type="button" onClick={() => onEdgeClick({}, edge)}>{edge.label}</button>)}
      </div>
    ),
  };
});

vi.mock('../src/services/investigation/investigationService');
vi.mock('../src/services/evidence/evidenceService');

import { investigationService } from '../src/services/investigation/investigationService';
import evidenceService from '../src/services/evidence/evidenceService';
import EvidenceGraphPage from '../src/pages/EvidenceGraphPage';

const renderPage = () => render(
  <MemoryRouter initialEntries={['/investigations/case-1/graph']}>
    <Routes>
      <Route path="/investigations/:id/graph" element={<EvidenceGraphPage />} />
    </Routes>
  </MemoryRouter>
);

describe('EvidenceGraphPage', () => {
  beforeEach(() => {
    investigationService.getById.mockResolvedValue({ id: 'case-1', title: 'Graph case' });
    investigationService.getGraph.mockResolvedValue({
      nodes: [
        { id: 'evidence:e1', type: 'MESSAGE', label: 'MESSAGE', evidenceId: 'e1', verificationStatus: 'DIRECT' },
        { id: 'indicator:i1', type: 'URL', label: 'https://example.com', evidenceId: 'e1', verificationStatus: 'UNKNOWN' },
      ],
      edges: [{
        id: 'relationship:r1', source: 'evidence:e1', target: 'indicator:i1',
        type: 'CONTAINS_URL', verificationStatus: 'DIRECTLY_EXTRACTED',
        confidence: 1, explanation: 'The URL was extracted from this message.',
        sourceReference: 'URL_EXTRACTION_V1',
      }],
    });
    evidenceService.list.mockResolvedValue([{ id: 'e1' }]);
    evidenceService.listAnalysis.mockResolvedValue([]);
  });

  it('renders real nodes and labeled relationships and opens details', async () => {
    renderPage();
    expect(await screen.findByText('https://example.com')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'https://example.com' }));
    expect(screen.getByText('Entity value')).toBeInTheDocument();
    expect(screen.getByText('Unknown')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'contains URL' }));
    expect(screen.getByText('The URL was extracted from this message.')).toBeInTheDocument();
    expect(screen.getByText('100%')).toBeInTheDocument();
  });

  it('renders an empty graph state', async () => {
    investigationService.getGraph.mockResolvedValue({ nodes: [], edges: [] });
    renderPage();
    await waitFor(() => expect(screen.getByText('No relationships yet')).toBeInTheDocument());
  });

  it('renders graph request failures', async () => {
    investigationService.getGraph.mockRejectedValue(new Error('Graph unavailable'));
    renderPage();
    expect(await screen.findByRole('alert')).toHaveTextContent('Graph unavailable');
  });
});
