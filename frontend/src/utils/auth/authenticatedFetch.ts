import axios from 'axios';
import { getToken } from './token';
//import { Navigate } from 'react-router-dom';

const authApi = axios.create({
  // TODO use env variable instead of hardcoded
  baseURL: 'http://backend:8080/api',
});

authApi.interceptors.request.use(config => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } //else {
    //return (<Navigate to="/login" />)
  //} TODO Redirect to loggin if no token
  return config;
});

export default authApi;