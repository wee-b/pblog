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