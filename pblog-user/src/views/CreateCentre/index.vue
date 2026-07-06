<template>
  <div class="create-layout">
    <!-- 布局容器：控制整体边距和排列 -->
    <div class="layout-container">

      <!-- 左侧悬浮卡片 -->
      <div class="aside-card">
        <div class="aside-header">
          <div class="title">个人中心</div>
          <el-button type="primary" round class="create-btn" @click="handleGoEditor">
            <el-icon class="mr-1"><EditPen /></el-icon> 开始创作
          </el-button>
        </div>

        <el-menu
            :default-active="currentView"
            class="create-menu"
            @select="handleMenuSelect"
        >
          <!-- 1. 个人信息 -->
          <el-menu-item index="personalInfo">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <!-- 2. 评论管理 -->
          <el-menu-item index="CommentManage">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <!-- 3. 内容管理 -->
          <el-menu-item index="ContentManage">
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </el-menu-item>
          <!-- 4. 数据概览 -->
          <el-menu-item index="StatsView">
            <el-icon><DataLine /></el-icon>
            <span>数据概览</span>
          </el-menu-item>

          <div class="menu-divider"></div>

          <el-menu-item index="goHome">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容悬浮卡片 -->
      <div class="main-card">
        <div class="main-body">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="componentsMap[currentView]" />
            </keep-alive>
          </transition>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, DataLine, Document, ChatDotRound, HomeFilled, User } from '@element-plus/icons-vue'

import personalInfo from './components/personalInfo.vue'
import CommentManage from './components/CommentManage.vue'
import ContentManage from './components/ContentManage.vue'
import StatsView from './components/StatsView.vue'

const router = useRouter()
const currentView = ref('personalInfo')

const componentsMap = shallowRef({
  personalInfo,
  CommentManage,
  ContentManage,
  StatsView
})

const handleMenuSelect = (index) => {
  if (index === 'goHome') {
    router.push('/')
  } else {
    currentView.value = index
  }
}

const handleGoEditor = () => {
  router.push('/writeBlog')
}
</script>

<style scoped lang="scss">
/* 1. 最外层背景：使用渐变或图片 */
.create-layout {
  height: 100vh;
  width: 100vw;
  /* 示例背景：使用温和的渐变，实际项目中可替换为 background-image: url(...) */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  /* 如果想用图片，解开下面这行： */
  // background: url('https://source.unsplash.com/random/1920x1080?abstract,white') no-repeat center center / cover;

  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden; /* 防止出现滚动条 */
}

/* 2. 布局容器：控制卡片之间的间距和屏幕边缘的距离 */
.layout-container {
  width: 100%;
  height: 100%;
  max-width: 1600px; /* 限制大屏幕下的最大宽度 */
  padding: 24px;     /* 屏幕四周留白 */
  box-sizing: border-box;
  display: flex;
  gap: 24px;         /* 左侧菜单和右侧内容的间距 */
}

/* 3. 左侧侧边栏卡片 */
.aside-card {
  width: 260px;
  background: rgba(255, 255, 255, 0.9); /* 轻微透明 */
  backdrop-filter: blur(10px);          /* 磨砂玻璃效果 */
  border-radius: 24px;                  /* 大圆角 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05); /* 柔和阴影 */
  display: flex;
  flex-direction: column;
  padding-bottom: 20px;
  transition: all 0.3s ease;

  /* 头部区域 */
  .aside-header {
    padding: 40px 20px 30px;
    display: flex;
    flex-direction: column;
    align-items: center;

    .avatar-wrapper {
      margin-bottom: 16px;
      transition: transform 0.3s;
      &:hover { transform: scale(1.05); }
    }

    .title {
      font-size: 20px;
      font-weight: 800;
      color: #2c3e50;
      margin-bottom: 24px;
      letter-spacing: 1px;
    }

    .create-btn {
      width: 85%;
      height: 44px;
      font-size: 16px;
      font-weight: bold;
      border-radius: 12px; /* 按钮也用圆角 */
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      transition: transform 0.2s;

      &:hover { transform: translateY(-2px); }
      &:active { transform: translateY(0); }
    }
  }

  /* 菜单样式重写 */
  .create-menu {
    border-right: none;
    background: transparent;
    width: 100%;
    padding: 0 16px;
    box-sizing: border-box;

    :deep(.el-menu-item) {
      height: 54px;
      margin-bottom: 8px;
      border-radius: 16px; /* 菜单项圆角 */
      color: #606266;
      transition: all 0.3s;

      &:hover {
        background-color: rgba(64, 158, 255, 0.08);
        color: var(--el-color-primary);
      }

      &.is-active {
        background: var(--el-color-primary);
        color: #fff;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        font-weight: 600;
      }

      .el-icon { font-size: 18px; margin-right: 10px; }
      span { font-size: 15px; }
    }
  }
}

/* 4. 右侧内容卡片 */
.main-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px; /* 与左侧保持一致 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
  overflow: hidden; /* 确保内容不溢出圆角 */
  display: flex;
  flex-direction: column;
  position: relative;

  .main-body {
    flex: 1;
    padding: 40px;
    overflow-y: auto;

    /* 自定义滚动条样式 */
    &::-webkit-scrollbar { width: 6px; }
    &::-webkit-scrollbar-thumb { border-radius: 3px; background: #dcdfe6; }
    &::-webkit-scrollbar-track { background: transparent; }
  }
}

.menu-divider {
  height: 1px;
  background: rgba(0,0,0,0.05);
  margin: 15px 10px;
}
.mr-1 { margin-right: 6px; }

/* 动画效果 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.fade-enter-from { opacity: 0; transform: translateY(10px); }
.fade-leave-to { opacity: 0; transform: translateY(-10px); }
</style>