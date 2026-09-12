<template>
  <section v-if="loading || collections.length" class="featured-collections">
    <div class="section-header">
      <div><span class="section-index">02</span><div><h2>热门合集</h2><p>沿着主题，系统地读下去</p></div></div>
      <RouterLink to="/collection" class="all-link">查看全部 <span>→</span></RouterLink>
    </div>

    <div v-loading="loading" class="collections-grid">
      <article v-for="(collection, index) in collections" :key="collection.id" class="collection-card" :class="`tone-${index % 4}`" @click="openSeries(collection.id)">
        <div class="collection-art">
          <img v-if="collection.coverImage" :src="collection.coverImage" :alt="collection.seriesName">
          <template v-else><span></span><span></span><span></span></template>
        </div>
        <div class="collection-copy">
          <span class="collection-label">SERIES {{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ collection.seriesName }}</h3>
          <p>{{ collection.description || '这个合集正在持续整理中。' }}</p>
          <div class="collection-footer"><strong>{{ collection.articleCount || 0 }} 篇文章</strong><span>开始阅读 →</span></div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFeaturedSeries } from '@/apis/series.js'

const router = useRouter()
const collections = ref([])
const loading = ref(false)

const loadCollections = async () => {
  loading.value = true
  try {
    const response = await getFeaturedSeries(4)
    collections.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('热门合集加载失败', error)
    collections.value = []
  } finally { loading.value = false }
}

const openSeries = (id) => router.push(`/collection/${id}`)
onMounted(loadCollections)
</script>

<style scoped>
.featured-collections { position: relative; padding: 82px clamp(20px,7vw,100px) 94px; overflow: hidden; color: var(--geo-navy); background: var(--color-bg-soft); border-top: 2px solid var(--geo-navy); border-bottom: 2px solid var(--geo-navy); }
.featured-collections::before { position: absolute; top: -70px; right: 8%; width: 150px; height: 150px; content: ''; background: var(--geo-coral-light); border: 2px solid var(--geo-navy); transform: rotate(18deg); opacity: .45; }
.section-header { position: relative; z-index: 1; display: flex; max-width: 1200px; margin: 0 auto 38px; align-items: flex-end; justify-content: space-between; gap: 20px; }.section-header > div { display: flex; align-items: center; gap: 14px; }.section-index { padding: 8px 10px; color: #fff; font-weight: 900; background: var(--geo-navy); }.section-header h2 { margin: 0; font-size: 34px; letter-spacing: -.04em; }.section-header p { margin: 4px 0 0; color: var(--color-text-muted); font-size: 14px; }
.all-link { padding: 10px 14px; color: var(--geo-navy); font-size: 13px; font-weight: 850; text-decoration: none; background: #fff; border: 2px solid var(--geo-navy); box-shadow: 4px 4px 0 var(--geo-coral); }.all-link span { margin-left: 12px; }
.collections-grid { position: relative; z-index: 1; display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); max-width: 1200px; min-height: 160px; margin: 0 auto; gap: 20px; }
.collection-card { overflow: hidden; background: #fff; border: 2px solid var(--geo-navy); box-shadow: 6px 6px 0 var(--geo-navy); cursor: pointer; transition: transform .18s ease,box-shadow .18s ease; }.collection-card:hover { transform: translate(-4px,-4px); box-shadow: 10px 10px 0 var(--geo-navy); }
.collection-art { position: relative; height: 148px; overflow: hidden; background: var(--geo-gold-light); border-bottom: 2px solid var(--geo-navy); }.tone-1 .collection-art { background: var(--geo-coral-light); }.tone-2 .collection-art { background: var(--geo-sky-light); }.tone-3 .collection-art { background: #eeeef3; }.collection-art img { width: 100%; height: 100%; object-fit: cover; }.collection-art span { position: absolute; display: block; border: 2px solid var(--geo-navy); }.collection-art span:nth-child(1) { top: 26px; left: 13%; width: 58px; height: 58px; background: var(--geo-coral); transform: rotate(14deg); }.collection-art span:nth-child(2) { right: 12%; bottom: 18px; width: 54px; height: 54px; background: var(--geo-sky); border-radius: 50%; }.collection-art span:nth-child(3) { top: 19px; right: 25%; width: 38px; height: 38px; background: var(--geo-navy); clip-path: polygon(50% 0,100% 100%,0 100%); }
.collection-copy { padding: 20px; }.collection-label { color: var(--geo-coral-dark); font-size: 10px; font-weight: 900; letter-spacing: .13em; }.collection-copy h3 { margin: 8px 0; font-size: 20px; }.collection-copy > p { min-height: 65px; margin: 0; color: var(--color-text-secondary); font-size: 13px; line-height: 1.65; }.collection-footer { display: flex; margin-top: 18px; padding-top: 14px; justify-content: space-between; color: var(--color-text-secondary); font-size: 11px; border-top: 2px solid rgba(26,26,46,.1); }.collection-footer strong { color: var(--geo-navy); }
@media (max-width:1050px) { .collections-grid { grid-template-columns: repeat(2,minmax(0,1fr)); } }
@media (max-width:580px) { .featured-collections { padding: 60px 16px 72px; }.section-header { align-items: flex-start; flex-direction: column; }.collections-grid { grid-template-columns: 1fr; }.collection-art { height: 180px; } }
</style>
