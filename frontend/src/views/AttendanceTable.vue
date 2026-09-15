<template>
  <div class="attendance-table-container">
    <div class="form-container">
      <h2>Табель</h2>
      <form @submit.prevent="loadReport">
        <label>
          Регіон
          <select v-model="selectedRegion" required>
            <option disabled value="">-- Регіон --</option>
            <option v-for="r in regions" :key="r.id" :value="r.regionName">
              {{ r.regionName }}
            </option>
          </select>
        </label>
        <label>
          Місяць
          <input type="month" v-model="selectedMonth" required/>
        </label>
        <button type="submit">Запит</button>
      </form>
    </div>

    <div v-if="tableData.length" class="report-frame">
      <p>Місяць: {{ selectedMonth }}</p>

      <div class="table-scroll-container">
        <table class="full-width-table">
          <thead>
          <tr>
            <th>Name</th>
            <th>Rank</th>
            <th>Nickname</th>
            <th>Group</th>
            <th v-for="day in daysInMonth" :key="day">{{ day }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="row in tableData" :key="row[0]">
            <td>{{ row[0] }}</td>
            <td>{{ row[1] }}</td>
            <td>{{ row[2] }}</td>
            <td>{{ row[3] }}</td>
            <td v-for="(cell, idx) in row.slice(4)" :key="idx">{{ cell }}</td>
          </tr>
          </tbody>
        </table>
      </div>

      <button @click="exportExcel">Експорт</button>




    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import apiClient from '@/api'

const regions = ref([])
const selectedRegion = ref('')
const selectedMonth = ref('')
const tableData = ref([])
const daysInMonth = ref([])

const loadRegions = async () => {
  try {
    const res = await apiClient.get('/api/report100/regions')
    regions.value = res.data
  } catch (e) {
    console.error('Failed to load regions', e)
  }
}

const loadReport = async () => {
  try {
    const res = await apiClient.get('/api/report100/generate', {
      params: {regionName: selectedRegion.value, reportDate: selectedMonth.value}
    })
    tableData.value = res.data.table
    daysInMonth.value = res.data.daysInMonth
  } catch (e) {
    console.error('Failed to load report', e)
  }
}

const exportExcel = async () => {
  try {
    const res = await apiClient.get('/api/report100/export', {
      params: {
        regionName: selectedRegion.value,
        reportDate: `${selectedMonth.value}-01`
      },
      responseType: 'blob'
    })

    // turn the blob into an object URL
    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const url = window.URL.createObjectURL(blob)

    // figure out filename from Content-Disposition or fallback
    const disposition = res.headers['content-disposition'] || ''
    const match = disposition.match(/filename="?(.+)"?/)
    const filename = match?.[1] || `report100_${selectedRegion.value}_${selectedMonth.value}.xlsx`

    // create a hidden link and click it
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()

    // free memory
    window.URL.revokeObjectURL(url)
  } catch (err) {
    console.error('❌ Export failed', err)
    alert('Не вдалося експортувати звіт. Зайдіть заново чи оновіть сторінку.')
  }
}

onMounted(() => {
  loadRegions()
})

// persistent download URL (won’t include Axios JWT, only works if your session cookie is valid)
const exportUrl = computed(() => {
    if (!selectedRegion.value || !selectedMonth.value) return '#'
      const params = new URLSearchParams({
        regionName: selectedRegion.value,
        reportDate: `${selectedMonth.value}-01`
    })
    return `/api/report100/export?${params.toString()}`
    })
</script>

<style scoped>
.report-frame {
  margin-top: 30px;
  border: 2px solid #ccc;
  border-radius: 10px;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  padding: 20px 20px 10px;
}

.report-frame h3 {
  margin-top: 0;
  color: #333;

  padding: 20px 20px 10px;

}

.table-scroll-container {
  overflow-x: auto;
  overflow-y: auto;
  max-height: 60vh;
  margin-bottom: 15px;
}

.form-container {
  margin-bottom: 30px;
  padding: 20px 20px 10px;
}

.attendance-table-container {
  padding: 20px 20px 10px;
  overflow-x: auto;
  overflow-y: auto;
}

.full-width-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.full-width-table th,
.full-width-table td {
  border: 1px solid #aaa;
  padding: 5px;
  text-align: center;
}

.full-width-table th {
  background-color: #efefef;
}
</style>
