<template>
  <div class="stats-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in statsCards" :key="index">
        <el-card shadow="hover" class="data-card">
          <div class="card-icon" :style="{ background: item.bg }">
            <el-icon color="#fff"><component :is="item.icon" /></el-icon>
          </div>
          <div class="card-info">
            <div class="label">{{ item.label }}</div>
            <div class="num">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表占位区 -->
    <el-card shadow="never" class="mt-4 chart-card">
      <template #header>
        <div class="flex-between">
          <span>数据趋势</span>
          <el-radio-group v-model="timeRange" size="small">
            <el-radio-button label="7">近7天</el-radio-button>
            <el-radio-button label="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="chart-box">
        <el-empty description="ECharts 图表区域 (待集成)" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { DocumentCopy, View, Star, ChatLineRound } from '@element-plus/icons-vue'

const timeRange = ref('7')

// 模拟聚合数据
const statsCards = [
  { label: '累计文章', value: 28, icon: DocumentCopy, bg: '#409eff' },
  { label: '总阅读量', value: '12.5k', icon: View, bg: '#67c23a' },
  { label: '获赞总数', value: 892, icon: Star, bg: '#e6a23c' },
  { label: '收到评论', value: 156, icon: ChatLineRound, bg: '#f56c6c' },
]
</script>

<style scoped>
.data-card :deep(.el-card__body) { display: flex; align-items: center; padding: 20px; }
.card-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; }
.card-info .label { font-size: 14px; color: #909399; margin-bottom: 5px; }
.card-info .num { font-size: 24px; font-weight: bold; color: #303133; }
.mt-4 { margin-top: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.chart-box { height: 300px; display: flex; align-items: center; justify-content: center; background: #f9fafc; }
</style>

<style scoped>
.stats-container { color: var(--geo-navy, #1a1a2e); }
.stats-container :deep(.el-row) { row-gap: 20px; }
.data-card { min-height: 116px; overflow: visible; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 5px 5px 0 var(--geo-navy, #1a1a2e); }
.data-card:hover { transform: translate(-3px, -3px); box-shadow: 8px 8px 0 var(--geo-navy, #1a1a2e); }
.data-card :deep(.el-card__body) { min-height: 112px; padding: 18px; box-sizing: border-box; }
.card-icon { width: 52px; height: 52px; margin-right: 14px; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 3px 3px 0 var(--geo-navy, #1a1a2e); }
.stats-container :deep(.el-col:nth-child(1) .card-icon) { background: var(--geo-sky, #53bde8) !important; }
.stats-container :deep(.el-col:nth-child(2) .card-icon) { background: var(--geo-gold, #ffd54f) !important; }
.stats-container :deep(.el-col:nth-child(3) .card-icon) { background: var(--geo-coral, #ff6b6b) !important; }
.stats-container :deep(.el-col:nth-child(4) .card-icon) { background: var(--geo-navy, #1a1a2e) !important; }
.card-info .label { margin-bottom: 4px; color: var(--color-text-secondary, #555); font-size: 12px; font-weight: 800; }
.card-info .num { color: var(--geo-navy, #1a1a2e); font-size: clamp(24px, 2.5vw, 34px); font-weight: 950; letter-spacing: -.05em; }
.chart-card { margin-top: 28px; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 7px 7px 0 var(--geo-coral, #ff6b6b); }
.chart-card :deep(.el-card__header) { padding: 17px 20px; background: var(--geo-gold-light, #fff5c2); border-bottom: 2px solid var(--geo-navy, #1a1a2e); }
.chart-card :deep(.el-card__body) { padding: 20px; }
.flex-between > span { font-size: 18px; font-weight: 950; }
.chart-card :deep(.el-radio-button__inner) { color: var(--geo-navy, #1a1a2e); font-weight: 800; background: #fff; border-color: var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: none; }
.chart-card :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { color: var(--geo-navy, #1a1a2e); background: var(--geo-gold, #ffd54f); border-color: var(--geo-navy, #1a1a2e); box-shadow: -1px 0 0 var(--geo-navy, #1a1a2e); }
.chart-box { height: 320px; background-color: #fff; background-image: linear-gradient(rgba(26,26,46,.045) 1px,transparent 1px),linear-gradient(90deg,rgba(26,26,46,.045) 1px,transparent 1px); background-size: 32px 32px; border: 2px dashed var(--geo-navy, #1a1a2e); }
.chart-box :deep(.el-empty__description p) { color: var(--geo-navy, #1a1a2e); font-weight: 800; }
@media (max-width: 1100px) { .stats-container :deep(.el-col) { max-width: 50%; flex: 0 0 50%; } }
@media (max-width: 620px) { .stats-container :deep(.el-col) { max-width: 100%; flex-basis: 100%; }.flex-between { align-items: flex-start; flex-direction: column; gap: 12px; } }
</style>
