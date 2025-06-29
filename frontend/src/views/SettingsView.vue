<template>
  <div class="p-4">
    <h1 class="text-xl font-bold mb-4">App Settings</h1>

    <!-- existing settings -->
    <table class="min-w-full bg-white mb-6">
      <thead>
      <tr>
        <th class="py-2">Key</th>
        <th class="py-2">Value</th>
        <th class="py-2">Format</th>
        <th class="py-2">description</th>
        <th class="py-2">settingData</th>
        <th class="py-2">Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="s in settings" :key="s.settingKey">
        <td class="border px-4 py-2">{{ s.settingKey }}</td>
        <td class="border px-4 py-2">{{ s.settingValue }}</td>
        <td class="border px-4 py-2">{{ s.format }}</td>
        <td class="border px-4 py-2">{{ s.description }}</td>
        <td class="border px-4 py-2">{{ s.settingData }}</td>
        <td class="border px-4 py-2">
          <button @click="onEdit(s)" class="px-2 py-1 bg-green-200 rounded">Edit</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- form: new/edit -->
    <div class="space-y-4">
      <h2 class="text-lg font-medium">{{ editing ? 'Edit' : 'New' }} Setting</h2>
      <form @submit.prevent="onSave" class="space-y-2">
        <!-- key selector -->
        <div>
          <label class="block mb-1">Key</label>
          <select v-model="form.settingKey" :disabled="editing" required class="border p-2 w-full">
            <option value="" disabled>Select key</option>
            <option v-for="k in availableKeys" :key="k" :value="k">{{ k }}</option>
          </select>
        </div>

        <!-- value input -->
        <div>
          <label class="block mb-1">Value</label>
          <input v-model="form.settingValue" required class="border p-2 w-full" />
        </div>

        <!-- format (readonly) -->
        <div>
          <label class="block mb-1">Format</label>
          <input v-model="form.format" readonly class="border bg-gray-100 p-2 w-full" />
        </div>

        <!-- data JSON -->
        <div>
          <label class="block mb-1">Data (JSON)</label>
          <textarea v-model="form.settingData" rows="4" class="border p-2 w-full"></textarea>
        </div>

        <!-- description -->
        <div>
          <label class="block mb-1">Description</label>
          <textarea v-model="form.description" rows="2" class="border p-2 w-full"></textarea>
        </div>

        <!-- actions -->
        <div class="flex space-x-2">
          <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded">
            {{ editing ? 'Update' : 'Create' }}
          </button>
          <button v-if="editing" type="button" @click="onReset" class="bg-gray-300 px-4 py-2 rounded">
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import apiClient from '@/api';

const settings = ref([]);
const metadata = ref({});
const availableKeys = ref([]);
const form = ref({
  settingKey: '',
  settingValue: '',
  settingData: '{}',
  format: '',
  description: ''
});
const editing = ref(false);

async function loadMetadata() {
  const res = await apiClient.get('/api/settings/metadata');
  metadata.value = res.data;
  availableKeys.value = Object.keys(res.data);
}

async function loadSettings() {
  settings.value = (await apiClient.get('/api/settings')).data;
}

// auto-fill format when key changes
watch(() => form.value.settingKey, key => {
  form.value.format = metadata.value[key] || '';
});

function onEdit(item) {
  form.value = {
    settingKey: item.settingKey,
    settingValue: item.settingValue,
    settingData: JSON.stringify(item.settingData, null, 2),
    format: item.format,
    description: item.description
  };
  editing.value = true;
}

function onReset() {
  form.value = { settingKey: '', settingValue: '', settingData: '{}', format: '', description: '' };
  editing.value = false;
}

async function onSave() {
  const payload = { ...form.value, settingData: JSON.parse(form.value.settingData) };
  if (editing.value) {
    await apiClient.put(`/api/settings/${form.value.settingKey}`, payload);
  } else {
    // ensure format is set before creating
    form.value.format = metadata.value[form.value.settingKey];
    await apiClient.post('/api/settings', payload);
  }
  onReset();
  await loadSettings();
}


onMounted(async () => {
  await loadMetadata();
  await loadSettings();
});
</script>
<style scoped>
/* Custom scrollbar styling for the settings table */
div.bg-white.shadow.rounded-lg.overflow-hidden table::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
div.bg-white.shadow.rounded-lg.overflow-hidden table::-webkit-scrollbar-track {
  background: #f1f1f1;
}
div.bg-white.shadow.rounded-lg.overflow-hidden table::-webkit-scrollbar-thumb {
  background: #cbd5e0; /* Tailwind gray-300 */
  border-radius: 4px;
}
div.bg-white.shadow.rounded-lg.overflow-hidden table::-webkit-scrollbar-thumb:hover {
  background: #a0aec0; /* Tailwind gray-400 */
}

/* Smooth row hover transition */
tbody tr {
  transition: background-color 0.2s ease;
}

/* Button transition */
button {
  transition: background-color 0.2s ease, transform 0.1s ease;
}
button:active {
  transform: scale(0.98);
}
</style>
