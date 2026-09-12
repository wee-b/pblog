<template>
  <main v-loading="loading" class="series-detail">
    <template v-if="series">
      <section class="series-header">
        <button class="back-button" type="button" @click="router.push('/collection')">← 全部合集</button>
        <div class="series-heading">
          <div class="series-art" :class="{ 'has-cover': series.coverImage }">
            <img v-if="series.coverImage" :src="series.coverImage" :alt="series.seriesName">
            <template v-else><span></span><span></span><span></span></template>
          </div>
          <div class="series-copy">
            <p class="eyebrow">READING PATH / {{ series.id }}</p>
            <h1>{{ series.seriesName }}</h1>
            <p class="description">{{ series.description || '这个合集正在持续整理中。' }}</p>
            <div class="series-meta">
              <span><strong>{{ series.articleCount || 0 }}</strong> 篇文章</span>
              <span>创建于 {{ formatDate(series.createTime) }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="article-section">
        <div class="section-title"><span>02</span><div><h2>阅读目录</h2><p>建议按照顺序阅读</p></div></div>
        <div v-if="series.articles?.length" class="article-list">
          <article v-for="(article, index) in series.articles" :key="article.id" class="article-row" @click="openArticle(article.id)">
            <span class="article-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <div class="article-copy">
              <div class="article-labels">
                <span v-for="category in article.categories?.slice(0, 3)" :key="category.id">{{ category.categoryName }}</span>
              </div>
              <h3>{{ article.title }}</h3>
              <p>{{ article.summary || '暂无摘要' }}</p>
              <div class="article-meta">
                <span>{{ formatDate(article.publishedAt) }}</span>
                <span>{{ article.viewCount || 0 }} 阅读</span>
                <span>{{ article.likeCount || 0 }} 喜欢</span>
              </div>
            </div>
            <span class="article-arrow">→</span>
          </article>
        </div>
        <div v-else class="empty-series"><span></span><h3>这个合集还没有文章</h3><p>作者正在整理内容，请稍后再来。</p></div>
      </section>
    </template>
    <section v-else-if="!loading" class="not-found">
      <span></span><h1>合集不存在</h1><button type="button" @click="router.push('/collection')">返回全部合集</button>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeriesDetail } from '@/apis/series.js'

const route = useRoute()
const router = useRouter()
const series = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const response = await getSeriesDetail(route.params.id)
    series.value = response.data?.data || response.data || null
  } catch (error) {
    console.error('加载合集详情失败', error)
    series.value = null
    if (error.businessCode !== 400) ElMessage.error('合集详情加载失败')
  } finally { loading.value = false }
}

const openArticle = (id) => router.push(`/blog/${id}`)
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value)) : '未发布'
watch(() => route.params.id, fetchDetail)
onMounted(fetchDetail)
</script>

