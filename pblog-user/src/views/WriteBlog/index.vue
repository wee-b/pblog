<template>
  <div class="page-container">
    <!-- 1. 顶部导航栏 -->
    <nav class="editor-nav">
      <div class="nav-left">
        <div class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <span class="divider"></span>
        <span class="brand-text">创作中心</span>
      </div>

      <div class="nav-center">
        <input
            v-model="article.title"
            placeholder="请输入文章标题..."
            class="title-input"
        />
      </div>

      <div class="write-nav-right">
        <span class="status-badge" :class="{ saved: saveStatusText === '已保存' }">
          <span class="dot"></span> {{ saveStatusText }}
        </span>

        <el-button round class="draft-btn" @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" round class="publish-btn" @click="openPublishDialog">
          去发布
          <el-icon class="el-icon--right"><Promotion /></el-icon>
        </el-button>
        <GlobalAvatar size="36"/>
      </div>
    </nav>

    <!-- 2. 主体编辑区域 -->
    <main class="editor-body">
      <div class="center-area">
        <div class="paper-container">
          <MdEditor
              v-model="article.content"
              class="my-md-editor"
              placeholder="开始您的创作..."
              :toolbars="toolbars"
              :preview="true"
              @onUploadImg="onUploadImg"
              @onSave="saveDraft"
          />
        </div>
      </div>
    </main>

    <!-- 引入发布弹窗组件 -->
    <PublishDialog
        v-model:visible="dialogVisible"
        :article-title="article.title"
        :article-content="article.content"
        :init-meta-form="metaForm"
        :init-selected-tags="selectedTagIds"
        :is-loading="isPublishing"
        @confirm="handlePublishConfirm"
        @close="handleDialogClose"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import { ElLoading, ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Promotion } from '@element-plus/icons-vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { getArticleDetail, insertArticle, updateArticle, insertDraft } from '@/apis/article/article';
import { uploadImage } from '@/apis/file';
import GlobalAvatar from "@/components/GlobalAvatar.vue";
// 引入弹窗组件
import PublishDialog from './components/PublishDialog.vue';
import type { PublishFormData } from '@/types/article'; // 导入公共类型

// --- 状态定义 ---
const dialogVisible = ref(false);
const isPublishing = ref(false);
const saveStatusText = ref('已保存');
const isLoading = ref(false);
const currentId = ref('');
// 存储 blob URL -> File 对象的映射（key: blob URL, value: File）
const localImageMap = ref<Map<string, File>>(new Map())

// 文章主体数据
const article = reactive({
  title: '',
  content: '# 宽屏编辑模式\n\n现在页面最大宽度已调整为 **1400px**。\n\n你可以更舒服地展示：\n\n1. 宽大的表格\n2. 复杂的代码块\n3. 流程图\n\n导航栏也进行了美化，更加简约现代。'
});

// 元数据（传给弹窗）
const metaForm = reactive({
  summary: '',
  cover: '',
  tagIds: [] as number[]
});

// 选中的标签ID
const selectedTagIds = ref<number[]>([]);

// 编辑器工具栏配置
const toolbars = [
  'bold', 'underline', 'italic', '-', 'title', 'quote', 'unorderedList', 'orderedList', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', '-',
  'revoke', 'next', 'save', '=', 'preview', 'catalog'
] as any;

// --- 路由相关 ---
const route = useRoute();
const router = useRouter();

const goBack = () => {
  router.push("/createCentre");
};

// --- 图片上传 ---
/**
 * 编辑器图片上传钩子（单次触发，仅处理本次新增的1张图片）
 * @param files 本次上传的文件数组（实际仅取第1张）
 * @param callback 回调函数，返回本地 blob URL 给编辑器
 */
