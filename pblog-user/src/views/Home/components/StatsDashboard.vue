<template>
  <section class="stats-dashboard section">
    <div class="section-header">
      <h2 class="section-title">博客数据</h2>
      <p class="section-subtitle">统计与运营数据</p>
    </div>

    <div class="stats-grid">
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>文章分类</h3>
          </div>
        </template>
        <div class="chart-container">
          <el-empty v-if="!chartReady" description="图表加载中..." />
          <div ref="pieChart" class="chart-dom" />
        </div>
      </el-card>

      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>访客统计 (最近7天)</h3>
          </div>
        </template>
        <div class="chart-container">
          <el-empty v-if="!chartReady" description="图表加载中..." />
          <div ref="barChart" class="chart-dom" />
        </div>
      </el-card>

      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>热门标签</h3>
          </div>
        </template>
        <div class="tags-cloud">
          <el-tag
              v-for="tag in tags"
              :key="tag.id"
              :type="getTagType(tag.articleCount)"
              :size="getTagSize(tag.articleCount)"
              class="tag-item"
          >
            {{ tag.categoryName }} ({{ tag.articleCount || 0 }})
          </el-tag>
        </div>
      </el-card>

      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>运营数据</h3>
          </div>
        </template>
        <div class="metrics-container">
          <div class="metric-item metric-item--coral">
            <div class="metric-value">{{ metrics.totalViews }}</div>
            <div class="metric-label">总访问量</div>
          </div>
          <div class="metric-item metric-item--sky">
            <div class="metric-value">{{ metrics.avgQPS }}</div>
            <div class="metric-label">平均 QPS</div>
          </div>
          <div class="metric-item metric-item--gold">
            <div class="metric-value">{{ metrics.totalArticles }}</div>
            <div class="metric-label">文章总数</div>
          </div>
          <div class="metric-item metric-item--navy">
            <div class="metric-value">{{ metrics.totalComments }}</div>
            <div class="metric-label">评论总数</div>
          </div>
        </div>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getAllCategorys } from '@/apis/category.js'
import { ElMessage } from 'element-plus'

const chartReady = ref(false)
const pieChart = ref(null)
const barChart = ref(null)

const tags = ref([
  { id: 'loading', categoryName: '加载中...', articleCount: 0 }
])

const metrics = ref({
  totalViews: 12458,
  avgQPS: '12.5',
  totalArticles: 86,
  totalComments: 342
})

const pieInstance = ref(null)
const barInstance = ref(null)

const getTagType = computed(() => (count) => {
  if (count > 30) return 'success'
  if (count > 15) return 'primary'
  return 'info'
})

const getTagSize = computed(() => (count) => {
  if (count > 30) return 'medium'
  return 'small'
})

