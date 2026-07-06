<template>
  <div id="app" class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="app-header">
      <div class="header-left">
        <el-button
            icon="Menu"
            size="small"
            class="sidebar-toggle"
            @click="toggleSidebar"
        ></el-button>
        <h1 class="app-title">博客管理系统</h1>
      </div>

      <div class="header-right">
        <GlobalAvatar></GlobalAvatar>
      </div>
    </el-header>

    <div class="app-main">
      <!-- 侧边栏导航 -->
      <el-aside
          :width="sidebarWidth"
          class="app-sidebar"
          :class="{ 'sidebar-collapsed': isSidebarCollapsed }"
      >
        <el-menu
            default-active="dashboard"
            class="sidebar-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            :collapse="isSidebarCollapsed"
            router
        >
          <!-- 仪表盘 -->
          <el-menu-item index="dashboard">
            <el-icon><PieChart /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <!-- 文章管理 -->
          <el-menu-item index="articles">
            <el-icon><Document /></el-icon>
            <template #title>文章管理</template>
          </el-menu-item>

          <!-- 分类/标签管理 -->
          <el-menu-item index="categorys">
            <el-icon><Document /></el-icon>
            <template #title>分类/标签管理</template>
          </el-menu-item>

          <!-- 评论管理 -->
          <el-menu-item index="comments">
            <el-icon><Message /></el-icon>
            <template #title>评论管理</template>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-menu-item index="users">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>

          <!-- 系统设置 -->
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="app-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  PieChart,
  Document,
  FolderOpened,
  Tickets,
  Message,
  UserFilled,
  Setting,
  ArrowDown,
} from '@element-plus/icons-vue';
import GlobalAvatar from "@/components/GlobalAvatar.vue";

// 侧边栏状态管理
const isSidebarCollapsed = ref(false);
const sidebarWidth = ref('200px');

// 切换侧边栏展开/收起状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
  sidebarWidth.value = isSidebarCollapsed.value ? '64px' : '200px';
};
</script>

<!-- 关键：新增非 scoped 的全局样式，重置浏览器默认样式 -->
<style>
/* 全局重置：移除 body/html 的默认边距和滚动 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow: hidden !important; /* 强制禁用整个页面的滚动 */
}

/* 重置 Element Plus 布局组件的默认样式 */
.el-container, .el-header, .el-aside, .el-main {
  padding: 0;
  margin: 0;
  border: none;
}
</style>

<style scoped>
/* 核心容器：占满视口，禁用滚动 */
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden !important; /* 强制禁用 */
}

/* 顶部导航：固定高度，不压缩 */
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px; /* 固定高度，避免自适应导致高度变化 */
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 10;
  flex-shrink: 0; /* 禁止压缩 */
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  color: #666;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.user-avatar {
  background-color: #409EFF;
}

.username {
  font-size: 14px;
}

/* 主内容容器：自适应剩余高度，禁用滚动 */
.app-main {
  display: flex;
  flex: 1;
  overflow: hidden !important; /* 强制禁用 */
}

/* 侧边栏：固定宽度，不压缩 */
.app-sidebar {
  background-color: #304156;
  transition: width 0.3s ease;
  height: 100%;
  flex-shrink: 0; /* 禁止压缩 */
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-collapsed .el-sub-menu__title span,
.sidebar-collapsed .el-menu-item span {
  display: none;
}

.sidebar-collapsed .el-sub-menu__icon-arrow {
  display: none;
}

/* 内容区：仅这里允许滚动 */
.app-content {
  flex: 1;
  padding: 20px;
  background-color: #F5F7FA;
  overflow-y: auto; /* 仅纵向滚动 */
  overflow-x: hidden; /* 禁用横向滚动 */
  height: 100%;
}

/* 美化内容区滚动条（可选） */
.app-content::-webkit-scrollbar {
  width: 6px;
}

.app-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.app-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.app-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>