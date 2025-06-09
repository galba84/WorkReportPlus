<template>
  <header class="header">
    <img alt="Vue logo" class="logo" src="@/assets/squad.png" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld msg="Personal Manager Plus" />

      <nav>
        <RouterLink to="/">🏠 Home</RouterLink>
        <RouterLink to="/about">ℹ️ About</RouterLink>
        <RouterLink to="/new-report">📋 Подати Звіт ТГР</RouterLink>
        <RouterLink to="/admin">🛠️ Admin</RouterLink> <!-- ✅ NEW ADMIN LINK -->
        <RouterLink to="/export-report">🖹 Експорт Звіту</RouterLink> <!-- ✅ NEW ADMIN LINK -->
        <RouterLink to="/regions">🖹 Регіони</RouterLink> <!-- ✅ NEW ADMIN LINK -->
      </nav>
    </div>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import HelloWorld from '@/components/HelloWorld.vue'
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

nav {
  margin-top: 0.5rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
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
</style>
