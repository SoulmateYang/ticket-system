import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Token management
const TOKEN_KEY = 'auth_token'
const STAFF_KEY = 'auth_staff'

export const authApi = {
  login: (data) => apiClient.post('/auth/login', data),

  changePassword: (data) => apiClient.put('/auth/password', data),

  getToken: () => localStorage.getItem(TOKEN_KEY),

  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),

  removeToken: () => localStorage.removeItem(TOKEN_KEY),

  getStaff: () => {
    const staff = localStorage.getItem(STAFF_KEY)
    return staff ? JSON.parse(staff) : null
  },

  setStaff: (staff) => localStorage.setItem(STAFF_KEY, JSON.stringify(staff)),

  removeStaff: () => localStorage.removeItem(STAFF_KEY),

  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(STAFF_KEY)
  }
}

// Request interceptor - attach JWT token
apiClient.interceptors.request.use(
  config => {
    const token = authApi.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor - handle 401
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      authApi.clear()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Ticket APIs
export const ticketApi = {
  createPass: (data) => apiClient.post('/tickets/passes', data),
  activatePass: (id) => apiClient.post(`/tickets/passes/${id}/activate`),
  suspendPass: (id) => apiClient.post(`/tickets/passes/${id}/suspend`),
  cancelPass: (id) => apiClient.post(`/tickets/passes/${id}/cancel`),
  getPassesByVisitor: (visitorId) => apiClient.get(`/tickets/passes/visitor/${visitorId}`),
  getAllPasses: (params) => apiClient.get('/tickets/passes', { params }),
  createSingleTickets: (data) => apiClient.post('/tickets/single', data),
  verifyTicket: (data) => apiClient.post('/tickets/verify', data),
  getAllTickets: (params) => apiClient.get('/tickets', { params })
}

// OTA APIs
export const otaApi = {
  syncOrder: (data) => apiClient.post('/ota/orders/sync', data),
  getPendingOrders: (channel) => apiClient.get('/ota/orders/pending', { params: { channel } }),
  getAllPendingOrders: () => apiClient.get('/ota/orders/pending'),
  generateTickets: (orderId) => apiClient.post(`/ota/orders/${orderId}/generate`)
}

// Staff APIs
export const staffApi = {
  getAll: (params) => apiClient.get('/staff', { params }),
  getById: (id) => apiClient.get(`/staff/${id}`),
  create: (data) => apiClient.post('/staff', data),
  delete: (id) => apiClient.delete(`/staff/${id}`),
  updateRole: (id, role) => apiClient.put(`/staff/${id}/role`, { role }),
  suspend: (id) => apiClient.put(`/staff/${id}/suspend`),
  activate: (id) => apiClient.put(`/staff/${id}/activate`)
}

// Visitor APIs
export const visitorApi = {
  getAll: (params) => apiClient.get('/visitors', { params }),
  getById: (id) => apiClient.get(`/visitors/${id}`)
}

// Stats API
export const statsApi = {
  getDashboardStats: () => apiClient.get('/stats/dashboard'),
  getEntryLogs: (params) => apiClient.get('/logs/entries', { params })
}

export default apiClient