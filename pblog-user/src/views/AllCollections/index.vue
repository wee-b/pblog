<template>
  <main class="collections-page">
    <section class="collections-hero">
      <div class="hero-copy">
        <p class="eyebrow"><span></span> CURATED SERIES</p>
        <h1>把零散知识<br><em>整理成路径</em></h1>
        <p>按照主题连续阅读，从一篇文章走进一整套知识体系。</p>
      </div>
      <div class="hero-shapes" aria-hidden="true">
        <span class="shape-square"></span><span class="shape-circle"></span><span class="shape-triangle"></span>
      </div>
    </section>

    <section class="collections-content">
      <div class="collection-toolbar">
        <div class="toolbar-title">
          <span>01</span><div><h2>全部合集</h2><p>共整理 {{ total }} 个专题</p></div>
        </div>
        <div class="search-box">
          <el-input v-model="keyword" placeholder="搜索合集名称或简介" clearable @keyup.enter="handleSearch" @clear="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <button type="button" @click="handleSearch">搜索</button>
        </div>
      </div>

      <div v-loading="loading" class="collection-grid">
        <article
          v-for="(series, index) in seriesList"
          :key="series.id"
          class="collection-card"
          :class="`tone-${index % 4}`"
          @click="openSeries(series.id)"
        >
          <div class="card-cover">
            <img v-if="series.coverImage" :src="series.coverImage" :alt="series.seriesName">
            <div v-else class="fallback-art" aria-hidden="true"><span></span><span></span><span></span></div>
            <span class="article-count">{{ series.articleCount || 0 }} 篇</span>
          </div>
          <div class="card-body">
            <span class="card-number">COLLECTION / {{ String(index + 1).padStart(2, '0') }}</span>
            <h3>{{ series.seriesName }}</h3>
            <p>{{ series.description || '这个合集正在持续整理中。' }}</p>
            <button type="button" class="view-button">查看合集 <span>→</span></button>
          </div>
        </article>
      </div>

      <div v-if="!loading && seriesList.length === 0" class="empty-state">
        <span></span><h3>还没有找到合集</h3><p>换一个关键词试试看。</p>
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="fetchSeries" />
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { pageQuerySeries } from '@/apis/series.js'

const router = useRouter()
const keyword = ref('')
const pageNum = ref(1)
const pageSize = 9
const total = ref(0)
const seriesList = ref([])
const loading = ref(false)

