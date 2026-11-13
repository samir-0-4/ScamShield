import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../src/hooks/useAuth');

import { useAuth } from '../src/hooks/useAuth';
import LoginPage from '../src/pages/LoginPage';

const renderLogin = () =>
  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );

describe('LoginPage', () => {
  let login;
  beforeEach(() => {
    login = vi.fn();
    useAuth.mockReturnValue({ login, isAuthenticated: false, loading: false });
  });

  it('shows validation errors on empty submit', () => {
    renderLogin();
    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }));
    expect(screen.getByText(/Email is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Password is required/i)).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });

  it('toggles password visibility', () => {
    renderLogin();
    const passwordInput = screen.getByLabelText(/^Password/, { selector: 'input' });
    expect(passwordInput).toHaveAttribute('type', 'password');
    fireEvent.click(screen.getByLabelText(/Show password/i));
    expect(passwordInput).toHaveAttribute('type', 'text');
    fireEvent.click(screen.getByLabelText(/Hide password/i));
    expect(passwordInput).toHaveAttribute('type', 'password');
  });

  it('submits valid credentials to the auth service', async () => {
    login.mockResolvedValue({});
    renderLogin();
    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'a@b.com' } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }));
    expect(login).toHaveBeenCalledWith({ email: 'a@b.com', password: 'password123' });
  });
});
