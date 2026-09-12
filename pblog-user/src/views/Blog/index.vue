<template>
  <div class="blog-detail-container" v-loading="loading">

    <!-- 文章头图 -->
    <header class="blog-header-banner">
      <div class="banner-content">
        <div class="article-eyebrow"><span></span> ARTICLE / {{ article.id || '00' }}</div>
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(article.publishedAt) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ article.viewCount || 0 }} 阅读</span>
          </div>
        </div>
      </div>
      <div class="cover-frame">
        <div class="cover-image" :style="{ backgroundImage: `url(${article.coverImage || defaultCover})` }"></div>
        <span class="cover-index">READ / BLOG</span>
      </div>
      <div class="hero-shape hero-circle" aria-hidden="true"></div>
      <div class="hero-shape hero-triangle" aria-hidden="true"></div>
    </header>

    <div class="main-wrapper">
      <el-row :gutter="40">
        <!-- 左侧：文章内容 + 评论区 -->
        <el-col :xs="24" :sm="24" :md="18" :lg="18" :xl="18">
          <el-card class="article-card" shadow="never">
            <!-- 简介 -->
            <div class="article-summary" v-if="article.summary">
              <span class="summary-mark">“</span>
              <p>{{ article.summary }}</p>
            </div>

            <!-- 正文 (ref用于提取目录) -->
            <!-- typo-content 类用于专门修饰 Markdown 样式 -->
            <div
                ref="articleContentRef"
                class="article-content typo-content"
                v-html="renderedContent"
            ></div>

            <el-divider />

            <!-- 底部点赞等操作 -->
            <div class="article-footer">
              <div class="footer-label"><small>ENJOYED IT?</small><strong>喜欢这篇文章？</strong></div>
              <LikeButton
                  :target-id="article.id"
                  :target-type="1"
              />
            </div>
          </el-card>

          <!-- ================= 评论区组件 ================= -->
          <!-- 只有当文章ID存在时才渲染，避免空ID请求 -->
          <CommentSection v-if="article.id" :article-id="article.id" />

        </el-col>

        <!-- 右侧：侧边栏 -->
        <el-col :xs="0" :sm="0" :md="6" :lg="6" :xl="6">
          <div class="sidebar-wrapper">
            <!-- 作者卡片 -->
            <el-card class="sidebar-card" shadow="hover">
              <div class="author-card">
                <span class="sidebar-kicker">AUTHOR / 01</span>
                <el-avatar :size="72" :src="authorInfo.avatarUrl || defaultAvatar" />
                <h3 class="mt-2">{{authorInfo.nickname}}</h3>
                <p class="desc">{{ authorInfo.bio || '暂无简介'}}</p>
                <el-button type="primary" class="w-100 mt-3" @click="ElMessage.warning('功能未开放')">关注作者 <span>→</span></el-button>
              </div>
            </el-card>


            <!-- 目录 (动态生成 + 高亮) -->
            <el-card class="sidebar-card toc-card mt-4 sticky-card" shadow="hover">
              <template #header>
                <div class="sidebar-heading"><b>02</b><span><small>CONTENTS</small>文章目录</span></div>
              </template>
              <div class="toc-list" v-if="tocList.length > 0">
                <div
                    v-for="(item, index) in tocList"
                    :key="index"
                    class="toc-item"
                    :class="{
                    'active': activeTocIndex === index,
                    'pl-3': item.level === 3,
                    'pl-4': item.level > 3
                  }"
                    @click="scrollToHeading(item.id)"
                >
                  {{ item.text }}
                </div>
              </div>
              <div v-else class="toc-empty">暂无目录</div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/apis/article/article.js'
import UserApi from "@/apis/user/user.js";
// 引入拆分后的评论组件
import CommentSection from './components/CommentSection.vue'
import { Calendar, View, Pointer } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import defaultCoverUrl from '@/assets/imgs/default-cover.jpg'
import defaultAvatarUrl from '@/assets/imgs/default-avatar.png'
import {ElMessage} from "element-plus";


// --- 状态定义 ---
const route = useRoute()
const loading = ref(true)
const article = ref({})
const authorInfo = ref({})
const defaultCover = defaultCoverUrl
const defaultAvatar = defaultAvatarUrl
const articleContentRef = ref(null)

// --- Markdown 配置 ---
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
            hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
            '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

// 计算属性：渲染 HTML
const renderedContent = computed(() => {
  if (!article.value.content) return ''
  return md.render(article.value.content)
})

// --- 目录高亮逻辑 ---
const tocList = ref([])
const activeTocIndex = ref(0)