const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  // 仅处理本次新增的单张图片（取数组第1个）
  const file = files?.[0];
  if (!file) {
    ElMessage.warning('请选择要上传的图片');
    return callback([]);
  }

  try {
    // 1. 基础校验（类型/大小）
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`${file.name} 不是图片格式，无法缓存`);
      return callback([]);
    }
    const maxSize = 5 * 1024 * 1024; // 5MB限制
    if (file.size > maxSize) {
      ElMessage.error(`${file.name} 大小超过5MB，请压缩后上传`);
      return callback([]);
    }

    // 2. 生成本地 blob URL（仅用于编辑器展示）
    const blobUrl = URL.createObjectURL(file);

    // 3. 存入本地映射表（记录本次新增的图片）
    localImageMap.value.set(blobUrl, file);

    // 4. 回调编辑器，插入本地 blob URL（仅返回本次的URL）
    callback([blobUrl]);
    ElMessage.success('图片已缓存，发布/保存时自动上传');
  } catch (error) {
    ElMessage.error('图片缓存失败，请重试');
    callback([]);
  }
};

/**
 * 批量上传所有本地缓存的图片，替换内容中的 blob URL
 * @param content 原文章内容（含 blob URL）
 * @returns 替换后的内容（含真实线上 URL）
 */
const uploadLocalImages = async (content: string): Promise<string> => {
  // 无本地缓存图片，直接返回原内容
  if (localImageMap.value.size === 0) {
    return content;
  }

  let newContent = content;
  const uploadResults: Array<{ oldUrl: string; newUrl: string }> = [];

  // 1. 遍历本次所有缓存的图片（单次上传的图片已存入映射表）
  for (const [blobUrl, file] of localImageMap.value.entries()) {
    try {
      // 上传本次缓存的单张图片
      const res = await uploadImage(file);
      if (res.code === 200 && res.data) {
        uploadResults.push({ oldUrl: blobUrl, newUrl: res.data });
      } else {
        ElMessage.error(`${file.name} 上传失败：${res.message || '接口异常'}`);
        uploadResults.push({ oldUrl: blobUrl, newUrl: blobUrl }); // 失败则保留原URL
      }
    } catch (error) {
      ElMessage.error(`${file.name} 上传失败：网络异常`);
      uploadResults.push({ oldUrl: blobUrl, newUrl: blobUrl });
    }
  }

  // 2. 替换内容中的所有 blob URL（包括本次新增的）
  uploadResults.forEach(({ oldUrl, newUrl }) => {
    newContent = newContent.replace(new RegExp(oldUrl, 'g'), newUrl);
    // 释放本次图片的 blob URL
    URL.revokeObjectURL(oldUrl);
  });

  // 3. 清空本地缓存（本次上传完成）
  localImageMap.value.clear();

  return newContent;
};
// --- 初始化页面 ---
const initPage = async () => {
  // 获取路由参数
  const id = route.params.id as string;

  if (id && id !== 'undefined' && id !== 'null') {
    // 编辑模式
    currentId.value = id;
    await loadArticleDetails(id);
  } else {
    // 新建模式
    resetForm();
  }
};

/**
 * 获取文章详细数据
 * @param id
 */
const loadArticleDetails = async (id: string) => {
  isLoading.value = true;
  try {
    const res = await getArticleDetail(id);

    if (res.data.code === 200 && res.data) {
      const data = res.data.data;

      // 填充文章数据
      article.title = data.title || '';
      article.content = data.content || '';

      // 填充元数据
      metaForm.summary = data.summary || '';
      metaForm.cover = data.coverImage || '';

      // 处理分类/标签
      if (data.categories && Array.isArray(data.categories)) {
        selectedTagIds.value = data.categories.map((cat: any) => cat.id);
        metaForm.tagIds = [...selectedTagIds.value];
      }
    } else {
      ElMessage.error(res.data.message || "获取文章详情失败");
    }
  } catch (error) {
    console.error('加载文章详情失败:', error);
    ElMessage.error("获取文章详情失败");
  } finally {
    isLoading.value = false;
  }
};

// --- 重置表单 ---
const resetForm = () => {
  article.title = '';
  article.content = '# 宽屏编辑模式\n\n现在页面最大宽度已调整为 **1400px**。\n\n你可以更舒服地展示：\n\n1. 宽大的表格\n2. 复杂的代码块\n3. 流程图\n\n导航栏也进行了美化，更加简约现代。';

  // 释放封面URL
  if (metaForm.cover) {
    URL.revokeObjectURL(metaForm.cover);
  }

  metaForm.summary = '';
  metaForm.cover = '';
  metaForm.tagIds = [];
  selectedTagIds.value = [];
};

