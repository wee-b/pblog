<template>
  <div class="create-layout">
    <div class="page-decoration" aria-hidden="true">
      <span class="decor-square"></span>
      <span class="decor-circle"></span>
      <span class="decor-line"></span>
    </div>

    <div class="layout-container">
      <aside class="aside-card">
        <div class="aside-header">
          <p class="aside-kicker"><span></span> CREATOR SPACE</p>
          <div class="title"><small>MY</small>个人中心</div>
          <button type="button" class="create-btn" @click="handleGoEditor">
            <el-icon><EditPen /></el-icon><span>开始创作</span><b>→</b>
          </button>
        </div>

        <el-menu
            :default-active="currentView"
            class="create-menu"
            @select="handleMenuSelect"
        >
          <!-- 1. 个人信息 -->
          <el-menu-item index="personalInfo">
            <b class="menu-index">01</b>
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="CommentManage">
            <b class="menu-index">02</b>
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="ContentManage">
            <b class="menu-index">03</b>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </el-menu-item>
          <el-menu-item index="SeriesManage">
            <b class="menu-index">04</b>
            <el-icon><Collection /></el-icon>
            <span>合集管理</span>
          </el-menu-item>
          <el-menu-item index="StatsView">
            <b class="menu-index">05</b>
            <el-icon><DataLine /></el-icon>
            <span>数据概览</span>
          </el-menu-item>

          <div class="menu-divider"></div>

          <el-menu-item index="goHome">
            <b class="menu-index">↙</b>
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
        </el-menu>
        <div class="aside-signature"><span>PBLOG</span><small>WRITE · SHARE · CONNECT</small></div>
      </aside>

      <main class="main-card">
        <header class="main-header">
          <div class="current-section">
            <span>{{ currentSection.number }}</span>
            <div><small>PERSONAL CENTER</small><h1>{{ currentSection.label }}</h1></div>
          </div>
          <div class="header-marks" aria-hidden="true"><i></i><i></i><i></i></div>
        </header>
        <div class="main-body">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="componentsMap[currentView]" />
            </keep-alive>
          </transition>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, DataLine, Document, ChatDotRound, HomeFilled, User, Collection } from '@element-plus/icons-vue'

import personalInfo from './components/personalInfo.vue'
import CommentManage from './components/CommentManage.vue'
import ContentManage from './components/ContentManage.vue'
import SeriesManage from './components/SeriesManage.vue'
import StatsView from './components/StatsView.vue'

const router = useRouter()
const currentView = ref('personalInfo')
const sectionMeta = {
  personalInfo: { number: '01', label: '个人信息' },
  CommentManage: { number: '02', label: '评论管理' },
  ContentManage: { number: '03', label: '内容管理' },
  SeriesManage: { number: '04', label: '合集管理' },
  StatsView: { number: '05', label: '数据概览' }
}
const currentSection = computed(() => sectionMeta[currentView.value] || sectionMeta.personalInfo)

