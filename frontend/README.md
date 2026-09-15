# WorkReportPlus frontend

Vue 3, Pinia, Vue Router, and Vite. See the [root README](../README.md) for the demo, architecture, and configuration.

Use Node.js 22. Run `npm ci`, then `npm run dev` for development or `npm run build` for production assets.
Vite proxies /api to http://localhost:8082; set API_PROXY_TARGET to use another backend port.

With the Compose demo running: `npx playwright install chromium`, then `npx playwright test`.