const getTags = async () => {
  try {
    let res = await getAllCategorys()
    res = res.data.data
    if (Array.isArray(res)) {
      const validTags = res
          .filter(tag => tag.categoryName && (tag.articleCount || tag.articleCount === 0))
          .map(tag => ({
            id: tag.id || `tag_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            categoryName: tag.categoryName,
            articleCount: tag.articleCount || 0
          }))
      tags.value = validTags.length > 0 ? validTags : [{ id: 'empty', categoryName: '暂无标签', articleCount: 0 }]
    } else {
      tags.value = [{ id: 'empty', categoryName: '暂无标签', articleCount: 0 }]
      ElMessage.warning('标签数据格式异常')
    }
  } catch (error) {
    console.error('获取标签失败：', error)
    tags.value = [{ id: 'error', categoryName: '获取失败', articleCount: 0 }]
    ElMessage.error('获取标签信息失败，请稍后重试')
  }
}

const initCharts = () => {
  if (!pieChart.value || !barChart.value) return

  pieInstance.value = echarts.init(pieChart.value)
  pieInstance.value.setOption({
    tooltip: {
      trigger: 'item',
      textStyle: { fontSize: 12 }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12, color: '#4a6b57' }
    },
    series: [
      {
        name: '文章分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold',
            color: '#4a6b57'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 35, name: '前端开发' },
          { value: 25, name: '后端技术' },
          { value: 15, name: '数据库' },
          { value: 10, name: 'DevOps' },
          { value: 8, name: '产品设计' },
          { value: 7, name: '其他' }
        ],
        color: ['#F06292', '#FFD54F', '#4FC3F7', '#1A1A2E', '#B0BEC5', '#F8BBD0']
      }
    ]
  })

  barInstance.value = echarts.init(barChart.value)
  barInstance.value.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      textStyle: { fontSize: 12 }
    },
    grid: {
      left: '10%',
      right: '5%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLabel: { fontSize: 12, color: '#4a6b57' },
      axisLine: { lineStyle: { color: '#e1e1e1' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 12, color: '#4a6b57' },
      axisLine: { lineStyle: { color: '#e1e1e1' } },
      splitLine: { lineStyle: { color: '#f5f5f5' } }
    },
    series: [
      {
        name: '访问量',
        type: 'bar',
        data: [1200, 1500, 1800, 1350, 1650, 2100, 1950],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#F06292' },
            { offset: 1, color: '#FFD54F' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '40%'
      }
    ]
  })

  const isDark = document.documentElement.classList.contains('dark')
  if (isDark) {
    [pieInstance.value, barInstance.value].forEach(chart => {
      chart.setOption({
        textStyle: { color: '#fff' },
        legend: { textStyle: { color: '#fff' } },
        xAxis: { axisLabel: { color: '#fff' }, axisLine: { lineStyle: { color: '#444' } } },
        yAxis: { axisLabel: { color: '#fff' }, axisLine: { lineStyle: { color: '#444' } }, splitLine: { lineStyle: { color: '#333' } } }
      })
    })
  }
}

const handleResize = () => {
  pieInstance.value?.resize()
  barInstance.value?.resize()
}

onMounted(async () => {
  await Promise.all([getTags(), nextTick()])
  initCharts()
  chartReady.value = true
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  pieInstance.value?.dispose()
  barInstance.value?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.section {
  padding: 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 2rem;
  color: var(--color-text-primary);
  margin-bottom: 10px;
  position: relative;
  display: inline-block;
  font-weight: 800;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, var(--geo-gold), var(--geo-coral));
  border-radius: 2px;
}

.section-subtitle {
  font-size: 1rem;
  color: var(--color-text-muted);
  margin-top: 15px;
}

.stats-dashboard {
  background-color: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  border: none;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 1.1rem;
  font-weight: 700;
}

.stat-card >>> .el-card__header {
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.stat-card >>> .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.chart-container {
  height: 100%;
  min-height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-dom {
  width: 100%;
  height: 100%;
  min-height: 250px;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px 0;
  align-content: flex-start;
}

.tag-item {
  margin: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* Geometric metric items */
.metrics-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.metric-item {
  text-align: center;
  padding: 20px 15px;
  border-radius: var(--radius-md);
  transition: transform 0.3s ease;
  position: relative;
  overflow: hidden;
}

.metric-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.metric-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

/* Color-coded metric cards */
.metric-item--coral {
  background: linear-gradient(135deg, rgba(240, 98, 146, 0.08), rgba(240, 98, 146, 0.02));
}
.metric-item--coral::before {
  background: var(--geo-coral);
}
.metric-item--coral .metric-value {
  color: var(--geo-coral-dark);
}

.metric-item--sky {
  background: linear-gradient(135deg, rgba(79, 195, 247, 0.08), rgba(79, 195, 247, 0.02));
}
.metric-item--sky::before {
  background: var(--geo-sky);
}
.metric-item--sky .metric-value {
  color: var(--geo-sky-dark);
}

.metric-item--gold {
  background: linear-gradient(135deg, rgba(255, 213, 79, 0.1), rgba(255, 213, 79, 0.02));
}
.metric-item--gold::before {
  background: var(--geo-gold);
}
.metric-item--gold .metric-value {
  color: var(--geo-gold-dark);
}

.metric-item--navy {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.06), rgba(26, 26, 46, 0.02));
}
.metric-item--navy::before {
  background: var(--geo-navy);
}
.metric-item--navy .metric-value {
  color: var(--geo-navy);
}

.metric-value {
  font-size: 1.8rem;
  font-weight: 800;
  margin-bottom: 8px;
  line-height: 1.2;
}

.metric-label {
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .metrics-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .section {
    padding: 16px;
  }
  .section-title {
    font-size: 1.6rem;
  }
  .metric-value {
    font-size: 1.5rem;
  }
  .chart-container {
    min-height: 200px;
  }
}

:deep(.dark) .stats-dashboard {
  background-color: #1e1e1e;
}

:deep(.dark) .stat-card {
  background-color: #2d2d2d;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
}

:deep(.dark) .card-header h3,
:deep(.dark) .metric-value,
:deep(.dark) .section-title {
  color: #e0e0e0;
}

:deep(.dark) .section-subtitle,
:deep(.dark) .metric-label {
  color: #b0b0b0;
}

:deep(.dark) .metric-item {
  background-color: #383838;
}

:deep(.dark) .el-empty__description {
  color: #b0b0b0;
}
</style>
