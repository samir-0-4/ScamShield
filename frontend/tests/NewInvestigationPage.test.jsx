import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../src/services/investigation/investigationService');

import { investigationService } from '../src/services/investigation/investigationService';
import NewInvestigationPage from '../src/pages/NewInvestigationPage';

const renderNew = () =>
  render(
    <MemoryRouter>
      <NewInvestigationPage />
    </MemoryRouter>
  );

describe('NewInvestigationPage', () => {
  beforeEach(() => {
    investigationService.create = vi.fn();
  });

  it('shows validation errors when fields are empty', () => {
    renderNew();
    fireEvent.click(screen.getByRole('button', { name: /Initialize Case/i }));
    expect(screen.getByText(/Title must be at least 3 characters/i)).toBeInTheDocument();
    expect(screen.getByText(/Category is required/i)).toBeInTheDocument();
    expect(investigationService.create).not.toHaveBeenCalled();
  });

  it('creates an investigation and navigates to its detail page', async () => {
    investigationService.create.mockResolvedValue({ id: 'new-id', title: 'Test Case' });
    renderNew();
    fireEvent.change(screen.getByLabelText(/Investigation title/i), { target: { value: 'Test Case' } });
    fireEvent.change(screen.getByLabelText(/Category/i), { target: { value: 'PHISHING' } });
    fireEvent.click(screen.getByRole('button', { name: /Initialize Case/i }));
    await waitFor(() =>
      expect(investigationService.create).toHaveBeenCalledWith({
        title: 'Test Case',
        description: undefined,
        category: 'PHISHING',
      })
    );
  });
});
