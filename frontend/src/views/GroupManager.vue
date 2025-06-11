<template>
  <div class="p-4">
    <h2 class="text-xl font-bold mb-4">👥 Менеджер Груп</h2>

    <label class="mb-2 block">
      <input type="checkbox" v-model="showAll" @change="fetchGroups" />
      Показати всі (включно з неактивними)
    </label>

    <div class="mb-4">
      <label class="block font-semibold mb-1">Фільтр по регіонах:</label>
      <div class="flex flex-wrap gap-2">
        <label
          v-for="region in regionOptions"
          :key="region.id"
          class="flex items-center space-x-1"
        >
          <input
            type="checkbox"
            v-model="selectedRegionIds"
            :value="region.id"
          />
          <span>{{ region.name }}</span>
        </label>
      </div>
    </div>


    <div class="overflow-x-auto max-h-[500px] overflow-y-auto border border-gray-300 rounded mb-4">
      <table class="min-w-full">
        <thead>
        <tr class="bg-gray-200 text-left">
          <th class="p-2">ID</th>
          <th class="p-2">Назва Групи</th>
          <th class="p-2">ID Регіону</th>
          <th class="p-2">Бойова</th>
          <th class="p-2">Активна</th>
          <th class="p-2">Дії</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="group in filteredGroups" :key="group.id" class="border-t">

        <td class="p-2">{{ group.id }}</td>

          <td class="p-2">
            <template v-if="editedGroup && editedGroup.id === group.id">
              <input v-model="editedGroup.name" class="border px-2 py-1 w-full" />
            </template>
            <template v-else>
              {{ group.name }}
            </template>
          </td>

          <td class="p-2">
            {{ group.regionName || group.regionId }}
          </td>



          <td class="p-2">
            <template v-if="editedGroup?.id === group.id">
              <input type="checkbox" v-model="editedGroup.fighting" />
            </template>
            <template v-else>
              {{ group.fighting ? "🪖" : "—" }}
            </template>
          </td>

          <td class="p-2">
            {{ group.status ? "✅" : "❌" }}
          </td>

          <td class="p-2 space-x-2">
            <button
              v-if="editedGroup?.id !== group.id"
              @click="editGroup(group)"
              class="bg-blue-500 text-white px-2 py-1 rounded"
            >
              ✏️ Редагувати
            </button>

            <button
              v-else
              @click="saveGroup"
              class="bg-green-500 text-white px-2 py-1 rounded"
            >
              💾 Зберегти
            </button>

            <button
              @click="toggleGroupStatus(group)"
              :class="group.status ? 'bg-red-500' : 'bg-yellow-500'"
              class="text-white px-2 py-1 rounded"
            >
              {{ group.status ? '🗑️ Деактивувати' : '♻️ Відновити' }}
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="error" class="text-red-500">{{ error }}</div>

    <button
      @click="openCreateGroupModal"
      class="mb-4 bg-green-600 text-white px-4 py-2 rounded"
    >
      ➕ Нова Група
    </button>


    <div
      v-if="toastMessage"
      class="fixed top-4 right-4 bg-red-500 text-white px-4 py-2 rounded shadow-lg z-50"
    >
      {{ toastMessage }}
    </div>
  </div>

  <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow-lg w-[300px]">
      <h3 class="text-lg font-bold mb-4">Створити Групу</h3>

      <label class="block mb-2">
        Назва:
        <input
          v-model="newGroupName"
          type="text"
          class="border w-full px-2 py-1 mt-1"
        />
      </label>

      <label class="block mb-4">
        Регіон:
        <select
          v-model="newGroupRegionId"
          class="border w-full px-2 py-1 mt-1"
        >
          <option disabled value="">-- Виберіть регіон --</option>
          <option v-for="region in regionOptions" :key="region.id" :value="region.id">
            {{ region.name }}
          </option>
        </select>
      </label>

      <div class="flex justify-end space-x-2">
        <button @click="showCreateModal = false" class="px-3 py-1 bg-gray-300 rounded">Скасувати</button>
        <button @click="confirmCreateGroup" class="px-3 py-1 bg-green-600 text-white rounded">Створити</button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api'

const groups = ref([])
const editedGroup = ref(null)
const showAll = ref(true)
const error = ref(null)
const toastMessage = ref(null)

const showToast = (msg, timeout = 3000) => {
  toastMessage.value = msg
  setTimeout(() => (toastMessage.value = null), timeout)
}

const fetchGroups = async () => {
  try {
    const response = await axios.get('/api/groups', {
      params: { all: showAll.value }
    })
    groups.value = response.data
  } catch (err) {
    error.value = 'Помилка завантаження груп'
  }
}

const editGroup = (group) => {
  editedGroup.value = {
    ...group,
    fighting: group.isFighting ?? group.fighting // if API returns isFighting
  }
}

const saveGroup = async () => {
  try {
    await axios.put(`/api/groups/${editedGroup.value.id}`, {
      ...editedGroup.value,
      isFighting: editedGroup.value.fighting
    })
    await fetchGroups()
    editedGroup.value = null
  } catch (err) {
    error.value = 'Не вдалося оновити групу'
  }
}

const toggleGroupStatus = async (group) => {
  const confirmed = confirm(
    group.status
      ? 'Ви впевнені, що хочете деактивувати цю групу?'
      : 'Ви хочете активувати цю групу знову?'
  )
  if (!confirmed) return

  try {
    await axios.patch(`/api/groups/${group.id}/status`, null, {
      params: { active: !group.status }
    })
    await fetchGroups()
  } catch (err) {
    error.value = 'Не вдалося змінити статус групи'
  }
}

const regionOptions = ref([])
const selectedRegionIds = ref([])

const fetchRegions = async () => {
  try {
    const res = await axios.get('/api/regions', { params: { all: true } })
    regionOptions.value = res.data.map(r => ({
      id: r.id,
      name: r.regionName
    }))
  } catch {
    showToast('Не вдалося завантажити регіони')
  }
}

const filteredGroups = computed(() => {
  if (selectedRegionIds.value.length === 0) return groups.value
  return groups.value.filter(group =>
    selectedRegionIds.value.includes(group.regionId)
  )
})

const newGroupName = ref('')
const newGroupRegionId = ref('')
const showCreateModal = ref(false)

const openCreateGroupModal = () => {
  newGroupName.value = ''
  newGroupRegionId.value = ''
  showCreateModal.value = true
}

const confirmCreateGroup = async () => {
  if (!newGroupName.value || !newGroupRegionId.value) {
    showToast('Введіть назву та виберіть регіон')
    return
  }

  try {
    await axios.post('/api/groups', {
      name: newGroupName.value.trim(),
      regionId: newGroupRegionId.value
    })
    await fetchGroups()
    showCreateModal.value = false
  } catch (err) {
    error.value = 'Не вдалося створити групу'
    showToast(error.value)
  }
}



onMounted(async () => {
  await fetchRegions()
  await fetchGroups()
})
</script>

<style scoped>
/* same styles as RegionManager.vue */
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