const componentsMap = shallowRef({
  personalInfo,
  CommentManage,
  ContentManage,
  SeriesManage,
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
.create-layout {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  color: var(--geo-navy, #1a1a2e);
  background-color: #fff;
  background-image:
    linear-gradient(rgba(26, 26, 46, .045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 26, 46, .045) 1px, transparent 1px);
  background-size: 42px 42px;
}

.layout-container {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 278px minmax(0, 1fr);
  width: min(1480px, calc(100% - 48px));
  min-height: calc(100vh - 64px);
  margin: 0 auto;
  padding: 32px 0;
  box-sizing: border-box;
  gap: 30px;
}

.aside-card {
  display: flex;
  min-height: 0;
  flex-direction: column;
  background: #fff;
  border: 3px solid var(--geo-navy, #1a1a2e);
  box-shadow: 10px 10px 0 var(--geo-navy, #1a1a2e);

  .aside-header {
    padding: 28px 24px 24px;
    background: var(--geo-gold-light, #fff5c2);
    border-bottom: 3px solid var(--geo-navy, #1a1a2e);

    .title {
      margin: 12px 0 22px;
      font-size: 30px;
      font-weight: 950;
      line-height: 1;
      letter-spacing: -.06em;

      small { display: block; margin-bottom: 5px; color: var(--geo-coral, #ff6b6b); font-size: 13px; letter-spacing: .18em; }
    }

    .create-btn {
      display: grid;
      grid-template-columns: auto 1fr auto;
      width: 100%;
      min-height: 46px;
      padding: 0 13px;
      align-items: center;
      gap: 9px;
      color: var(--geo-navy, #1a1a2e);
      font-size: 14px;
      font-weight: 900;
      background: var(--geo-gold, #ffd54f);
      border: 2px solid var(--geo-navy, #1a1a2e);
      box-shadow: 5px 5px 0 var(--geo-navy, #1a1a2e);
      cursor: pointer;
      transition: transform .18s ease, box-shadow .18s ease;

      &:hover { transform: translate(-2px, -2px); box-shadow: 7px 7px 0 var(--geo-navy, #1a1a2e); }
      &:active { transform: translate(2px, 2px); box-shadow: 2px 2px 0 var(--geo-navy, #1a1a2e); }
      b { font-size: 20px; }
    }
  }

  .create-menu {
    width: 100%;
    padding: 18px 16px 8px;
    box-sizing: border-box;
    border-right: 0;
    background: transparent;

    :deep(.el-menu-item) {
      display: grid;
      grid-template-columns: 30px 22px 1fr;
      height: 50px;
      margin-bottom: 9px;
      padding: 0 12px !important;
      gap: 8px;
      color: var(--geo-navy, #1a1a2e);
      border: 2px solid transparent;
      transition: transform .16s ease, background .16s ease, box-shadow .16s ease;

      &:hover {
        background: var(--geo-sky-light, #dff5ff);
        border-color: var(--geo-navy, #1a1a2e);
        transform: translateX(3px);
      }

      &.is-active {
        color: var(--geo-navy, #1a1a2e);
        font-weight: 900;
        background: var(--geo-coral-light, #ffdada);
        border-color: var(--geo-navy, #1a1a2e);
        box-shadow: 4px 4px 0 var(--geo-navy, #1a1a2e);
      }

      .el-icon { width: 22px; margin: 0; font-size: 17px; }
      span { font-size: 14px; }
    }
  }
}

.aside-kicker { display: flex; margin: 0; align-items: center; gap: 8px; font-size: 10px; font-weight: 950; letter-spacing: .14em; }
.aside-kicker span { width: 24px; height: 6px; background: linear-gradient(90deg, var(--geo-coral, #ff6b6b) 0 55%, var(--geo-sky, #53bde8) 55%); }
.menu-index { font-size: 10px; font-weight: 950; letter-spacing: .06em; }
.menu-divider { height: 2px; margin: 16px 4px; background: var(--geo-navy, #1a1a2e); opacity: .16; }
.aside-signature { display: flex; margin: auto 20px 20px; padding-top: 18px; flex-direction: column; border-top: 2px solid var(--geo-navy, #1a1a2e); }
.aside-signature span { font-size: 13px; font-weight: 950; letter-spacing: .16em; }
.aside-signature small { margin-top: 3px; color: var(--color-text-muted, #777); font-size: 8px; letter-spacing: .1em; }

.main-card {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border: 3px solid var(--geo-navy, #1a1a2e);
  box-shadow: 10px 10px 0 var(--geo-coral, #ff6b6b);

  .main-body {
    flex: 1;
    min-height: 0;
    padding: clamp(24px, 3.2vw, 46px);
    overflow-y: auto;

    &::-webkit-scrollbar { width: 8px; }
    &::-webkit-scrollbar-thumb { background: var(--geo-gold, #ffd54f); border: 2px solid var(--geo-navy, #1a1a2e); }
    &::-webkit-scrollbar-track { background: #fff; border-left: 1px solid rgba(26, 26, 46, .12); }
  }
}

.main-header { display: flex; min-height: 108px; padding: 20px clamp(24px, 3.2vw, 46px); box-sizing: border-box; align-items: center; justify-content: space-between; gap: 20px; background: #fff; border-bottom: 3px solid var(--geo-navy, #1a1a2e); }
.current-section { display: flex; align-items: center; gap: 15px; }
.current-section > span { display: grid; width: 48px; height: 48px; place-items: center; color: #fff; font-size: 16px; font-weight: 950; background: var(--geo-navy, #1a1a2e); box-shadow: 5px 5px 0 var(--geo-gold, #ffd54f); }
.current-section small { display: block; margin-bottom: 3px; color: var(--geo-coral, #ff6b6b); font-size: 9px; font-weight: 900; letter-spacing: .16em; }
.current-section h1 { margin: 0; font-size: clamp(24px, 2.5vw, 34px); line-height: 1; letter-spacing: -.05em; }
.header-marks { display: flex; align-items: center; gap: 9px; }
.header-marks i { display: block; width: 22px; height: 22px; border: 2px solid var(--geo-navy, #1a1a2e); }
.header-marks i:first-child { background: var(--geo-coral, #ff6b6b); transform: rotate(12deg); }
.header-marks i:nth-child(2) { background: var(--geo-gold, #ffd54f); border-radius: 50%; }
.header-marks i:last-child { width: 30px; background: var(--geo-sky, #53bde8); transform: skewX(-14deg); }

.page-decoration span { position: absolute; z-index: 0; display: block; border: 2px solid var(--geo-navy, #1a1a2e); }
.decor-square { top: -28px; right: 8%; width: 82px; height: 82px; background: var(--geo-gold, #ffd54f); transform: rotate(14deg); }
.decor-circle { left: -30px; bottom: 9%; width: 72px; height: 72px; background: var(--geo-sky, #53bde8); border-radius: 50%; }
.decor-line { right: 1.5%; bottom: 3%; width: 90px; height: 18px; background: var(--geo-coral, #ff6b6b); transform: rotate(-8deg); }

.fade-enter-active, .fade-leave-active { transition: opacity .18s ease, transform .18s ease; }
.fade-enter-from { opacity: 0; transform: translateX(8px); }
.fade-leave-to { opacity: 0; transform: translateX(-8px); }

@media (max-width: 900px) {
  .create-layout { overflow: auto; }
  .layout-container { grid-template-columns: 1fr; width: calc(100% - 32px); padding: 20px 0 32px; gap: 22px; }
  .aside-card { min-height: auto; box-shadow: 7px 7px 0 var(--geo-navy, #1a1a2e); }
  .aside-card .aside-header { display: grid; grid-template-columns: 1fr minmax(180px, 230px); padding: 20px; align-items: end; gap: 12px 22px; }
  .aside-card .aside-header .aside-kicker { grid-column: 1 / -1; }
  .aside-card .aside-header .title { margin: 0; }
  .aside-card .create-menu { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); padding: 14px; gap: 8px; }
  .aside-card .create-menu :deep(.el-menu-item) { width: 100%; margin: 0; }
  .menu-divider, .aside-signature { display: none; }
  .main-card { min-height: 720px; box-shadow: 7px 7px 0 var(--geo-coral, #ff6b6b); }
}

@media (max-width: 600px) {
  .layout-container { width: calc(100% - 20px); padding-top: 12px; }
  .aside-card .aside-header { grid-template-columns: 1fr; }
  .aside-card .aside-header .create-btn { max-width: none; }
  .aside-card .create-menu { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .aside-card .create-menu :deep(.el-menu-item) { grid-template-columns: 23px 19px 1fr; padding: 0 8px !important; gap: 5px; }
  .aside-card .create-menu :deep(.el-menu-item span) { font-size: 12px; }
  .main-header { min-height: 88px; padding: 15px 18px; }
  .current-section > span { width: 42px; height: 42px; }
  .header-marks { display: none; }
  .main-card .main-body { padding: 20px 14px 32px; }
}
</style>
