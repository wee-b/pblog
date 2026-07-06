<template>
  <div class="wall-container">
    <div class="wall-header">
      <h1>🎢 留言墙</h1>
      <p>记录此刻的想法</p>
    </div>

    <!-- 留言列表 -->
    <div class="card-grid">
      <div
          v-for="item in commentTree"
          :key="item.id"
          class="flip-card-container"
          @click="handleCardClick(item)"
      >
        <div class="flip-card-inner" :class="{ 'is-flipped': item.isFlipped }">

          <!-- === 正面：展示根评论 (CommentVO) === -->
          <div class="card-face card-front" :class="getThemeClass(item.id)">
            <div class="card-body">
              <div class="quote-icon">“</div>
              <div class="content-text">{{ item.content }}</div>
            </div>

            <div class="card-footer">
              <div class="author-info">
                <!-- 对应 UserInfoVO -->
                <el-avatar :size="24" :src="item.userInfoVO?.avatarUrl" class="mini-avatar">
                  {{ item.userInfoVO?.nickname ? item.userInfoVO.nickname.charAt(0) : '匿' }}
                </el-avatar>
                <div class="meta">
                  <span class="nickname">{{ item.userInfoVO?.nickname || '匿名用户' }}</span>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>

              <div class="actions">
                <div class="action-btn" @click.stop="handleLike(item)">
                  <el-icon><HeartFilled v-if="item.isLiked" /><Heart v-else /></el-icon>
                  <span>{{ item.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- === 背面：展示回复或输入框 === -->
          <div class="card-face card-back" :class="getThemeClass(item.id)">
            <div class="back-content">
              <div v-if="item.children && item.children.length > 0" class="reply-display">
                <h3><el-icon><Avatar /></el-icon> 回复：</h3>
                <div class="reply-text">
                  {{ item.children[0].content }}
                </div>
                <div class="reply-meta">
                  — {{ item.children[0].userInfoVO?.nickname }}
                </div>
              </div>

              <!-- 场景2：没有回复，显示回复输入框 -->
              <div v-else class="reply-input-area" @click.stop>
                <div class="reply-label">
                  回复 @{{ item.userInfoVO?.nickname }} :
                </div>
                <el-input
                    v-model="item.tempReplyContent"
                    type="textarea"
                    rows="3"
                    placeholder="写下你的回复..."
                    class="transparent-textarea"
                />
                <el-button
                    size="small"
                    round
                    class="reply-btn"
                    @click="submitReply(item)"
                >
                  发送回复
                </el-button>
              </div>
            </div>

            <div class="back-footer">
              <span class="tip-text">点击空白翻转</span>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- 悬浮发布按钮 -->
    <div class="fab-container" @click="openPostDialog">
      <el-icon size="24"><Plus /></el-icon>
      <span>写留言</span>
    </div>

    <!-- 发布弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        title="留下一张卡片"
        width="90%"
        style="max-width: 500px; border-radius: 16px;"
        align-center
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules">
        <!-- 随机颜色预览区 -->
        <div class="preview-box" :class="previewTheme">
          <el-form-item prop="content" style="margin-bottom: 0;">
            <el-input
                v-model="form.content"
                type="textarea"
                :rows="5"
                placeholder="写下你想说的话..."
                maxlength="200"
                show-word-limit
                class="transparent-textarea"
            />
          </el-form-item>
        </div>

        <p class="random-tip">🎨 卡片颜色将随机生成</p>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="submitComment" round color="#333" :loading="submitting">
          发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus,  Avatar } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {getArticleAllComments, insertComment, insertRemark,deleteComment} from '@/apis/comment.js'

// --- 状态定义 ---
const isLoading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const commentList = ref([]) // 原始数据
const commentTree = ref([]) // 树形数据

// 预设主题列表
const themes = ['bg-blue', 'bg-purple', 'bg-pink', 'bg-orange', 'bg-green', 'bg-dark', 'bg-teal', 'bg-indigo']

// 表单数据 (对应 CommentDTO 的部分字段)
const form = reactive({
  content: '',
  articleId: 0, // 0 代表留言板
  rootId:0,   // 0 代表根评论
  parentId: 0,
  toReplayUsername: ''
})

const rules = {
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }]
}

// 预览时的随机颜色（每次打开弹窗变一次）
const previewTheme = ref('bg-blue')

// --- 方法逻辑 ---

// 1. 获取列表 (模拟调用后端 GET /comments 接口)
const fetchComments = async () => {
  isLoading.value = true;
  try{
    const res = await getArticleAllComments(0);
    if(res.data && res.data.code === 200){
      commentList.value = res.data.data
      commentTree.value = buildCommentTree(commentList.value)
    }else{
      ElMessage.error("获取留言板信息失败")
    }
  }catch (e){
    ElMessage.error("获取留言板信息失败")
  }finally {
    isLoading.value = false;
  }
}

