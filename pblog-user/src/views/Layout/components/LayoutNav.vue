<template>
  <div class="layout-nav-container">
    <el-affix :offset="0">
      <nav
        class="layout-nav"
        :class="{
          'nav-transparent': isTransparent && !forceSolid,
          'nav-solid': !isTransparent || forceSolid,
          'menu-open': mobileMenuOpen
        }"
      >
        <div class="nav-left">
          <RouterLink to="/" class="nav-brand" aria-label="PBlog 首页" @click="closeMobileMenu">
            <span class="brand-mark" aria-hidden="true">
              <span></span><span></span><span></span>
            </span>
            <span class="brand-text">PBlog</span>
          </RouterLink>

          <div class="desktop-menu" aria-label="主要导航">
            <RouterLink
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              class="nav-button"
              exact-active-class="active-link"
            >
              {{ item.name }}
            </RouterLink>
          </div>
        </div>

        <div class="nav-center" :class="{ hidden: !configState.showSearch }">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文章..."
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>

        <div class="nav-right">
          <div class="utility-links">
            <RouterLink
              v-for="item in utilityItems"
              :key="item.path"
              :to="item.path"
              class="utility-link"
              exact-active-class="active-link"
            >
              {{ item.name }}
            </RouterLink>
          </div>

          <div class="avatar-frame">
            <GlobalAvatar :size="34" />
          </div>

          <button
            class="mobile-menu-button"
            type="button"
            :aria-expanded="mobileMenuOpen"
            :aria-label="mobileMenuOpen ? '关闭导航菜单' : '打开导航菜单'"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <span></span><span></span><span></span>
          </button>
        </div>

        <Transition name="mobile-menu">
          <div v-if="mobileMenuOpen" class="mobile-panel">
            <el-input
              v-if="configState.showSearch"
              v-model="searchQuery"
              placeholder="搜索文章..."
              class="mobile-search"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>

            <div class="mobile-links">
              <RouterLink
                v-for="item in allMobileItems"
                :key="item.path"
                :to="item.path"
                class="mobile-link"
                exact-active-class="active-link"
                @click="closeMobileMenu"
              >
                {{ item.name }}
              </RouterLink>
            </div>
          </div>
        </Transition>
      </nav>
    </el-affix>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import GlobalAvatar from '@/components/GlobalAvatar.vue';

const menuItems = [
  { name: '首页', path: '/' },
  { name: '博客', path: '/allBlogs' },
  { name: '合集', path: '/collection' },
  { name: '留言板', path: '/remark' }
];

const utilityItems = [
  { name: '友链', path: '/links' },
  { name: '个人中心', path: '/createcentre' },
  { name: '小工具', path: '/tools' }
];

const allMobileItems = [...menuItems, ...utilityItems];
const route = useRoute();
const router = useRouter();

const searchQuery = ref('');
const isTransparent = ref(false);
const mobileMenuOpen = ref(false);

const configState = computed(() => {
  if (route.path === '/') return { forceSolid: false, showSearch: true };
  if (route.path === '/allBlogs') return { forceSolid: true, showSearch: false };
  return { forceSolid: true, showSearch: true };
});

const forceSolid = computed(() => configState.value.forceSolid);

const checkScrollState = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop || document.body.scrollTop;
  isTransparent.value = route.path === '/' && scrollTop < 300;
};

let ticking = false;
const onScroll = () => {
  if (ticking) return;
  window.requestAnimationFrame(() => {
    checkScrollState();
    ticking = false;
  });
  ticking = true;
};

const closeMobileMenu = () => {
  mobileMenuOpen.value = false;
};

const handleSearch = () => {
  const keyword = searchQuery.value.trim();
  if (!keyword) return;
  closeMobileMenu();
  router.push({ path: '/allBlogs', query: { keyword } });
};

watch(
  () => route.path,
  () => {
    checkScrollState();
    closeMobileMenu();
  }
);

onMounted(() => {
  checkScrollState();
  window.addEventListener('scroll', onScroll, { passive: true });
});

onUnmounted(() => window.removeEventListener('scroll', onScroll));
</script>

<style scoped>
.layout-nav-container {
  position: relative;
  z-index: 999;
  width: 100%;
  height: 68px;
}

:deep(.el-affix--fixed) {
  right: 0 !important;
  left: 0 !important;
  width: 100% !important;
}

.layout-nav {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  height: 68px;
  padding: 0 32px;
  box-sizing: border-box;
  color: var(--geo-navy);
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 2px solid var(--geo-navy);
  box-shadow: 0 5px 0 rgba(26, 26, 46, 0.1);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
}

.nav-transparent {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: none;
}

.nav-solid {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 6px 0 rgba(26, 26, 46, 0.1);
}

.nav-left,
.nav-right,
.desktop-menu,
.utility-links {
  display: flex;
  align-items: center;
}

.nav-left {
  flex: 1;
  min-width: 0;
  gap: clamp(18px, 2vw, 32px);
}

.nav-right {
  flex: 1;
  justify-content: flex-end;
  gap: 18px;
}

.nav-brand {
  display: inline-flex;
  flex: none;
  align-items: center;
  gap: 10px;
  color: var(--geo-navy);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.04em;
  text-decoration: none;
}

