<script setup>
import { RouterView } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
</script>

<template>
  <div id="app">
    <AppHeader />
    <main class="main-layout">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
#app {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

main.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding: 1rem;
  width: 90vw;               /* ⬅️ new: 90% of the screen width */
  margin: 0 auto;            /* ⬅️ center the content horizontally */
  box-sizing: border-box;
}
</style>
