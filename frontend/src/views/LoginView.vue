<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'

const email = ref('')
const password = ref('')
const error = ref(null)
const router = useRouter()

const submit = async () => {
  try {
    const user = await login(email.value, password.value)
    console.log('✅ Login successful:', user)

    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    const message = e.response?.data?.message || e.response?.data || e.message || 'Login failed'
    console.error('❌ Login failed:', message)
    error.value = message
  }
}
</script>

<template>
  <div class="login">
    <h2>Login</h2>
    <form @submit.prevent="submit">
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
      <p v-if="error" style="color: red">{{ error }}</p>
    </form>
  </div>
</template>