/**
 * 保存草稿，不会更新封面
 */
const saveDraft = async () => {
  if (!article.title) {
    ElMessage.warning('请输入标题');
    return;
  }

  saveStatusText.value = '保存中...';
  const loading = ElLoading.service({
    text: '保存中...',
    background: 'rgba(0,0,0,0.1)'
  });
  ElMessage.info("保存草稿不会保存封面")

  try {
    // 构造草稿数据
    const draftData: any = {
      title: article.title.trim(),
      content: article.content.trim(),
      summary: metaForm.summary.trim() || '',
    };
    // 保存文章中的所有图片
    draftData.content = await uploadLocalImages(draftData.content);

    // 标签ID（非空时才传）
    if (selectedTagIds.value.length > 0) {
      draftData.tagIds = selectedTagIds.value;
    }

    if (currentId.value) {
      // 编辑模式 - 更新
      draftData.id = currentId.value;
      const res = await updateArticle(draftData);

      if (res.data.code !== 200) {
        throw new Error(res.data.message || '保存失败');
      }
    } else {
      // 新建模式 - 插入
      const res = await insertDraft(draftData);
      if (res.data.code === 200 && res.data) {
        currentId.value = res.data;

        // 更新URL
        await router.replace({
          name: 'writeBlogEdit',
          params: { id: currentId.value },
        });
      } else {
        throw new Error(res.data.message || '保存失败');
      }
    }

    saveStatusText.value = '已保存';
    ElMessage.success('草稿保存成功');
  } catch (error) {
    saveStatusText.value = '保存失败';
    ElMessage.error((error as Error).message || '草稿保存失败，请重试');
    console.error('保存草稿失败:', error);
  } finally {
    loading.close();
  }
};

// --- 发布弹窗相关 ---
// 打开发布弹窗
const openPublishDialog = () => {
  dialogVisible.value = true;
};

/**
 * 上传封面照片
 * @param file
 */
const uploadCoverImg = async (file: File) => {
  try {
    // 校验文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`${file.name} 不是图片格式，跳过上传`);
    }
    // 校验文件大小（5MB限制）
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      ElMessage.error(`${file.name} 大小超过5MB，请压缩后上传`);
    }

    // 上传图片
    const res = await uploadImage(file);
    if (res.code === 200 && res.data) {
      metaForm.cover = res.data;
      // 释放旧的临时URL（编辑模式下的旧封面）
      if (metaForm.cover && metaForm.cover.startsWith('blob:')) {
        URL.revokeObjectURL(metaForm.cover);
      }
      return res.data; // 返回真实URL，供发布逻辑使用
    } else {
      ElMessage.error(`${file.name} 上传失败：${res.message || '接口返回异常'}`);
      return null;
    }
  } catch (error) {
    ElMessage.error('图片上传失败，请检查网络或联系管理员');
    return null;
  }
};

/**
 * 提交发布表单
 * @param formData
 */
const handlePublishConfirm = async (formData: PublishFormData) => {
  isPublishing.value = true;

  try {
    // 构造提交数据
    const payload: any = {
      title: formData.title,
      content: formData.content,
      summary: formData.summary,
      coverImage: '',
      tagIds: formData.tagIds
    };
    // 上传新添加的图片
    payload.content = await uploadLocalImages(payload.content);

    // 有选中封面文件时才上传
    if (formData.coverFile) {
      // 调用文件上传接口（FormData 格式）
      const coverUrl = await uploadCoverImg(formData.coverFile);
      payload.coverImage = coverUrl;
    }else if (metaForm.cover && !metaForm.cover.startsWith('blob:')) {
      // 编辑模式：已有封面（非临时URL），直接复用
      payload.coverImage = metaForm.cover;
    }

    if (currentId.value) {
      // 编辑模式 - 更新文章
      payload.id = currentId.value;
      const res = await updateArticle(payload);

      if (res.data.code === 200) {
        ElMessage.success('文章更新成功！');
        dialogVisible.value = false;
        router.push('/createCentre');
      } else {
        throw new Error(res.data.message || '更新失败');
      }
    } else {
      // 新建模式 - 发布文章
      const res = await insertArticle(payload);

      if (res.data.code === 200 && res.data) {
        ElMessage.success('发布成功，请耐心等待审核');
        dialogVisible.value = false;

        currentId.value = res.data;
        // 更新URL
        await router.replace({
          name: 'writeBlogEdit',
          params: { id: currentId.value },
        });

        // 跳转到创作中心
        router.push('/createCentre');
      } else {
        throw new Error(res.data.message || '发布失败');
      }
    }
  } catch (error) {
    ElMessage.error((error as Error).message || '操作失败');
    console.error('发布失败:', error);
  } finally {
    isPublishing.value = false;
  }
};

