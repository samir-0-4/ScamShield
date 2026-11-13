import { createContext, useState, useEffect } from 'react';
import { tokenStorage } from '../services/storage/tokenStorage';
import { authService } from '../services/auth/authService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => tokenStorage.getUser());
  const [token, setToken] = useState(() => tokenStorage.getToken());
  const [loading, setLoading] = useState(Boolean(tokenStorage.getToken()));

  // Validate a persisted session on mount via /auth/me so an expired token
  // is cleared before a protected route trusts it.
  useEffect(() => {
    const stored = tokenStorage.getToken();
    if (!stored) {
      setLoading(false);
      return;
    }
    let active = true;
    authService
      .getCurrentUser()
      .then((res) => {
        if (active) {
          setUser(res.data);
          setLoading(false);
        }
      })
      .catch(() => {
        if (active) {
          tokenStorage.clear();
          setUser(null);
          setToken(null);
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  // React to mid-session 401s dispatched by the API client.
  useEffect(() => {
    const handler = () => {
      tokenStorage.clear();
      setUser(null);
      setToken(null);
    };
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, []);

  const login = async (credentials) => {
    setLoading(true);
    try {
      const res = await authService.login(credentials);
      setUser(res.data.user);
      setToken(res.data.token);
      return res;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  const isAuthenticated = Boolean(token);

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
