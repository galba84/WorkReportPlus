<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const error = ref(null)
const success = ref(null)

const router = useRouter()

const submit = async () => {
  error.value = null
  success.value = null

  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match'
    return
  }

  try {
    const result = await register(email.value, password.value, nickname.value)
    success.value = result || 'User registered successfully'
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    error.value = e.response?.data || e.message || 'Registration failed'
  }
}
</script>

<template>
  <div class="register">
    <h2>Register</h2>
    <form @submit.prevent="submit">
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="nickname" type="text" placeholder="Nickname" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <input v-model="confirmPassword" type="password" placeholder="Confirm Password" required />
      <button type="submit">Register</button>

      <p v-if="error" style="color: red">{{ error }}</p>
      <p v-if="success" style="color: green">{{ success }}</p>
    </form>
  </div>
</template>

