import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [vue(), vueJsx(), vueDevTools()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    host: '0.0.0.0', // 可选：解决本地局域网访问问题
    open: true, // 可选：启动后自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        // 新增：如果后端是http协议，关闭https校验（开发环境）
        secure: false,
        // 新增：打印代理日志，方便排查
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // console.log('代理请求路径：', req.url);
            // console.log('转发到：', options.target + req.url.replace(/^\/api/, ''));
          });
        }
      },
    },
  },
})