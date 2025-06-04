<!-- src/views/NewReport.vue -->
<script setup>
import {ref, onMounted, watch, computed} from 'vue'
import apiClient from '@/api'

const selectedDate = ref('2025-05-25')

const reports = ref([])
const loading = ref(false)
const error = ref(null)
const validationErrors = ref({})
const hasValidationErrorsForGroup = index => {
  const prefix = `groupReports[${index}]`
  return Object.keys(validationErrors.value).some(key => key.startsWith(prefix))
}



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

    // Set up report list with per-report selectedFightingPlaceId
    reports.value = response.data.map(report => ({
      ...report,
      selectedFightingPlaceId: report.fightingPlaces?.[0]?.id || null
    }))

    error.value = null
  } catch (err) {
    error.value = err.response?.data?.message || err.message
  } finally {
    loading.value = false
  }
}


// Computed stats
const totalGroups = computed(() => reports.value.length)

const totalFightingGroups = computed(() =>
  reports.value.filter(r => r.isFighting == true).length
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

const totalContractors = computed(() =>
  reports.value.reduce((sum, r) => sum + r.fightingContractors.length + r.restContractors.length, 0)
)

const totalFightingContractors = computed(() =>
  reports.value.reduce((sum, r) => sum + r.fightingContractors.length, 0)
)

const totalRestContractors = computed(() =>
  reports.value.reduce((sum, r) => sum + r.restContractors.length, 0)
)

// React to date/region changes
watch([selectedDate, selectedRegion], () => {
  if (selectedRegion.value) {
    fetchReports()
    fetchContractorData()
  }
})

onMounted(async () => {
  await fetchRegions()
  await fetchContractorData()

  if (selectedRegion.value) {
    fetchReports()
  }

  if (contractorData.value.arrived?.[0]) {
    arrivedOrderNumber.value = contractorData.value.arrived[0].orderNumber || ''
    arrivedOrderDate.value = contractorData.value.arrived[0].orderDate || ''
  }

  if (contractorData.value.departed?.[0]) {
    departedOrderNumber.value = contractorData.value.departed[0].orderNumber || ''
    departedOrderDate.value = contractorData.value.departed[0].orderDate || ''
  }
})


const submitReport = async () => {
  try {
    validationErrors.value = {}

    const payload = {
      regionName: regions.value.find(r => r.id === selectedRegion.value)?.regionName || '',
      reportDate: selectedDate.value,
      groupReports: reports.value.map(report => ({
        groupName: report.groupName,
        description: report.description,
        placeCoefficients: report.placeCoefficients || {}, // Map<UUID, String>
        contractorPlaceMap: report.contractorPlaceMap || {}, // Map<String, List<String>>
        successReport: report.fightingReport || '', // renamed in DTO
        ammunition: report.ammunition || '',

        contractorLoosesIdTypeMap: report.contractorLoosesIdTypeMap || {}, // optional
        worked: report.fightingReport?.trim() !== '',
        ammoVerified: report.ammoVerified || false,
        extraDataGroupReport: report.extraDataGroupReport || {},

        fightingContractors: report.fightingContractors.map(c => c.id),
        restContractors: report.restContractors.map(c => c.id),
        fightingPlaces: report.selectedFightingPlaceId ? [report.selectedFightingPlaceId] : [],
        restPlaces: report.restPlaces.map(p => p.id)
      })),
      extraData: {
        departedOrderNumber: departedOrderNumber.value,
        departedOrderDate: departedOrderDate.value,
        arrivedOrderNumber: arrivedOrderNumber.value,
        arrivedOrderDate: arrivedOrderDate.value
      },
      status: true, // or another meaningful value
      arrivedContractors: contractorData.value.arrived.map(c => c.id),
      departedContractors: contractorData.value.departed.map(c => c.id),
      regionDescription: "description region"
    }

    const response = await apiClient.post('/api/daily-work-report', payload)
    alert('✅ Звіт успішно подано!')
    console.log(response.data)
  } catch (err) {
    if (err.response?.status === 400) {
      validationErrors.value = err.response.data
    } else {
      console.error('❌ Failed to submit report:', err)
      alert('❌ Submission failed: ' + (err.response?.data?.message || err.message))
    }
  }
}


const contractorData = ref({arrived: [], departed: []})

const fetchContractorData = async () => {
  if (!selectedRegion.value) return;

  try {
    const res = await apiClient.get('/api/daily-work-report/contractors', {
      params: {
        date: selectedDate.value,
        regionId: selectedRegion.value
      }
    });

    contractorData.value.arrived = res.data.arrived
      .filter(r => r.region.id === selectedRegion.value)
      .map(r => ({
        ...r.contractor,
        orderNumber: r.orderNumber,
        orderDate: r.orderDate
      }));

    contractorData.value.departed = res.data.departed
      .filter(r => r.region.id === selectedRegion.value)
      .map(r => ({
        ...r.contractor,
        orderNumber: r.orderNumber,
        orderDate: r.orderDate
      }));

    // Assign after data is ready
    if (contractorData.value.arrived?.[0]) {
      arrivedOrderNumber.value = contractorData.value.arrived[0].orderNumber || '';
      arrivedOrderDate.value = contractorData.value.arrived[0].orderDate || '';
    }

    if (contractorData.value.departed?.[0]) {
      departedOrderNumber.value = contractorData.value.departed[0].orderNumber || '';
      departedOrderDate.value = contractorData.value.departed[0].orderDate || '';
    }

  } catch (err) {
    console.error('Failed to fetch contractor data:', err);
  }
};


const getFieldErrors = (index, fieldName) => {
  const key = `groupReports[${index}].${fieldName}`
  return validationErrors.value[key] || ''
}


const departedOrderNumber = ref('')
const departedOrderDate = ref('')
const arrivedOrderNumber = ref('')
const arrivedOrderDate = ref('')


watch([departedOrderNumber, departedOrderDate], ([newNumber, newDate]) => {
  if (contractorData.value.departed?.[0]) {
    contractorData.value.departed[0].orderNumber = newNumber
    contractorData.value.departed[0].orderDate = newDate
  }
})

watch([arrivedOrderNumber, arrivedOrderDate], ([newNumber, newDate]) => {
  if (contractorData.value.arrived?.[0]) {
    contractorData.value.arrived[0].orderNumber = newNumber
    contractorData.value.arrived[0].orderDate = newDate
  }
})


</script>

<template>
  <div class="page-container">
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
            {{ regions.find(r => r.id === selectedRegion)?.regionName || '—' }}
          </td>
        </tr>
      </table>

      <div class="group-stats">
        <table>
          <tr>
            <td colspan="2"><strong>👥 Статистика груп</strong></td>
          </tr>
          <tr>
            <td>👥 Груп:</td>
            <td>{{ totalGroups }}</td>
          </tr>
          <tr>
            <td>🪖 Бойових:</td>
            <td>{{ totalFightingGroups }}</td>
          </tr>
          <tr>
            <td>🔥 На виході:</td>
            <td>{{ totalFightingGroupsWorked }}</td>
          </tr>
          <tr>
            <td>☑️ Вихід перевірено:</td>
            <td>{{ totalFightingGroupsWorkedVerified }}</td>
          </tr>
        </table>
      </div>

      <div class="contractor-stats">
        <table>
          <tr>
            <td colspan="2"><strong>👷‍♂️ Статистика службовців</strong></td>
          </tr>
          <tr>
            <td>👷 Усього службовців:</td>
            <td>{{ totalContractors }}</td>
          </tr>
          <tr>
            <td>🪖 На бойових:</td>
            <td>{{ totalFightingContractors }}</td>
          </tr>
          <tr>
            <td>🛌 На ППД:</td>
            <td>{{ totalRestContractors }}</td>
          </tr>
        </table>
      </div>

      <div class="contractor-stats">
        <div class="column">
          <h3>🟢 Прибули ({{ contractorData.arrived.length }})</h3>

          <div v-if="contractorData.arrived.length > 0">
            <p>
              📄 Наказ: {{ contractorData.arrived[0].orderNumber || '—' }}
              від {{ contractorData.arrived[0].orderDate || '—' }}
            </p>
            <ul>
              <li v-for="c in contractorData.arrived" :key="c.id">
                {{ c.firstName }} {{ c.lastName }} ({{ c.nickName }})
              </li>
            </ul>
          </div>

          <span v-else>—</span>
        </div>

      </div>
      <div class="contractor-stats">
        <div class="column">
          <h3>🔴 Вибули ({{ contractorData.departed.length }})</h3>

          <div v-if="contractorData.departed.length > 0">
            <div v-if="!contractorData.departed[0].orderNumber || !contractorData.departed[0].orderDate">
              <label>
                📄 Наказ №:
                <input v-model="departedOrderNumber" placeholder="Номер наказу" />
              </label>
              <br />
              <label>
                Дата:
                <input type="date" v-model="departedOrderDate" />
              </label>
            </div>
            <p v-else>
              📄 Наказ: {{ contractorData.departed[0].orderNumber }}
              від {{ contractorData.departed[0].orderDate }}
            </p>

            <ul>
              <li v-for="c in contractorData.departed" :key="c.id">
                {{ c.firstName }} {{ c.lastName }} ({{ c.nickName }})
              </li>
            </ul>
          </div>
          <span v-else>—</span>
        </div>
      </div>

    </aside>

    <section class="report-section">
      <div class="report-header">

        <div class="header-left">
          <div class="header-line">
            <button
              @click="submitReport"
              :disabled="loading || reports.length === 0"
              class="submit-button"
            >
              ✅ Подати звіт
            </button>

            <h2 class="report-title">📊 Подати Новий Звіт ТГР</h2>

            <div v-if="generalError" class="general-error">
              {{ generalError }}
            </div>

          </div>

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
            <input type="date" v-model="selectedDate"/>
          </div>
        </div>


      </div>


      <div v-if="loading" class="loading-indicator">
        ⏳ Завантаження звітів...
      </div>
      <div v-else-if="reports.length === 0" class="empty-state">
        📭 Немає доступних звітів для цієї дати та регіону.
      </div>


      <ul v-else class="report-list">
        <li
          v-for="(report, index) in reports"
          :key="report.groupId"
          :class="[
    'report-card',
    report.isFighting ? 'fighting' : 'non-fighting',
    hasValidationErrorsForGroup(index) ? 'tab-error' : ''
  ]"
          style="position: relative;"
        >
          <!-- 🗡️ Іконка меча для бойової групи -->
          <span
            v-if="report.isFighting"
            style="
      position: absolute;
      top: 8px;
      right: 12px;
      font-size: 28px;
      color: crimson;
      transform: rotate(-10deg);
    "
            title="Бойова група"
          >
    🗡️
  </span>
          <!-- 🛑 Помилки -->
          <div v-if="hasValidationErrorsForGroup(index)" class="error">
            <ul>
              <li
                v-for="([key, message]) in Object.entries(validationErrors).filter(([k]) =>
        k.startsWith(`groupReports[${index}]`)
      )"
                :key="key"
              >
                <strong>{{ key.split('.').pop() }}:</strong> {{ message }}
              </li>
            </ul>
          </div>


          <!-- Заголовки -->
          <strong>👥 {{ report.groupName }}</strong><br/>
          📋 Опис: {{ report.description || '—' }}<br/>
          📣 Результат бойової роботи: {{ report.fightingReport || '—' }}<br/>
          💥 Розхід БК: {{ report.ammunition || '—' }}<br/>

          <!-- Перевірка БК -->
          💥 Розхід БК перевірено:
          <span :class="report.ammoVerified ? 'yes' : 'no'">
    {{ report.ammoVerified ? 'Так' : 'Ні' }}
  </span><br/>

          💥 Бойова група: {{ report.isFighting ? 'Так' : 'Ні' }}<br/>

          <!-- Службовці на бойових -->
          <div>
            📦 Службовці на бойових:
            <ul v-if="report.fightingContractors.length > 0">
              <li v-for="contractor in report.fightingContractors" :key="contractor.id">
                {{ contractor.firstName }} {{ contractor.middleName }} {{ contractor.lastName }}
                ({{ contractor.nickName }})
              </li>
            </ul>
            <span v-else>Немає</span>
          </div>

          <!-- Службовці на ППД -->
          <div>
            📦 Службовці не на бойових:
            <ul v-if="report.restContractors.length > 0">
              <li v-for="contractor in report.restContractors" :key="contractor.id">
                {{ contractor.firstName }} {{ contractor.middleName }} {{ contractor.lastName }}
                ({{ contractor.nickName }})
              </li>
            </ul>
            <span v-else>Немає</span>
          </div>

          <!-- Місця бойових дій -->
          <div>
            📍 Місця бойових дій:
            <select v-if="report.fightingPlaces.length > 0"
                    v-model="report.selectedFightingPlaceId">
              <option v-for="place in report.fightingPlaces" :key="place.id" :value="place.id">
                {{ place.name }}
              </option>
            </select>
            <span v-else>Немає</span>

            <span v-if="getFieldErrors(index, 'placeIds')" class="error">
      {{ getFieldErrors(index, 'placeIds') }}
    </span>
          </div>

          <!-- Місця ППД -->
          <div>
            🏕 Місця ППД:
            <span v-if="report.restPlaces.length > 0">
      {{ report.restPlaces.map(place => place.name).join(', ') }}
    </span>
            <span v-else>Немає</span>
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
  width: 100%;
}

