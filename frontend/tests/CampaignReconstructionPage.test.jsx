import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import CampaignReconstructionPage from '../src/pages/CampaignReconstructionPage';
import investigationService from '../src/services/investigation/investigationService';

vi.mock('../src/services/investigation/investigationService', () => ({
  default: {
    getById: vi.fn(),
    getCampaign: vi.fn(),
    seedDemoCampaign: vi.fn(),
  },
}));

describe('CampaignReconstructionPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders campaign classification, connected cases, and shared indicators', async () => {
    const caseId = 'c0a80101-0000-0000-0000-000000000001';

    investigationService.getById.mockResolvedValue({
      id: caseId,
      title: 'Apex Campaign — SMS Banking Alert',
      status: 'COMPLETED',
      category: 'PHISHING',
    });

    investigationService.getCampaign.mockResolvedValue({
      campaignClassification: 'CONFIRMED',
      attributionSummary: 'Cross-case correlation confirms shared malicious banking infrastructure.',
      connectedInvestigations: [
        {
          id: 'c0a80101-0000-0000-0000-000000000002',
          title: 'Apex Campaign — Spear-Phishing Email & KYC Lure',
          category: 'PHISHING',
          status: 'COMPLETED',
          sharedIndicatorCount: 2,
          sharedIndicatorValues: ['secure-netbanking-verify.internal-demo.test', '185.220.101.5'],
          relationshipExplanation: 'Shares 2 infrastructure indicators with current case.',
          confidence: 'CONFIRMED',
        },
      ],
      sharedIndicators: [
        {
          value: 'secure-netbanking-verify.internal-demo.test',
          type: 'DOMAIN',
          count: 3,
          crossInvestigation: true,
          verificationStatus: 'VERIFIED_MALICIOUS',
          evidenceIds: ['ev-1', 'ev-2'],
        },
      ],
      campaignSignals: [
        {
          description: '2 shared indicator(s) confirmed malicious by threat intelligence feeds — confirmed coordinated campaign',
          confidence: 'CONFIRMED',
          supportingEvidenceIds: ['ev-1', 'ev-2'],
        },
      ],
    });

    render(
      <MemoryRouter initialEntries={[`/investigations/${caseId}/campaign`]}>
        <Routes>
          <Route path="/investigations/:id/campaign" element={<CampaignReconstructionPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Apex Campaign — SMS Banking Alert')).toBeInTheDocument();
      expect(screen.getByText('CONFIRMED CAMPAIGN')).toBeInTheDocument();
      expect(screen.getByText(/Cross-case correlation confirms shared malicious banking infrastructure/i)).toBeInTheDocument();
      expect(screen.getByText('Apex Campaign — Spear-Phishing Email & KYC Lure')).toBeInTheDocument();
      expect(screen.getAllByText('secure-netbanking-verify.internal-demo.test').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Multi-Case Campaign')).toBeInTheDocument();
    });
  });

  it('renders the empty state when no shared indicators or signals are found', async () => {
    const caseId = 'c0a80101-0000-0000-0000-000000000003';

    investigationService.getById.mockResolvedValue({
      id: caseId,
      title: 'Isolated SMS Inquiry',
      status: 'OPEN',
      category: 'PHISHING',
    });

    investigationService.getCampaign.mockResolvedValue({
      campaignClassification: 'ISOLATED_INCIDENT',
      attributionSummary: 'No cross-evidence or cross-investigation indicator matches were identified.',
      connectedInvestigations: [],
      sharedIndicators: [],
      campaignSignals: [],
    });

    render(
      <MemoryRouter initialEntries={[`/investigations/${caseId}/campaign`]}>
        <Routes>
          <Route path="/investigations/:id/campaign" element={<CampaignReconstructionPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('No Shared Campaign Footprint Yet')).toBeInTheDocument();
      expect(screen.getByText('Load Demo Campaign')).toBeInTheDocument();
    });
  });
});
