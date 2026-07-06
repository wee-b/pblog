<template>
  <section class="featured-collections section">
    <div class="section-header">
      <h2 class="section-title">热门合集</h2>
      <p class="section-subtitle">精心整理的技术专题</p>
    </div>

    <div class="collections-container">
      <div
          v-for="collection in collections"
          :key="collection.id"
          class="collection-card"
          :style="{ backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url(${collection.image})` }"
      >
        <div class="collection-content">
          <h3>{{ collection.title }}</h3>
          <p class="collection-desc">{{ collection.description }}</p>
          <div class="collection-stats">
            <span><el-icon><Document /></el-icon> {{ collection.articles }} 篇文章</span>
            <span><el-icon><Star /></el-icon> {{ collection.followers }} 人关注</span>
          </div>
          <el-button type="success" plain round class="view-button">查看合集</el-button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { Document, Star } from '@element-plus/icons-vue'

const collections = [
  {
    id: 1,
    title: 'Vue3 全家桶',
    description: '从基础到高级，全面掌握 Vue3 生态系统',
    articles: 24,
    followers: 156,
    image: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
  },
  {
    id: 2,
    title: '前端性能优化',
    description: '提升用户体验的关键技巧',
    articles: 18,
    followers: 98,
    image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
  },
  {
    id: 3,
    title: 'TypeScript 进阶',
    description: '深入理解 TypeScript 高级特性',
    articles: 15,
    followers: 112,
    image: 'https://images.unsplash.com/photo-1633356122102-3fe601e05bd2?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
  },
  {
    id: 4,
    title: 'CSS 魔法',
    description: '现代 CSS 技巧与创意效果',
    articles: 21,
    followers: 134,
    image: 'https://images.unsplash.com/photo-1626785774573-4b799315345d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
  }
]
</script>

<style scoped>
.featured-collections {
  background-color: #f5f9f4;
  padding: 80px 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.section-title {
  font-size: 2.2rem;
  color: #4a6b57;
  margin-bottom: 15px;
}

.section-subtitle {
  font-size: 1.1rem;
  color: #7f9c8d;
}

.collections-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.collection-card {
  height: 350px;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  align-items: flex-end;
  padding: 25px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.collection-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.collection-content {
  width: 100%;
  z-index: 2;
}

.collection-content h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  font-weight: 600;
}

.collection-desc {
  font-size: 0.95rem;
  margin-bottom: 20px;
  line-height: 1.5;
  opacity: 0.9;
}

.collection-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 0.85rem;
  opacity: 0.8;
}

.collection-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.view-button {
  width: 100%;
}

@media (max-width: 768px) {
  .featured-collections {
    padding: 60px 15px;
  }

  .section-title {
    font-size: 1.8rem;
  }

  .collections-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .collection-card {
    height: 300px;
  }
}
</style>