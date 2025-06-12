import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    assetsInclude: ['**/*.glb', '**/*.png'],
    server: {
      port: 5555,
    },
    base: "/",
  }
})