const fetchSeries = async () => {
  loading.value = true
  try {
    const response = await pageQuerySeries({ pageNum: pageNum.value, pageSize, keyword: keyword.value.trim() || null, sortDir: 'desc' })
    const data = response.data?.data || response.data || {}
    seriesList.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) {
    console.error('加载合集失败', error)
    seriesList.value = []
    total.value = 0
    ElMessage.error('合集加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; fetchSeries() }
const openSeries = (id) => router.push(`/collection/${id}`)
onMounted(fetchSeries)
</script>

<style scoped>
.collections-page { min-height: calc(100vh - 68px); color: var(--geo-navy); background: #fff; }
.collections-hero { position: relative; display: flex; min-height: 360px; padding: 72px clamp(24px, 8vw, 120px); overflow: hidden; align-items: center; box-sizing: border-box; background-image: linear-gradient(rgba(26,26,46,.035) 1px,transparent 1px),linear-gradient(90deg,rgba(26,26,46,.035) 1px,transparent 1px); background-size: 48px 48px; border-bottom: 2px solid var(--geo-navy); }
.hero-copy { position: relative; z-index: 2; }
.eyebrow { display: flex; align-items: center; gap: 10px; margin: 0 0 20px!important; font-size: 12px!important; font-weight: 900; letter-spacing: .15em; }
.eyebrow span { width: 30px; height: 7px; background: linear-gradient(90deg,var(--geo-coral) 0 60%,var(--geo-gold) 60%); }
.hero-copy h1 { margin: 0; font-size: clamp(44px,6vw,78px); line-height: .98; letter-spacing: -.06em; font-weight: 950; }
.hero-copy h1 em { color: transparent; font-style: normal; -webkit-text-stroke: 2px var(--geo-navy); }
.hero-copy > p:last-child { max-width: 520px; margin: 24px 0 0; color: var(--color-text-secondary); font-size: 16px; line-height: 1.8; }
.hero-shapes span { position: absolute; display: block; border: 3px solid var(--geo-navy); }
.shape-square { top: 72px; right: 18%; width: 92px; height: 92px; background: var(--geo-coral); box-shadow: 9px 9px 0 var(--geo-navy); transform: rotate(12deg); }
.shape-circle { right: 8%; bottom: 58px; width: 72px; height: 72px; background: var(--geo-sky); border-radius: 50%; }
.shape-triangle { right: 29%; bottom: 32px; width: 76px; height: 76px; background: var(--geo-gold); clip-path: polygon(50% 0,100% 100%,0 100%); transform: rotate(-8deg); }
.collections-content { max-width: 1240px; margin: 0 auto; padding: 64px 32px 88px; }
.collection-toolbar { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 38px; gap: 30px; }
.toolbar-title { display: flex; align-items: center; gap: 14px; }.toolbar-title > span { padding: 8px 10px; color: #fff; font-weight: 900; background: var(--geo-navy); }
.toolbar-title h2 { margin: 0; font-size: 30px; letter-spacing: -.04em; }.toolbar-title p { margin: 4px 0 0; color: var(--color-text-muted); font-size: 13px; }
.search-box { display: flex; width: min(100%,430px); }.search-box :deep(.el-input__wrapper) { min-height: 44px; border: 2px solid var(--geo-navy); border-radius: 0; box-shadow: none; }.search-box :deep(.el-input__wrapper.is-focus) { background: var(--geo-gold-light); }
.search-box button,.view-button { color: var(--geo-navy); font-weight: 850; background: var(--geo-gold); border: 2px solid var(--geo-navy); cursor: pointer; }.search-box button { min-width: 78px; margin-left: -2px; box-shadow: 4px 4px 0 var(--geo-navy); }
.collection-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); min-height: 180px; gap: 28px; }
.collection-card { overflow: hidden; background: #fff; border: 2px solid var(--geo-navy); box-shadow: 7px 7px 0 var(--geo-navy); cursor: pointer; transition: transform .2s ease,box-shadow .2s ease; }.collection-card:hover { transform: translate(-4px,-4px); box-shadow: 11px 11px 0 var(--geo-navy); }
.card-cover { position: relative; height: 190px; overflow: hidden; border-bottom: 2px solid var(--geo-navy); }.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.fallback-art { position: absolute; inset: 0; background: var(--geo-gold-light); }.tone-1 .fallback-art { background: var(--geo-coral-light); }.tone-2 .fallback-art { background: var(--geo-sky-light); }.tone-3 .fallback-art { background: #eeeef3; }
.fallback-art span { position: absolute; display: block; border: 2px solid var(--geo-navy); }.fallback-art span:nth-child(1) { top: 34px; left: 14%; width: 74px; height: 74px; background: var(--geo-coral); transform: rotate(17deg); }.fallback-art span:nth-child(2) { right: 14%; bottom: 25px; width: 66px; height: 66px; background: var(--geo-sky); border-radius: 50%; }.fallback-art span:nth-child(3) { top: 26px; right: 26%; width: 45px; height: 45px; background: var(--geo-navy); clip-path: polygon(50% 0,100% 100%,0 100%); }
.article-count { position: absolute; right: 10px; bottom: 10px; padding: 6px 10px; color: #fff; font-size: 12px; font-weight: 850; background: var(--geo-navy); border: 2px solid #fff; }
.card-body { padding: 22px; }.card-number { color: var(--geo-coral-dark); font-size: 10px; font-weight: 900; letter-spacing: .12em; }.card-body h3 { margin: 9px 0 10px; font-size: 23px; line-height: 1.2; }.card-body p { min-height: 48px; margin: 0 0 20px; color: var(--color-text-secondary); font-size: 14px; line-height: 1.7; }
.view-button { min-height: 40px; padding: 0 14px; background: #fff; box-shadow: 4px 4px 0 var(--geo-coral); }.view-button span { margin-left: 16px; font-size: 18px; }
.empty-state { padding: 80px 20px; text-align: center; border: 2px dashed var(--geo-navy); }.empty-state > span { display: block; width: 54px; height: 54px; margin: 0 auto 20px; background: var(--geo-gold); border: 2px solid var(--geo-navy); box-shadow: 6px 6px 0 var(--geo-navy); transform: rotate(8deg); }.empty-state h3 { margin: 0 0 8px; }.empty-state p { margin: 0; color: var(--color-text-muted); }
.pagination-wrap { display: flex; justify-content: center; margin-top: 54px; }.pagination-wrap :deep(.el-pager li),.pagination-wrap :deep(button) { border-radius: 0!important; border: 1px solid var(--geo-navy); }.pagination-wrap :deep(.el-pager li.is-active) { color: var(--geo-navy); background: var(--geo-gold); }
@media (max-width:900px) { .collection-grid { grid-template-columns: repeat(2,minmax(0,1fr)); }.shape-square { right: 10%; opacity: .6; }.shape-triangle { display: none; } }
@media (max-width:640px) { .collections-hero { min-height: 310px; padding: 56px 22px; }.hero-copy { max-width: 88%; }.hero-copy > p:last-child { font-size: 14px; }.shape-square { right: -40px; }.shape-circle { right: 5%; bottom: 24px; width: 48px; height: 48px; }.collections-content { padding: 46px 16px 70px; }.collection-toolbar { align-items: stretch; flex-direction: column; }.search-box { width: 100%; }.collection-grid { grid-template-columns: 1fr; } }
</style>
