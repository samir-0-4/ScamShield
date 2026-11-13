import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from '../src/App';

describe('PhantomTrace App', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('renders the landing page hero copy', () => {
    render(<App />);
    expect(screen.getByText(/Investigate Before You Trust/i)).toBeInTheDocument();
    expect(screen.getAllByText(/PhantomTrace/i).length).toBeGreaterThan(0);
  });
});
