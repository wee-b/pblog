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
          <div class="metric-item">
            <div class="metric-value">{{ metrics.totalViews }}</div>
            <div class="metric-label">总访问量</div>
          </div>
          <div class="metric-item">
            <div class="metric-value">{{ metrics.avgQPS }}</div>
            <div class="metric-label">平均 QPS</div>
          </div>
          <div class="metric-item">
            <div class="metric-value">{{ metrics.totalArticles }}</div>
            <div class="metric-label">文章总数</div>
          </div>
          <div class="metric-item">
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

// 类型定义（TS环境可直接使用，JS环境可忽略类型声明）
/**
 * @typedef {Object} CategoryItem
 * @property {number|string} id - 分类ID（唯一标识）
 * @property {string} categoryName - 分类名称
 * @property {number} articleCount - 该分类下的文章数量
 */

/**
 * @typedef {Object} MetricsData
 * @property {number} totalViews - 总访问量
 * @property {string} avgQPS - 平均QPS
 * @property {number} totalArticles - 文章总数
 * @property {number} totalComments - 评论总数
 */

// 响应式数据
const chartReady = ref(false)
const pieChart = ref(null)
const barChart = ref(null)

/** @type {import('vue').Ref<CategoryItem[]>} */
const tags = ref([
  { id: 'loading', categoryName: '加载中...', articleCount: 0 }
])

/** @type {import('vue').Ref<MetricsData>} */
const metrics = ref({
  totalViews: 12458,
  avgQPS: '12.5',
  totalArticles: 86,
  totalComments: 342
})

// ECharts实例存储（用于销毁和resize）
const pieInstance = ref(null)
const barInstance = ref(null)

// 计算标签类型（根据文章数量动态切换颜色）
const getTagType = computed(() => (count) => {
  if (count > 30) return 'success'
  if (count > 15) return 'primary'
  return 'info'
})

// 计算标签大小（根据文章数量动态切换尺寸）
const getTagSize = computed(() => (count) => {
  if (count > 30) return 'medium'
  return 'small'
})

/**
 * 获取热门标签数据
 */
const getTags = async () => {
  try {
    let res = await getAllCategorys()
    res = res.data.data
    // 数据校验：确保返回数组且包含必要字段
    if (Array.isArray(res)) {
      const validTags = res
          .filter(tag => tag.categoryName && (tag.articleCount || tag.articleCount === 0))
          .map(tag => ({
            id: tag.id || `tag_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 兜底ID
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

/**
 * 初始化ECharts图表
 */
const initCharts = () => {
  if (!pieChart.value || !barChart.value) return

  // 饼图（文章分类）
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
        color: ['#67C23A', '#95D475', '#B3E19D', '#D1EDC4', '#E1F3D8', '#F0F9EB']
      }
    ]
  })

  // 柱状图（访客统计）
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
          color: '#67C23A',
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '40%'
      }
    ]
  })

  // 适配暗黑模式（可选）
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

/**
 * 窗口大小变化时调整图表尺寸
 */
const handleResize = () => {
  pieInstance.value?.resize()
  barInstance.value?.resize()
}

// 组件生命周期钩子
onMounted(async () => {
  // 并行执行数据请求和DOM就绪等待，提升加载效率
  await Promise.all([getTags(), nextTick()])
  initCharts()
  chartReady.value = true
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例，避免内存泄漏
  pieInstance.value?.dispose()
  barInstance.value?.dispose()
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 基础样式 */
.section {
  padding: 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 2rem;
  color: #4a6b57;
  margin-bottom: 10px;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: #67c23a;
}

.section-subtitle {
  font-size: 1rem;
  color: #7f9c8d;
  margin-top: 15px;
}

.stats-dashboard {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 网格布局 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* 卡片样式 */
.stat-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header h3 {
  margin: 0;
  color: #4a6b57;
  font-size: 1.1rem;
  font-weight: 600;
}

.stat-card >>> .el-card__header {
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.stat-card >>> .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

/* 图表容器 */
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

/* 标签云样式 */
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

/* 运营数据样式 */
.metrics-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.metric-item {
  text-align: center;
  padding: 20px 15px;
  background-color: #f5f9f4;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.metric-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.metric-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #4a6b57;
  margin-bottom: 8px;
  line-height: 1.2;
}

.metric-label {
  font-size: 0.9rem;
  color: #7f9c8d;
  text-transform: capitalize;
}

/* 响应式适配 */
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

/* 暗黑模式适配（可选） */
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