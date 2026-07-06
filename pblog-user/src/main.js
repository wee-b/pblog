import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

// 导入登录相关核心模块
import { initLoginManager } from '@/utils/loginManager';
import GlobalAvatar from '@/components/GlobalAvatar.vue';
import LikeButton from '@/components/LikeButton.vue';

const app = createApp(App)

// 自定义指令：平滑滚动
app.directive('scroll', {
    mounted(el, binding) {
        el.style.scrollBehavior = 'smooth'
    }
})

// 注册所有 Element Plus 图标（全局可用）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 1. 初始化登录管理器（必须在 app.use 之前，确保全局方法优先挂载）
initLoginManager(app);

// 2. 全局注册登录弹窗组件（注释修正：之前误写为登录管理器相关注释）
app.component('GlobalAvatar', GlobalAvatar); // 全局注册
app.component('LikeButton', LikeButton);


// 3. 挂载第三方插件（ElementPlus、Pinia、路由）
app.use(ElementPlus)
app.use(createPinia())
app.use(router)

// 4. 挂载应用
app.mount('#app')