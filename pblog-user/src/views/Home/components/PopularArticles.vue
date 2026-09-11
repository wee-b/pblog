<!-- components/PopularArticles.vue -->
<template>
  <section class="popular-articles section">
    <div class="section-header">
      <h2 class="section-title">热门文章</h2>
      <p class="section-subtitle">最受欢迎的技术文章</p>
    </div>

    <div class="articles-container">
      <ArticlesItem
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @click="handleArticleClick(article.id)"
      />
    </div>
  </section>
</template>

<script setup>
import ArticlesItem from './ArticlesItem.vue'
import {getCollectionArticles } from '@/apis/article/article.js'
import {nextTick, onMounted, ref} from "vue";
import {ElMessage} from "element-plus";

const articles = ref([
  {
    "id": 1,
    "title": "推荐文章加载失败",
    "summary": "加载失败，加载失败，加载失败，加载失败，加载失败，加载失败，加载失败，加载失败",
    "coverImage": "https://pblog-cover.oss-cn-hangzhou.aliyuncs.com/vue3-vite.jpg",
    "authorId": 123445,
    "viewCount": 2896,
    "likeCount": 156,
    "commentCount": 89,
    "sticky": "1",
    "featured": "1",
    "publishedAt": "2025-03-15T10:00:00",
    "categories": [
      {
        "id": 1,
        "categoryName": "加载",
        "parentId": 0,
        "orderNum": 1,
        "articleCount": null,
        "description": null
      },
      {
        "id": 3,
        "categoryName": "失败",
        "parentId": 0,
        "orderNum": 3,
        "articleCount": null,
        "description": null
      }
    ]
  },
])

const loadFeaturedArticles = async ()=>{
  try {
    let res = await getCollectionArticles();
    res = res.data.data
    if(Array.isArray(res)){
      articles.value = res
    }else{
      ElMessage.warning('文章数据格式异常')
    }
  }catch (error) {
    ElMessage.error('获取文章信息失败，请稍后重试')
  }
}

onMounted(async () => {
  await Promise.all([loadFeaturedArticles()])
})

const handleArticleClick = (id) => {
  console.log('点击了文章:', id)
}
</script>

<style scoped>
.section {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

/* Geometric decorative element */
.section::before {
  content: '';
  position: absolute;
  top: 40px;
  right: 5%;
  width: 0;
  height: 0;
  border-left: 40px solid transparent;
  border-right: 60px solid transparent;
  border-bottom: 80px solid var(--geo-gold-light);
  opacity: 0.3;
  pointer-events: none;
  z-index: 0;
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
  letter-spacing: -0.01em;
}

/* Geometric underline for section title */
.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  margin: 12px auto 0;
  background: linear-gradient(90deg, var(--geo-coral), var(--geo-gold));
  border-radius: 2px;
}

.section-subtitle {
  font-size: 1.1rem;
  color: var(--color-text-muted);
}

.articles-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .section {
    padding: 60px 20px;
  }
  .section-title {
    font-size: 1.8rem;
  }
}
</style>
