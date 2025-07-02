import axios from 'axios';
import { getToken, removeToken } from './token';  // assume clearToken() nukes your stored token
import { useNavigate } from 'react-router-dom';

// build baseURL dynamically from the current origin + "/api"
const baseURL = `${window.location.origin}/api`;

const authenticatedFetch = axios.create({ baseURL });

// attach the bearer header
authenticatedFetch.interceptors.request.use(config => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } else {
    // force‐navigate to login
    redirectToLogin();
  }
  return config;
});

// catch any 401 or 403 and redirect
authenticatedFetch.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      // clear any stale token
      removeToken();
      // force‐navigate to login
      redirectToLogin();
    }
    return Promise.reject(error);
  }
);


const redirectToLogin = () => {
  const navigate = useNavigate();
  navigate('/login');
}

export default authenticatedFetch;