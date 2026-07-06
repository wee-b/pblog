<template>
  <div class="blog-detail-container" v-loading="loading">

    <!-- 顶部大图/封面 -->
    <div class="blog-header-banner" :style="{ backgroundImage: `url(${article.coverImage || defaultCover})` }">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
<!--          <div class="meta-item author-info">-->
<!--            <el-avatar :size="32" :src="defaultAvatar" />-->
<!--            <span class="author-name">用户 {{ article.authorId || '未知' }}</span>-->
<!--          </div>-->
          <span class="meta-separator">·</span>
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(article.publishedAt) }}</span>
          </div>
          <span class="meta-separator">·</span>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ article.viewCount || 0 }} 阅读</span>
          </div>
        </div>
      </div>
    </div>

    <div class="main-wrapper">
      <el-row :gutter="40">
        <!-- 左侧：文章内容 + 评论区 -->
        <el-col :xs="24" :sm="24" :md="18" :lg="18" :xl="18">
          <el-card class="article-card" shadow="never">
            <!-- 简介 -->
            <div class="article-summary" v-if="article.summary">
              <el-icon><Quote /></el-icon>
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
                <el-avatar :size="64" :src="authorInfo.avatarUrl" />
                <h3 class="mt-2">{{authorInfo.nickname}}</h3>
                <p class="desc">{{ authorInfo.bio || '暂无简介'}}</p>
                <el-button type="primary" class="w-100 mt-3" @click="ElMessage.warning('功能未开放')"  round>关注作者</el-button>
              </div>
            </el-card>


            <!-- 目录 (动态生成 + 高亮) -->
            <el-card class="sidebar-card mt-4 sticky-card" shadow="hover" header="目录">
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
/* 页面基础布局 */
.blog-detail-container { min-height: 100vh; background-color: #f5f7fa; padding-bottom: 60px; }
.blog-header-banner { height: 400px; background-size: cover; background-position: center; position: relative; display: flex; align-items: center; justify-content: center; color: #fff; text-align: center; margin-bottom: -60px; }
.banner-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); }
.banner-content { position: relative; z-index: 2; max-width: 800px; padding: 0 20px; }
.article-title { font-size: 2.5rem; font-weight: 700; margin-bottom: 20px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); }
.article-meta { display: flex; justify-content: center; align-items: center; gap: 15px; opacity: 0.95; }
.meta-item { display: flex; align-items: center; gap: 6px; }

.main-wrapper { max-width: 1200px; margin: 0 auto; padding: 0 20px; position: relative; z-index: 10; }
.article-card { border-radius: 12px; padding: 20px; min-height: 500px; }
.article-summary { background-color: #f8f9fa; border-left: 4px solid var(--el-color-primary); padding: 15px 20px; margin-bottom: 30px; color: #666; font-style: italic; }
.article-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 30px; padding-top: 20px; }
.action-buttons { display: flex; align-items: center; .action-count { margin-left: 8px; color: #666; font-weight: bold; } }

/* 侧边栏与目录 */
.sidebar-wrapper { position: relative; height: 100%; }
.sidebar-card { border-radius: 8px; border: none; }
.author-card { text-align: center; padding: 10px; .desc { color: #888; font-size: 13px; margin: 10px 0; } }
.sticky-card { position: sticky; top: 80px; }

.toc-list {
  max-height: 70vh;
  overflow-y: auto;
  .toc-item {
    padding: 8px 12px;
    font-size: 14px;
    color: #606266;
    cursor: pointer;
    border-radius: 4px;
    border-left: 2px solid transparent;
    transition: all 0.2s;

    &:hover { background-color: #f0f9eb; color: var(--el-color-primary); }
    &.active {
      color: var(--el-color-primary);
      font-weight: bold;
      background-color: #f0f9eb;
      border-left-color: var(--el-color-primary);
    }
  }
}
.pl-3 { padding-left: 24px !important; }
.pl-4 { padding-left: 36px !important; }
.toc-empty { color: #999; font-size: 13px; text-align: center; padding: 10px 0; }
.mt-2 { margin-top: 8px; }
.mt-3 { margin-top: 12px; }
.mt-4 { margin-top: 16px; }
.w-100 { width: 100%; }

/* --- 核心：Markdown 排版样式 --- */
:deep(.typo-content) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  word-wrap: break-word;

  h1, h2, h3, h4 { margin-top: 1.2em; scroll-margin-top: 80px; font-weight: 700; color: #1a1a1a; }
  h1 { font-size: 2em; padding-bottom: 0.3em; border-bottom: 1px solid #eaecef; }
  h2 { font-size: 1.6em; padding-bottom: 0.3em; border-bottom: 1px solid #eaecef; }
  h3 { font-size: 1.35em; }
  p { margin-bottom: 1.5em; text-align: justify; }
  ul, ol { padding-left: 2em; margin-bottom: 1.5em; }
  li { margin-bottom: 0.5em; }

  img { max-width: 100%; height: auto; border-radius: 8px; display: block; margin: 1em auto; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }

  pre {
    background-color: #282c34;
    color: #abb2bf;
    padding: 1em;
    border-radius: 6px;
    overflow-x: auto;
    font-family: Consolas, Monaco, monospace;
    margin-bottom: 1.5em;
  }

  code { font-family: Consolas, Monaco, monospace; }
  p code, li code { background-color: rgba(27,31,35,0.05); color: #d63200; padding: 0.2em 0.4em; border-radius: 3px; font-size: 85%; }

  blockquote { margin: 1.5em 0; padding: 0.8em 1.5em; color: #6a737d; border-left: 4px solid #dfe2e5; background-color: #f9f9f9; }

  table { display: block; width: 100%; overflow: auto; margin-bottom: 1.5em; border-spacing: 0; border-collapse: collapse; }
  th, td { padding: 6px 13px; border: 1px solid #dfe2e5; }
  tr:nth-child(2n) { background-color: #f6f8fa; }
}
</style>