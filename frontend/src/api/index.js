import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
})

// 👉 Додаємо токен до кожного запиту
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token && token.split('.').length === 3) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 👉 Опційно: глобальна обробка 401 (якщо потрібно)
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      console.warn('🔒 Token expired or unauthorized')
      // Можна автоматично розлогінити:
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login' // або через router
    }
    return Promise.reject(error)
  }
)

export default apiClient
