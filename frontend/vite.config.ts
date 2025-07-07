import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dynamically set proxy target and log it for debugging
const BACKEND_URL = process.env.VITE_BACKEND_URL || 'http://localhost:8080';
const VITE_PORT =  parseInt(process.env.VITE_PORT || "5173");


console.log(`Vite proxy target: ${BACKEND_URL}`);

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: VITE_PORT,
    allowedHosts: true,  // This will allow all hosts
    proxy: {
      // Proxy only known routes (/api)
      '/api': {
        target: BACKEND_URL,
        changeOrigin: true,
        secure: false,
      }
    },
  },
  preview: {
    host: true
  }
});
