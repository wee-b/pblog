<template>
  <div class="wall-container">
    <header class="wall-header">
      <div class="header-copy">
        <div class="eyebrow"><span></span> GUESTBOOK / 04</div>
        <h1>留言<span>墙</span></h1>
        <p>把路过此刻的想法，留成一张会翻面的彩色卡片。</p>
        <div class="wall-count"><strong>{{ commentTree.length }}</strong><span>张公开留言</span></div>
      </div>
      <div class="header-shapes" aria-hidden="true">
        <span class="shape-square"></span>
        <span class="shape-circle"></span>
        <span class="shape-triangle"></span>
      </div>
    </header>

    <!-- 留言列表 -->
    <main class="wall-content">
      <div class="section-heading">
        <div><b>01</b><span><small>MESSAGES</small>留言卡片</span></div>
        <p>点击卡片翻面，查看或写下回复</p>
      </div>

      <div v-if="isLoading" class="wall-state">
        <span class="state-shape"></span>
        <strong>正在打捞留言...</strong>
      </div>

      <div v-else-if="commentTree.length === 0" class="wall-state">
        <span class="state-shape"></span>
        <strong>留言墙还是空的</strong>
        <p>来留下第一张彩色卡片吧。</p>
      </div>

      <div v-else class="card-grid">
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
                  <span class="heart-icon">{{ item.isLiked ? '♥' : '♡' }}</span>
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
    </main>

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
        class="remark-dialog"
        style="max-width: 500px;"
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

        <p class="random-tip"><span></span> 卡片颜色将随机生成</p>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="submitting">
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
      ElMessage.success("留言发布成功")
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