// 2. 提交根留言 (对应 CommentDTO)
const submitComment = async ()=>{
  if (!form.content.trim()) return ElMessage.warning('内容不能为空')

  submitting.value = true
  const dto = {
    articleId: 0, // 留言板固定为0
    rootId: 0,   // 根评论
    parentId: 0, // 根评论
    toReplayUsername: null,
    content: form.content
  }
  try{
    const res = await insertRemark(dto);
    if(res.data && res.data.code === 200){
      dialogVisible.value = false
      form.content = ''
      // 刷新
      await fetchComments();
      ElMessage.success("留言发布成功,审核通过后可以展示")
    }else{
      ElMessage.success("留言发布失败")
    }
  }catch (e) {
    ElMessage.success("留言发布失败")
  }finally {
    submitting.value = false
  }
}

// 3. 提交回复 (在卡片背面)
const submitReply = async (parentVo)=>{
  if (!parentVo.tempReplyContent.trim()) return ElMessage.warning('回复内容不能为空')
  submitting.value = true

  // 构造 DTO
  const replyDto = {
    articleId: 0,
    rootId: parentVo.id,   // 根ID是当前卡片ID
    parentId: parentVo.id, // 父ID也是当前卡片ID
    toReplayUsername: parentVo.userInfoVO?.username, // 注意 Java 类里的拼写是 toReplay
    content: parentVo.tempReplyContent
  }

  try{
    const res = await insertComment(replyDto);
    if(res.data && res.data.code === 200){
      parentVo.tempReplyContent = ''
      // 刷新
      await fetchComments();
      ElMessage.success("回复成功")
    }else{
      ElMessage.success("回复失败")
    }
  }catch (e) {
    ElMessage.success("回复失败")
  }finally {
    submitting.value = false
  }
}

// --- 工具函数 ---

// 构建树形结构
const buildCommentTree = (list) => {
  if (!Array.isArray(list) || list.length === 0) return []

  const map = new Map()
  const tree = []

  // 1. 初始化map，确保每个对象都是新的引用，并添加 children
  list.forEach(item => {
    // 浅拷贝对象，防止引用污染
    map.set(item.id, { ...item, children: [] })
  })

  // 2. 构建树形结构
  list.forEach(item => {
    // 获取当前项在 map 中的引用（带有 children 属性的那个对象）
    const node = map.get(item.id)

    // 判断是否为根节点：rootId 为 0、null 或 -1 都视为根节点
    const isRoot = !item.rootId || item.rootId === 0 || item.rootId === -1

    if (isRoot) {
      tree.push(node)
    } else {
      // 是子评论，找到它的父亲 (根节点)
      const parentNode = map.get(item.rootId)
      if (parentNode) {
        parentNode.children.push(node)
      } else {
        // 如果找不到父节点（可能数据丢失），暂时作为根节点显示，防止数据隐身
        tree.push(node)
      }
    }
  })

  // 3. 排序工具函数 (修正 create_time -> createTime)
  const sortByCreateTime = (a, b) => {
    // 确保有时间字段，否则默认排前面
    const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
    const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
    return timeB - timeA // 降序：最新的在前面 (如果想旧的在前，改为 timeA - timeB)
  }

  // 4. 执行排序
  tree.sort(sortByCreateTime)
  tree.forEach(root => {
    if (root.children && root.children.length > 0) {
      root.children.sort(sortByCreateTime)
    }
  })

  return tree
}

// 根据 ID 确定颜色 (确定性算法，确保刷新后同一个ID颜色不变)
const getThemeClass = (id) => {
  if (!id) return themes[0]
  const index = id % themes.length
  return themes[index]
}

const formatTime = (timeStr) => {
  return dayjs(timeStr).format('MM-DD HH:mm')
}

const handleCardClick = (item) => {
  item.isFlipped = !item.isFlipped
}

const openPostDialog = () => {
  dialogVisible.value = true
  // 打开弹窗时随机一个预览色
  previewTheme.value = themes[Math.floor(Math.random() * themes.length)]
}

const handleLike = (item) => {
  // 真实场景调用点赞接口
  item.isLiked = !item.isLiked
  item.likeCount += item.isLiked ? 1 : -1
}

