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
  // 这里可以添加跳转逻辑
}




</script>

<style scoped>
.section {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
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

.articles-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
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