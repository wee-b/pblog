<template>
  <div class="layout-nav-container">
    <el-affix :offset="0">
      <nav
          class="layout-nav"
          :class="{
            'nav-transparent': isTransparent && !forceSolid,
            'nav-solid': !isTransparent || forceSolid
          }"
      >
        <!-- 1. 左侧导航 -->
        <div class="nav-left">
          <RouterLink
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              class="nav-link"
              active-class="active-link"
          >
            {{ item.name }}
            <span class="nav-link-underline"></span>
          </RouterLink>
        </div>

        <!-- 2. 中间搜索框 -->
        <div class="nav-center" :class="{ 'hidden': !configState.showSearch }">
          <el-input
              v-model="searchQuery"
              placeholder="搜索文章..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search/></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 3. 右侧功能区 -->
        <div class="nav-right">
          <GlobalAvatar size="36" />

          <RouterLink to="/links" class="nav-link" active-class="active-link">
            友链 <span class="nav-link-underline"></span>
          </RouterLink>
          <RouterLink to="/createcentre" class="nav-link" active-class="active-link">
            个人中心 <span class="nav-link-underline"></span>
          </RouterLink>
          <RouterLink to="/tools" class="nav-link" active-class="active-link">
            小工具 <span class="nav-link-underline"></span>
          </RouterLink>
        </div>
      </nav>
    </el-affix>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import GlobalAvatar from '@/components/GlobalAvatar.vue';

// --- 1. 静态配置 ---
const menuItems = [
  { name: '首页', path: '/' },
  { name: '博客', path: '/allBlogs' },
  { name: '合集', path: '/collection' },
  { name: '留言板', path: '/remark' }
];

// --- 2. 状态定义 ---
const route = useRoute();
const router = useRouter();

const searchQuery = ref('');
const isTransparent = ref(false);

// --- 3. 计算属性：UI配置策略 ---
const configState = computed(() => {
  const path = route.path;
  if (path === '/') {
    return { forceSolid: false, showSearch: true };
  } else if (path === '/allBlogs') {
    return { forceSolid: true, showSearch: false };
  } else if (path.startsWith('/blog/')) {
    return { forceSolid: true, showSearch: true };
  }
  return { forceSolid: true, showSearch: true };
});

const forceSolid = computed(() => configState.value.forceSolid);

// --- 4. 核心逻辑：状态检查 ---
const checkScrollState = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop || document.body.scrollTop;
  // 仅在首页且滚动 < 300 时透明
  if (route.path === '/') {
    isTransparent.value = scrollTop < 300;
  } else {
    isTransparent.value = false;
  }
};

// --- 5. 滚动监听 (防抖优化) ---
let ticking = false;
const onScroll = () => {
  if (!ticking) {
    window.requestAnimationFrame(() => {
      checkScrollState();
      ticking = false;
    });
    ticking = true;
  }
};

// --- 6. 业务交互 ---
const handleSearch = () => {
  if (!searchQuery.value.trim()) return;
  router.push({ path: '/allBlogs', query: { keyword: searchQuery.value } });
};

// --- 7. 监听路由变化 (修复点击链接不更新状态的问题) ---
watch(() => route.path, () => {
  checkScrollState();
});

// --- 8. 生命周期 ---
onMounted(() => {
  checkScrollState();
  window.addEventListener('scroll', onScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
});
</script>

<style scoped>
.layout-nav-container {
  width: 100%;
  position: relative;
  z-index: 999;
  height: 64px;
}

:deep(.el-affix--fixed) {
  width: 100% !important;
  left: 0 !important;
  right: 0 !important;
}

/* Nav body — geometric style */
.layout-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 40px;
  width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease-in-out;
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
}

/* Geometric accent bar at bottom */
.layout-nav::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(
    90deg,
    var(--geo-coral) 0%,
    var(--geo-gold) 33%,
    var(--geo-sky) 66%,
    var(--geo-navy) 100%
  );
  opacity: 0;
  transition: opacity 0.3s;
}

.nav-solid::after {
  opacity: 1;
}

.nav-transparent {
  background-color: transparent !important;
  border-bottom-color: transparent !important;
  backdrop-filter: none !important;
}

.nav-transparent::after {
  opacity: 0;
}

.nav-solid {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-sm);
}

/* Layout areas */
.nav-left, .nav-center, .nav-right {
  flex: 1;
  display: flex;
  align-items: center;
}

.nav-left {
  justify-content: flex-start;
  gap: 30px;
}

.nav-center {
  justify-content: center;
  transition: opacity 0.3s;
}
.nav-center.hidden {
  visibility: hidden;
  pointer-events: none;
}

.nav-right {
  justify-content: flex-end;
  gap: 24px;
}

/* Link styles — geometric underline */
.nav-link {
  position: relative;
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  padding: 8px 0;
  cursor: pointer;
  transition: color 0.25s;
  letter-spacing: 0.01em;
}

.nav-link-underline {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 3px;
  background: var(--geo-coral);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 2px;
}

.nav-link:hover, .active-link {
  color: var(--geo-coral);
}

.nav-link:hover .nav-link-underline,
.active-link .nav-link-underline {
  width: 100%;
}

/* Transparent mode */
.nav-transparent .nav-link {
  color: var(--color-text-primary);
  text-shadow: none;
}
.nav-transparent .nav-link:hover,
.nav-transparent .active-link {
  color: var(--geo-coral-dark);
}
.nav-transparent .nav-link-underline {
  background: var(--geo-coral);
}

.nav-transparent .search-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.78);
  border-color: rgba(26, 26, 46, 0.1);
  backdrop-filter: blur(8px);
}

/* Search input */
.search-input {
  width: 300px;
  transition: width 0.3s;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: rgba(245, 245, 247, 0.8);
  box-shadow: none;
  border: 1px solid var(--color-border);
}
.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px var(--geo-coral-light);
  border-color: var(--geo-coral);
}

/* Responsive */
@media (max-width: 1200px) {
  .layout-nav { padding: 0 20px; }
}

@media (max-width: 992px) {
  .nav-left { gap: 20px; }
  .nav-right { gap: 16px; }
  .search-input { width: 250px; }
}

@media (max-width: 768px) {
  .layout-nav { padding: 0 16px; height: 56px; }
  .layout-nav-container { height: 56px; }
  .nav-left, .nav-center, .nav-right { flex: none; }
  .nav-left { flex: 1; justify-content: flex-start; gap: 12px; }
  .nav-center { display: none; }
  .nav-right { flex: 1; justify-content: flex-end; gap: 12px; }
  .nav-link { font-size: 14px; }
}

@media (max-width: 576px) {
  .layout-nav { padding: 0 12px; }
  .nav-left { gap: 8px; }
  .nav-right { gap: 8px; }
  .nav-link { font-size: 13px; }
}
</style>
