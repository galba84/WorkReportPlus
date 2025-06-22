// src/api/auth.js
import apiClient from '@/api' // ✅ Make sure src/api/index.js exists and exports axios instance

// ✅ Login: send credentials, store JWT & user
export async function login(email, password) {
  const response = await apiClient.post('/api/auth/login', { email, password })

  const { token, user } = response.data

  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))

  return user
}

// ✅ Register a new user
export async function register(email, password, nickname) {
  const response = await apiClient.post('/api/auth/register', {
    email,
    password,
    nickname
  })
  return response.data
}


// ✅ Logout: clear session data
export function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

// ✅ Validate JWT and get current user from backend
export async function getCurrentUser() {
  const response = await apiClient.get('/api/auth/me')
  return response.data
}
