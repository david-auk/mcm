import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    allowedHosts: true,  // This will allow all hosts
    proxy: {
      // Proxy only known routes (/api)
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
      }
    },
  },
  preview: {
    host: true
  }
});