<style scoped>
.series-detail { min-height: calc(100vh - 68px); color: var(--geo-navy); background: #fff; }
.series-header { padding: 42px clamp(22px,7vw,110px) 72px; background-image: linear-gradient(rgba(26,26,46,.035) 1px,transparent 1px),linear-gradient(90deg,rgba(26,26,46,.035) 1px,transparent 1px); background-size: 48px 48px; border-bottom: 2px solid var(--geo-navy); }
.back-button,.not-found button { min-height: 40px; padding: 0 14px; color: var(--geo-navy); font-weight: 800; background: #fff; border: 2px solid var(--geo-navy); box-shadow: 4px 4px 0 var(--geo-coral); cursor: pointer; }
.series-heading { display: grid; grid-template-columns: minmax(280px,420px) 1fr; max-width: 1180px; margin: 50px auto 0; gap: clamp(42px,7vw,100px); align-items: center; }
.series-art { position: relative; height: 300px; overflow: hidden; background: var(--geo-gold-light); border: 3px solid var(--geo-navy); box-shadow: 12px 12px 0 var(--geo-navy); }
.series-art img { width: 100%; height: 100%; object-fit: cover; }.series-art span { position: absolute; display: block; border: 3px solid var(--geo-navy); }
.series-art span:nth-child(1) { top: 48px; left: 42px; width: 110px; height: 110px; background: var(--geo-coral); transform: rotate(14deg); }.series-art span:nth-child(2) { right: 36px; bottom: 34px; width: 94px; height: 94px; background: var(--geo-sky); border-radius: 50%; }.series-art span:nth-child(3) { top: 34px; right: 54px; width: 60px; height: 60px; background: var(--geo-navy); clip-path: polygon(50% 0,100% 100%,0 100%); }
.eyebrow { margin: 0 0 16px; color: var(--geo-coral-dark); font-size: 11px; font-weight: 900; letter-spacing: .15em; }.series-copy h1 { margin: 0; font-size: clamp(42px,5vw,70px); line-height: 1; letter-spacing: -.06em; }.description { max-width: 620px; margin: 22px 0; color: var(--color-text-secondary); font-size: 16px; line-height: 1.8; }.series-meta { display: flex; gap: 20px; font-size: 13px; font-weight: 700; }.series-meta span { padding: 8px 10px; background: #fff; border: 2px solid var(--geo-navy); }.series-meta span:first-child { background: var(--geo-gold); box-shadow: 3px 3px 0 var(--geo-navy); }
.article-section { max-width: 1050px; margin: 0 auto; padding: 70px 28px 100px; }.section-title { display: flex; align-items: center; margin-bottom: 36px; gap: 14px; }.section-title > span { padding: 8px 10px; color: #fff; font-weight: 900; background: var(--geo-navy); }.section-title h2 { margin: 0; font-size: 30px; }.section-title p { margin: 3px 0 0; color: var(--color-text-muted); font-size: 13px; }
.article-list { display: grid; gap: 18px; }.article-row { display: grid; grid-template-columns: 72px 1fr 42px; padding: 24px; gap: 22px; align-items: center; background: #fff; border: 2px solid var(--geo-navy); box-shadow: 6px 6px 0 var(--geo-navy); cursor: pointer; transition: transform .18s ease,box-shadow .18s ease; }.article-row:hover { transform: translate(-4px,-4px); box-shadow: 10px 10px 0 var(--geo-navy); }.article-index { display: grid; width: 58px; height: 58px; place-items: center; font-size: 20px; font-weight: 900; background: var(--geo-gold); border: 2px solid var(--geo-navy); }.article-labels { display: flex; gap: 6px; }.article-labels span { padding: 3px 7px; color: var(--geo-navy); font-size: 10px; font-weight: 800; background: var(--geo-sky-light); border: 1px solid var(--geo-navy); }.article-copy h3 { margin: 8px 0; font-size: 21px; }.article-copy > p { margin: 0; color: var(--color-text-secondary); font-size: 13px; line-height: 1.6; }.article-meta { display: flex; margin-top: 12px; gap: 16px; color: var(--color-text-muted); font-size: 11px; }.article-arrow { font-size: 28px; font-weight: 900; }
.empty-series,.not-found { padding: 80px 20px; text-align: center; border: 2px dashed var(--geo-navy); }.empty-series > span,.not-found > span { display: block; width: 58px; height: 58px; margin: 0 auto 22px; background: var(--geo-sky); border: 2px solid var(--geo-navy); box-shadow: 6px 6px 0 var(--geo-navy); transform: rotate(9deg); }.empty-series h3,.not-found h1 { margin: 0 0 8px; }.empty-series p { color: var(--color-text-muted); }.not-found { max-width: 640px; margin: 90px auto; }.not-found button { margin-top: 20px; }
@media (max-width:760px) { .series-heading { grid-template-columns: 1fr; margin-top: 36px; gap: 42px; }.series-art { height: 230px; }.series-header { padding-bottom: 54px; }.series-meta { align-items: flex-start; flex-direction: column; gap: 10px; }.article-section { padding: 52px 16px 80px; }.article-row { grid-template-columns: 48px 1fr; padding: 18px; gap: 14px; }.article-index { width: 44px; height: 44px; font-size: 16px; }.article-arrow { display: none; }.article-copy h3 { font-size: 18px; }.article-meta { flex-wrap: wrap; } }
</style>
