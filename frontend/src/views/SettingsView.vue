<template>
  <div class="app-settings">
    <h1>App Settings</h1>

    <!-- Scrollable table -->
    <div class="settings-table-wrapper">
      <table class="settings-table">
        <thead>
        <tr>
          <th>Key</th>
          <th>Value</th>
          <th>Format</th>
          <th>Description</th>
          <th>Setting Data</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in settings" :key="s.settingKey">
          <td>{{ s.settingKey }}</td>
          <td>{{ s.settingValue }}</td>
          <td>{{ s.format }}</td>
          <td>{{ s.description }}</td>
          <td class="json-cell">{{ s.settingData }}</td>
          <td>
            <button @click="onEdit(s)">Edit</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Form -->
    <div class="settings-form">
      <h2>{{ editing ? 'Edit' : 'New' }} Setting</h2>
      <form @submit.prevent="onSave">
        <label>Key
          <select v-model="form.settingKey" :disabled="editing" required>
            <option value="" disabled>Select key</option>
            <option v-for="k in availableKeys" :key="k" :value="k">{{ k }}</option>
          </select>
        </label>

        <label>Value
          <template v-if="form.format === 'boolean'">
            <div class="radio-group">
              <label><input type="radio" value="true" v-model="form.settingValue" /> True</label>
              <label><input type="radio" value="false" v-model="form.settingValue" /> False</label>
            </div>
          </template>
          <template v-else>
            <input v-model="form.settingValue" required />
          </template>
        </label>

        <label>Format
          <input v-model="form.format" readonly />
        </label>

        <label>Data (JSON)
          <textarea v-model="form.settingData" rows="4"></textarea>
        </label>

        <label>Description
          <textarea v-model="form.description" rows="2"></textarea>
        </label>

        <div class="form-actions">
          <button type="submit">{{ editing ? 'Update' : 'Create' }}</button>
          <button v-if="editing" type="button" @click="onReset">Cancel</button>
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
.app-settings {
  padding: 3rem;
  font-family: sans-serif;
  max-width: 1000px;
  overflow-y: auto
}

h1 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.settings-table-wrapper {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ccc;
}

.settings-table {
  width: 100%;
  border-collapse: collapse;
  overflow-y: auto
}

.settings-table th,
.settings-table td {
  border: 1px solid #ccc;
  padding: 8px;
  vertical-align: top;
  text-align: left;
}

.settings-table th {
  background-color: #f5f5f5;
  position: sticky;
  top: 0;
  z-index: 1;
}

.json-cell {
  word-break: break-word;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9em;
}

.settings-form {
  margin-top: 2rem;
  padding: 10px;

  overflow-y: auto
}

.settings-form form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto
}

.settings-form label {
  display: flex;
  flex-direction: column;
  font-weight: bold;
}

.settings-form input,
.settings-form select,
.settings-form textarea {
  font-weight: normal;
  font-size: 1rem;
  padding: 0.5rem;
  border: 1px solid #aaa;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  gap: 1rem;
}

button {
  padding: 6px 12px;
  font-size: 1rem;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #444;
  background-color: #e0e0e0;
}

button:hover {
  background-color: #d0d0d0;
}

.radio-group {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
  font-weight: normal;
}

.radio-group input[type="radio"] {
  margin-right: 0.4rem;
}

</style>