const generateToc = () => {
  if (!articleContentRef.value) return
  const headers = articleContentRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const toc = []
  headers.forEach((header, index) => {
    const id = `heading-${index}`
    header.setAttribute('id', id)
    toc.push({
      id: id,
      text: header.innerText,
      level: parseInt(header.tagName.replace('H', '')),
      top: header.offsetTop
    })
  })
  tocList.value = toc
}

const handleScroll = () => {
  if (tocList.value.length === 0) return
  const scrollY = window.scrollY + 100
  let activeIndex = -1
  for (let i = 0; i < tocList.value.length; i++) {
    const item = tocList.value[i]
    const element = document.getElementById(item.id)
    if (element && element.offsetTop <= scrollY) {
      activeIndex = i
    } else {
      break
    }
  }
  activeTocIndex.value = activeIndex >= 0 ? activeIndex : 0
}

const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    const top = element.offsetTop - 80
    window.scrollTo({ top: top, behavior: 'smooth' })
  }
}

// --- 初始化逻辑 ---
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

/**
 * 获取作者信息
 * @param username
 * @returns {Promise<void>}
 */
const fetchAuthorInfo = async (username)=>{
  const authorRes = await UserApi.getUserInfoByUserName(username);
  if(authorRes.data){
    authorInfo.value = authorRes.data.data
  }else{
    ElMessage.error("获取作者信息失败")
  }
}

/**
 * 获取页面信息
 * @returns {Promise<void>}
 */
const fetchDetail = async () => {
  const id = route.params.id
  if (!id) return

  try {
    loading.value = true
    const res = await getArticleDetail(id)
    if (res.data) {
      article.value = res.data.data
      document.title = article.value.title

      await fetchAuthorInfo(article.value.authorUsername);
      nextTick(() => {
        generateToc()
        handleScroll()
      })
    }
  } finally {
    loading.value = false
  }
}



onMounted(() => {
  fetchDetail()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
/* Page layout */
.blog-detail-container { min-height: 100vh; background-color: var(--color-bg-soft); padding-bottom: 60px; }

/* Geometric banner */
.blog-header-banner {
  height: 400px;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  margin-bottom: -60px;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.7), rgba(240, 98, 146, 0.4));
}

.banner-content { position: relative; z-index: 2; max-width: 800px; padding: 0 20px; }
.article-title { font-size: 2.5rem; font-weight: 800; margin-bottom: 20px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); letter-spacing: -0.01em; }
.article-meta { display: flex; justify-content: center; align-items: center; gap: 15px; opacity: 0.95; }
.meta-item { display: flex; align-items: center; gap: 6px; }

.main-wrapper { max-width: 1200px; margin: 0 auto; padding: 0 20px; position: relative; z-index: 10; }

.article-card {
  border-radius: var(--radius-lg);
  padding: 20px;
  min-height: 500px;
  border: none;
  box-shadow: var(--shadow-md);
}

.article-summary {
  background: linear-gradient(135deg, rgba(240, 98, 146, 0.06), rgba(79, 195, 247, 0.06));
  border-left: 4px solid var(--geo-coral);
  padding: 15px 20px;
  margin-bottom: 30px;
  color: var(--color-text-secondary);
  font-style: italic;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.article-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 30px; padding-top: 20px; }
