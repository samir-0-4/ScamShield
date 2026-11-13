import axios from 'axios';
import { appConfig } from '../../config/appConfig';
import { tokenStorage } from '../storage/tokenStorage';

const apiClient = axios.create({
  baseURL: appConfig.apiBaseUrl,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the JWT bearer token to every request when present.
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenStorage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Unwrap the backend ApiResponse envelope (response.data) and handle 401 globally.
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.status === 401) {
      tokenStorage.clear();
      // Let AuthContext (listening for this event) reset state and ProtectedRoute redirect.
      window.dispatchEvent(new CustomEvent('auth:expired'));
    }
    return Promise.reject(error);
  }
);

export const getApiErrorMessage = (error, fallback = 'Something went wrong. Please try again.') =>
  error?.response?.data?.message || error?.message || fallback;

export default apiClient;
