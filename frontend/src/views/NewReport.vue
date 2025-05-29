<!-- src/views/NewReport.vue -->
<script setup>
import {ref, onMounted, watch, computed} from 'vue'
import apiClient from '@/api'

const selectedDate = ref('2025-05-25')

const reports = ref([])
const loading = ref(false)
const error = ref(null)

const totalGroups = computed(() => reports.value.length)
const totalFightingGroups = computed(() =>
  reports.value.filter(r => r.isFighting == true).length // == allows "true" too
)

const totalFightingGroupsWorked = computed(() =>
  reports.value.filter(r =>
    r.fightingGroup == true && r.fightingReport?.trim() !== ''
  ).length
)

const totalFightingGroupsWorkedVerified = computed(() =>
  reports.value.filter(r =>
    r.fightingGroup == true && r.fightingReport?.trim() !== '' && r.ammoVerified == true
  ).length
)


const regions = ref([])
const selectedRegion = ref(null)

const fetchRegions = async () => {
  try {
    const response = await apiClient.get('/api/regions')
    regions.value = response.data
    if (!selectedRegion.value && regions.value.length > 0) {
      selectedRegion.value = regions.value[0].id
    }
  } catch (err) {
    console.error('Failed to load regions:', err)
    error.value = 'Failed to load regions.'
  }
}

const fetchReports = async () => {
  if (!selectedRegion.value) return

  loading.value = true
  try {
    const response = await apiClient.get('/api/group-report-prefill', {
      params: {
        regionId: selectedRegion.value,
        date: selectedDate.value
      }
    })
    reports.value = response.data
    error.value = null
  } catch (err) {
    error.value = err.response?.data?.message || err.message
  } finally {
    loading.value = false
  }
}

watch([selectedDate, selectedRegion], () => {
  if (selectedRegion.value) {
    fetchReports()
  }
})

onMounted(async () => {
  await fetchRegions()
  if (selectedRegion.value) {
    fetchReports()
  }
})
</script>

