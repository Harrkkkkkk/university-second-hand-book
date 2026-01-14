import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/book-api': {
        // 开发环境代理配置，指向华为云ECS弹性公网IP
        target: 'http://113.44.187.116:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/book-api/, '/book-api')
      }
    }
  }
})