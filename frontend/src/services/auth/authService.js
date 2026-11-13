import apiClient from '../api/apiClient';
import { tokenStorage } from '../storage/tokenStorage';

export const authService = {
  login: async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    if (response?.data?.token) {
      tokenStorage.setToken(response.data.token);
      tokenStorage.setUser(response.data.user);
    }
    return response;
  },

  register: async (userData) => {
    return apiClient.post('/auth/register', userData);
  },

  getCurrentUser: async () => {
    return apiClient.get('/auth/me');
  },

  logout: () => {
    tokenStorage.clear();
  },
};

