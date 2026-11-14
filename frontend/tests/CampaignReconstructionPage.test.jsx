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

  it('renders synthetic demo campaign classification, connected cases, and indicators', async () => {
    const caseId = 'c0a80101-0000-0000-0000-000000000001';

    investigationService.getById.mockResolvedValue({
      id: caseId,
      title: '[DEMO] Apex Campaign — SMS Banking Alert',
      status: 'COMPLETED',
      category: 'PHISHING',
    });

    investigationService.getCampaign.mockResolvedValue({
      campaignClassification: 'SYNTHETIC_DEMO_CAMPAIGN',
      attributionSummary: 'Synthetic demonstration campaign footprint detected. Correlated demonstration cases via RFC 2606 (.test) and RFC 5737 (TEST-NET) indicators.',
      connectedInvestigations: [
        {
          id: 'c0a80101-0000-0000-0000-000000000002',
          title: '[DEMO] Apex Campaign — Spear-Phishing Email & KYC Lure',
          category: 'PHISHING',
          status: 'COMPLETED',
          sharedIndicatorCount: 2,
          sharedIndicatorValues: ['secure-netbanking-verify.internal-demo.test', '198.51.100.42'],
          relationshipExplanation: '[DEMO] Shares 2 synthetic demonstration indicator(s) with current case.',
          confidence: 'SYNTHETIC_DEMO',
        },
      ],
      sharedIndicators: [
        {
          value: 'secure-netbanking-verify.internal-demo.test',
          type: 'DOMAIN',
          count: 3,
          crossInvestigation: true,
          verificationStatus: 'SYNTHETIC_TEST_INDICATOR',
          evidenceIds: ['ev-1', 'ev-2'],
        },
      ],
      campaignSignals: [
        {
          description: '2 synthetic demonstration indicator(s) cross-correlated across 2 demonstration cases (RFC 2606/5737 test fixtures)',
          confidence: 'SYNTHETIC_DEMO_CAMPAIGN',
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
      expect(screen.getByText('[DEMO] Apex Campaign — SMS Banking Alert')).toBeInTheDocument();
      expect(screen.getAllByText('SYNTHETIC DEMO CAMPAIGN').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText(/Synthetic demonstration campaign footprint detected/i)).toBeInTheDocument();
      expect(screen.getByText('[DEMO] Apex Campaign — Spear-Phishing Email & KYC Lure')).toBeInTheDocument();
      expect(screen.getAllByText('secure-netbanking-verify.internal-demo.test').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Multi-Case Campaign')).toBeInTheDocument();
      expect(screen.getByText('SYNTHETIC DEMO')).toBeInTheDocument();
    });
  });

  it('renders confirmed campaign classification for genuine verified investigations', async () => {
    const caseId = 'c0a80101-0000-0000-0000-000000000099';

    investigationService.getById.mockResolvedValue({
      id: caseId,
      title: 'Executive Wire Fraud Investigation',
      status: 'COMPLETED',
      category: 'PHISHING',
    });

    investigationService.getCampaign.mockResolvedValue({
      campaignClassification: 'CONFIRMED',
      attributionSummary: 'Cross-case correlation confirms shared malicious banking infrastructure.',
      connectedInvestigations: [
        {
          id: 'c0a80101-0000-0000-0000-000000000098',
          title: 'Corporate Payroll Phishing Incident',
          category: 'PHISHING',
          status: 'COMPLETED',
          sharedIndicatorCount: 1,
          sharedIndicatorValues: ['evil-c2-infrastructure.com'],
          relationshipExplanation: 'Shares 1 infrastructure indicator with current case.',
          confidence: 'CONFIRMED',
        },
      ],
      sharedIndicators: [
        {
          value: 'evil-c2-infrastructure.com',
          type: 'DOMAIN',
          count: 2,
          crossInvestigation: true,
          verificationStatus: 'VERIFIED_MALICIOUS',
          evidenceIds: ['ev-10', 'ev-20'],
        },
      ],
      campaignSignals: [
        {
          description: '1 shared indicator(s) confirmed malicious by threat intelligence feeds — confirmed coordinated campaign',
          confidence: 'CONFIRMED',
          supportingEvidenceIds: ['ev-10', 'ev-20'],
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
      expect(screen.getByText('Executive Wire Fraud Investigation')).toBeInTheDocument();
      expect(screen.getByText('CONFIRMED CAMPAIGN')).toBeInTheDocument();
      expect(screen.getByText('Corporate Payroll Phishing Incident')).toBeInTheDocument();
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
