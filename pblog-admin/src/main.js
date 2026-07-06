import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'


import GlobalAvatar from '@/components/GlobalAvatar.vue';
import { initLoginManager } from '@/utils/loginManager';

// 创建应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 1. 初始化登录管理器（必须在 app.use 之前，确保全局方法优先挂载）
initLoginManager(app);
app.component('GlobalAvatar', GlobalAvatar); // 全局注册

// 安装插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')