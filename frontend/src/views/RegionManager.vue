<template>
  <div class="p-4">
    <h2 class="text-xl font-bold mb-4">📍 Менеджер Регіонів</h2>

    <label class="mb-2 block">
      <input type="checkbox" v-model="showAll" @change="fetchRegions" />
      Показати всі (включно з неактивними)
    </label>

    <div class="overflow-x-auto max-h-[500px] overflow-y-auto border border-gray-300 rounded mb-4">
      <table class="min-w-full">
      <thead>
      <tr class="bg-gray-200 text-left">
        <th class="p-2">ID</th>
        <th class="p-2">Назва Регіону</th>
        <th class="p-2">Активний</th>
        <th class="p-2">Дії</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="region in regions" :key="region.id" class="border-t">
        <td class="p-2">{{ region.id }}</td>

        <td class="p-2">
          <template v-if="editedRegion && editedRegion.id === region.id">
            <input
              v-model="editedRegion.regionName"
              class="border px-2 py-1 w-full"
            />
          </template>
          <template v-else>
            {{ region.regionName }}
          </template>
        </td>

        <td class="p-2">
          {{ region.status ? "✅" : "❌" }}
        </td>

        <td class="p-2 space-x-2">
          <button
            v-if="!editedRegion || editedRegion.id !== region.id"
            @click="editRegion(region)"
            class="bg-blue-500 text-white px-2 py-1 rounded"
          >
            ✏️ Редагувати
          </button>

          <button
            v-else
            @click="saveRegion"
            class="bg-green-500 text-white px-2 py-1 rounded"
          >
            💾 Зберегти
          </button>

          <button
            @click="toggleRegionStatus(region)"
            :class="region.status ? 'bg-red-500' : 'bg-yellow-500'"
            class="text-white px-2 py-1 rounded"
          >
            {{ region.status ? '🗑️ Деактивувати' : '♻️ Відновити' }}
          </button>

        </td>
      </tr>
      </tbody>
    </table>
    </div>

    <div v-if="error" class="text-red-500">{{ error }}</div>

    <button
      @click="createRegion"
      class="mb-4 bg-green-600 text-white px-4 py-2 rounded"
    >

      ➕ Новий Регіон
    </button>


  </div>

  <div
    v-if="toastMessage"
    class="fixed top-4 right-4 bg-red-500 text-white px-4 py-2 rounded shadow-lg z-50"
  >
    {{ toastMessage }}
  </div>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api' // assumes you have apiClient set up as default axios instance

const regions = ref([])
const editedRegion = ref(null)
const error = ref(null)

const showAll = ref(true)

const toastMessage = ref(null)

const showToast = (msg, timeout = 3000) => {
  toastMessage.value = msg
  setTimeout(() => (toastMessage.value = null), timeout)
}

const fetchRegions = async () => {
  try {
    const response = await axios.get('/api/regions', {
      params: { all: showAll.value }
    })
    regions.value = response.data
  } catch (err) {
    error.value = 'Помилка завантаження регіонів'
  }
}


const editRegion = (region) => {
  editedRegion.value = { ...region }
}

const saveRegion = async () => {
  try {
    await axios.put(`/api/regions/${editedRegion.value.id}`, editedRegion.value)
    await fetchRegions()
    editedRegion.value = null
  } catch (err) {
    error.value = 'Не вдалося оновити регіон'
  }
}

const toggleRegionStatus = async (region) => {
  const confirmed = confirm(
    region.status
      ? 'Ви впевнені, що хочете деактивувати цей регіон?'
      : 'Ви хочете активувати цей регіон знову?'
  )
  if (!confirmed) return

  try {
    await axios.put(`/api/regions/${region.id}`, {
      ...region,
      status: !region.status
    })
    await fetchRegions()
  } catch (err) {
    error.value = 'Не вдалося змінити статус регіону'
  }
}

const createRegion = async () => {
  const regionName = prompt('Введіть назву нового регіону:')
  if (!regionName || regionName.trim() === '') return

  try {
    await axios.post('/api/regions', {
      regionName: regionName.trim()
    }) // UUID та status генеруються на бекенді
    await fetchRegions()
  } catch (err) {
    error.value = 'Не вдалося створити регіон'
    showToast(error.value)
  }
}



onMounted(fetchRegions)
</script>

<style scoped>
.overflow-x-auto {
  overflow-x: auto;
}

.max-h-\[500px\] {
  max-height: 500px;
}

.overflow-y-auto {
  overflow-y: auto;
}

table {
  border-collapse: separate;
  border-spacing: 0;
  width: 100%;
}

th,
td {
  border: 1px solid #ccc;
  padding: 0.75rem;
  text-align: left;
  vertical-align: middle;
}

thead {
  background-color: #f3f4f6;
  position: sticky;
  top: 0;
  z-index: 1;
}

tbody tr:nth-child(even) {
  background-color: #f9fafb;
}

tbody tr:hover {
  background-color: #f1f5f9;
}

button {
  transition: background-color 0.2s ease;
}

button:hover {
  filter: brightness(1.1);
}


</style>
