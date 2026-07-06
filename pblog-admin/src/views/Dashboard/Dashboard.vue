<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h2>数据概览</h2>
    </div>

    <!-- 数据卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">总文章数</p>
            <h3 class="stat-value">248</h3>
            <p class="stat-change positive">
              <span>↑ 12.5%</span>
              <span class="stat-period">较上月</span>
            </p>
          </div>
          <div class="stat-icon article-icon">
            <Document />
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">总访问量</p>
            <h3 class="stat-value">15,782</h3>
            <p class="stat-change positive">
              <span>↑ 8.3%</span>
              <span class="stat-period">较上月</span>
            </p>
          </div>
          <div class="stat-icon view-icon">
            <View />
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">总评论数</p>
            <h3 class="stat-value">1,246</h3>
            <p class="stat-change positive">
              <span>↑ 5.7%</span>
              <span class="stat-period">较上月</span>
            </p>
          </div>
          <div class="stat-icon comment-icon">
            <Message />
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <p class="stat-label">总用户数</p>
            <h3 class="stat-value">356</h3>
            <p class="stat-change negative">
              <span>↓ 2.1%</span>
              <span class="stat-period">较上月</span>
            </p>
          </div>
          <div class="stat-icon user-icon">
            <UserFilled />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-card class="chart-card">
        <div slot="header">访问趋势</div>
        <div class="chart-content">
          <el-select
              v-model="dateRange"
              size="small"
              class="date-select"
          >
            <el-option label="近7天" value="7" />
            <el-option label="近30天" value="30" />
            <el-option label="近90天" value="90" />
          </el-select>
          <div class="chart-wrapper">
            <canvas id="visitChart"></canvas>
          </div>
        </div>
      </el-card>

      <el-card class="chart-card">
        <div slot="header">文章分类占比</div>
        <div class="chart-content">
          <div class="chart-wrapper">
            <canvas id="categoryChart"></canvas>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近文章 -->
    <el-card class="recent-articles">
      <div slot="header">最近发布的文章</div>
      <el-table :data="recentArticles" border>
        <el-table-column prop="title" label="标题" width="400"></el-table-column>
        <el-table-column prop="category" label="分类"></el-table-column>
        <el-table-column prop="publishDate" label="发布时间"></el-table-column>
        <el-table-column prop="views" label="阅读量"></el-table-column>
        <el-table-column prop="comments" label="评论数"></el-table-column>
        <el-table-column label="操作">
          <template #default>
            <el-button size="small" type="text">编辑</el-button>
            <el-button size="small" type="text" text-color="#F56C6C">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Document, View, Message, UserFilled } from '@element-plus/icons-vue';
import Chart from 'chart.js/auto';

// 日期范围选择
const dateRange = ref('30');

// 最近文章数据
const recentArticles = ref([
  {
    title: 'Vue 3 Composition API 完全指南',
    category: '前端开发',
    publishDate: '2023-06-15',
    views: 1243,
    comments: 45
  },
  {
    title: 'Element Plus 主题定制最佳实践',
    category: 'UI框架',
    publishDate: '2023-06-10',
    views: 987,
    comments: 32
  },
  {
    title: '现代前端工程化解决方案',
    category: '工程化',
    publishDate: '2023-06-05',
    views: 1562,
    comments: 67
  },
  {
    title: 'JavaScript 性能优化技巧',
    category: 'JavaScript',
    publishDate: '2023-05-28',
    views: 2105,
    comments: 89
  },
  {
    title: '响应式设计原则与实践',
    category: 'CSS',
    publishDate: '2023-05-20',
    views: 876,
    comments: 23
  }
]);

// 初始化图表
onMounted(() => {
  // 访问趋势图表
  const visitCtx = document.getElementById('visitChart').getContext('2d');
  new Chart(visitCtx, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
      datasets: [{
        label: '访问量',
        data: [1200, 1900, 1500, 2400, 2800, 3200],
        borderColor: '#409EFF',
        backgroundColor: 'rgba(64, 158, 255, 0.1)',
        tension: 0.3,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  });

  // 分类占比图表
  const categoryCtx = document.getElementById('categoryChart').getContext('2d');
  new Chart(categoryCtx, {
    type: 'doughnut',
    data: {
      labels: ['前端开发', 'JavaScript', 'CSS', 'UI框架', '工程化'],
      datasets: [{
        data: [35, 25, 15, 15, 10],
        backgroundColor: [
          '#409EFF',
          '#67C23A',
          '#E6A23C',
          '#F56C6C',
          '#909399'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right'
        }
      }
    }
  });
});
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard-header {
  padding: 10px 0;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-label {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-change {
  margin: 0;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.positive {
  color: #67C23A;
}

.negative {
  color: #F56C6C;
}

.stat-period {
  color: #909399;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.article-icon {
  background-color: #409EFF;
}

.view-icon {
  background-color: #67C23A;
}

.comment-icon {
  background-color: #E6A23C;
}

.user-icon {
  background-color: #F56C6C;
}

.charts-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

@media (max-width: 1024px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-content {
  flex: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.date-select {
  align-self: flex-end;
  margin-bottom: 15px;
  width: 120px;
}

.chart-wrapper {
  flex: 1;
  min-height: 300px;
}

.recent-articles {
  margin-top: 10px;
}
</style>