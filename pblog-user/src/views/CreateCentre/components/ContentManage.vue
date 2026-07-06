<template>
  <div class="content-manage">
    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
        <el-form-item label="文章标题" prop="keyword">
          <el-input
              v-model="queryParams.keyword"
              placeholder="请输入文章标题"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select
              v-model="queryParams.status"
              placeholder="请选择"
              clearable
              style="width: 140px"
              @change="handleQuery"
          >
            <el-option
                v-for="item in STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序" prop="sortField">
          <el-select
              v-model="queryParams.sortField"
              style="width: 140px"
              @change="handleQuery"
          >
            <el-option label="最新发布" value="publishedAt" />
            <el-option label="最多浏览" value="viewCount" />
            <el-option label="最多点赞" value="likeCount" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery" :loading="loading">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格栏 -->
    <el-card shadow="never" class="table-card">
      <!-- 表格工具条 -->
      <div class="table-toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新建文章</el-button>
          <el-button
              type="danger"
              plain
              :icon="Delete"
              :disabled="ids.length === 0"
              @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
        <div class="right-info">
          <span class="selection-text" v-if="ids.length > 0">已选择 {{ ids.length }} 项</span>
        </div>
      </div>

      <!-- 表格主体 -->
      <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          row-key="id"
          @selection-change="handleSelectionChange"
          highlight-current-row
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
                class="table-cover"
                :src="row.coverImage"
                :preview-src-list="row.coverImage ? [row.coverImage] : []"
                fit="cover"
                preview-teleported
            >
              <template #error>
                <div class="no-cover"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-cell">
              <el-tag v-if="row.sticky === '1'" type="danger" size="small" effect="dark" class="sticky-tag">顶</el-tag>
              <span class="title-text" @click="handleEdit(row)">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="分类" width="200">
          <template #default="{ row }">
            <div v-if="row.categories && row.categories.length">
              <!-- 只显示前两个，剩下的显示+N -->
              <el-tag
                  v-for="(cat, index) in row.categories.slice(0, 2)"
                  :key="index"
                  size="small"
                  effect="plain"
                  class="mr-1"
              >
                {{ cat.categoryName }}
              </el-tag>
              <el-tag v-if="row.categories.length > 2" size="small" type="info">+{{ row.categories.length - 2 }}</el-tag>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <div class="status-dot" :class="getStatusClass(row.status)">
              <span class="dot"></span>
              {{ getStatusLabel(row.status) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="数据" width="160">
          <template #default="{ row }">
            <div class="stats-row">
              <span title="浏览"><el-icon><View /></el-icon> {{ formatCount(row.viewCount) }}</span>
              <span title="点赞"><el-icon><Star /></el-icon> {{ formatCount(row.likeCount) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              {{ formatTime(row.publishedAt || row.createTime) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-divider direction="vertical" />

            <!-- 使用 Dropdown 收纳更多操作 -->
            <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
              <el-button link type="primary">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="status">
                    {{ handleStatus(row.status) }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: #f56c6c;">
                    删除文章
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleQuery"
            @current-change="handlePageChange"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Delete, View, Star,
  Picture, ArrowDown
} from '@element-plus/icons-vue'
import {useRouter} from "vue-router";


import {
  deleteArticleById,
  enableDisableArticle,
  pageQueryArticles
} from '@/apis/article/article.js'

// --- 常量定义 ---
const STATUS_OPTIONS = [
  { label: '全部', value: '-1' },
  { label: '草稿', value: '0' },
  { label: '已发布', value: '1' },
  { label: '待审核', value: '2' }
]

const STATUS_MAP = {
  '0': { label: '草稿', class:  'status-info'},
  '1': { label: '已发布', class: 'status-success' },
  '2': { label: '待审核', class: 'status-warning' }
}

const router = useRouter()
// --- 响应式数据 ---
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const ids = ref([]) // 选中项ID数组
const queryFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sortField: 'publishedAt',
  status: '-1'
})

// --- 核心方法 ---

const getList = async () => {
  loading.value = true
  try {
    // 实际项目中可能需要处理空字符串或特定参数
    const res = await pageQueryArticles(queryParams)
    // 适配后端返回结构，假设是 res.data.data 或 res.data
    const data = res.data?.data || res.data
    if (res.data?.code === 200 || res.code === 200) {
      tableData.value = data.records || []
      total.value = parseInt(data.total || 0)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  if (queryFormRef.value) queryFormRef.value.resetFields()
  // resetFields 只能重置 prop 绑定的字段，且重置为初始值
  // 手动修正一些默认值
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.sortField = 'publishedAt'
  queryParams.status = '-1'
  handleQuery()
}

const handlePageChange = () => {
  getList()
}

/**
 * 新建文章
 */
const handleAdd = () => {
  // router.push('/article/editor')
  ElMessage.info("跳转到新建页面")
}

/**
 * 编辑文章
 */
const handleEdit = (row) => {
  router.push(`/writeBlog/${row.id}`)
}

/**
 * 表格多选变化
 */
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
}

/**
 * 处理更多菜单命令
 */
const handleCommand = (command, row) => {
  if (command === 'delete') {
    handleDelete(row)
  } else if (command === 'status') {
    handleStatusChange(row)
  }
}
const handleStatus = (status) =>{
  if(status === '0'){
    return '发布'
  }else if(status === '1'){
    return '下架'
  }else if(status === '2'){
    return '取消发布'
  }
}

/**
 * 状态变更 (发布/下架)
 */
const handleStatusChange = async (row) => {
  let actionText;
  const articleStatus = row.status
  if(articleStatus === '0'){
    actionText = '发布'
  }else if(articleStatus === '1'){
    actionText = '下架'
  }else if(articleStatus === '2'){
    actionText = '取消发布'
  }


  try {
    const res = await enableDisableArticle(row.id)
    if (res.data?.code === 200 || res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      getList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  }
}

/**
 * 单个删除
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认要删除文章《${row.title}》吗？`, '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    // 调用删除接口
    const res = await deleteArticleById(row.id)
    if(res.data?.code === 200 || res.code === 200) {
      ElMessage.success("删除成功")
      getList()
    }
  })
}

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  if (!ids.value.length) return
  ElMessageBox.confirm(`确认要删除选中的 ${ids.value.length} 篇文章吗？`, '批量删除', {
    type: 'warning',
    confirmButtonText: '确定删除',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    // 假设后端支持批量删除接口 deleteArticles(ids)
    // await deleteArticles(ids.value)
    ElMessage.success("模拟批量删除成功")
    ids.value = [] // 清空选中
    getList()
  })
}

// --- 辅助函数 ---

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16) // 只显示到分钟
}

const formatCount = (num) => {
  if (!num) return 0
  return num > 9999 ? (num / 10000).toFixed(1) + 'w' : num
}

const getStatusLabel = (status) => STATUS_MAP[status]?.label || '未知'
const getStatusClass = (status) => STATUS_MAP[status]?.class || ''

onMounted(() => {
  getList()
})
</script>

<style scoped>
.content-manage {
  padding: 20px;
  background-color: #f6f8fa; /* 更柔和的背景色 */
  min-height: calc(100vh - 40px);
}

/* 搜索栏样式 */
.search-card {
  margin-bottom: 16px;
  border: none;
  border-radius: 8px; /* 圆角 */
}

.search-card :deep(.el-card__body) {
  padding-bottom: 2px; /* 紧凑一点 */
}

/* 表格卡片样式 */
.table-card {
  border: none;
  border-radius: 8px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.selection-text {
  font-size: 13px;
  color: #909399;
}

/* 封面图样式 */
.table-cover {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  display: block;
}

.no-cover {
  width: 48px;
  height: 48px;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #c0c4cc;
  border-radius: 4px;
  font-size: 20px;
}

/* 标题样式 */
.title-cell {
  display: flex;
  align-items: center;
}

.title-text {
  cursor: pointer;
  color: #303133;
  font-weight: 500;
  transition: color 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-text:hover {
  color: #409eff;
}

.sticky-tag {
  margin-right: 6px;
  flex-shrink: 0;
}

/* 标签微调 */
.mr-1 {
  margin-right: 4px;
}

.text-gray {
  color: #909399;
}

/* 状态小圆点样式 (Status Dot) */
.status-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.status-dot .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  background-color: #dcdfe6;
}

.status-success {
  color: #67c23a;
}

.status-success .dot {
  background-color: #67c23a;
}

.status-warning {
  color: #e6a23c;
}

.status-warning .dot {
  background-color: #e6a23c;
}

.status-info {
  color: #909399;
}

.status-info .dot {
  background-color: #909399;
}

/* 数据统计图标 */
.stats-row {
  display: flex;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.stats-row span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>