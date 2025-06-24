<!--DailyReportView.vue-->
<template>
  <div class="daily-report-page">
    <div class="main-content">
      <h2>Daily Regional Report</h2>

      <div v-if="error" class="error-msg">{{ error }}</div>

      <div class="report-details" v-if="report">
        <p><strong>ID:</strong> {{ report.id }}</p>
        <p><strong>Date:</strong> {{ report.date }}</p>
        <p><strong>Region:</strong> {{ report.regionName }}</p>
        <p><strong>Description:</strong> {{ report.description }}</p>
        <p><strong>Created By:</strong> {{ report.createdBy }}</p>
        <p><strong>Created On:</strong> {{ report.createdOn }}</p>
        <p><strong>Updated By:</strong> {{ report.updatedBy }}</p>
        <p><strong>Updated On:</strong> {{ report.updatedOn }}</p>
        <p><strong>Status:</strong> {{ report.status }}</p>

        <h3>Arrived Contractors</h3>
        <ul>
          <li v-for="([key, dto]) in Object.entries(report.arrivedContractors)" :key="key">
            {{ dto.lastName }} {{ dto.firstName }} ({{ dto.nickName }})
          </li>
        </ul>

        <h3>Departed Contractors</h3>
        <ul>
          <li v-for="([key, dto]) in Object.entries(report.departedContractors)" :key="key">
            {{ dto.lastName }} {{ dto.firstName }} ({{ dto.nickName }})
          </li>
        </ul>

        <h3>Additional Information</h3>
        <table v-if="hasExtraData">
          <thead><tr><th>Key</th><th>Value</th></tr></thead>
          <tbody>
          <tr v-for="(value, key) in report.extraData" :key="key">
            <td>{{ key }}</td><td>{{ value }}</td>
          </tr>
          </tbody>
        </table>
        <p v-else>No extra data available.</p>

        <h3>Group Reports</h3>
        <div v-if="report.groupReports?.length">
          <div v-for="group in report.groupReports" :key="group.groupName" class="group-report">
            <h4>{{ group.groupName }}</h4>
            <p><strong>Worked:</strong> {{ group.worked }}</p>
            <p><strong>Created By:</strong> {{ group.createdBy }}</p>
            <p><strong>Created On:</strong> {{ group.createdOn }}</p>
            <p><strong>Updated By:</strong> {{ group.updatedBy }}</p>
            <p><strong>Updated On:</strong> {{ group.updatedOn }}</p>
            <p><strong>Status:</strong> {{ group.status }}</p>
            <p><strong>Звіт:</strong> {{ group.description }}</p>
            <p><strong>Успіхи:</strong> {{ group.successReport }}</p>

            <h5>Ammunition</h5>
            <ul>
              <li v-for="ammo in group.ammunition" :key="ammo.name">
                {{ ammo.name }} — {{ ammo.amount }} — {{ ammo.unit }}
              </li>
            </ul>

            <h5>Contractors</h5>
            <table class="sub-table">
              <thead><tr><th>Name</th><th>Rank</th><th>Position</th><th>Nickname</th></tr></thead>
              <tbody>
              <tr v-for="con in group.contractors" :key="con.id">
                <td>{{ con.firstName }} {{ con.lastName }}</td>
                <td>{{ con.rank }}</td>
                <td>{{ con.position }}</td>
                <td>{{ con.nickname }}</td>
              </tr>
              </tbody>
            </table>

            <h5>Working Areas</h5>
            <table class="sub-table">
              <thead><tr><th>Name</th><th>Type</th><th>County</th><th>District</th><th>Coef</th></tr></thead>
              <tbody>
              <tr v-for="area in group.workingAreas" :key="area.id">
                <td>{{ area.name }}</td>
                <td>{{ area.areaType }}</td>
                <td>{{ area.county }}</td>
                <td>{{ area.district }}</td>
                <td>{{ area.coeficient }}</td>
              </tr>
              </tbody>
            </table>

            <h5>Contractor Losses</h5>
            <ul>
              <li v-for="id in group.contractorLooses" :key="id">
                {{ id }}
              </li>
            </ul>
          </div>
        </div>
        <p v-else>No group reports available.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getReportById } from '@/api/dailyWorkReport'

const route = useRoute()
const report = ref(null)
const error = ref(null)

async function fetchReport() {
  try {
    report.value = await getReportById(route.params.id)
  } catch (e) {
    error.value = e.response?.data || e.message || 'Failed to load report'
  }
}

onMounted(fetchReport)

const hasExtraData = computed(() => report.value && Object.keys(report.value.extraData || {}).length > 0)
</script>

<style scoped>
.daily-report-page {
  /* full viewport height minus header height */
  height: calc(100vh - 60px);
  overflow-y: auto;
  padding: 1.5rem;
}

.main-content {
  background: white;
  border-radius: 8px;
  padding: 5rem;
}

.error-msg {
  color: red;
  margin-bottom: 1rem;
}

.report-details p {
  margin: 0.5rem 0;
}

.group-report {
  margin-bottom: 2rem;
  border-bottom: 1px solid #ccc;
  padding-bottom: 1rem;
}

.sub-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.sub-table th, .sub-table td {
  border: 1px solid #ddd;
  padding: 0.5rem;
}

.sub-table thead th {
  background: #f4f4f4;
}
</style>
