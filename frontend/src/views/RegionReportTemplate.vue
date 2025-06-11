<template>
  <div class="p-4">
    <h2 class="text-xl font-bold mb-4">📄 Шаблони Регіональних Звітів</h2>

    <div class="overflow-x-auto max-h-[500px] overflow-y-auto border border-gray-300 rounded mb-4">
      <table class="min-w-full">
        <thead>
        <tr class="bg-gray-200 text-left">
          <th class="p-2">ID</th>
          <th class="p-2">Регіон</th>
          <th class="p-2">Преамбула (preamble)</th>
          <th class="p-2">Підпис (signature)</th>
          <th class="p-2">Формат</th>
          <th class="p-2">Змінні</th>
          <th class="p-2">Вміст</th>
          <th class="p-2">Дії</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="template in templates" :key="template.id" class="border-t">
          <td class="p-2">{{ template.id }}</td>
          <td class="p-2">
            {{ regionOptions.find(r => r.id === template.regionId)?.name || '—' }}
          </td>

          <td class="p-2 max-w-[300px] whitespace-pre-wrap text-sm text-gray-800">
            <template v-if="editedTemplate && editedTemplate.id === template.id">
              <textarea v-model="editedTemplate.preamble" rows="3" class="border px-2 py-1 w-full"></textarea>
            </template>
            <template v-else>
              <span class="text-blue-600 underline cursor-pointer"
                    @click="selectedTemplate = { content: template.preamble, title: 'Преамбула' }">
                Переглянути
              </span>
            </template>
          </td>

          <td class="p-2 max-w-[300px] whitespace-pre-wrap text-sm text-gray-800">
            <template v-if="editedTemplate && editedTemplate.id === template.id">
              <textarea v-model="editedTemplate.signature" rows="3" class="border px-2 py-1 w-full"></textarea>
            </template>
            <template v-else>
              <span class="text-blue-600 underline cursor-pointer"
                    @click="selectedTemplate = { content: template.signature, title: 'Підпис' }">
                Переглянути
              </span>
            </template>
          </td>


          <td class="p-2">{{ template.fileFormat }}</td>
          <td class="p-2">
            <ul class="list-disc list-inside">
              <li v-for="v in template.variables" :key="v">{{ v }}</li>
            </ul>
          </td>
          <td class="p-2 max-w-[300px] whitespace-pre-wrap text-sm text-gray-800">
            <template v-if="editedTemplate && editedTemplate.id === template.id">
              <textarea v-model="editedTemplate.content" rows="3"
                        class="border px-2 py-1 w-full"></textarea>
            </template>
            <template v-else>
              <span class="text-blue-600 underline cursor-pointer"
                    @click="selectedTemplate = { content: template.content, title: 'Вміст' }">
                Переглянути
              </span>
            </template>
          </td>
          <td class="p-2 space-x-2">
            <button v-if="!editedTemplate || editedTemplate.id !== template.id"
                    @click="editTemplate(template)"
                    class="bg-blue-500 text-white px-2 py-1 rounded">✏️
            </button>
            <button v-else @click="saveTemplate" class="bg-green-500 text-white px-2 py-1 rounded">
              💾
            </button>
            <button @click="deleteTemplate(template.id)"
                    class="bg-red-500 text-white px-2 py-1 rounded">🗑️
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="error" class="text-red-500">{{ error }}</div>

    <button @click="openCreateModal" class="mb-4 bg-green-600 text-white px-4 py-2 rounded">➕ Новий
      Шаблон
    </button>
  </div>

  <div v-if="toastMessage"
       class="fixed top-4 right-4 bg-red-500 text-white px-4 py-2 rounded shadow-lg z-50">
    {{ toastMessage }}
  </div>

  <div v-if="selectedTemplate"
       class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-8">
    <div class="bg-white p-6 rounded shadow-lg max-w-2xl w-full">

      <h3 class="text-lg font-bold mb-4">{{ selectedTemplate.title }}</h3>
      <div class="whitespace-pre-wrap text-sm text-gray-800 max-h-[400px] overflow-y-auto"
           v-html="formattedContent"></div>
      <div class="flex justify-end pt-4">
        <button @click="selectedTemplate = null" class="bg-blue-500 text-white px-4 py-1 rounded">
          Закрити
        </button>
      </div>
    </div>
  </div>

  <div v-if="showCreateModal"
       class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow-lg w-[500px]">
      <h3 class="text-lg font-bold mb-4">Новий Шаблон Звіту</h3>
      <div class="space-y-4">
        <div>
          <label class="block mb-1 font-medium">Регіон:</label>
          <select v-model="selectedRegionId" class="border w-full px-2 py-1">
            <option disabled value="">-- Виберіть регіон --</option>
            <option v-for="region in regionOptions" :key="region.id" :value="region.id">
              {{ region.name }}
            </option>
          </select>
        </div>

        <div>
          <label class="block mb-1 font-medium">Преамбула:</label>
          <textarea v-model="newPreamble" rows="4" class="border w-full px-2 py-1"></textarea>
        </div>

        <div>
          <label class="block mb-1 font-medium">Підпис:</label>
          <textarea v-model="newSignature" rows="4" class="border w-full px-2 py-1"></textarea>
        </div>

        <div>
          <label class="block mb-1 font-medium">Формат файлу (наприклад, RTF):</label>
          <input v-model="newFileFormat" type="text" class="border w-full px-2 py-1"/>
        </div>

        <div>
          <label class="block mb-1 font-medium">Вміст шаблону:</label>
          <textarea v-model="newContent" rows="4" class="border w-full px-2 py-1"></textarea>
        </div>

        <div>
          <label class="block mb-1 font-medium">Змінні (через кому):</label>
          <input v-model="newVariablesString" type="text" class="border w-full px-2 py-1"
                 placeholder="наприклад: groupName,date,region"/>
        </div>

        <div class="flex justify-end space-x-2 pt-2">
          <button @click="showCreateModal = false" class="px-3 py-1 bg-gray-300 rounded">Скасувати
          </button>
          <button @click="confirmCreateTemplate" class="px-3 py-1 bg-green-600 text-white rounded">
            Створити
          </button>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
