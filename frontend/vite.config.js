import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 头像等上传文件的静态访问，直达后端（对应 WebMvcConfig 的 /upload/** 资源映射）
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  base: '/'
}) 