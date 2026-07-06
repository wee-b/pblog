import { createRouter, createWebHistory } from 'vue-router';
import Layout from "@/views/layout/index.vue"

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login.vue'),
    meta: {
      title: '登录 - 博客管理系统',
      requiresAuth: false
    }
  },
  {
    path: '/register', // 改为小写，统一规范
    name: 'Register',
    component: () => import('../views/register.vue'),
    meta: {
      title: '注册 - 博客管理系统',
      requiresAuth: false
    }
  },
  {
    path: '',
    component: Layout,
    redirect: '/dashboard',
    meta: {
      requiresAuth: true // 需要登录才能访问的布局
    },
    children: [
      {
        path: '/dashboard', // 改为小写，统一规范
        name: 'Dashboard',
        component: () => import('../views/Dashboard/Dashboard.vue'),
        meta: {
          title: '仪表盘 - 博客管理系统',
          requiresAuth: true
        }
      },
      {
        path: '/articles',
        name: 'Articles', // 改为 PascalCase 规范
        component: () => import('../views/Article/ContentManage.vue'),
        meta: {
          title: '文章管理 - 博客管理系统',
          requiresAuth: true
        }
      },
      {
        path: '/categorys',
        name: 'Categorys', // 改为 PascalCase 规范
        component: () => import('../views/Categorys/index.vue'),
        meta: {
          title: '分类管理 - 博客管理系统', // 添加页面标题
          requiresAuth: true // 明确需要登录
        }
      },
      {
        path: '/comments',
        name: 'Comments', // 改为 PascalCase 规范
        component: () => import('../views/Comments/CommentManage.vue'),
        meta: {
          title: '评论管理 - 博客管理系统',
          requiresAuth: true
        }
      },
      {
        path: '/users',
        name: 'Users', // 改为 PascalCase 规范
        component: () => import('../views/User/index.vue'),
        meta: {
          title: '用户管理 - 博客管理系统',
          requiresAuth: true
        }
      },
      {
        path: '/settings',
        name: 'Settings', // 改为 PascalCase 规范
        component: () => import('../views/Dashboard/Dashboard.vue'),
        meta: {
          title: '系统设置 - 博客管理系统',
          requiresAuth: true
        }
      },
    ]
  },
  // 添加 404 路由，防止访问不存在的路径
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 解决路由切换时页面滚动问题
  scrollBehavior(to, from, savedPosition) {
    return { top: 0 };
  }
});

// 全局路由守卫：设置页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title;
  }
  next();
});

export default router;