const selectedTemplate = ref(null); // { content: '', title: '' }
import {ref, computed, onMounted} from 'vue'
import axios from '@/api'

const templates = ref([])
const editedTemplate = ref(null)
const error = ref(null)
const toastMessage = ref(null)

const showCreateModal = ref(false)
const selectedRegionId = ref('')
const newPreamble = ref('')
const newSignature = ref('')
const newFileFormat = ref('RTF')
const newContent = ref('')
const newVariablesString = ref('')

const regionOptions = ref([])

const showToast = (msg, timeout = 3000) => {
  toastMessage.value = msg
  setTimeout(() => (toastMessage.value = null), timeout)
}

const formattedContent = computed(() =>
  selectedTemplate.value?.content?.replace(/\n/g, '<br>') || ''
)

const fetchTemplates = async () => {
  try {
    const response = await axios.get('/api/region-report-templates')
    templates.value = response.data
  } catch (err) {
    error.value = 'Не вдалося завантажити шаблони'
  }
}

const fetchRegions = async () => {
  try {
    const res = await axios.get('/api/regions', {params: {all: true}})
    regionOptions.value = res.data.map(r => ({
      id: r.id,
      name: r.regionName
    }))
  } catch {
    showToast('Не вдалося завантажити регіони')
  }
}

const openCreateModal = () => {
  selectedRegionId.value = ''
  newPreamble.value = ''
  newSignature.value = ''
  newFileFormat.value = 'RTF'
  newContent.value = ''
  newVariablesString.value = ''
  showCreateModal.value = true
}

const confirmCreateTemplate = async () => {
  if (!selectedRegionId.value || !newPreamble.value || !newSignature.value || !newFileFormat.value || !newContent.value) {
    showToast('Усі поля обовʼязкові')
    return
  }

  try {
    await axios.post('/api/region-report-templates', {
      regionId: selectedRegionId.value,
      preamble: newPreamble.value,
      signature: newSignature.value,
      fileFormat: newFileFormat.value,
      content: newContent.value,
      variables: newVariablesString.value.split(',').map(v => v.trim()).filter(Boolean)
    })
    await fetchTemplates()
    showCreateModal.value = false
  } catch (err) {
    error.value = 'Не вдалося створити шаблон'
    showToast(error.value)
  }
}

const editTemplate = (template) => {
  editedTemplate.value = {...template}
}

const saveTemplate = async () => {
  try {
    await axios.put(`/api/region-report-templates/${editedTemplate.value.id}`, editedTemplate.value)
    await fetchTemplates()
    editedTemplate.value = null
  } catch (err) {
    error.value = 'Не вдалося оновити шаблон'
  }
}

const deleteTemplate = async (id) => {
  if (!confirm('Ви впевнені, що хочете видалити цей шаблон?')) return
  try {
    await axios.delete(`/api/region-report-templates/${id}`)
    await fetchTemplates()
  } catch (err) {
    error.value = 'Не вдалося видалити шаблон'
  }
}

onMounted(() => {
  fetchTemplates()
  fetchRegions()
})
</script>

<style scoped>
.overflow-x-auto {
  overflow-x: auto;
}

.max-h-[

500
px

]
{
  max-height: 500px
;
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
