<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
const googleEnabled = ref(false)
const message = ref('')
const busy = ref(false)
const imports = ['positions', 'units', 'regions', 'groups', 'contractors', 'groups/descriptions', 'places', 'ranks', 'sync']
onMounted(async () => {
  try { googleEnabled.value = (await api.get('/api/admin/integration')).data.googleEnabled }
  catch { message.value = 'Unable to load integration status.' }
})
async function runImport(name) {
  busy.value = true
  try { await api.post('/api/admin/' + name); message.value = 'Import completed.' }
  catch (error) { message.value = error.response?.data?.message || 'Import failed.' }
  finally { busy.value = false }
}
</script>
<template>
  <main class="imports">
    <h1>Data imports</h1>
    <p v-if="!googleEnabled">Google Sheets is not connected. You can explore the demo reports and manage local reference data.</p>
    <p v-else>Import reference and operational data from the configured Google Sheets.</p>
    <p role="status">{{ message }}</p>
    <button v-for="name in imports" :key="name" :disabled="!googleEnabled || busy" @click="runImport(name)">Import {{ name }}</button>
    <p><RouterLink to="/settings">Application settings</RouterLink></p>
  </main>
</template>
<style scoped>.imports { padding: 2rem; } button { margin: .4rem; padding: .6rem; } button:disabled { opacity: .5; }</style>
