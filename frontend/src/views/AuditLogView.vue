// ---------------------------------------------
// File: frontend/src/views/AuditLogView.vue
// ---------------------------------------------
<template>
  <div class="audit-log-page">
    <h2>📜 Audit Log</h2>

    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-else>
      <div class="log-table-wrapper">
        <table class="log-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Action</th>
            <th>Service</th>
            <th>Entity ID</th>
            <th>User ID</th>
            <th>IP Address</th>
            <th>Timestamp</th>
            <th>Details</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.action }}</td>
            <td>{{ log.serviceId }}</td>
            <td>{{ log.entityId }}</td>
            <td>{{ log.userId || '—' }}</td>
            <td>{{ log.ipAddress }}</td>
            <td>{{ formatDate(log.timestamp) }}</td>
            <td>{{ log.details || '—' }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAuditLogs } from '@/api/auditLog'

const logs = ref([])
const error = ref(null)

function formatDate(ts) {
  // assume ISO string
  const d = new Date(ts)
  return d.toLocaleString()
}

async function fetchLogs() {
  error.value = null
  try {
    logs.value = await listAuditLogs()
  } catch (e) {
    error.value = e.response?.data || e.message || 'Failed to load audit logs'
  }
}

onMounted(fetchLogs)
</script>

<style scoped>
.audit-log-page {
  padding: 1.5rem;
}

.log-table-wrapper {
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid #ccc;
  margin-top: 1rem;
  /* remove padding-bottom, use table margin instead */
}

.log-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem; /* ensure space below last row */
}

.log-table th,
.log-table td {
  border: 1px solid #ddd;
  padding: 0.5rem;
  text-align: left;
  font-size: 0.9rem;
}

.log-table thead th {
  background-color: #f4f4f4;
  position: sticky;
  top: 0;
  z-index: 1;
}

.error-msg {
  color: red;
  font-weight: bold;
}
</style>
