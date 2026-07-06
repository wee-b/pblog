<template>
  <div class="comment-manage">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="收到的评论" name="received" />
      <el-tab-pane label="发出的评论" name="sent" />
    </el-tabs>

    <!-- 表格区域 -->
    <el-table
        v-loading="isLoading"
        :data="commentList"
        style="width: 100%"
    >
      <!-- 评论内容列 -->
      <el-table-column prop="content" label="评论内容" min-width="250">
        <template #default="{ row }">
          <div class="comment-content">{{ row.content }}</div>
          <!-- 适配：articleId为0或标题为空时显示留言板 -->
          <div class="source-article">
            来源：{{ (row.articleId === 0 || !row.articleTitle) ? '留言板' : row.articleTitle }}
          </div>
        </template>
      </el-table-column>

      <!-- 评论人列 -->
      <el-table-column label="评论人" width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <!--
              头像适配：
              1. 收到的评论(received)：显示发送者(userInfoVO)的头像
              2. 发出的评论(sent)：显示我自己(userInfo)的头像
            -->
            <el-avatar
                :size="24"
                :src="activeTab === 'received' ? row.userInfoVO?.avatarUrl : userInfo.avatar"
            />

            <!-- 昵称适配 -->
            <span class="ml-2">
              {{ activeTab === 'received' ? row.userInfoVO?.nickname : (userInfo.nickname || '我') }}
            </span>
          </div>
        </template>
      </el-table-column>

      <!-- 时间列 -->
      <el-table-column prop="createTime" label="时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- 只有收到的评论才显示回复按钮 -->
          <el-button
              v-if="activeTab === 'received'"
              link
              type="primary"
              size="small"
              @click="handleReply(row)"
          >
            回复
          </el-button>

          <el-popconfirm title="确定删除这条评论吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getCommentsFromMe, getCommentsForMe, deleteComment } from '@/apis/comment.js'
import { checkLogin, getUserInfo } from "@/utils/loginManager.js"
import { ElMessage } from "element-plus"

const isLoading = ref(false)
const userInfo = ref({})
const activeTab = ref('received')
const commentList = ref([])

/**
 * 格式化时间 (简单的格式化，建议使用 dayjs)
 */
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

/**
 * 删除评论
 */
const handleDelete = async (id) => {
  try {
    const res = await deleteComment(id)
    if (res.data && res.data.code === 200) {
      ElMessage.success("删除评论成功")
      // 删除成功后刷新列表
      fetchComments()
    } else {
      ElMessage.error(res.data?.message || "删除评论失败")
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("删除评论出现异常")
  }
}

/**
 * 回复评论 (占位函数，需自行实现回复逻辑)
 */
const handleReply = (row) => {
  console.log('回复对象:', row)
  // 这里可以打开一个 Dialog 组件进行回复
  ElMessage.info(`准备回复: ${row.userInfoVO?.nickname}`)
}

/**
 * 获取评论列表
 */
const fetchComments = async () => {
  isLoading.value = true
  commentList.value = [] // 清空旧数据防止闪烁

  try {
    let res
    if (activeTab.value === 'received') {
      res = await getCommentsForMe()
    } else if (activeTab.value === 'sent') {
      res = await getCommentsFromMe()
    }

    if (res.data && res.data.code === 200) {
      commentList.value = res.data.data
    } else {
      ElMessage.error(res.data?.message || "获取评论列表失败")
    }
  } catch (e) {
    console.error(e)
    ElMessage.error("获取评论列表网络异常")
  } finally {
    isLoading.value = false
  }
}

// 监听 Tab 切换，自动拉取对应数据
watch(activeTab, () => {
  fetchComments()
})

onMounted(() => {
  // 1. 获取用户信息
  if (checkLogin()) {
    userInfo.value = getUserInfo()
    // 假设 getUserInfo 返回的对象里 avatar 字段可能叫 avatarUrl，这里做个兼容处理，具体看你本地存储结构
    if (!userInfo.value.avatar && userInfo.value.avatarUrl) {
      userInfo.value.avatar = userInfo.value.avatarUrl
    }
  } else {
    ElMessage.warning('未检测到登录状态')
  }

  // 2. 加载评论
  fetchComments()
})
</script>

<style scoped>
.comment-manage {
  padding: 20px;
  background: #fff;
}
.comment-content {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.5;
}
.source-article {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}
.user-cell {
  display: flex;
  align-items: center;
}
.ml-2 {
  margin-left: 8px;
  font-size: 14px;
}
</style>