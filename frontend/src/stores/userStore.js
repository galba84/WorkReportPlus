// userStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const username = ref('')
  const role = ref('')
  const token = ref('')

  const isLoggedIn = computed(() => !!token.value)

  function loadFromStorage() {
    const userData = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')

    if (!userData || !storedToken) {
      logout()
      return
    }

    // Перевірка що token має 3 частини (header.payload.signature)
    const parts = storedToken.split('.')
    if (parts.length !== 3) {
      console.warn('❌ Invalid JWT format in localStorage')
      logout()
      return
    }

    try {
      const payload = JSON.parse(atob(parts[1]))
      if (payload.exp * 1000 < Date.now()) {
        logout()
        return
      }

      const user = JSON.parse(userData)
      username.value = user.email
      role.value = user.role
      token.value = storedToken
    } catch (e) {
      console.error('❌ Failed to decode JWT or user data', e)
      logout()
    }
  }


  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    username.value = ''
    role.value = ''
    token.value = ''
  }

  return { username, role, token, isLoggedIn, loadFromStorage, logout }
})
