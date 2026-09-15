import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  workers: 1,
  retries: 0,
  use: { baseURL: process.env.DEMO_URL || 'http://localhost:5178', timezoneId: 'UTC', viewport: { width: 1440, height: 1000 } },
})