<template>
  <div class="page-container">
    <!-- Left: Table or static content -->
    <aside class="sidebar">
      <h3>📋 Дані</h3>
      <table>
        <tr>
          <td>🔰 Стан:</td>
          <td>Активний</td>
        </tr>
        <tr>
          <td>📅 Обрано:</td>
          <td>{{ selectedDate }}</td>
        </tr>
        <tr>
          <td>🌍 Регіон:</td>
          <td>
            {{
              regions.find(r => r.id === selectedRegion)?.regionName || '—'
            }}
          </td>

        </tr>
        <tr>
          <td>👥 Груп:</td>
          <td>{{ totalGroups }}</td>
        </tr>
        <tr>
          <td> Бойових:</td>
          <td>{{ totalFightingGroups }}</td>
        </tr>
        <tr>
          <td>🔥 Бойових на виході:</td>
          <td>{{ totalFightingGroupsWorked }}</td>
        </tr>
        <tr>
          <td>☑️ Бойових на виході БК перевірено:</td>
          <td>{{ totalFightingGroupsWorkedVerified }}</td>
        </tr>
      </table>
    </aside>

    <!-- Right: Report form and list -->
    <section class="report-section">
      <div class="report-header">
        <h2>📊 Подати Новий Звіт ТГР</h2>

        <!-- Moved this block to the top -->
        <div v-if="loading">⏳ Loading...</div>
        <div v-else-if="error" class="error">❌ {{ error }}</div>
        <div v-else-if="reports.length === 0">⚠️ No reports found for the selected region and date.</div>

        <div class="form-control">
          <label>Region:</label>
          <select v-model="selectedRegion" :disabled="regions.length === 0">
            <option disabled value="">Select a region</option>
            <option v-for="region in regions" :key="region.id" :value="region.id">
              {{ region.regionName }}
            </option>
          </select>
        </div>

        <div class="form-control">
          <label>Date:</label>
          <input type="date" v-model="selectedDate" />
        </div>
      </div>



      <ul class="report-list">
        <li
          v-for="report in reports"
          :key="report.groupId"
          :class="['report-card', report.isFighting ? 'fighting' : 'non-fighting']"
        >
          <strong>👥 {{ report.groupName }}</strong><br/>
          📋 Опис: {{ report.description }}<br/>
          📣 Результат бойової роботи: {{ report.fightingReport }}<br/>
          💥 Розхід БК перевірено:
          <span :class="report.ammoVerified ? 'yes' : 'no'">
            {{ report.ammoVerified ? 'Yes' : 'No' }}
          </span><br/>
          💥 Бойова група: {{ report.isFighting ? 'Yes' : 'No' }}<br/>

          <div>
            📦 Службовці на бойових:
            <ul v-if="report.fightingContractors.length > 0">
              <li v-for="contractor in report.fightingContractors" :key="contractor.id">
                {{ contractor.firstName }} {{ contractor.middleName }} {{ contractor.lastName }} ({{ contractor.nickName }})
              </li>
            </ul>
            <span v-else>None</span>
          </div>

          <div>
            📦 Службовці не на бойових:
            <ul v-if="report.restContractors.length > 0">
              <li v-for="contractor in report.restContractors" :key="contractor.id">
                {{ contractor.firstName }} {{ contractor.middleName }} {{ contractor.lastName }} ({{ contractor.nickName }})
              </li>
            </ul>
            <span v-else>None</span>
          </div>

          <div>
            📍 Місця бойових дій:
            <ul v-if="report.fightingPlaces.length > 0">
              <li v-for="place in report.fightingPlaces" :key="place.id">
                {{ place.name }}
              </li>
            </ul>
            <span v-else>None</span>
          </div>

          <div>
            🏕 Місця відпочинку:
            <ul v-if="report.restPlaces.length > 0">
              <li v-for="place in report.restPlaces" :key="place.id">
                {{ place.name }}
              </li>
            </ul>
            <span v-else>None</span>
          </div>

        </li>
      </ul>


    </section>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex: 1;
  overflow: auto;
  gap: 2rem;
  width: 100%; /* use full width of the 90vw from parent */
}


.sidebar {
  flex: 0 0 250px;
  background: #f0f0f0;
  padding: 1rem;
  border-radius: 8px;
  min-height: 100%;
}

.sidebar table {
  width: 100%;
  font-size: 0.9rem;
  border-collapse: collapse; /* ensure borders are merged */
}

.sidebar table td {
  padding: 0.5rem;
  border-bottom: 1px solid #ccc; /* ⬅️ adds a row delimiter */
}

.sidebar table tr:last-child td {
  border-bottom: none; /* ⬅️ remove bottom border from last row */
}

.report-section {
  flex: 1;
  overflow-y: auto;
  max-width: 900px; /* ⬅️ limit how wide the right side can get */
}

.form-control {
  margin-bottom: 1rem;
}

input[type="date"],
select {
  padding: 4px 8px;
  border: 1px solid #aaa;
  border-radius: 4px;
}

.report-list {
  list-style: none;
  padding: 0;
}

.report-card {
  border: 1px solid #ccc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  background: #f9f9f9;
}

.error {
  color: red;
}

.report-card {
  border: 1px solid #ccc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  background: #f9f9f9;
}

/* Red background for fighting groups */
.fighting {
  background-color: #ffe5e5; /* light red */
  border-left: 5px solid #cc0000;
}

/* Yellow background for non-fighting groups */
.non-fighting {
  background-color: #fffbe0; /* light yellow */
  border-left: 5px solid #e6b800;
}

.yes {
  color: green;
  font-weight: bold;
}

.no {
  color: red;
  font-weight: bold;
}

.report-header {
  position: sticky;
  top: 0;
  background: #fff;
  padding: 1rem 0;
  z-index: 10;
  border-bottom: 1px solid #ccc;
}



</style>