.sidebar {
  max-height: 100vh;     /* Fill full viewport height */
  overflow-y: auto;      /* Enables vertical scroll */
  padding: 1rem;
  background-color: #f8f9fa; /* Optional background */
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.1); /* Optional scroll styling */
}


.sidebar table {
  width: 100%;
  font-size: 0.9rem;
  border-collapse: collapse;
}

.sidebar table td {
  padding: 0.5rem;
  border-bottom: 1px solid #ccc;
}

.sidebar table tr:last-child td {
  border-bottom: none;
}

.report-section {
  flex: 1;
  overflow-y: auto;
  max-width: 900px;
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

.contractor-stats {
  margin-top: 1rem;
  padding: 0.5rem;
  border: 1px dashed #aaa;
  border-radius: 6px;
  background-color: #fafafa;
}

.group-stats {
  margin-top: 1rem;
  padding: 0.6rem;
  border: 1px dashed #aaa;
  border-radius: 6px;
  background-color: #f4f4f4;
}

.report-header {
  display: flex;
  justify-content: space-between;
  gap: 2rem;
  padding: 1rem 0;
  border-bottom: 1px solid #ccc;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left,
.header-right-two-columns .column {
  flex: 1;
  overflow-y: auto;
  max-height: 30vh; /* Optional: apply to each column separately */
}

.header-line {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.report-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold;
}

.submit-button {
  padding: 6px 12px;
  background-color: #28a745;
  border: none;
  color: white;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.submit-button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

.tab-error {
  border: 2px solid red;
  background-color: #ffe5e5;
}

.error {
  color: red;
  font-size: 0.9em;
  margin-top: 4px;
}

.report-card .error ul {
  margin: 0 0 0.5rem 1rem;
  padding: 0;
  list-style: disc;
}
</style>
