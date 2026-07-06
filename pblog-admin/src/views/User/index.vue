<template>
  <div class="user-manage">

    <!-- Tab 切换 -->
    <el-tabs
        v-model="queryParams.status"
        @tabChange="handleQuery"
    >
      <el-tab-pane
          v-for="item in STATUS_OPTIONS"
          :key="item.value"
          :label="item.label"
          :name="item.value"
      />
    </el-tabs>

    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
        <el-form-item label="用户名称" prop="keyword">
          <el-input
              v-model="queryParams.keyword"
              placeholder="输入账号或昵称"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="排序" prop="sortField">
          <el-select
              v-model="queryParams.sortField"
              style="width: 140px"
              @change="handleQuery"
          >
            <el-option label="注册时间" value="createTime" />
            <el-option label="最后登录" value="lastLogin" />
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
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
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

        <el-table-column label="用户" width="240">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :size="40" :src="row.avatar" class="mr-2">
                {{ row.nickname.charAt(0) }}
              </el-avatar>
              <div class="user-meta">
                <div class="nickname">{{ row.nickname }}</div>
                <div class="username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="contact-info">
              <div v-if="row.email"><el-icon><Message /></el-icon> {{ row.email }}</div>
              <div v-if="row.phone" class="text-gray"><el-icon><Iphone /></el-icon> {{ row.phone }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" effect="plain" size="small">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
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

        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              {{ formatTime(row.createTime) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-divider direction="vertical" />

            <!-- 更多操作 Dropdown -->
            <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
              <el-button link type="primary">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 禁用/启用切换 -->
                  <el-dropdown-item v-if="row.status === '1'" command="disable" style="color: #e6a23c;">
                    <el-icon><Lock /></el-icon>禁用账户
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === '0'" command="enable" style="color: #67c23a;">
                    <el-icon><Unlock /></el-icon>启用账户
                  </el-dropdown-item>

                  <el-dropdown-item command="resetPwd">
                    <el-icon><Key /></el-icon>重置密码
                  </el-dropdown-item>

                  <el-dropdown-item command="delete" divided style="color: #f56c6c;">
                    <el-icon><Delete /></el-icon>删除用户
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Delete, ArrowDown,
  Message, Iphone, Lock, Unlock, Key
} from '@element-plus/icons-vue'

// --- 常量定义 ---
const STATUS_OPTIONS = [
  { label: '全部用户', value: '-1' },
  { label: '正常用户', value: '1' },
  { label: '已禁用', value: '0' }
]

const STATUS_MAP = {
  '1': { label: '正常', class: 'status-success' },
  '0': { label: '已禁用', class: 'status-error' }
}

// --- 响应式数据 ---
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const ids = ref([])
const queryFormRef = ref(null)

// 模拟所有数据（Mock Data）
const allMockData = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sortField: 'createTime',
  status: '-1'
})

// --- 初始化 Mock 数据 ---
const initMockData = () => {
  const mockList = []
  for (let i = 1; i <= 35; i++) {
    const isNormal = Math.random() > 0.2 // 80% 正常
    mockList.push({
      id: i,
      username: `user_${1000 + i}`,
      nickname: `测试用户${i}号`,
      avatar: i % 3 === 0 ? 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' : '',
      email: `user${i}@example.com`,
      phone: `1380000${String(i).padStart(4, '0')}`,
      role: i === 1 ? 'admin' : 'user',
      status: isNormal ? '1' : '0', // 1:正常, 0:禁用
      createTime: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toISOString()
    })
  }
  // 按时间倒序模拟数据库默认排序
  mockList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  allMockData.value = mockList
}

// --- 核心方法 ---

/**
 * 获取列表 (模拟后端查询)
 */
