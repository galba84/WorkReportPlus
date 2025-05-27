<!-- src/views/GroupReportPrefill.vue -->
<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import apiClient from '@/api' // ✅ your shared instance
// ✅ Reactive values
const selectedRegion = ref('d72c99fc-2308-4e7c-a46b-4edadbb12228')
const selectedDate = ref('2025-05-25') // Default to ISO string (you may replace it dynamically)

const reports = ref([])
const loading = ref(true)
const error = ref(null)

const fetchReports = async () => {
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

// 🔁 Watch for changes in date or region and refetch
watch([selectedDate, selectedRegion], fetchReports)

onMounted(fetchReports)
</script>

<template>
  <div style="max-width: 800px; margin: 2rem auto;">
    <h2>📊 Group Report Prefill</h2>

    <!-- Region dropdown -->
    <div style="margin-bottom: 1rem;">
      <label>Region:</label>
      <select v-model="selectedRegion">
        <option value="d72c99fc-2308-4e7c-a46b-4edadbb12228">Region A</option>
        <option value="b81b4473-99a1-4a47-bdc2-83e48dd7ee6e">Region B</option>
      </select>
    </div>

    <!-- Date selector -->
    <div style="margin-bottom: 1rem;">
      <label>Date:</label>
      <input type="date" v-model="selectedDate" />
    </div>

    <!-- Loading & error states -->
    <div v-if="loading">⏳ Loading...</div>
    <div v-else-if="error">❌ {{ error }}</div>

    <!-- Empty state -->
    <div v-else-if="reports.length === 0">
      ⚠️ No reports found for the selected region and date.
    </div>

    <!-- Reports -->
    <ul v-else style="padding-left: 0;">
      <li v-for="report in reports" :key="report.groupId" style="margin-bottom: 1.5rem; list-style: none; border: 1px solid #ccc; padding: 1rem; border-radius: 8px;">
        <strong>👥 {{ report.groupName }}</strong><br />
        📋 Description: {{ report.description }}<br />
        📣 Fighting Report: {{ report.fightingReport }}<br />
        💥 Ammo Verified: {{ report.ammoVerified ? 'Yes' : 'No' }}<br />
        📦 Fighting Contractors:
        <ul v-if="report.fightingContractors.length > 0">
          <li v-for="cid in report.fightingContractors" :key="cid">{{ cid }}</li>
        </ul>
        <span v-else>None</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
input[type="date"],
select {
  padding: 4px 8px;
  border: 1px solid #aaa;
  border-radius: 4px;
}
</style>