.action-buttons { display: flex; align-items: center; .action-count { margin-left: 8px; color: #666; font-weight: bold; } }

/* Sidebar */
.sidebar-wrapper { position: relative; height: 100%; }
.sidebar-card {
  border-radius: var(--radius-md);
  border: none;
  box-shadow: var(--shadow-sm);
}

.author-card { text-align: center; padding: 10px; .desc { color: var(--color-text-muted); font-size: 13px; margin: 10px 0; } }
.sticky-card { position: sticky; top: 80px; }

.toc-list {
  max-height: 70vh;
  overflow-y: auto;
  .toc-item {
    padding: 8px 12px;
    font-size: 14px;
    color: var(--color-text-secondary);
    cursor: pointer;
    border-radius: var(--radius-sm);
    border-left: 2px solid transparent;
    transition: all 0.2s;

    &:hover { background-color: rgba(240, 98, 146, 0.06); color: var(--geo-coral); }
    &.active {
      color: var(--geo-coral);
      font-weight: bold;
      background-color: rgba(240, 98, 146, 0.06);
      border-left-color: var(--geo-coral);
    }
  }
}
.pl-3 { padding-left: 24px !important; }
.pl-4 { padding-left: 36px !important; }
.toc-empty { color: var(--color-text-muted); font-size: 13px; text-align: center; padding: 10px 0; }
.mt-2 { margin-top: 8px; }
.mt-3 { margin-top: 12px; }
.mt-4 { margin-top: 16px; }
.w-100 { width: 100%; }

/* Markdown typography */
:deep(.typo-content) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-primary);
  word-wrap: break-word;

  h1, h2, h3, h4 { margin-top: 1.2em; scroll-margin-top: 80px; font-weight: 700; color: var(--geo-navy); }
  h1 { font-size: 2em; padding-bottom: 0.3em; border-bottom: 2px solid var(--color-border); }
  h2 { font-size: 1.6em; padding-bottom: 0.3em; border-bottom: 1px solid var(--color-border); }
  h3 { font-size: 1.35em; }
  p { margin-bottom: 1.5em; text-align: justify; }
  ul, ol { padding-left: 2em; margin-bottom: 1.5em; }
  li { margin-bottom: 0.5em; }

  img { max-width: 100%; height: auto; border-radius: var(--radius-md); display: block; margin: 1em auto; box-shadow: var(--shadow-md); }

  pre {
    background-color: var(--geo-navy);
    color: #abb2bf;
    padding: 1em;
    border-radius: var(--radius-md);
    overflow-x: auto;
    font-family: Consolas, Monaco, monospace;
    margin-bottom: 1.5em;
  }

  code { font-family: Consolas, Monaco, monospace; }
  p code, li code { background-color: rgba(240, 98, 146, 0.08); color: var(--geo-coral-dark); padding: 0.2em 0.4em; border-radius: 3px; font-size: 85%; }

  blockquote {
    margin: 1.5em 0;
    padding: 0.8em 1.5em;
    color: var(--color-text-secondary);
    border-left: 4px solid var(--geo-sky);
    background-color: rgba(79, 195, 247, 0.05);
    border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  }

  table { display: block; width: 100%; overflow: auto; margin-bottom: 1.5em; border-spacing: 0; border-collapse: collapse; }
  th, td { padding: 6px 13px; border: 1px solid var(--color-border); }
  tr:nth-child(2n) { background-color: var(--color-bg-soft); }
}
</style>

