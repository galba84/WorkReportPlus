<template>
  <div class="form-container">
    <h2>Експортувати звіт ТГР</h2>

    <form class="report-form" @submit.prevent="generateDocument">
      <!-- Region selection -->
      <label for="regionId">Вибрати регіон:</label>
      <select id="regionId" v-model="selectedRegionId" required>
        <option disabled value="">-- Вибрати регіон --</option>
        <option v-for="region in regionNames" :key="region.id" :value="region.id">
          {{ region.regionName }}
        </option>
      </select>

      <!-- Report date -->
      <label for="reportDate">Дата звіту:</label>
      <input type="date" id="reportDate" v-model="reportDate" required />

      <br />
      <button type="submit" class="generate-button">згенерувати</button>
    </form>

    <!-- Download link -->
    <p v-if="downloadUrl" id="downloadLink">
      <a :href="downloadUrl" :download="`RegionReport_${reportDate}.rtf`">
        завантажити
      </a>
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from "@/api/index.js";

const regionNames = ref([])
const selectedRegionId = ref('')
const reportDate = ref('')
const downloadUrl = ref('')

onMounted(async () => {
  try {
    const response = await apiClient.get('/api/regions')
    regionNames.value = response.data
  } catch (error) {
    console.error('Failed to fetch regions:', error)
    alert('Failed to load region list.')
  }
})

async function generateDocument() {
  if (!reportDate.value || !selectedRegionId.value) {
    alert('Please select both a region and a report date.')
    return
  }

  try {
    const url = `/api/daily-work-report/export/word?templateName=Region Report&reportDate=${encodeURIComponent(reportDate.value)}&regionId=${encodeURIComponent(selectedRegionId.value)}`
    const response = await apiClient.get(url, { responseType: 'blob' })

    const contentType = response.headers['content-type']
    if (contentType && contentType.includes('application/json')) {
      // ✅ Convert blob to text first
      const text = await response.data.text?.() || await new Response(response.data).text()
      const json = JSON.parse(text)
      throw new Error(json.message || 'Невідома помилка сервера')
    }

    // ✅ If it's a file, generate the blob URL
    downloadUrl.value = URL.createObjectURL(response.data)

  } catch (err) {
    const message = err?.response?.data?.message || err?.message || 'Помилка сервера'
    alert('❌ ' + message)
  }
}


</script>

<style scoped>
.form-container {
  max-width: 500px;
  margin: 2rem auto;
  padding: 2rem;
  border: 2px solid #ccc;
  border-radius: 12px;
  background-color: #fdfdfd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.form-container h2 {
  font-size: 1.6rem;
  margin-bottom: 1.5rem;
  font-weight: 700;
}
.report-form > * {
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.report-form label {
  font-weight: 600;
  margin-bottom: 0.3rem;
}

.report-form select,
.report-form input[type="date"] {
  padding: 0.5rem 0.75rem;
  font-size: 1rem;
  border: 1px solid #aaa;
  border-radius: 6px;
}

.generate-button {
  margin-top: 1rem; /* 👈 adds vertical space above the button */
  padding: 0.75rem;
  font-size: 1.1rem;
  background-color: #007bff;
  color: white;
  border: 2px solid #0056b3;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s ease;
}


.generate-button:hover {
  background-color: #0056b3;
}

.download-link {
  margin-top: 1rem;
  font-weight: bold;
}

.download-link a {
  color: #28a745;
  text-decoration: none;
}

.download-link a:hover {
  text-decoration: underline;
}

</style>
