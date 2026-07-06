<template>
  <el-card class="comment-card mt-4" shadow="never" id="comment-section">
    <div class="comment-header">
      <h3>评论 ({{ totalCount }})</h3>
    </div>

    <!-- 发表评论框 -->
    <div class="comment-input-wrapper">
      <el-avatar :size="40" :src="userInfo.avatarUrl || defaultAvatarUrl" class="mr-3"/>
      <div class="input-box">
        <el-input
            v-model="newCommentContent"
            type="textarea"
            :rows="3"
            :placeholder="placeholder"
            resize="none"
        />
        <div class="input-actions">
          <el-button v-if="replyTarget" link @click="cancelReply" class="mr-2">取消回复</el-button>
          <el-button type="primary" size="small" @click="handlePostComment">发布评论</el-button>
        </div>
      </div>
    </div>

    <el-divider />

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <!-- 调试信息：方便排查数据问题 -->
      <div v-if="!loading" class="debug-info">
        原始评论数：{{ commentList.length }} | 树形评论数：{{ commentTree.length }}
      </div>

      <div v-for="item in commentTree" :key="item.id" class="comment-item">
        <!-- 根评论 -->
        <div class="root-comment">
          <el-avatar :size="40" :src="item.userInfoVO.avatarUrl" class="comment-avatar"/>
          <div class="comment-content-box">
            <div class="comment-info">
              <span class="nickname">{{ item.userInfoVO.nickname }}</span>
              <span class="time">{{ formatDate(item.create_time) }}</span>
            </div>
            <p class="text">{{ item.content }}</p>
            <div class="comment-actions">
              <span class="action-btn" @click="handleReply(item)">回复</span>
              <LikeButton :target-type="2" :target-id="item.id"></LikeButton>
            </div>
          </div>
        </div>

        <!-- 子评论 (回复) -->
        <div class="sub-comments" v-if="item.children && item.children.length > 0">
          <div v-for="sub in item.children" :key="sub.id" class="sub-comment-item">
            <el-avatar :size="32" :src="sub.userInfoVO.avatarUrl" class="comment-avatar"/>
            <div class="comment-content-box">
              <div class="comment-info">
                <span class="nickname">{{ sub.userInfoVO.nickname }}</span>
                <span class="reply-text" v-if="sub.to_reply_username">
                   回复 <span class="at-user">@{{ sub.to_reply_username }}</span>
                 </span>
                <span class="time">{{ formatDate(sub.create_time) }}</span>
              </div>
              <p class="text">{{ sub.content }}</p>
              <div class="comment-actions">
                <span class="action-btn" @click="handleReply(item, sub)">回复</span>
                <LikeButton :target-type="2" :target-id="sub.id"></LikeButton>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && commentTree.length === 0" description="暂无评论，快来抢沙发吧~" />
    </div>
  </el-card>
</template>

<script setup>
import {ref, onMounted, watch, computed} from 'vue'
import {ElMessage} from 'element-plus'
import {getArticleAllComments, insertComment} from "@/apis/comment.js";
import {getUserInfo, checkLogin} from '@/utils/loginManager.js';
import defaultAvatarUrl from '@/assets/imgs/default-avatar.png'
import LikeButton from "@/components/LikeButton.vue";

// --- Props 定义 ---
const props = defineProps({
  articleId: {
    type: [Number, String],
    required: true
  }
})

// --- 状态定义 ---
const userInfo = ref({});
const loading = ref(false)
const newCommentContent = ref('')
const commentList = ref([]) // 原始数据
const commentTree = ref([]) // 树形数据
// 移除TS类型注解，改为纯JS对象
const replyTarget = ref(null)

const totalCount = computed(() => commentList.value.length)
const placeholder = computed(() => {
  return replyTarget.value
      ? `回复 @${replyTarget.value.to_reply_username}：`
      : '写下你的评论...'
})

// --- Mock Data (移除TS类型注解) ---
const mockCommentsData = [
  {
    id: 1,
    articleId: 1,
    rootId: 0,
    parentId: 0, // 修正：接口定义是number，不能为null
    toReplyUsername: null,
    content: "这篇文章写得太好了，Vite 的构建速度确实快！",
    likeCount: 12,
    createTime: "2025-03-15 10:30:00",
    userInfoVO: {
      username: "1234567890",
      nickname: "superAdmin",
      avatarUrl: "http://192.168.150.103:9000/pblog/avatars/1/20251204/b_06664a7452c07b2ba68877312b7f0785.jpg",
      bio: null
    }
  }
]

