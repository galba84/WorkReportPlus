<!--SearchReportView.vue-->
<template>
  <div class="search-reports-page">
    <h2>📋 Search Reports</h2>

    <form @submit.prevent="onSearch" class="filter-form">
      <div class="form-group">
        <label for="startDate">Start Date:</label>
        <input type="date" id="startDate" v-model="filters.startDate" />
      </div>
      <div class="form-group">
        <label for="endDate">End Date:</label>
        <input type="date" id="endDate" v-model="filters.endDate" />
      </div>
      <div class="form-group">
        <label for="region">Region:</label>
        <select id="region" v-model="filters.region">
          <option value="">-- All Regions --</option>
          <option v-for="name in regionNames" :key="name" :value="name">
            {{ name }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label for="status">Status:</label>
        <select id="status" v-model="filters.status">
          <option value="ACTIVE">ACTIVE</option>
          <option value="ALL">ALL</option>
          <option value="DELETED">DELETED</option>
        </select>
      </div>
      <button type="submit">Search</button>
    </form>

    <div v-if="error" class="error-msg">{{ error }}</div>

    <div v-if="reports.length > 0" class="results">
      <h3>Results</h3>
      <div class="table-wrapper">
        <table class="results-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Status</th>
            <th>Date</th>
            <th>Region</th>
            <th>Description</th>
            <th>Action</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="r in reports" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.status }}</td>
            <td>{{ r.date }}</td>
            <td>{{ r.regionName }}</td>
            <td>{{ r.description }}</td>
            <td>
               <RouterLink :to="{ name: 'open-report', params: { id: r.id } }">Open</RouterLink>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div v-else-if="!loading" class="no-results">
      No reports found.
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { searchReports } from '@/api/dailyWorkReport'
import { RouterLink } from 'vue-router'

const regionNames = ref([])
const reports = ref([])
const error = ref(null)
const loading = ref(false)

const today = new Date().toISOString().split('T')[0]
const filters = reactive({
  startDate: today,
  endDate: today,
  region: '',
  status: 'ACTIVE'
})

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    const data = await searchReports(filters)
    reports.value = data.reports
    regionNames.value = data.regionNames
    filters.startDate = data.startDate
    filters.endDate = data.endDate
  } catch (e) {
    error.value = e.response?.data || e.message || 'Search failed'
  } finally {
    loading.value = false
  }
}

function onSearch() {
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.search-reports-page {
  padding: 1.5rem;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.table-wrapper {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ccc;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
}

.results-table th,
.results-table td {
  padding: 0.5rem;
  border: 1px solid #ddd;
  text-align: left;
}

.results-table thead th {
  background: #f4f4f4;
  position: sticky;
  top: 0;
}

.error-msg {
  color: red;
  margin-bottom: 1rem;
}

.no-results {
  font-style: italic;
  color: #666;
}
</style>
