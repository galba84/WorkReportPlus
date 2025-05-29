
<template>
  <div class="container">
    <h1>{{ $t('page.heading.admin') }}</h1>

    <div v-if="infoMessage" class="alert alert-info">
      <p>{{ infoMessage }}</p>
    </div>

    <p>{{ $t('page.label.roles') }}</p>
    <p v-for="role in userRoles" :key="role">
      <span>{{ role.replace('ROLE_', '') }}</span>
    </p>

    <div class="form-container">
      <button @click="submit('/admin/contractors')">{{ $t('button.updateContractors') }}</button>
      <button @click="submit('/admin/groups')">{{ $t('button.updateGroups') }}</button>
      <button @click="submit('/admin/regions')">{{ $t('button.updateRegions') }}</button>
      <button @click="submit('/admin/groups/descriptions')">{{ $t('button.Description') }}</button>
      <button @click="submit('/admin/places')">{{ $t('button.Places') }}</button>
      <button @click="submit('/admin/ranks')">{{ $t('button.Ranks') }}</button>
      <button @click="submit('/admin/positions')">{{ $t('button.Positions') }}</button>
      <button @click="submit('/admin/units')">{{ $t('button.Units') }}</button>

      <div>
        <p>
          🔗
          <a
            href="https://docs.google.com/spreadsheets/d/1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss/edit?gid=197991214"
            target="_blank"
            rel="noopener noreferrer"
          >
            таблицю звітів у Google Sheets
          </a>
        </p>
      </div>

      <div>
        <p>
          🔗
          <a
            href="https://docs.google.com/spreadsheets/d/1LkkLuk7y_fB8BTa-T4kYPYTgT-cVhIIgJTlZyo2b0UA/edit?gid=1453816151"
            target="_blank"
            rel="noopener noreferrer"
          >
            таблицю ШПС у Google Sheets
          </a>
        </p>
      </div>

      <div>
        <p>
          🔗
          <a
            href="https://docs.google.com/spreadsheets/d/1YtvuAvmyZ5JZFG9uQTy3GF38oHJpQsHRAjAGOl7ioF8"
            target="_blank"
            rel="noopener noreferrer"
          >
            оперативний звіт у Google Sheets
          </a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import api from '@/api' // ✅ your configured Axios with JWT

const { t } = useI18n() // enables $t in <template>

const infoMessage = ref(null)
const userRoles = ref([])

onMounted(async () => {
  try {
    const response = await api.get('/api/user/roles') // ✅ use `api`
    userRoles.value = response.data
  } catch (e) {
    console.error('Failed to load user roles', e)
  }
})

function submit(endpoint) {
  api.post(endpoint) // ✅ use `api` to send JWT with request
    .then(() => {
      alert('Success')
    })
    .catch(err => {
      console.error('Submission failed', err)
      alert('Error')
    })
}
</script>


<style scoped>
.container {
  padding: 2rem;
}
.alert {
  background-color: #e0f3ff;
  padding: 1rem;
  border: 1px solid #a3c5e0;
  margin-bottom: 1rem;
}
.form-container button {
  display: block;
  margin: 0.5rem 0;
}
</style>