// 处理弹窗关闭
const handleDialogClose = () => {
  // 释放封面临时URL（仅blob格式）
  if (metaForm.cover && metaForm.cover.startsWith('blob:')) {
    URL.revokeObjectURL(metaForm.cover);
    metaForm.cover = ''; // 清空临时URL
  }
};

// --- 生命周期 ---
onMounted(() => {
  initPage();
});

onUnmounted(() => {
  // 释放封面URL
  if (metaForm.cover) {
    URL.revokeObjectURL(metaForm.cover);
  }
});
</script>

<style lang="scss" scoped>
/* 定义设计变量 */
$nav-height: 64px;
$bg-color: #f7f9fc;
$primary-color: #2c3e50;
$accent-color: #409eff;

.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-color;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 导航栏样式 */
.editor-nav {
  height: $nav-height;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  z-index: 100;
  flex-shrink: 0;

  .nav-left {
    display: flex;
    align-items: center;
    width: 220px;

    .back-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      cursor: pointer;
      color: #606266;
      transition: background 0.2s;

      &:hover { background: #f2f3f5; color: #333; }
    }

    .divider {
      height: 16px;
      width: 1px;
      background: #e4e7ed;
      margin: 0 16px;
    }

    .brand-text {
      font-weight: 600;
      color: #303133;
      font-size: 15px;
    }
  }

  .nav-center {
    flex: 1;
    display: flex;
    justify-content: center;

    .title-input {
      width: 100%;
      max-width: 800px;
      height: 40px;
      border: none;
      background: transparent;
      outline: none;
      font-size: 18px;
      font-weight: 500;
      color: #333;
      text-align: center;
      border-radius: 6px;
      transition: all 0.3s;

      &:hover, &:focus {
        background: rgba(0,0,0,0.03);
      }
      &::placeholder { color: #a8abb2; }
    }
  }

  .write-nav-right {
    width: 400px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 12px;

    .status-badge {
      font-size: 12px;
      color: #909399;
      margin-right: 8px;
      display: flex;
      align-items: center;

      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #e6a23c;
        margin-right: 6px;
        transition: background 0.3s;
      }

      &.saved .dot { background: #67c23a; }
    }

    .draft-btn {
      color: #606266;
      border-color: #dcdfe6;
      &:hover {
        color: $accent-color;
        border-color: $accent-color;
        background-color: #ecf5ff;
      }
    }

    .publish-btn {
      background: #333;
      border-color: #333;
      font-weight: 500;
      &:hover {
        background: #000;
        border-color: #000;
      }
    }
  }
}

/* 编辑区域样式 */
.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;

  .center-area {
    flex: 1;
    overflow-y: auto;
    padding: 30px 20px;
    display: flex;
    justify-content: center;

    .paper-container {
      width: 100%;
      max-width: 1400px;
      min-height: calc(100vh - 120px);
      background: #fff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
      border-radius: 8px;
      display: flex;
      flex-direction: column;

      @media (max-width: 1440px) {
        max-width: 95%;
      }
      @media (max-width: 768px) {
        max-width: 100%;
        border-radius: 0;
        margin: -30px -20px;
      }
    }

    .my-md-editor {
      height: 100%;
      min-height: 800px;
      border: none !important;

      :deep(.md-editor-content-wrapper) {
        padding: 10px 40px;
      }
      :deep(.md-editor-toolbar-wrapper) {
        border-bottom: 1px solid #f2f2f2;
        padding: 6px 20px;
      }
    }
  }
}
</style>