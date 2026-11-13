import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../src/context/AuthContext';
import LandingPage from '../src/pages/LandingPage';
import LoginPage from '../src/pages/LoginPage';
import RegisterPage from '../src/pages/RegisterPage';
import EvidenceAnalysisPage from '../src/pages/EvidenceAnalysisPage';
import { investigationService } from '../src/services/investigation/investigationService';
import evidenceService from '../src/services/evidence/evidenceService';

vi.mock('../src/services/investigation/investigationService');
vi.mock('../src/services/evidence/evidenceService');

describe('Responsive Layout & Overflow Safeguards', () => {
  beforeEach(() => {
    investigationService.getById.mockResolvedValue({ id: 'case-demo', title: 'Case Demo' });
    evidenceService.list.mockResolvedValue([
      {
        id: 'ev-1',
        evidenceType: 'URL',
        url: 'https://tracking.axonbloom.com/tracking/click?d=very-long-unbroken-tracking-parameter-that-previously-caused-a-layout-crush-bug-in-css-grid',
        status: 'COMPLETED',
        createdAt: '2026-11-11T23:16:13Z',
      },
    ]);
  });

  it('renders EvidenceAnalysisPage with min-w-0 containers and responsive 12-col grid', async () => {
    const { container } = render(
      <AuthProvider>
        <MemoryRouter initialEntries={['/investigations/case-demo/analysis']}>
          <Routes>
            <Route path="/investigations/:id/analysis" element={<EvidenceAnalysisPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    );

    const title = await screen.findByText('Evidence workspace');
    expect(title).toBeInTheDocument();

    // Check that the grid uses responsive grid-cols-1 lg:grid-cols-12 with min-w-0
    const gridContainer = container.querySelector('.grid.grid-cols-1.lg\\:grid-cols-12');
    expect(gridContainer).not.toBeNull();
    expect(gridContainer.className).toContain('min-w-0');

    // Check that the Add Evidence card has min-w-0
    const addCardHeading = screen.getByRole('heading', { name: 'Add evidence' });
    const addCard = addCardHeading.closest('.min-w-0');
    expect(addCard).not.toBeNull();

    // Check that the long URL evidence has truncate and title
    const urlItem = screen.getByTitle(/tracking\.axonbloom\.com/);
    expect(urlItem.className).toContain('truncate');
    expect(urlItem.className).toContain('font-mono');
  });

  it('renders LandingPage without horizontal overflow classes', () => {
    const { container } = render(
      <AuthProvider>
        <MemoryRouter>
          <LandingPage />
        </MemoryRouter>
      </AuthProvider>
    );
    expect(screen.getByText(/Investigate Before You Trust/i)).toBeInTheDocument();
    expect(container.querySelector('.max-w-4xl')).not.toBeNull();
  });

  it('renders LoginPage and RegisterPage with centered, bounded containers', () => {
    const { container: loginContainer } = render(
      <AuthProvider>
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      </AuthProvider>
    );
    expect(loginContainer.querySelector('.max-w-md')).not.toBeNull();

    const { container: regContainer } = render(
      <AuthProvider>
        <MemoryRouter>
          <RegisterPage />
        </MemoryRouter>
      </AuthProvider>
    );
    expect(regContainer.querySelector('.max-w-md')).not.toBeNull();
  });
});
