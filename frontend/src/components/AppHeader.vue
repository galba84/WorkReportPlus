<template>
  <header class="header">
    <img alt="Vue logo" class="logo" src="@/assets/squad.png" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld msg="Personal Manager Plus" />

      <nav class="header-nav">
        <!-- top row: visible to any logged-in user -->
        <div class="nav-row public-links">
          <RouterLink to="/">🏠 Home</RouterLink>
          <RouterLink to="/about">ℹ️ About</RouterLink>
          <RouterLink
            to="/new-report"
            v-if="['POWER_USER', 'ADMIN'].includes(userStore.role)"
          >📋 Подати Звіт ТГР</RouterLink>
          <RouterLink
            to="/search-report"
            v-if="['USER', 'POWER_USER', 'ADMIN'].includes(userStore.role)"
          >🖹 Пошук Звіту</RouterLink>
          <RouterLink
            to="/export-report"
            v-if="['USER', 'POWER_USER', 'ADMIN'].includes(userStore.role)"
          >🖹 Експорт Звіту</RouterLink>
          <RouterLink to="/attendance">🛠️ Табель</RouterLink>
        </div>

        <!-- bottom row: only ADMINs -->
        <div class="nav-row admin-links" v-if="userStore.role === 'ADMIN'">
          <RouterLink to="/admin">🛠️ Admin</RouterLink>

          <RouterLink to="/regions">🖹 Регіони</RouterLink>
          <RouterLink to="/groups">👥 Групи</RouterLink>
          <RouterLink to="/RegionReportTemplate">👥 ТГР Темплейти</RouterLink>
          <RouterLink to="/users">👥 UsersView</RouterLink>
          <RouterLink to="/audit-logs">👥 Audit Logs</RouterLink>
          <RouterLink to="/settings">👥 Налаштування</RouterLink>
        </div>
      </nav>
    </div>

    <div class="user-info">
      <div class="user-details" v-if="userStore.isLoggedIn">
        <div><strong>{{ userStore.username }}</strong></div>
        <div style="font-size: 0.85em;">🧑‍💼 {{ userStore.role }}</div>
      </div>

      <button @click="logout" v-if="userStore.isLoggedIn">🚪 Logout</button>

      <div v-else class="auth-links">
        <RouterLink to="/login">🔐 Login</RouterLink>
        <p>
          Don’t have an account?
          <RouterLink to="/register">Register here</RouterLink>
        </p>
      </div>
    </div>


  </header>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import HelloWorld from '@/components/HelloWorld.vue'
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const router = useRouter()

onMounted(() => {
  userStore.loadFromStorage()
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>



<style scoped>
.header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background-color: #2c3e50;
  color: white;
  flex-wrap: wrap;
}

.logo {
  width: 125px;
  height: 125px;
}

.wrapper {
  display: flex;
  flex-direction: column; /* ⬅️ stack HelloWorld and nav vertically */
  justify-content: center;
}

nav a {
  color: white;
  text-decoration: none;
  font-weight: bold;
  padding: 0 0.5rem;
  border-left: 1px solid rgba(255, 255, 255, 0.3);
}

nav a:first-of-type {
  border-left: none;
}

nav a.router-link-exact-active {
  text-decoration: underline;
}

.user-info {
  margin-left: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  padding-left: 1rem;
  color: white;
  min-width: 120px;
}

.user-info button {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 0.5rem;
}

.user-info button:hover {
  background: #c0392b;
}

.user-details {
  text-align: right;
}

.auth-links {
  color: white;
  margin-top: 0.5em;
}
.auth-links a {
  color: white;
  text-decoration: underline;
}

.header-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem; /* space between rows */
}

/* each row lays out links in a row */
.nav-row {
  display: flex;
  gap: 1rem;
}

/* optional: visually separate the admin row */
.admin-links {
  padding-top: 0.5rem;
  border-top: 1px solid #ccc;
}

</style>
