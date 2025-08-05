import axios from 'axios'
import { QueryClient } from '@tanstack/react-query'


export const api = axios.create({
  baseURL: process.env.VITE_API_BASE || 'http://localhost:8080',
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  const bucketKey = localStorage.getItem('bucketKey')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (bucketKey && config.headers) {
    config.headers['X-API-KEY'] = bucketKey
  }
  return config
})

api.interceptors.response.use(
  res => res,
  async err => {  
    if (err.response?.status === 401 && !err.config._retry && !err.config.url?.includes('/auth/login')
    ) {
      err.config._retry = true;
      const { data } = await api.post(`/auth/refresh`, {}, { withCredentials: true });
      localStorage.setItem('token', data.token);
      err.config.headers.Authorization = `Bearer ${data.token}`;
      return axios(err.config);
    }
    return Promise.reject(err);
  }
);

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})