// 初始化
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.wall-container {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.wall-header {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
}
.wall-header h1 { margin-bottom: 5px; font-weight: 800; }
.wall-header p { color: #888; font-size: 14px; }

/* 网格布局 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto 80px;
}

/* 3D 翻转容器 */
.flip-card-container {
  background-color: transparent;
  aspect-ratio: 1 / 1;
  perspective: 1000px;
  cursor: pointer;
}

.flip-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  text-align: center;
  transition: transform 0.6s cubic-bezier(0.4, 0.2, 0.2, 1);
  transform-style: preserve-3d;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-radius: 20px;
}

.flip-card-inner.is-flipped {
  transform: rotateY(180deg);
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  color: #fff;
  overflow: hidden;
}

/* 正面布局 */
.card-front {
  justify-content: space-between;
}

/* 背面布局 */
.card-back {
  transform: rotateY(180deg);
  background: rgba(0, 0, 0, 0.2); /* 默认加深一点 */
  /* 使用伪元素或者 backdrop-filter 来做一点模糊区分 */
  backdrop-filter: brightness(0.9);
  justify-content: center;
  align-items: center;
}

/* 内容样式 */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.quote-icon {
  font-size: 60px;
  position: absolute;
  top: -20px;
  left: -10px;
  opacity: 0.3;
  font-family: Georgia, serif;
  line-height: 1;
}

.content-text {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.6;
  text-align: left;
  z-index: 1;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-avatar {
  background: rgba(255,255,255,0.25);
  font-weight: bold;
  color: #fff;
}

.meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.nickname {
  font-size: 14px;
  font-weight: bold;
}

.time {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: rgba(255,255,255,0.2);
  padding: 6px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.action-btn:hover {
  background: rgba(255,255,255,0.35);
}

/* 背面回复样式 */
.back-content {
  width: 100%;
  text-align: left;
}

.reply-display h3 {
  font-size: 16px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  opacity: 0.9;
}

.reply-text {
  background: rgba(255,255,255,0.15);
  padding: 12px;
  border-radius: 8px;
  font-size: 15px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.reply-meta {
  text-align: right;
  font-size: 12px;
  opacity: 0.8;
}

.reply-input-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-label {
  font-size: 14px;
  opacity: 0.9;
}

.reply-btn {
  align-self: flex-end;
  background: rgba(255,255,255,0.25);
  border: none;
  color: #fff;
}
.reply-btn:hover { background: rgba(255,255,255,0.4); }

.back-footer {
  position: absolute;
  bottom: 15px;
  width: 100%;
  text-align: center;
}
.tip-text { font-size: 12px; opacity: 0.6; }

/* 悬浮按钮 */
.fab-container {
  position: fixed;
  bottom: 40px; right: 40px;
  background: #222; color: #fff;
  height: 56px; padding: 0 24px;
  border-radius: 28px;
  display: flex; align-items: center; gap: 8px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.3);
  cursor: pointer; z-index: 100;
  transition: transform 0.3s;
}
.fab-container:hover { transform: translateY(-3px); background: #000; }

/* 弹窗与表单 */
.preview-box {
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 10px;
  transition: background 0.3s;
}

/* 透明输入框穿透 */
.transparent-textarea :deep(.el-textarea__inner) {
  background: transparent !important;
  box-shadow: none !important;
  color: #fff;
  font-size: 16px;
  resize: none;
  padding: 0;
}
.transparent-textarea :deep(.el-textarea__inner)::placeholder {
  color: rgba(255,255,255,0.6);
}

.random-tip {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 渐变色主题定义 */
.bg-blue { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.bg-purple { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.bg-pink { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%); color: #444; } /* 浅色适配 */
.bg-orange { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); color: #fff; text-shadow: 0 1px 1px rgba(0,0,0,0.1); }
.bg-green { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); color: #2d5560; }
.bg-dark { background: linear-gradient(135deg, #434343 0%, black 100%); }
.bg-teal { background: linear-gradient(135deg, #20c997 0%, #0099cc 100%); }
.bg-indigo { background: linear-gradient(135deg, #6610f2 0%, #6f42c1 100%); }

/* 针对浅色背景的特定样式覆盖 */
.bg-pink .card-body, .bg-green .card-body { color: #333; }
.bg-pink .transparent-textarea :deep(.el-textarea__inner),
.bg-green .transparent-textarea :deep(.el-textarea__inner) { color: #333; }
.bg-pink .transparent-textarea :deep(.el-textarea__inner)::placeholder,
.bg-green .transparent-textarea :deep(.el-textarea__inner)::placeholder { color: rgba(0,0,0,0.4); }
.bg-pink .mini-avatar, .bg-green .mini-avatar { background: rgba(0,0,0,0.1); color: #333; }
.bg-pink .action-btn, .bg-green .action-btn { background: rgba(0,0,0,0.05); color: #333; }
</style>