.brand-mark {
  position: relative;
  display: block;
  width: 32px;
  height: 32px;
  background: var(--geo-gold);
  border: 2px solid var(--geo-navy);
  box-shadow: 4px 4px 0 var(--geo-navy);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.nav-brand:hover .brand-mark {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 var(--geo-navy);
}

.brand-mark span {
  position: absolute;
  display: block;
}

.brand-mark span:nth-child(1) {
  top: 5px;
  left: 5px;
  width: 9px;
  height: 9px;
  background: var(--geo-coral);
}

.brand-mark span:nth-child(2) {
  right: 4px;
  bottom: 4px;
  width: 10px;
  height: 10px;
  background: var(--geo-sky);
  border-radius: 50%;
}

.brand-mark span:nth-child(3) {
  right: 4px;
  top: 4px;
  width: 8px;
  height: 8px;
  background: var(--geo-navy);
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
}

.desktop-menu {
  gap: 9px;
}

.nav-button,
.utility-link,
.mobile-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--geo-navy);
  font-weight: 750;
  text-decoration: none;
  white-space: nowrap;
  background: #fff;
  border: 2px solid transparent;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.nav-button {
  min-height: 38px;
  padding: 0 13px;
  font-size: 14px;
}

.nav-button:hover,
.utility-link:hover {
  background: var(--geo-sky-light);
  border-color: var(--geo-navy);
  transform: translate(-2px, -2px);
  box-shadow: 3px 3px 0 var(--geo-navy);
}

.nav-button.active-link {
  background: var(--geo-gold);
  border-color: var(--geo-navy);
  transform: translate(-2px, -2px);
  box-shadow: 4px 4px 0 var(--geo-navy);
}

.nav-center {
  position: absolute;
  top: 50%;
  left: 50%;
  width: clamp(220px, 21vw, 300px);
  transform: translate(-50%, -50%);
  transition: opacity 0.2s ease;
}

.nav-center.hidden {
  visibility: hidden;
  opacity: 0;
  pointer-events: none;
}

.search-input,
.mobile-search {
  width: 100%;
}

.search-input :deep(.el-input__wrapper),
.mobile-search :deep(.el-input__wrapper) {
  min-height: 40px;
  background: #fff;
  border: 2px solid var(--geo-navy);
  border-radius: 0;
  box-shadow: 3px 3px 0 var(--geo-coral);
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.search-input :deep(.el-input__wrapper.is-focus),
.mobile-search :deep(.el-input__wrapper.is-focus) {
  transform: translate(-2px, -2px);
  box-shadow: 5px 5px 0 var(--geo-coral);
}

.search-input :deep(.el-input__inner),
.mobile-search :deep(.el-input__inner) {
  color: var(--geo-navy);
  font-weight: 600;
}

.utility-links {
  gap: 4px;
}

.utility-link {
  min-height: 34px;
  padding: 0 8px;
  font-size: 13px;
}

.utility-link.active-link {
  background: var(--geo-coral-light);
  border-color: var(--geo-navy);
  box-shadow: 3px 3px 0 var(--geo-navy);
}

.avatar-frame {
  display: flex;
  flex: none;
  padding: 2px;
  background: #fff;
  border: 2px solid var(--geo-navy);
  box-shadow: 3px 3px 0 var(--geo-sky);
}

.avatar-frame :deep(.el-avatar) {
  border-radius: 0;
}

.mobile-menu-button {
  display: none;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  gap: 5px;
  color: var(--geo-navy);
  background: var(--geo-gold);
  border: 2px solid var(--geo-navy);
  box-shadow: 3px 3px 0 var(--geo-navy);
  cursor: pointer;
}

.mobile-menu-button span {
  width: 18px;
  height: 2px;
  background: currentColor;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.menu-open .mobile-menu-button span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.menu-open .mobile-menu-button span:nth-child(2) {
  opacity: 0;
}

.menu-open .mobile-menu-button span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

.mobile-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 12px;
  left: 12px;
  display: none;
  padding: 14px;
  background: #fff;
  border: 2px solid var(--geo-navy);
  box-shadow: 6px 6px 0 var(--geo-navy);
}

.mobile-search {
  margin-bottom: 14px;
}

.mobile-links {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.mobile-link {
  min-height: 44px;
  font-size: 14px;
  border-color: var(--geo-navy);
}

.mobile-link:nth-child(3n + 1) {
  background: var(--geo-gold-light);
}

.mobile-link:nth-child(3n + 2) {
  background: var(--geo-coral-light);
}

.mobile-link:nth-child(3n) {
  background: var(--geo-sky-light);
}

.mobile-link.active-link,
.mobile-link:hover {
  background: var(--geo-gold);
  transform: translate(-2px, -2px);
  box-shadow: 3px 3px 0 var(--geo-navy);
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
  transform-origin: top center;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

@media (max-width: 1240px) {
  .layout-nav {
    padding: 0 20px;
  }

  .brand-text {
    display: none;
  }

  .nav-left {
    gap: 18px;
  }

  .nav-button {
    padding: 0 9px;
  }

  .nav-right {
    gap: 12px;
  }

  .utility-link {
    padding: 0 5px;
  }

  .nav-center {
    width: clamp(190px, 19vw, 240px);
  }
}

@media (max-width: 960px) {
  .layout-nav-container,
  .layout-nav {
    height: 64px;
  }

  .layout-nav {
    padding: 0 14px;
  }

  .desktop-menu,
  .nav-center,
  .utility-links {
    display: none;
  }

  .brand-text {
    display: inline;
  }

  .mobile-menu-button,
  .mobile-panel {
    display: flex;
  }

  .mobile-panel {
    flex-direction: column;
  }
}

@media (max-width: 420px) {
  .brand-text {
    font-size: 17px;
  }

  .mobile-links {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .brand-mark,
  .nav-button,
  .utility-link,
  .mobile-menu-button span,
  .mobile-menu-enter-active,
  .mobile-menu-leave-active {
    transition: none;
  }
}
</style>
