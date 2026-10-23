import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  server: {
    // 前端开发服务器端口
    port: 5173,
    // 端口被占用时直接报错，别偷偷换端口（免得代理配置对不上）
    strictPort: true,
    // 启动后自动打开浏览器
    open: false,
    // 把 /api 开头的请求转发给 Spring Boot 后端，前端代码里不用写完整域名，
    // 也就不存在跨域（CORS）问题
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  build: {
    // 打包产物目录
    outDir: 'dist',
    // 生产环境不生成 sourcemap，产物更小；调试时可以改成 true
    sourcemap: false,
  },
})
