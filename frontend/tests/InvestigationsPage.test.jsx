import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../src/services/investigation/investigationService');

import { investigationService } from '../src/services/investigation/investigationService';
import InvestigationsPage from '../src/pages/InvestigationsPage';

const pageOf = (content) => ({
  content,
  pageNumber: 0,
  pageSize: 10,
  totalElements: content.length,
  totalPages: 1,
  last: true,
});

const renderPage = () =>
  render(
    <MemoryRouter>
      <InvestigationsPage />
    </MemoryRouter>
  );

describe('InvestigationsPage', () => {
  beforeEach(() => {
    investigationService.list.mockResolvedValue(pageOf([]));
  });

  it('renders the empty state when there are no investigations', async () => {
    renderPage();
    await waitFor(() => expect(screen.getByText(/No investigations yet/i)).toBeInTheDocument());
  });

  it('renders investigation rows when data exists', async () => {
    investigationService.list.mockResolvedValue(
      pageOf([
        { id: 'abc', title: 'Phishing Campaign', status: 'DRAFT', category: 'PHISHING', createdAt: '2026-01-01T00:00:00Z' },
      ])
    );
    renderPage();
    await waitFor(() => expect(screen.getByText('Phishing Campaign')).toBeInTheDocument());
  });
});
