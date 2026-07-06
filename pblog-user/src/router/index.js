import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/Home/index.vue'
import Layout from '../views/Layout/index.vue'
import {ElMessage} from "element-plus";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView,
        },
        {
          path: 'blog/:id',
          name: 'blog',
          component: () => import('../views/Blog/index.vue')
        },
        {
          path: "allBlogs",
          name: "allBlogs",
          component: () => import("../views/AllBlogs/index.vue")
        },
        {
          path: "remark",
          name: "remark",
          component: () => import("../views/remark/index.vue")
        }
      ]
    },
    {
      path: '/createcentre',
      name: 'createcentre',
      component: () => import('../views/CreateCentre/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/writeBlog/:id',
      name: 'writeBlogEdit',
      component: () => import('../views/WriteBlog/index.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/writeBlog',
      name: 'writeBlogNew',
      component: () => import('../views/WriteBlog/index.vue'),
      meta: { requiresAuth: true }
    }
  ],
})

// 全局前置守卫：所有路由跳转前都会触发
router.beforeEach((to, from, next) => {
  // 1. 判断当前路由是否需要登录权限（通过 meta.requiresAuth 标识）
  const requiresAuth = to.meta.requiresAuth

  if (requiresAuth) {
    // 2. 获取登录状态（示例：从 localStorage 读 token，实际建议用 Pinia/Vuex）
    const userToken = localStorage.getItem('token') // 后端返回的登录凭证

    if (userToken) {
      // 3. 已登录：直接放行
      next()
    } else {
      ElMessage.error("请先登录")
    }
  } else {
    // 5. 无需鉴权的路由（如首页、博客详情）：直接放行
    next()
  }
})

export default router