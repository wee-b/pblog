<template>
  <el-card class="article-card" shadow="hover" :body-style="{ padding: '0px', display: 'flex', flexDirection: 'column', height: '100%' }">
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

    <!-- 全局点击覆盖层 (通过 CSS absolute 定位) -->
    <!-- 注意：如果你希望用户能复制文字，建议移除此遮罩，只依靠 @click 事件 -->
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

// 点击卡片跳转
const handleCardClick = () => {
  if (props.article.id) {
    router.push(`/blog/${props.article.id}`)
  }
}

// 格式化日期 (原生实现，无需依赖库)
const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date)
}

// 简单的数字格式化 (超过1000显示k)
const formatNumber = (num) => {
  if (!num) return 0
  return num > 999 ? (num / 1000).toFixed(1) + 'k' : num
}
</script>

<style scoped>
.article-card {
  height: 100%; /* 关键：撑满父容器高度 */
  border: none;
  background-color: rgba(208,240,203,0.93);
  border-radius: 8px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

/* --- 头部 --- */
.card-header {
  padding: 20px 20px 10px 20px;
}

.article-title {
  margin: 0 0 10px 0;
  font-size: 1.15rem;
  line-height: 1.4;
  height: 2.8em; /* 固定高度：约等于2行的高度 */

  /* 多行省略号核心代码 */
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 限制显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.title-link {
  color: #2c3e50;
  text-decoration: none;
  transition: color 0.2s;
  position: relative;
  z-index: 2; /* 保证标题链接在遮罩层之上可点击 */
}

.title-link:hover {
  color: #42b983;
}

.article-meta {
  display: flex;
  font-size: 0.85rem;
  color: #999;
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* --- 中间内容 (自适应填充) --- */
.card-body {
  padding: 0 20px;
  flex: 1; /* 关键：占据剩余所有空间，将 footer 挤到底部 */
  display: flex;
  flex-direction: column;
}

.article-excerpt {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 15px 0;

  /* 摘要限制3行 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags-container {
  margin-top: auto; /* 如果摘要很短，标签也会被挤到底部 */
  margin-bottom: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* --- 底部 --- */
.card-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.stats-container {
  display: flex;
  justify-content: space-between; /* 或 flex-start + gap */
  color: #999;
  font-size: 0.85rem;
}

.stats-container span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* --- 点击交互 --- */
.card-clickable-area {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
  z-index: 1; /* 位于底层，但在内容之上 */
}
</style>