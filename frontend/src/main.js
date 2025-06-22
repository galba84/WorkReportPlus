// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createI18n } from 'vue-i18n'
import { createPinia } from 'pinia'
const messages = {
  en: {
    page: {
      heading: {
        admin: "Welcome to the ADMIN Page"
      },
      label: {
        roles: "Current User Roles:"
      }
    },
    button: {
      updateContractors: "Update Contractors from Google Sheets",
      updateGroups: "Update Groups from Google Sheets",
      updateRegions: "Update Regions from Google Sheets",
      Description: "Update Group Descriptions from Google Sheets",
      Places: "Update Places from Google Sheets",
      Ranks: "Update Ranks from Google Sheets",
      Positions: "Update Positions from Google Sheets",
      Units: "Update Units from Google Sheets"
    }
  }
}

const i18n = createI18n({
  legacy: false,        // ✅ REQUIRED for Composition API
  globalInjection: true, // ✅ Makes $t available in <template>
  locale: 'en',
  messages
})

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(i18n) // ✅ This line is REQUIRED
app.mount('#app')
