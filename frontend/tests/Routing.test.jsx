import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute, PublicOnlyRoute } from '../src/routes/ProtectedRoute';

vi.mock('../src/hooks/useAuth');

import { useAuth } from '../src/hooks/useAuth';

const Protected = () => (
  <MemoryRouter initialEntries={['/secret']}>
    <Routes>
      <Route
        path="/secret"
        element={
          <ProtectedRoute>
            <div>secret content</div>
          </ProtectedRoute>
        }
      />
      <Route path="/login" element={<div>login page</div>} />
    </Routes>
  </MemoryRouter>
);

const PublicOnly = () => (
  <MemoryRouter initialEntries={['/login']}>
    <Routes>
      <Route
        path="/login"
        element={
          <PublicOnlyRoute>
            <div>login form</div>
          </PublicOnlyRoute>
        }
      />
      <Route path="/dashboard" element={<div>dashboard</div>} />
    </Routes>
  </MemoryRouter>
);

describe('ProtectedRoute', () => {
  beforeEach(() => {
    useAuth.mockReturnValue({ isAuthenticated: false, loading: false });
  });

  it('redirects unauthenticated users to /login', () => {
    render(<Protected />);
    expect(screen.getByText('login page')).toBeInTheDocument();
    expect(screen.queryByText('secret content')).not.toBeInTheDocument();
  });

  it('renders protected content when authenticated', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, loading: false });
    render(<Protected />);
    expect(screen.getByText('secret content')).toBeInTheDocument();
  });
});

describe('PublicOnlyRoute', () => {
  it('redirects authenticated users to /dashboard', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, loading: false });
    render(<PublicOnly />);
    expect(screen.getByText('dashboard')).toBeInTheDocument();
    expect(screen.queryByText('login form')).not.toBeInTheDocument();
  });

  it('renders the public page when unauthenticated', () => {
    useAuth.mockReturnValue({ isAuthenticated: false, loading: false });
    render(<PublicOnly />);
    expect(screen.getByText('login form')).toBeInTheDocument();
  });
});
