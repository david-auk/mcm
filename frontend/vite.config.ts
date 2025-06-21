import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 80,
    host: true,
    allowedHosts: true,  // This will allow all hosts
    proxy: {
      // Proxy only known routes (/api)
      '/api': {
        target: 'http://mcm-backend:8080',
        changeOrigin: true,
        secure: false,
      }
    },
  },
  preview: {
    port: 80,
    host: true
  }
});
