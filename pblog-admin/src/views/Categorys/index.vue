<template>
  <div class="category-manage">

    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="分类名称">
          <el-input
              v-model="queryParams.keyword"
              placeholder="请输入分类名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" style="width: 120px">
            <el-option label="全部" value="-1" />
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格栏 -->
    <el-card shadow="never" class="table-card">
      <!-- 工具条 -->
      <div class="table-toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAddRoot">新增一级分类</el-button>
          <el-button plain :icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
        </div>
        <div class="right-info">
          <el-tooltip content="刷新数据" placement="top">
            <el-button circle :icon="RefreshRight" @click="handleQuery" />
          </el-tooltip>
        </div>
      </div>

      <!-- 树形表格主体 -->
      <el-table
          v-if="refreshTable"
          :data="tableData"
          style="width: 100%"
          row-key="id"
          :default-expand-all="isExpandAll"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          v-loading="loading"
      >
        <el-table-column label="分类名称" min-width="200">
          <template #default="{ row }">
            <span class="category-name">
               <!-- 根据层级显示不同图标 -->
              <el-icon class="cat-icon" :class="'level-' + row.level">
                <Folder v-if="row.level === 1"/>
                <FolderOpened v-else-if="row.level === 2"/>
                <Document v-else/>
              </el-icon>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="排序" width="100" align="center">
          <template #default="{ row }">
            <span class="sort-text">{{ row.sort }}</span>
          </template>
        </el-table-column>

        <el-table-column label="层级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small" effect="plain">
              {{ row.level === 1 ? '一级' : row.level === 2 ? '二级' : '三级' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                active-value="1"
                inactive-value="0"
                inline-prompt
                active-text="启"
                inactive-text="停"
                @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="text-gray">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>

            <!-- 只有1级和2级可以添加子级 -->
            <el-button
                v-if="row.level < 3"
                link
                type="primary"
                :icon="Plus"
                @click="handleAddSub(row)"
            >
              新增
            </el-button>

            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 对话框 -->
    <el-dialog
        :title="dialog.title"
        v-model="dialog.visible"
        width="500px"
        append-to-body
    >
      <el-form :model="form" ref="categoryFormRef" :rules="rules" label-width="80px">
        <el-form-item label="上级分类" v-if="form.parentId !== 0">
          <el-input v-model="form.parentName" disabled />
        </el-form-item>

        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="1">正常</el-radio>
            <el-radio label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Delete, Edit, Sort,
  RefreshRight, Folder, FolderOpened, Document
} from '@element-plus/icons-vue'

// --- 响应式数据 ---
const loading = ref(false)
const refreshTable = ref(true) // 用于强制刷新表格DOM以应用展开/折叠
const isExpandAll = ref(false)
const tableData = ref([])
const queryFormRef = ref(null)

const queryParams = reactive({
  keyword: '',
  status: '-1'
})

// 对话框相关
const categoryFormRef = ref(null)
const dialog = reactive({
  visible: false,
  title: ''
})
const form = reactive({
  id: undefined,
  parentId: 0,
  parentName: '',
  name: '',
  sort: 0,
  status: '1',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// --- Mock Data 生成逻辑 ---
const generateMockData = () => {
  const data = []
  // 生成 5 个一级分类
  for (let i = 1; i <= 5; i++) {
    const level1 = {
      id: i,
      name: `技术分类 ${i}`,
      level: 1,
      sort: i,
      status: '1',
      createTime: '2023-10-20T10:00:00',
      children: []
    }

    // 每个一级下生成 3 个二级分类
    for (let j = 1; j <= 3; j++) {
      const id2 = i * 100 + j
      const level2 = {
        id: id2,
        parentId: i,
        name: `二级子类 ${i}-${j}`,
        level: 2,
        sort: j,
        status: '1',
        createTime: '2023-10-21T11:00:00',
        children: []
      }

      // 第一个二级下生成 2 个三级分类 (为了演示层级)
      if (j === 1) {
        for (let k = 1; k <= 2; k++) {
          const id3 = id2 * 100 + k
          const level3 = {
            id: id3,
            parentId: id2,
            name: `三级末类 ${i}-${j}-${k}`,
            level: 3,
            sort: k,
            status: Math.random() > 0.5 ? '1' : '0',
            createTime: '2023-10-22T12:00:00'
          }
          level2.children.push(level3)
        }
      }
      level1.children.push(level2)
    }
    data.push(level1)
  }
  return data
}

// --- 核心方法 ---

const getList = () => {
  loading.value = true
  // 模拟异步请求
  setTimeout(() => {
    let mock = generateMockData()
    // 简单的前端搜索过滤 (仅演示一级过滤)
    if (queryParams.keyword) {
      mock = mock.filter(item => item.name.includes(queryParams.keyword))
    }
    tableData.value = mock
    loading.value = false
  }, 400)
}

const handleQuery = () => {
  getList()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = '-1'
  getList()
}

/**
 * 展开/折叠 所有行
 * Element Plus 的 Table 展开需要通过改动 default-expand-all 并重绘 Table
 */
const toggleExpandAll = () => {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

// 格式化表单
const resetForm = () => {
  form.id = undefined
  form.parentId = 0
  form.parentName = ''
  form.name = ''
  form.sort = 0
  form.status = '1'
  form.remark = ''
  if(categoryFormRef.value) categoryFormRef.value.resetFields()
}

/**
 * 新增一级分类
 */
const handleAddRoot = () => {
  resetForm()
  dialog.title = '新增一级分类'
  dialog.visible = true
  form.parentId = 0
}

/**
 * 新增子分类
 */
const handleAddSub = (row) => {
  resetForm()
  dialog.title = '新增子分类'
  form.parentId = row.id
  form.parentName = row.name
  dialog.visible = true
}

/**
 * 编辑分类
 */
const handleEdit = (row) => {
  resetForm()
  dialog.title = '编辑分类'
  // 简单深拷贝
  Object.assign(form, JSON.parse(JSON.stringify(row)))

  // 模拟查找父级名称(真实场景后端会返回)
  if(row.level > 1) {
    form.parentName = "上级分类(Mock)"
  }
  dialog.visible = true
}

/**
 * 提交表单
 */
const submitForm = () => {
  categoryFormRef.value.validate(valid => {
    if (valid) {
      ElMessage.success(form.id ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    }
  })
}

/**
 * 删除分类
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除分类"${row.name}"及其所有子分类？`, '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    getList()
  })
}

/**
 * 状态修改
 */
const handleStatusChange = (row) => {
  const text = row.status === '1' ? '启用' : '停用'
  ElMessage.success(`已${text}分类 "${row.name}"`)
}

// --- 辅助函数 ---

const getLevelTagType = (level) => {
  if (level === 1) return ''
  if (level === 2) return 'success'
  return 'warning'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.category-manage {
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

/* 分类名称列样式 */
.category-name {
  display: flex;
  align-items: center;
  font-weight: 500;
}
.cat-icon {
  margin-right: 8px;
  font-size: 16px;
}
.level-1 { color: #409eff; }
.level-2 { color: #67c23a; }
.level-3 { color: #e6a23c; }

.text-gray {
  color: #909399;
  font-size: 13px;
}

/* 覆盖 Element 表格默认的展开图标位置(可选) */
:deep(.el-table__expand-icon) {
  margin-right: 6px;
}
</style>