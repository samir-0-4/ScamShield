export const appConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  appName: import.meta.env.VITE_APP_NAME || 'PhantomTrace',
  appEnv: import.meta.env.VITE_APP_ENV || 'development',
};

