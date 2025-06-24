<!-- src/views/UsersView.vue -->
<template>
  <div class="users-page">
    <h2>Керування користувачами</h2>

    <p v-if="error" class="error-msg">{{ error }}</p>
    <p v-if="success" class="success-msg">{{ success }}</p>
    <div class="users-table-wrapper">
      <table class="users-table">
        <thead>
        <tr>
          <th>Nickname</th>
          <th>Email</th>
          <th>Role</th>
          <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>
            <input v-model="user.nickname" type="text"/>
          </td>
          <td>{{ user.email }}</td>
          <td>
            <select v-model="user.role">
              <option value="ADMIN">ADMIN</option>
              <option value="POWER_USER">POWER_USER</option>
              <option value="USER">USER</option>
              <option value="GUEST">GUEST</option>
            </select>
          </td>
          <td>
            <button @click="saveUser(user)">Save</button>
          </td>
        </tr>
        </tbody>
      </table>

      <hr/>

      <h3>Додати нового користувача</h3>
      <form @submit.prevent="createUser" class="new-user-form">
        <input
          v-model="newUser.nickname"
          type="text"
          placeholder="Nickname"
          required
        />
        <input
          v-model="newUser.email"
          type="email"
          placeholder="Email"
          required
        />
        <input
          v-model="newUser.password"
          type="password"
          placeholder="Password"
          required
        />
        <select v-model="newUser.role">
          <option value="ADMIN">ADMIN</option>
          <option value="POWER_USER">POWER_USER</option>
          <option value="USER">USER</option>
        </select>
        <button type="submit">Add User</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {listUsers, upsertUser} from '@/api/users'

const users = ref([])
const error = ref(null)
const success = ref(null)

const newUser = ref({
  nickname: '',
  email: '',
  password: '',
  role: 'USER'
})

async function fetchUsers() {
  error.value = null
  try {
    users.value = await listUsers()
  } catch (e) {
    error.value = e.response?.data || e.message || 'Failed to load users'
  }
}

async function saveUser(user) {
  error.value = null
  success.value = null
  try {
    // omit password for existing users
    await upsertUser({...user, password: null})
    success.value = 'User updated successfully'
    await fetchUsers()
  } catch (e) {
    error.value = e.response?.data || e.message || 'Update failed'
  }
}

async function createUser() {
  error.value = null
  success.value = null
  try {
    await upsertUser(newUser.value)
    success.value = 'User created successfully'
    newUser.value = {nickname: '', email: '', password: '', role: 'USER'}
    await fetchUsers()
  } catch (e) {
    error.value = e.response?.data || e.message || 'Creation failed'
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.users-page {
  padding: 1.5rem;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.users-table th,
.users-table td {
  border: 1px solid #ccc;
  padding: 0.5rem;
}

.new-user-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 400px;
  margin-top: 1rem;
}

.error-msg {
  color: red;
  margin-bottom: 0.5rem;
}

.success-msg {
  color: green;
  margin-bottom: 0.5rem;
}

.users-page {
  padding: 1.5rem;
}

/* Scrollable wrapper */
.users-table-wrapper {
  max-height: 300px; /* adjust as needed */
  overflow-y: auto;
  border: 1px solid #ccc; /* optional frame */
  margin-bottom: 1rem;
}

/* Make sure the table fills its container */
.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  border: 1px solid #ccc;
  padding: 0.5rem;
}
</style>