<style scoped>
.wall-container {
  min-height: calc(100vh - 68px);
  padding: 0 0 110px;
  color: var(--geo-navy, #1a1a2e);
  background-color: #fff;
  background-image:
    linear-gradient(rgba(26, 26, 46, .04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 26, 46, .04) 1px, transparent 1px);
  background-size: 46px 46px;
  font-family: inherit;
}

.wall-header {
  position: relative;
  display: flex;
  min-height: 330px;
  margin: 0;
  padding: 64px clamp(24px, 8vw, 120px);
  overflow: hidden;
  align-items: center;
  box-sizing: border-box;
  color: var(--geo-navy, #1a1a2e);
  text-align: left;
  background: rgba(255, 255, 255, .92);
  border-bottom: 3px solid var(--geo-navy, #1a1a2e);
}

.header-copy { position: relative; z-index: 2; max-width: 670px; }
.eyebrow { display: flex; margin-bottom: 16px; align-items: center; gap: 10px; color: var(--geo-coral-dark, #d9485f); font-size: 11px; font-weight: 950; letter-spacing: .18em; }
.eyebrow span { width: 38px; height: 8px; background: linear-gradient(90deg, var(--geo-coral, #ff6b6b) 0 58%, var(--geo-sky, #53bde8) 58%); border: 1px solid var(--geo-navy, #1a1a2e); }
.wall-header h1 { margin: 0; font-size: clamp(52px, 7vw, 88px); font-weight: 950; line-height: .95; letter-spacing: -.08em; }
.wall-header h1 span { color: var(--geo-gold, #ffd54f); -webkit-text-stroke: 2px var(--geo-navy, #1a1a2e); }
.wall-header p { max-width: 520px; margin: 24px 0 0; color: var(--color-text-secondary, #555568); font-size: 16px; line-height: 1.8; }
.wall-count { display: inline-flex; margin-top: 28px; align-items: stretch; border: 2px solid var(--geo-navy, #1a1a2e); box-shadow: 5px 5px 0 var(--geo-sky, #53bde8); }
.wall-count strong { display: grid; min-width: 52px; padding: 8px 12px; place-items: center; color: #fff; font-size: 24px; background: var(--geo-navy, #1a1a2e); }
.wall-count span { display: grid; padding: 8px 14px; place-items: center; font-size: 12px; font-weight: 850; background: var(--geo-gold-light, #fff5c2); }

.header-shapes span { position: absolute; display: block; z-index: 1; border: 3px solid var(--geo-navy, #1a1a2e); }
.shape-square { top: 54px; right: 14%; width: 104px; height: 104px; background: var(--geo-coral, #ff6b6b); box-shadow: 10px 10px 0 var(--geo-navy, #1a1a2e); transform: rotate(12deg); }
.shape-circle { right: 7%; bottom: 52px; width: 78px; height: 78px; background: var(--geo-sky, #53bde8); border-radius: 50%; }
.shape-triangle { right: 27%; bottom: 34px; width: 90px; height: 82px; background: var(--geo-gold, #ffd54f); clip-path: polygon(50% 0, 100% 100%, 0 100%); transform: rotate(-9deg); }

.wall-content { max-width: 1280px; margin: 0 auto; padding: 58px 28px 0; }
.section-heading { display: flex; margin-bottom: 34px; align-items: flex-end; justify-content: space-between; gap: 24px; }
.section-heading > div { display: flex; align-items: center; gap: 14px; }
.section-heading b { display: grid; width: 50px; height: 50px; place-items: center; color: #fff; background: var(--geo-navy, #1a1a2e); border: 2px solid var(--geo-navy, #1a1a2e); box-shadow: 6px 6px 0 var(--geo-gold, #ffd54f); }
.section-heading span { font-size: 26px; font-weight: 950; line-height: 1; }
.section-heading small { display: block; margin-bottom: 6px; color: var(--geo-coral-dark, #d9485f); font-size: 9px; letter-spacing: .16em; }
.section-heading p { margin: 0; color: var(--color-text-muted, #777); font-size: 13px; }

.card-grid { max-width: none; margin: 0; gap: 28px; grid-template-columns: repeat(auto-fill, minmax(270px, 1fr)); }
.flip-card-container { aspect-ratio: 1 / .94; }
.flip-card-inner { border-radius: 0; box-shadow: 8px 8px 0 var(--geo-navy, #1a1a2e); transition: transform .55s cubic-bezier(.2, .75, .25, 1), box-shadow .2s ease; }
.flip-card-container:hover .flip-card-inner { box-shadow: 12px 12px 0 var(--geo-navy, #1a1a2e); }
.flip-card-inner.is-flipped { transform: rotateY(180deg); }
.card-face { padding: 24px; color: var(--geo-navy, #1a1a2e); background: var(--card-color, var(--geo-gold, #ffd54f)); border: 3px solid var(--geo-navy, #1a1a2e); border-radius: 0; }
.card-face::after { position: absolute; top: 17px; right: 17px; width: 22px; height: 22px; content: ''; background: rgba(255, 255, 255, .72); border: 2px solid var(--geo-navy, #1a1a2e); transform: rotate(11deg); }
.card-back { background: var(--card-color, var(--geo-gold, #ffd54f)); backdrop-filter: none; }
.quote-icon { top: -16px; left: -5px; color: var(--geo-navy, #1a1a2e); font-size: 68px; opacity: .18; }
.content-text { font-size: 17px; font-weight: 800; line-height: 1.7; }
.mini-avatar { color: var(--geo-navy, #1a1a2e); background: rgba(255, 255, 255, .7); border: 2px solid var(--geo-navy, #1a1a2e); }
.nickname { font-weight: 900; }
.time { opacity: .65; }
.action-btn { padding: 5px 10px; color: var(--geo-navy, #1a1a2e); background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 3px 3px 0 rgba(26, 26, 46, .35); }
.action-btn:hover { background: var(--geo-gold-light, #fff5c2); transform: translate(-1px, -1px); }
.heart-icon { font-size: 18px; line-height: 1; }
.reply-display h3, .reply-label { font-weight: 900; opacity: 1; }
.reply-text { padding: 13px; color: var(--geo-navy, #1a1a2e); background: rgba(255, 255, 255, .75); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; }
.reply-meta, .tip-text { font-weight: 750; opacity: .7; }
.reply-btn { color: var(--geo-navy, #1a1a2e); font-weight: 900; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 3px 3px 0 var(--geo-navy, #1a1a2e); }
.reply-btn:hover { color: var(--geo-navy, #1a1a2e); background: var(--geo-gold-light, #fff5c2); }
.card-back .transparent-textarea :deep(.el-textarea__inner) { padding: 10px; color: var(--geo-navy, #1a1a2e); background: rgba(255, 255, 255, .8) !important; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; }
.card-back .transparent-textarea :deep(.el-textarea__inner)::placeholder { color: rgba(26, 26, 46, .48); }

.bg-blue { --card-color: var(--geo-sky, #53bde8); background: var(--card-color); }
.bg-purple { --card-color: #b8a7ff; background: var(--card-color); }
.bg-pink { --card-color: var(--geo-coral, #ff6b6b); background: var(--card-color); }
.bg-orange { --card-color: #ff9d58; background: var(--card-color); color: var(--geo-navy, #1a1a2e); text-shadow: none; }
.bg-green { --card-color: #7ed7a5; background: var(--card-color); color: var(--geo-navy, #1a1a2e); }
.bg-dark { --card-color: #bbbcc8; background: var(--card-color); }
.bg-teal { --card-color: #62d7cd; background: var(--card-color); }
.bg-indigo { --card-color: var(--geo-gold, #ffd54f); background: var(--card-color); }
.bg-pink .card-body, .bg-green .card-body,
.bg-pink .mini-avatar, .bg-green .mini-avatar,
.bg-pink .action-btn, .bg-green .action-btn { color: var(--geo-navy, #1a1a2e); }

.wall-state { display: grid; min-height: 280px; padding: 40px; box-sizing: border-box; place-items: center; align-content: center; text-align: center; background: #fff; border: 2px dashed var(--geo-navy, #1a1a2e); }
.wall-state .state-shape { width: 56px; height: 56px; margin-bottom: 22px; background: var(--geo-gold, #ffd54f); border: 2px solid var(--geo-navy, #1a1a2e); box-shadow: 6px 6px 0 var(--geo-coral, #ff6b6b); transform: rotate(8deg); }
.wall-state strong { font-size: 20px; }
.wall-state p { margin: 8px 0 0; color: var(--color-text-muted, #777); }

.fab-container { right: 36px; bottom: 34px; height: 52px; padding: 0 20px; color: var(--geo-navy, #1a1a2e); font-weight: 900; background: var(--geo-gold, #ffd54f); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 6px 6px 0 var(--geo-navy, #1a1a2e); transition: transform .2s ease, box-shadow .2s ease; }
.fab-container:hover { color: var(--geo-navy, #1a1a2e); background: var(--geo-coral, #ff6b6b); box-shadow: 9px 9px 0 var(--geo-navy, #1a1a2e); transform: translate(-3px, -3px); }

.preview-box { padding: 22px; background: var(--card-color, var(--geo-sky, #53bde8)); border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 6px 6px 0 var(--geo-navy, #1a1a2e); }
.preview-box .transparent-textarea :deep(.el-textarea__inner) { color: var(--geo-navy, #1a1a2e); font-weight: 750; }
.preview-box .transparent-textarea :deep(.el-textarea__inner)::placeholder { color: rgba(26, 26, 46, .5); }
.random-tip { display: flex; margin-top: 17px; align-items: center; justify-content: center; gap: 8px; color: var(--geo-navy, #1a1a2e); font-weight: 800; }
.random-tip span { width: 22px; height: 7px; background: linear-gradient(90deg, var(--geo-coral, #ff6b6b) 0 50%, var(--geo-sky, #53bde8) 50%); border: 1px solid var(--geo-navy, #1a1a2e); }

@media (max-width: 760px) {
  .wall-header { min-height: 300px; padding: 46px 22px; }
  .wall-header h1 { font-size: 54px; }
  .header-shapes { opacity: .24; }
  .shape-square { right: -28px; }
  .shape-circle { right: 12px; }
  .shape-triangle { display: none !important; }
  .wall-content { padding: 42px 18px 0; }
  .section-heading { align-items: flex-start; flex-direction: column; }
  .section-heading p { padding-left: 66px; }
  .card-grid { grid-template-columns: 1fr; }
  .flip-card-container { aspect-ratio: 1 / .9; }
  .fab-container { right: 20px; bottom: 22px; }
}
</style>

<style>
.remark-dialog { border: 3px solid var(--geo-navy, #1a1a2e) !important; border-radius: 0 !important; box-shadow: 10px 10px 0 var(--geo-coral, #ff6b6b) !important; }
.remark-dialog .el-dialog__header { padding: 20px 24px; background: var(--geo-gold-light, #fff5c2); border-bottom: 3px solid var(--geo-navy, #1a1a2e); }
.remark-dialog .el-dialog__title { color: var(--geo-navy, #1a1a2e); font-size: 22px; font-weight: 950; }
.remark-dialog .el-dialog__headerbtn { top: 13px; right: 15px; width: 34px; height: 34px; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); }
.remark-dialog .el-dialog__close { color: var(--geo-navy, #1a1a2e); }
.remark-dialog .el-dialog__body { padding: 24px; }
.remark-dialog .el-dialog__footer { padding: 17px 24px 23px; border-top: 2px solid var(--geo-navy, #1a1a2e); }
.remark-dialog .el-button { height: 38px; color: var(--geo-navy, #1a1a2e); font-weight: 900; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 3px 3px 0 var(--geo-sky, #53bde8); }
.remark-dialog .el-button--primary { background: var(--geo-gold, #ffd54f); border-color: var(--geo-navy, #1a1a2e); box-shadow: 4px 4px 0 var(--geo-navy, #1a1a2e); }
</style>
