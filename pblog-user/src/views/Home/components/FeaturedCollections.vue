<template>
  <section class="featured-collections section">
    <div class="section-header">
      <h2 class="section-title">热门合集</h2>
      <p class="section-subtitle">精心整理的技术专题</p>
    </div>

    <div class="collections-container">
      <div
          v-for="(collection, idx) in collections"
          :key="collection.id"
          class="collection-card"
          :class="`collection-card--${idx % 4}`"
      >
        <div class="collection-geo-bg"></div>
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
  background-color: var(--color-bg-soft);
  padding: 80px 20px;
  position: relative;
  overflow: hidden;
}

/* Geometric background decoration */
.featured-collections::before {
  content: '';
  position: absolute;
  top: -100px;
  left: -60px;
  width: 0;
  height: 0;
  border-left: 200px solid transparent;
  border-right: 150px solid transparent;
  border-bottom: 280px solid var(--geo-coral-light);
  opacity: 0.12;
  pointer-events: none;
}

.featured-collections::after {
  content: '';
  position: absolute;
  bottom: -80px;
  right: -40px;
  width: 0;
  height: 0;
  border-left: 180px solid transparent;
  border-right: 120px solid transparent;
  border-bottom: 240px solid var(--geo-sky-light);
  opacity: 0.12;
  pointer-events: none;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 2.2rem;
  color: var(--color-text-primary);
  margin-bottom: 15px;
  font-weight: 800;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  margin: 12px auto 0;
  background: linear-gradient(90deg, var(--geo-sky), var(--geo-gold));
  border-radius: 2px;
}

.section-subtitle {
  font-size: 1.1rem;
  color: var(--color-text-muted);
}

.collections-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.collection-card {
  height: 350px;
  border-radius: var(--radius-lg);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  align-items: flex-end;
  padding: 25px;
  color: white;
  box-shadow: var(--shadow-md);
}

.collection-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-lg);
}

/* Geometric backgrounds for each card — no images needed */
.collection-geo-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

/* Card 0: Coral + Navy */
.collection-card--0 .collection-geo-bg {
  background: linear-gradient(135deg, var(--geo-coral) 0%, var(--geo-navy) 100%);
}
.collection-card--0::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-left: 120px solid transparent;
  border-bottom: 160px solid rgba(255, 255, 255, 0.1);
  z-index: 1;
}

/* Card 1: Sky + Navy */
.collection-card--1 .collection-geo-bg {
  background: linear-gradient(135deg, var(--geo-sky) 0%, var(--geo-navy) 100%);
}
.collection-card--1::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 0;
  border-right: 100px solid transparent;
  border-top: 140px solid rgba(255, 255, 255, 0.08);
  z-index: 1;
}

/* Card 2: Gold + Navy */
.collection-card--2 .collection-geo-bg {
  background: linear-gradient(135deg, var(--geo-gold) 0%, var(--geo-navy-soft) 100%);
}
.collection-card--2::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  z-index: 1;
}

/* Card 3: Coral + Sky */
.collection-card--3 .collection-geo-bg {
  background: linear-gradient(135deg, var(--geo-coral-dark) 0%, var(--geo-sky-dark) 100%);
}
.collection-card--3::before {
  content: '';
  position: absolute;
  bottom: 30px;
  right: 30px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  z-index: 1;
}

.collection-content {
  width: 100%;
  z-index: 2;
}

.collection-content h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  font-weight: 700;
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
