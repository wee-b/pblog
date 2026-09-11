<template>
  <el-card class="article-card" shadow="hover" :body-style="{ padding: '0px', display: 'flex', flexDirection: 'column', height: '100%' }">
    <!-- Geometric accent stripe -->
    <div class="card-accent-stripe"></div>

    <!-- 头部区域：标题 + Meta -->
    <div class="card-header">
      <h3 class="article-title" :title="article.title">
          {{ article.title }}
      </h3>
      <div class="article-meta">
        <span class="meta-item"><el-icon><User /></el-icon> {{ article.authorNickName || '未知作者' }}</span>
        <span class="meta-item"><el-icon><Clock /></el-icon> {{ formatDate(article.publishedAt) }}</span>
      </div>
    </div>

    <!-- 中间内容区域：摘要 + 标签 -->
    <div class="card-body">
      <p class="article-excerpt">
        {{ article.summary || '暂无摘要内容...' }}
      </p>

      <div class="tags-container" v-if="article.categories && article.categories.length">
        <el-tag
            v-for="tag in article.categories"
            :key="tag.id"
            type="success"
            size="small"
            effect="plain"
        >
          {{ tag.categoryName }}
        </el-tag>
      </div>
    </div>

    <!-- 底部数据区域 -->
    <div class="card-footer">
      <div class="stats-container">
        <span><el-icon><View /></el-icon> {{ formatNumber(article.viewCount) }}</span>
        <span><el-icon><Star /></el-icon> {{ formatNumber(article.likeCount) }}</span>
        <span><el-icon><ChatDotRound /></el-icon> {{ formatNumber(article.commentCount) }}</span>
      </div>
    </div>

    <!-- 全局点击覆盖层 -->
    <div class="card-clickable-area" @click="handleCardClick"></div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, Clock, View, Star, ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  article: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const router = useRouter()

const handleCardClick = () => {
  if (props.article.id) {
    router.push(`/blog/${props.article.id}`)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date)
}

const formatNumber = (num) => {
  if (!num) return 0
  return num > 999 ? (num / 1000).toFixed(1) + 'k' : num
}
</script>

<style scoped>
.article-card {
  height: 100%;
  border: none;
  background-color: var(--color-bg-card);
  border-radius: var(--radius-lg);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-sm);
}

/* Geometric accent stripe at top */
.card-accent-stripe {
  height: 4px;
  width: 100%;
  background: linear-gradient(
    90deg,
    var(--geo-coral) 0%,
    var(--geo-gold) 50%,
    var(--geo-sky) 100%
  );
  flex-shrink: 0;
}

.article-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

/* Geometric corner decoration on hover */
.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 60px 60px 0;
  border-color: transparent var(--geo-coral-light) transparent transparent;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 0;
}

.article-card:hover::before {
  opacity: 0.4;
}

/* --- Header --- */
.card-header {
  padding: 20px 20px 10px 20px;
  position: relative;
  z-index: 1;
}

.article-title {
  margin: 0 0 10px 0;
  font-size: 1.15rem;
  line-height: 1.4;
  height: 2.8em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--color-text-primary);
  font-weight: 700;
}

.article-meta {
  display: flex;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* --- Body --- */
.card-body {
  padding: 0 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.article-excerpt {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags-container {
  margin-top: auto;
  margin-bottom: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* --- Footer --- */
.card-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border);
  background-color: var(--color-bg-soft);
  position: relative;
  z-index: 1;
}

.stats-container {
  display: flex;
  justify-content: space-between;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.stats-container span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* --- Clickable area --- */
.card-clickable-area {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
  z-index: 2;
}
</style>
