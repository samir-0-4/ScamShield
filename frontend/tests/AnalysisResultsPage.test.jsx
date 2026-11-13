import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('../src/services/investigation/investigationService');
vi.mock('../src/services/evidence/evidenceService');

import { investigationService } from '../src/services/investigation/investigationService';
import evidenceService from '../src/services/evidence/evidenceService';
import AnalysisResultsPage from '../src/pages/AnalysisResultsPage';

const renderPage = () => render(
  <MemoryRouter initialEntries={['/investigations/case-1/results']}>
    <Routes>
      <Route path="/investigations/:id/results" element={<AnalysisResultsPage />} />
    </Routes>
  </MemoryRouter>
);

describe('AnalysisResultsPage', () => {
  beforeEach(() => {
    investigationService.getById.mockResolvedValue({
      id: 'case-1', title: 'Job offer review', status: 'COMPLETED', createdAt: '2025-01-01T00:00:00Z',
    });
    investigationService.getRiskAssessment.mockResolvedValue({
      score: 55, category: 'HIGH', confidence: 0.8, explanation: 'A payment request was found.',
      contributingSignals: [], missingInformation: 'Sender identity',
    });
    investigationService.getGraph.mockResolvedValue({ nodes: [], edges: [] });
    evidenceService.list.mockResolvedValue([{ id: 'e1', evidenceType: 'TEXT_MESSAGE', description: 'Message' }]);
    evidenceService.listIndicators.mockResolvedValue([{ id: 'i1', indicatorType: 'PAYMENT_TERM', normalizedValue: 'registration fee' }]);
    evidenceService.listThreatLookups.mockResolvedValue([]);
    evidenceService.listAnalysis.mockResolvedValue([{
      id: 'x1', agentName: 'TEXT_DETECTIVE', executionStatus: 'COMPLETED',
      outputJson: JSON.stringify({
        findings: [{
          title: 'Upfront payment request', description: 'The sender requests a fee.',
          severity: 'HIGH', confidence: 0.9, verificationStatus: 'EXTRACTED',
          evidenceReference: 'e1', explanation: 'Payment language is present.',
          recommendedAction: 'Verify independently.', sourceType: 'DETERMINISTIC',
        }],
      }),
    }]);
  });

  it('renders risk and structured findings from backend responses', async () => {
    renderPage();
    expect(await screen.findByText('Job offer review')).toBeInTheDocument();
    expect(screen.getByText('55/100')).toBeInTheDocument();
    expect(screen.getByText('Upfront payment request')).toBeInTheDocument();
    expect(screen.getByText('Extracted from submitted evidence')).toBeInTheDocument();
  });

  it('renders an empty state when analysis has not run', async () => {
    evidenceService.listAnalysis.mockResolvedValue([]);
    investigationService.getRiskAssessment.mockResolvedValue(null);
    renderPage();
    await waitFor(() => expect(screen.getByText('No findings returned')).toBeInTheDocument());
    expect(screen.getByRole('button', { name: /Run analysis/i })).toBeInTheDocument();
  });
});
