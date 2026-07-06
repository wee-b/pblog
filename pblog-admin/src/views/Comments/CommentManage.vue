<template>
  <div class="comment-manage">
    <!-- 状态筛选 Tabs -->
    <el-tabs
        v-model="queryParams.status"
        @tab-change="handleQuery"
    >
      <el-tab-pane
          v-for="item in STATUS_OPTIONS"
          :key="item.value"
          :label="item.label"
          :name="item.value"
      />
    </el-tabs>


    <!-- 表格区域 -->
    <el-table
        v-loading="isLoading"
        :data="commentList"
        style="width: 100%"
        border
        stripe
        row-key="id"
    >
      <!-- 评论内容列 -->
      <el-table-column prop="content" label="评论内容" min-width="300">
        <template #default="{ row }">
          <div class="comment-content">{{ row.content }}</div>
          <div class="source-wrapper">
             <span class="source-tag">
               {{ (row.articleId === 0 || !row.articleTitle) ? '留言板' : '文章' }}
             </span>
            <span v-if="row.articleId !== 0 && row.articleTitle" class="source-title">
               {{ row.articleTitle }}
             </span>
          </div>
        </template>
      </el-table-column>

      <!-- 评论人列 -->
      <el-table-column label="评论人" width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="30" :src="row.userInfoVO?.avatarUrl" icon="el-icon-user-solid">
              {{ (row.userInfoVO?.nickname || '匿').charAt(0) }}
            </el-avatar>
            <div class="user-info-text">
              <div class="nickname">{{ row.userInfoVO?.nickname || '匿名用户' }}</div>
              <div class="username-tip" v-if="row.userInfoVO?.username !== '0'">
                @{{ row.userInfoVO?.username }}
              </div>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 状态展示列 -->
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === '0'" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.status === '1'" type="success">已通过</el-tag>
          <el-tag v-else-if="row.status === '2'" type="danger">已驳回</el-tag>
        </template>
      </el-table-column>

      <!-- 时间列 -->
      <el-table-column prop="createTime" label="时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-buttons">

            <!-- 待审核 (0)：显示通过、驳回 -->
            <template v-if="row.status === '0'">
              <el-button link type="success" size="small" @click="handleAudit(row, '1')">通过</el-button>
              <el-button link type="warning" size="small" @click="handleAudit(row, '2')">驳回</el-button>
            </template>

            <!-- 已通过 (1)：显示回复、驳回(反悔) -->
            <template v-else-if="row.status === '1'">
              <el-button link type="primary" size="small" @click="handleReply(row)">回复</el-button>
              <el-button link type="warning" size="small" @click="handleAudit(row, '2')">驳回</el-button>
            </template>

            <!-- 已驳回 (2)：显示通过(反悔) -->
            <template v-else-if="row.status === '2'">
              <el-button link type="success" size="small" @click="handleAudit(row, '1')">通过</el-button>
            </template>

            <!-- 删除 -->
            <el-popconfirm title="确定删除这条评论吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>

          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchComments"
          @current-change="fetchComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
// 导入你提供的真实接口
import { commentPageQuery, deleteComment, setStatus } from '@/apis/comment.js'
import { checkLogin, getUserInfo } from "@/utils/loginManager.js"
import { ElMessage } from "element-plus"

const isLoading = ref(false)
const userInfo = ref({})
const commentList = ref([])
const total = ref(0)

const STATUS_OPTIONS = [
  { label: '全部评论', value: '-1' },
  { label: '待审核', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已驳回', value: '2' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  status: '-1',
  articleId: ''
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

/**
 * 获取评论列表
 */
const fetchComments = async () => {
  isLoading.value = true
  try {
    // 调用 pageQuery 接口
    const res = await commentPageQuery(queryParams)

    if (res.data && res.data.code === 200) {
      // 注意：根据你的Mock数据，结构是 res.data.data.records
      const pageData = res.data.data
      commentList.value = pageData.records
      total.value = pageData.total
    } else {
      ElMessage.error(res.data?.message || "获取列表失败")
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("网络异常")
  } finally {
    isLoading.value = false
  }
}

/**
 * Tab 切换事件
 */
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchComments()
}

/**
 * 审核操作 (通过/驳回)
 * 对应接口: setStatus(id, s)
 */
const handleAudit = async (row, status) => {
  try {
    // 直接传入 id 和 status 字符串
    const res = await setStatus(row.id, status)

    if (res.data && res.data.code === 200) {
      const actionMap = {'1': '通过', '2': '驳回'}
      ElMessage.success(`评论已${actionMap[status]}`)
      fetchComments() // 刷新列表
    } else {
      ElMessage.error(res.data?.message || "操作失败")
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("操作异常")
  }
}

/**
 * 删除评论
 * 对应接口: deleteComment(id)
 */
const handleDelete = async (id) => {
  try {
    const res = await deleteComment(id)
    if (res.data && res.data.code === 200) {
      ElMessage.success("删除成功")
      fetchComments()
    } else {
      ElMessage.error(res.data?.message || "删除失败")
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("删除异常")
  }
}

/**
 * 回复评论 (待实现)
 */
const handleReply = (row) => {
  ElMessage.info(`回复给: ${row.userInfoVO?.nickname}`)
  // TODO: 这里添加弹出回复框的逻辑
}

onMounted(() => {
  if (checkLogin()) {
    userInfo.value = getUserInfo()
  }
  fetchComments()
})
</script>

<style scoped>
.comment-manage {
  padding: 20px;
  background: #fff;
  min-height: 500px;
}

.comment-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.source-wrapper {
  display: flex;
  align-items: center;
  font-size: 12px;
  gap: 6px;
}

.source-tag {
  background: #f0f2f5;
  color: #909399;
  padding: 2px 6px;
  border-radius: 4px;
}

.source-title {
  color: #409eff;
  cursor: pointer;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-cell {
  display: flex;
  align-items: center;
}

.user-info-text {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.username-tip {
  font-size: 12px;
  color: #999;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>