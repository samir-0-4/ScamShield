import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../src/services/auth/authService');

import { authService } from '../src/services/auth/authService';
import RegisterPage from '../src/pages/RegisterPage';

const renderRegister = () =>
  render(
    <MemoryRouter>
      <RegisterPage />
    </MemoryRouter>
  );

describe('RegisterPage', () => {
  beforeEach(() => {
    authService.register = vi.fn();
  });

  it('shows validation errors on empty submit', () => {
    renderRegister();
    fireEvent.click(screen.getByRole('button', { name: /Create Account/i }));
    expect(screen.getByText(/Full name must be at least 2 characters/i)).toBeInTheDocument();
    expect(screen.getByText(/Email is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Password is required/i)).toBeInTheDocument();
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('requires an 8-character password', () => {
    renderRegister();
    fireEvent.change(screen.getByLabelText(/Full name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'jane@b.com' } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'short' } });
    fireEvent.click(screen.getByRole('button', { name: /Create Account/i }));
    expect(screen.getByText(/at least 8 characters/i)).toBeInTheDocument();
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('registers a valid account', async () => {
    authService.register.mockResolvedValue({});
    renderRegister();
    fireEvent.change(screen.getByLabelText(/Full name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'jane@b.com' } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /Create Account/i }));
    await waitFor(() => expect(authService.register).toHaveBeenCalledWith({
      fullName: 'Jane Doe',
      email: 'jane@b.com',
      password: 'password123',
    }));
  });
});