const getList = async () => {
  loading.value = true

  // 模拟网络延迟
  setTimeout(() => {
    let filtered = [...allMockData.value]

    // 1. 状态筛选
    if (queryParams.status !== '-1') {
      filtered = filtered.filter(item => item.status === queryParams.status)
    }

    // 2. 关键词筛选
    if (queryParams.keyword) {
      const kw = queryParams.keyword.toLowerCase()
      filtered = filtered.filter(item =>
          item.username.toLowerCase().includes(kw) ||
          item.nickname.includes(kw)
      )
    }

    // 3. 排序 (仅演示逻辑，Mock数据初始化时已乱序，这里简单重排)
    if (queryParams.sortField === 'createTime') {
      filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }

    // 4. 分页
    total.value = filtered.length
    const start = (queryParams.pageNum - 1) * queryParams.pageSize
    const end = start + queryParams.pageSize
    tableData.value = filtered.slice(start, end)

    loading.value = false
  }, 300)
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  if (queryFormRef.value) queryFormRef.value.resetFields()
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.sortField = 'createTime'
  queryParams.status = '-1'
  handleQuery()
}

const handlePageChange = () => {
  getList()
}

const handleAdd = () => {
  ElMessage.info("点击了新建用户")
}

const handleEdit = (row) => {
  ElMessage.success(`正在编辑用户：${row.nickname}`)
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
}

/**
 * 处理更多菜单命令
 */
const handleCommand = (command, row) => {
  switch (command) {
    case 'disable':
      handleStatusChange(row, '0')
      break
    case 'enable':
      handleStatusChange(row, '1')
      break
    case 'resetPwd':
      handleResetPwd(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

/**
 * 切换状态
 */
const handleStatusChange = (row, newStatus) => {
  const actionText = newStatus === '0' ? '禁用' : '启用'
  const type = newStatus === '0' ? 'warning' : 'success'

  ElMessageBox.confirm(`确认要${actionText}用户 "${row.nickname}" 吗？`, '提示', {
    type: type
  }).then(() => {
    // 模拟后端更新
    const target = allMockData.value.find(item => item.id === row.id)
    if(target) target.status = newStatus

    ElMessage.success(`${actionText}成功`)
    getList() // 刷新当前页
  })
}

/**
 * 重置密码
 */
const handleResetPwd = (row) => {
  ElMessageBox.prompt('请输入新密码', `重置密码 - ${row.username}`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.{6,}/,
    inputErrorMessage: '密码长度至少6位'
  }).then(({ value }) => {
    ElMessage.success('密码重置成功')
  }).catch(() => {})
}

/**
 * 单个删除
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认要删除用户 "${row.username}" 吗？此操作无法恢复。`, '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    // 模拟后端删除
    allMockData.value = allMockData.value.filter(item => item.id !== row.id)
    ElMessage.success("删除成功")
    getList()
  })
}

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  if (!ids.value.length) return
  ElMessageBox.confirm(`确认要删除选中的 ${ids.value.length} 个用户吗？`, '批量删除', {
    type: 'warning',
    confirmButtonText: '确定删除',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    allMockData.value = allMockData.value.filter(item => !ids.value.includes(item.id))
    ElMessage.success("批量删除成功")
    ids.value = []
    getList()
  })
}

// --- 辅助函数 ---

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

const getStatusLabel = (status) => STATUS_MAP[status]?.label || '未知'
const getStatusClass = (status) => STATUS_MAP[status]?.class || ''

onMounted(() => {
  initMockData() // 初始化 Mock 数据
  getList()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
  background-color: #f6f8fa;
  min-height: calc(100vh - 40px);
}

.search-card {
  margin-bottom: 16px;
  border: none;
  border-radius: 8px;
}
.search-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

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

/* 用户信息单元格 */
.user-info-cell {
  display: flex;
  align-items: center;
}
.mr-2 {
  margin-right: 12px;
  flex-shrink: 0;
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}
.nickname {
  font-weight: 500;
  color: #303133;
}
.username {
  font-size: 12px;
  color: #909399;
}

/* 联系方式 */
.contact-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.contact-info .el-icon {
  position: relative;
  top: 1px;
  margin-right: 2px;
}

.text-gray {
  color: #909399;
}

/* 状态点 */
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
}
.status-success { color: #67c23a; }
.status-success .dot { background-color: #67c23a; }
.status-error { color: #f56c6c; } /* 红色表示禁用 */
.status-error .dot { background-color: #f56c6c; }

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>