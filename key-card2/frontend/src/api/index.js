import axios from 'axios'
import router from '../router/index.js'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor: add Authorization header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: handle 401 → redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  check: () => api.get('/auth/check'),
  logout: () => api.post('/auth/logout'),
}

export const cardApi = {
  list: (params) => api.get('/cards', { params }),
  get: (id) => api.get(`/cards/${id}`),
  create: (data) => api.post('/cards', data),
  update: (id, data) => api.put(`/cards/${id}`, data),
  delete: (id) => api.delete(`/cards/${id}`),
  exportAll: () => api.get('/cards/export'),
  importCards: (data) => api.post('/cards/import', data),
}

export const tagApi = {
  list: () => api.get('/tags'),
  create: (data) => api.post('/tags', data),
  rename: (id, data) => api.put(`/tags/${id}`, data),
  delete: (id) => api.delete(`/tags/${id}`),
}

export const templateApi = {
  list: () => api.get('/templates'),
}

export const generateApi = {
  preview: (data) => api.post('/generate/preview', data),
  export: (data) => api.post('/generate/export', data),
}

export default api
