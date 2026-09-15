// vite.config.js
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'




// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5178, // 👈 set your desired port
    strictPort: true, // 👈 fail if the port is already in use
    proxy: {
      '/api': {
        target: process.env.API_PROXY_TARGET || 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
