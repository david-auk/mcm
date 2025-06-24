import axios from 'axios';
import { getToken, removeToken } from './token';  // assume clearToken() nukes your stored token

const authenticadedFetch = axios.create({
  baseURL: 'http://localhost/api',
});

// attach the bearer header
authenticadedFetch.interceptors.request.use(config => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } else {
      // force‐navigate to login
      window.location.href = '/login';
  }
  return config;
});

// catch any 401 or 403 and redirect
authenticadedFetch.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      // clear any stale token
      removeToken();
      // force‐navigate to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default authenticadedFetch;