<style scoped lang="scss">
.blog-detail-container {
  min-height: calc(100vh - 68px);
  padding-bottom: 90px;
  color: var(--geo-navy, #1a1a2e);
  background-color: #fff;
  background-image:
    linear-gradient(rgba(26, 26, 46, .04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 26, 46, .04) 1px, transparent 1px);
  background-size: 46px 46px;
}

.blog-header-banner {
  position: relative;
  display: grid;
  height: auto;
  min-height: 440px;
  margin: 0;
  padding: 64px max(28px, calc((100vw - 1240px) / 2));
  overflow: hidden;
  grid-template-columns: minmax(0, 1.08fr) minmax(360px, .72fr);
  align-items: center;
  gap: clamp(50px, 7vw, 100px);
  box-sizing: border-box;
  color: var(--geo-navy, #1a1a2e);
  text-align: left;
  background: rgba(255, 255, 255, .94);
  border-bottom: 3px solid var(--geo-navy, #1a1a2e);
}

.banner-content { position: relative; z-index: 3; max-width: none; padding: 0; }
.article-eyebrow { display: flex; margin-bottom: 20px; align-items: center; gap: 10px; color: var(--geo-coral-dark, #d9485f); font-size: 11px; font-weight: 950; letter-spacing: .17em; }
.article-eyebrow span { width: 38px; height: 8px; background: linear-gradient(90deg, var(--geo-coral, #ff6b6b) 0 58%, var(--geo-sky, #53bde8) 58%); border: 1px solid var(--geo-navy, #1a1a2e); }
.article-title { max-width: 760px; margin: 0 0 30px; color: var(--geo-navy, #1a1a2e); font-size: clamp(38px, 5vw, 68px); font-weight: 950; line-height: 1.08; letter-spacing: -.065em; text-shadow: none; }
.article-meta { display: flex; justify-content: flex-start; gap: 12px; opacity: 1; }
.meta-item { min-height: 36px; padding: 0 12px; color: var(--geo-navy, #1a1a2e); font-size: 13px; font-weight: 800; background: var(--geo-gold-light, #fff5c2); border: 2px solid var(--geo-navy, #1a1a2e); }
.meta-item:nth-child(2) { background: var(--geo-sky-light, #dff5ff); }
.meta-separator { display: none; }

.cover-frame { position: relative; z-index: 2; width: 100%; max-width: 470px; justify-self: end; }
.cover-image { aspect-ratio: 16 / 10; background-position: center; background-size: cover; border: 3px solid var(--geo-navy, #1a1a2e); box-shadow: 12px 12px 0 var(--geo-coral, #ff6b6b); }
.cover-index { position: absolute; right: -14px; bottom: -16px; padding: 8px 11px; color: #fff; font-size: 9px; font-weight: 950; letter-spacing: .15em; background: var(--geo-navy, #1a1a2e); border: 2px solid #fff; }
.hero-shape { position: absolute; z-index: 1; border: 3px solid var(--geo-navy, #1a1a2e); }
.hero-circle { top: 30px; right: 39%; width: 52px; height: 52px; background: var(--geo-sky, #53bde8); border-radius: 50%; }
.hero-triangle { right: 46%; bottom: 26px; width: 54px; height: 54px; background: var(--geo-gold, #ffd54f); clip-path: polygon(50% 0, 100% 100%, 0 100%); transform: rotate(-11deg); }

.main-wrapper { max-width: 1240px; margin: 0 auto; padding: 58px 28px 0; z-index: 2; }
.article-card { min-height: 500px; padding: 0; background: #fff; border: 3px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 10px 10px 0 var(--geo-coral, #ff6b6b); }
.article-card :deep(.el-card__body) { padding: clamp(26px, 5vw, 58px); }

.article-summary { position: relative; margin: 0 0 42px; padding: 24px 28px 24px 66px; color: var(--geo-navy, #1a1a2e); font-style: normal; background: var(--geo-gold-light, #fff5c2); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 6px 6px 0 var(--geo-sky, #53bde8); }
.article-summary .summary-mark { position: absolute; top: 6px; left: 18px; font-family: Georgia, serif; font-size: 58px; font-weight: 900; line-height: 1; opacity: .25; }
.article-summary p { margin: 0; font-size: 15px; font-weight: 750; line-height: 1.8; }

.article-card :deep(.el-divider) { margin: 48px 0 30px; border-color: var(--geo-navy, #1a1a2e); border-width: 2px 0 0; }
.article-footer { margin: 0; padding: 0; justify-content: space-between; gap: 20px; }
.footer-label { display: flex; flex-direction: column; }
.footer-label small { margin-bottom: 5px; color: var(--geo-coral-dark, #d9485f); font-size: 9px; font-weight: 950; letter-spacing: .15em; }
.footer-label strong { font-size: 18px; }
.article-footer :deep(.like-btn) { min-height: 42px; padding: 0 16px; color: var(--geo-navy, #1a1a2e); font-weight: 900; background: var(--geo-gold, #ffd54f); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 4px 4px 0 var(--geo-navy, #1a1a2e); }
.article-footer :deep(.like-btn:hover) { background: var(--geo-coral, #ff6b6b); transform: translate(-2px, -2px); }

.sidebar-wrapper { position: relative; }
.sidebar-card { overflow: visible; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 7px 7px 0 var(--geo-navy, #1a1a2e); }
.sidebar-card :deep(.el-card__body) { padding: 24px; }
.author-card { padding: 4px; }
.sidebar-kicker { display: block; margin-bottom: 18px; color: var(--geo-coral-dark, #d9485f); font-size: 9px; font-weight: 950; letter-spacing: .15em; text-align: left; }
.author-card :deep(.el-avatar) { color: var(--geo-navy, #1a1a2e); background: var(--geo-sky-light, #dff5ff); border: 3px solid var(--geo-navy, #1a1a2e); box-shadow: 5px 5px 0 var(--geo-gold, #ffd54f); }
.author-card h3 { margin: 17px 0 0; font-size: 21px; font-weight: 950; }
.author-card .desc { min-height: 40px; margin: 10px 0 18px; color: var(--color-text-muted, #777); line-height: 1.65; }
.author-card :deep(.el-button) { height: 42px; margin: 0; color: var(--geo-navy, #1a1a2e); font-weight: 900; background: var(--geo-gold, #ffd54f); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 4px 4px 0 var(--geo-navy, #1a1a2e); }
.author-card :deep(.el-button:hover) { background: var(--geo-coral, #ff6b6b); }
.author-card :deep(.el-button span span) { margin-left: auto; font-size: 18px; }

.toc-card :deep(.el-card__header) { padding: 18px 20px; background: var(--geo-gold-light, #fff5c2); border-bottom: 2px solid var(--geo-navy, #1a1a2e); }
.sidebar-heading { display: flex; align-items: center; gap: 12px; }
.sidebar-heading b { display: grid; width: 34px; height: 34px; place-items: center; color: #fff; font-size: 12px; background: var(--geo-navy, #1a1a2e); box-shadow: 4px 4px 0 var(--geo-sky, #53bde8); }
.sidebar-heading span { font-size: 16px; font-weight: 950; line-height: 1; }
.sidebar-heading small { display: block; margin-bottom: 4px; color: var(--geo-coral-dark, #d9485f); font-size: 8px; letter-spacing: .12em; }
.sticky-card { top: 88px; }
.toc-list .toc-item { padding: 9px 10px; color: var(--color-text-secondary, #555568); font-weight: 700; border-left: 3px solid transparent; border-radius: 0; }
.toc-list .toc-item:hover { color: var(--geo-navy, #1a1a2e); background: var(--geo-sky-light, #dff5ff); }
.toc-list .toc-item.active { color: var(--geo-navy, #1a1a2e); font-weight: 900; background: var(--geo-gold, #ffd54f); border-left-color: var(--geo-navy, #1a1a2e); }

:deep(.typo-content) {
  color: var(--color-text-primary, #28283a);
  font-family: inherit;
  font-size: 16px;
  line-height: 1.9;

  h1, h2, h3, h4 { position: relative; margin: 1.8em 0 .8em; color: var(--geo-navy, #1a1a2e); font-weight: 950; letter-spacing: -.035em; scroll-margin-top: 92px; }
  h1 { padding: 0 0 12px 18px; font-size: 2em; border-bottom: 3px solid var(--geo-navy, #1a1a2e); }
  h1::before { position: absolute; bottom: -3px; left: 0; width: 72px; height: 8px; content: ''; background: var(--geo-coral, #ff6b6b); }
  h2 { padding: 8px 13px; font-size: 1.55em; background: var(--geo-gold-light, #fff5c2); border: 2px solid var(--geo-navy, #1a1a2e); }
  h3 { padding-left: 14px; font-size: 1.3em; border-left: 6px solid var(--geo-sky, #53bde8); }
  p { margin: 0 0 1.45em; text-align: left; }
  a { color: var(--geo-coral-dark, #d9485f); font-weight: 750; text-decoration-thickness: 2px; }
  ul, ol { margin-bottom: 1.5em; padding-left: 1.8em; }
  li { margin-bottom: .55em; }
  li::marker { color: var(--geo-coral-dark, #d9485f); font-weight: 900; }
  img { max-width: 100%; height: auto; margin: 1.8em auto; border: 3px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 8px 8px 0 var(--geo-sky, #53bde8); }
  pre { margin: 1.8em 0; padding: 20px; background: var(--geo-navy, #1a1a2e); border: 3px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 7px 7px 0 var(--geo-coral, #ff6b6b); }
  p code, li code { padding: .2em .45em; color: var(--geo-coral-dark, #d9485f); font-weight: 750; background: var(--geo-coral-light, #ffe4e8); border: 1px solid var(--geo-coral, #ff6b6b); border-radius: 0; }
  blockquote { margin: 1.8em 0; padding: 18px 22px; color: var(--geo-navy, #1a1a2e); font-weight: 700; background: var(--geo-sky-light, #dff5ff); border: 2px solid var(--geo-navy, #1a1a2e); border-left: 8px solid var(--geo-sky, #53bde8); border-radius: 0; }
  table { border: 2px solid var(--geo-navy, #1a1a2e); }
  th, td { padding: 9px 12px; border: 1px solid var(--geo-navy, #1a1a2e); }
  th { background: var(--geo-gold, #ffd54f); }
  tr:nth-child(2n) { background: var(--geo-sky-light, #dff5ff); }
  hr { height: 3px; margin: 40px 0; background: var(--geo-navy, #1a1a2e); border: 0; }
}

@media (max-width: 991px) {
  .blog-header-banner { min-height: 0; padding: 54px 28px 64px; grid-template-columns: 1fr; gap: 44px; }
  .cover-frame { max-width: 680px; justify-self: start; }
  .hero-shape { display: none; }
  .main-wrapper { padding-top: 42px; }
}

@media (max-width: 640px) {
  .blog-header-banner { padding: 42px 20px 52px; }
  .article-title { font-size: 38px; }
  .article-meta { align-items: stretch; flex-direction: column; }
  .meta-item { width: fit-content; }
  .main-wrapper { padding: 30px 14px 0; }
  .article-card { border-right: 0; border-left: 0; box-shadow: none; }
  .article-card :deep(.el-card__body) { padding: 28px 20px; }
  .article-summary { padding: 54px 18px 18px; }
  .article-summary .summary-mark { top: 7px; left: 15px; }
  .article-footer { align-items: flex-start; flex-direction: column; }
}
</style>