// --- 工具方法 ---
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  // 增强兼容性：处理各种时间格式
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr // 解析失败时返回原字符串
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 构建树形结构
const buildCommentTree = (list) => {
  if (!Array.isArray(list) || list.length === 0) return []

  const map = new Map()
  const tree = []

  // 初始化map，添加children字段
  list.forEach(item => {
    map.set(item.id, {
      ...item,
      children: []
    })
  })

  // 构建树形结构
  list.forEach(item => {
    if (item.rootId === 0) {
      // 根评论
      tree.push(map.get(item.id))
    } else {
      // 子评论：挂载到对应根评论的children
      const rootItem = map.get(item.rootId)
      if (rootItem) {
        rootItem.children.push(map.get(item.id))
      }
    }
  })

  // 排序：按创建时间升序
  const sortByCreateTime = (a, b) => {
    const timeA = new Date(a.create_time).getTime()
    const timeB = new Date(b.create_time).getTime()
    return timeA - timeB
  }

  // 根评论排序
  tree.sort(sortByCreateTime)
  // 子评论排序
  tree.forEach(root => {
    root.children.sort(sortByCreateTime)
  })

  return tree
}

// --- 业务逻辑 ---
const fetchComments = async () => {
  const articleId = Number(props.articleId)
  if (!articleId) return

  loading.value = true
  try {
    const res = await getArticleAllComments(articleId)
    if (res.data && res.data.code === 200) {
      commentList.value = res.data.data
      commentTree.value = buildCommentTree(commentList.value)
    } else {
      ElMessage.error('获取评论失败：' + (res.data?.msg || '接口返回异常'))
    }
  } catch (e) {
    console.error('获取评论接口异常：', e)
    ElMessage.error('获取评论失败：网络异常')
  } finally {
    loading.value = false
  }
}

const handlePostComment = async () => {
  const content = newCommentContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  // 校验登录状态
  if (!checkLogin()) {
    ElMessage.warning('请先登录后发表评论')
    return
  }

  // 构造提交参数 (移除TS类型注解)
  const payload = {
    articleId: Number(props.articleId),
    content,
    rootId: replyTarget.value ? replyTarget.value.rootId : 0,
    parentId: replyTarget.value ? replyTarget.value.parentId : 0,
    toReplyUsername: replyTarget.value ? replyTarget.value.toUser : null
  }

  try {
    const res = await insertComment(payload)
    if (res.data && res.data.code === 200) {
      ElMessage.success('发表评论成功,审核通过后展示')
      newCommentContent.value = ''
      cancelReply() // 清空回复状态
      await fetchComments() // 重新加载评论
    } else {
      ElMessage.error('发表评论失败：' + (res.data?.msg || '接口返回异常'))
    }
  } catch (e) {
    console.error('发表评论接口异常：', e)
    ElMessage.error('发表评论失败：网络异常')
  }
}

// 点击回复按钮 (移除TS类型注解，修正字段取值)
const handleReply = (rootComment, subComment) => {
  // 处理subComment可选参数（JS中未传则为undefined）
  subComment = subComment || null
  const target = subComment || rootComment

  replyTarget.value = {
    rootId: rootComment.id, // 根评论ID
    parentId: target.id,    // 被回复的评论ID
    toUser: target.userInfoVO.username, // 从userInfoVO取username
    to_reply_username: target.userInfoVO.nickname // 从userInfoVO取nickname
  }

  // 聚焦输入框
  const inputEl = document.querySelector('.comment-input-wrapper textarea')
  if (inputEl) {
    inputEl.focus()
    // 自动填充回复前缀（可选）
    newCommentContent.value = `@${target.userInfoVO.nickname} `
  }
}

const cancelReply = () => {
  replyTarget.value = null
  newCommentContent.value = '' // 清空输入框
}

// --- 生命周期 ---
onMounted(() => {
  // 获取用户信息
  if (checkLogin()) {
    userInfo.value = getUserInfo()
  } else {
    ElMessage.warning('未检测到登录状态，仅可查看评论')
  }
  // 加载评论
  fetchComments()
})

// 监听文章ID变化，重新加载评论
watch(() => props.articleId, () => {
  commentList.value = []
  commentTree.value = []
  fetchComments()
}, {immediate: false})
</script>

<style scoped lang="scss">
// 新增调试信息样式
.debug-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

// 原有样式保持不变
.comment-card {
  border-radius: 12px;
  padding: 10px;
}

.comment-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.comment-input-wrapper {
  display: flex;
  margin-bottom: 20px;

  .input-box {
    flex: 1;

    .input-actions {
      margin-top: 10px;
      text-align: right;
    }
  }
}

.comment-list {
  .comment-item {
    margin-bottom: 24px;
  }
}

.root-comment {
  display: flex;
  gap: 16px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content-box {
  flex: 1;

  .comment-info {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;

    .nickname {
      font-weight: 600;
      font-size: 14px;
      color: #333;
    }

    .reply-text {
      font-size: 13px;
      color: #666;

      .at-user {
        color: #409eff;
        cursor: pointer;
      }
    }

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .text {
    font-size: 14px;
    color: #555;
    line-height: 1.6;
    margin: 0 0 8px 0;
  }

  .comment-actions {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #999;

    .action-btn {
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
}

.sub-comments {
  margin-top: 16px;
  margin-left: 56px;
  background-color: #f9f9f9;
  padding: 16px;
  border-radius: 8px;

  .sub-comment-item {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.mt-4 {
  margin-top: 16px;
}

.mr-2 {
  margin-right: 8px;
}

.mr-3 {
  margin-right: 12px;
}
</style>