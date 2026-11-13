import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('../src/services/investigation/investigationService');
vi.mock('../src/services/evidence/evidenceService');

import { investigationService } from '../src/services/investigation/investigationService';
import evidenceService from '../src/services/evidence/evidenceService';
import EvidenceAnalysisPage from '../src/pages/EvidenceAnalysisPage';

const renderPage = () => render(
  <MemoryRouter initialEntries={['/investigations/case-1/analysis']}>
    <Routes>
      <Route path="/investigations/:id/analysis" element={<EvidenceAnalysisPage />} />
    </Routes>
  </MemoryRouter>
);

describe('EvidenceAnalysisPage', () => {
  beforeEach(() => {
    investigationService.getById.mockResolvedValue({ id: 'case-1', title: 'Test case' });
    evidenceService.list.mockResolvedValue([]);
    evidenceService.create.mockResolvedValue({ id: 'e1', evidenceType: 'TEXT_MESSAGE' });
  });

  it('validates empty text evidence and submits multiple evidence items', async () => {
    renderPage();
    await waitFor(() => expect(screen.getByText('Evidence workspace')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: /Add evidence/i }));
    expect(await screen.findByText('Message content is required')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/Message content/), { target: { value: 'Suspicious message' } });
    fireEvent.click(screen.getByRole('button', { name: /Add evidence/i }));
    await waitFor(() => expect(evidenceService.create).toHaveBeenCalledWith(
      'case-1',
      expect.objectContaining({ evidenceType: 'TEXT_MESSAGE', textContent: 'Suspicious message' })
    ));

    fireEvent.change(screen.getByLabelText('Evidence type'), { target: { value: 'URL' } });
    fireEvent.change(document.querySelector('input[type="url"]'), { target: { value: 'https://example.com' } });
    fireEvent.click(screen.getByRole('button', { name: /Add evidence/i }));
    await waitFor(() => expect(evidenceService.create).toHaveBeenCalledTimes(2));
  });

  it('rejects unsupported files before upload', async () => {
    renderPage();
    await waitFor(() => expect(screen.getByText('Evidence workspace')).toBeInTheDocument());
    fireEvent.change(screen.getByLabelText('Evidence type'), { target: { value: 'PDF' } });
    const input = document.querySelector('input[type="file"]');
    fireEvent.change(input, { target: { files: [new File(['image'], 'bad.png', { type: 'image/png' })] } });
    expect(await screen.findByText(/Unsupported file type/i)).toBeInTheDocument();
    expect(evidenceService.createFile).not.toHaveBeenCalled();
  });

  it('renders ultra-long URLs in submitted evidence with truncate and min-w-0 classes', async () => {
    const longUrl =
      'https://tracking.axonbloom.com/tracking/click?d=SncqCPNIGrj16EQ8jbYroHD56RTws4NEIxkssd_HNMYluKI123vMI7B8fEQrUzfQZXNi5mqPZlmqXFFPG1NpueTbUOZGas5afDFkvihSnh-fp8u8wFJZlH1234567890';
    evidenceService.list.mockResolvedValue([
      {
        id: 'ev-long',
        evidenceType: 'URL',
        url: longUrl,
        status: 'COMPLETED',
        createdAt: '2026-11-11T23:16:13Z',
      },
    ]);

    renderPage();
    await waitFor(() => expect(screen.getByText('Evidence workspace')).toBeInTheDocument());

    const urlElement = screen.getByText(longUrl);
    expect(urlElement).toBeInTheDocument();
    expect(urlElement.className).toContain('truncate');
    expect(urlElement.className).toContain('font-mono');
    expect(urlElement.getAttribute('title')).toBe(longUrl);

    // Verify container has min-w-0
    const parentContainer = urlElement.closest('.min-w-0');
    expect(parentContainer).not.toBeNull();
  });
});
