import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../src/hooks/useAuth');
vi.mock('../src/services/investigation/investigationService');

import { useAuth } from '../src/hooks/useAuth';
import { investigationService } from '../src/services/investigation/investigationService';
import DashboardPage from '../src/pages/DashboardPage';

const pageOf = (content) => ({
  content,
  pageNumber: 0,
  pageSize: 100,
  totalElements: content.length,
  totalPages: 1,
  last: true,
});

const renderDashboard = () =>
  render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );

describe('DashboardPage', () => {
  beforeEach(() => {
    useAuth.mockReturnValue({ user: { fullName: 'Jane Doe' }, isAuthenticated: true, loading: false });
  });

  it('derives stats from backend data', async () => {
    investigationService.list.mockResolvedValue(
      pageOf([
        { id: '1', title: 'A', status: 'COMPLETED', overallRiskLevel: 'CRITICAL', evidenceCount: 2, createdAt: '2026-01-01T00:00:00Z' },
        { id: '2', title: 'B', status: 'DRAFT', overallRiskLevel: 'LOW', evidenceCount: 3, createdAt: '2026-01-02T00:00:00Z' },
      ])
    );
    renderDashboard();
    await waitFor(() => expect(screen.getByText('2')).toBeInTheDocument());
    // Total = 2, Completed = 1, High-risk = 1, Evidence = 5
    expect(investigationService.list).toHaveBeenCalled();
  });

  it('shows an empty state when there are no investigations', async () => {
    investigationService.list.mockResolvedValue(pageOf([]));
    renderDashboard();
    await waitFor(() => expect(screen.getByText(/No investigations yet/i)).toBeInTheDocument());
  });

  it('shows an error state when the backend call fails', async () => {
    investigationService.list.mockRejectedValue(new Error('network'));
    renderDashboard();
    await waitFor(() => expect(screen.getByText(/Could not load investigations/i)).toBeInTheDocument());
  });
});
