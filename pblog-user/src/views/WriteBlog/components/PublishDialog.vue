<template>
  <el-dialog
      v-model="visible"
      title="发布文章"
      width="640px"
      align-center
      destroy-on-close
      class="custom-dialog"
      @close="handleClose"
  >
    <el-form :model="metaForm" label-position="top" class="publish-form">
      <!-- 标签选择区域 -->
      <div class="form-row">
        <el-form-item label="标签" style="flex: 1;">
          <!-- 新增外层容器，明确分隔三个区块 -->
          <div class="tag-select-wrapper">
            <!-- 一级标签 (分类) - 独立区块 -->
            <div class="first-tag-wrapper">
              <div class="tags-row">
                <el-check-tag
                    v-for="tag in firstLevelTags"
                    :key="tag.id"
                    :checked="selectedFirstLevelTagId === tag.id"
                    type="primary"
                    :class="{'custom-check-tag': selectedFirstLevelTagId !== tag.id}"
                    @change="handleFirstLevelTagClick(tag.id)"
                >
                  {{ tag.categoryName }}
                </el-check-tag>
              </div>
            </div>

            <!-- 二级/三级标签容器 - 独立区块 -->
            <div v-if="selectedFirstLevelTagId" class="tag-content-container">
              <div v-for="secondTag in secondLevelTags" :key="secondTag.id" class="tags-row sub-tags-row">
                <el-check-tag
                    :checked="selectedTagIds.includes(secondTag.id)"
                    type="primary"
                    @change="() => handleTagClick(secondTag.id)"
                    :disabled="isTagDisabled(secondTag)"
                    class="second-level-tag"
                >
                  {{ secondTag.categoryName }}
                </el-check-tag>

                <span class="tag-separator" v-if="thirdLevelTagsMap[secondTag.id]">: </span>

                <div class="third-tag-group" v-if="thirdLevelTagsMap[secondTag.id]">
                  <el-check-tag
                      v-for="thirdTag in thirdLevelTagsMap[secondTag.id]"
                      :key="thirdTag.id"
                      :checked="selectedTagIds.includes(thirdTag.id)"
                      type="primary"
                      @change="() => handleTagClick(thirdTag.id)"
                      :disabled="isTagDisabled(thirdTag)"
                  >
                    {{ thirdTag.categoryName }}
                  </el-check-tag>
                </div>
              </div>
              <div v-if="secondLevelTags.length === 0" class="no-sub-tags">暂无子标签</div>
            </div>

            <!-- 已选择标签展示区 - 独立区块 -->
            <div class="selected-tags-wrapper">
              <div class="tags-row selected-area">
                <span class="selected-tags-label">已选择:</span>
                <div class="selected-tags-container">
                  <el-tag
                      v-for="tagId in selectedTagIds"
                      :key="tagId"
                      closable
                      @close="() => handleTagRemove(tagId)"
                      type="primary"
                      effect="plain"
                  >
                    {{ getTagById(tagId).categoryName }}
                  </el-tag>
                  <span v-if="selectedTagIds.length === 0" class="no-selected-tips">暂无选择</span>
                </div>
                <el-button
                    v-if="selectedTagIds.length > 0"
                    link type="primary" size="small"
                    @click="clearAllTags"
                    class="clear-btn"
                >
                  清空
                </el-button>
              </div>
            </div>
          </div>
        </el-form-item>
      </div>

      <!-- 摘要 -->
      <el-form-item label="摘要">
        <el-input
            v-model="metaForm.summary"
            type="textarea"
            :rows="3"
            placeholder="文章简介..."
            resize="none"
        />
      </el-form-item>

      <!-- 封面图 -->
      <el-form-item label="封面图">
        <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
        >
          <img v-if="metaForm.cover" :src="metaForm.cover" class="cover-img" />
          <div v-else class="uploader-content">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <span>点击上传封面 (16:9)</span>
          </div>
        </el-upload>
      </el-form-item>
    </el-form>

    <!-- 弹窗底部按钮 -->
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button @click="">保存并退出</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="isLoading">保存并发布</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getAllCategorys } from '@/apis/category.js';
import {PublishFormData} from "@/types/article";

const coverFile = ref<File | null>(null); // 保存选中的封面文件

import {CategoryVO } from '@/types/vo'

interface MetaForm {
  summary: string;
  cover: string;
  tagIds: number[];
}

interface Props {
  visible: boolean;
  articleTitle: string;
  articleContent: string;
  initMetaForm: MetaForm;
  initSelectedTags: number[];
  isLoading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  articleTitle: '',
  articleContent: '',
  initMetaForm: () => ({ summary: '', cover: '', tagIds: [] }),
  initSelectedTags: () => [],
  isLoading: false
});

// --- Emits 定义 ---
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', formData: PublishFormData): void; // 改用公共类型
  (e: 'close'): void;
}>()


// --- 状态定义 ---
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

// 标签相关状态
const tags = ref<CategoryVO[]>([]);
const selectedFirstLevelTagId = ref<number | null>(null);
const selectedTagIds = ref<number[]>([]);

// 表单数据
const metaForm = ref<MetaForm>({
  summary: '',
  cover: '',
  tagIds: []
});

// --- 计算属性 ---
const tagMap = computed(() => {
  const map = new Map<number, CategoryVO>();
  tags.value.forEach(tag => map.set(tag.id, tag));
  return map;
});

const tagChildrenMap = computed(() => {
  const map = new Map<number, CategoryVO[]>();
  tags.value.forEach(tag => {
    const pid = tag.parentId || 0;
    if (!map.has(pid)) map.set(pid, []);
    map.get(pid)!.push(tag);
  });
  map.forEach(list => list.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0)));
  return map;
});

const firstLevelTags = computed(() => tagChildrenMap.value.get(0) || []);
const secondLevelTags = computed(() => {
  if (!selectedFirstLevelTagId.value) return [];
  return tagChildrenMap.value.get(selectedFirstLevelTagId.value) || [];
});

const thirdLevelTagsMap = computed(() => {
  const result: Record<number, CategoryVO[]> = {};
  secondLevelTags.value.forEach(secondTag => {
    const children = tagChildrenMap.value.get(secondTag.id);
    if (children && children.length > 0) {
      result[secondTag.id] = children;
    }
  });
  return result;
});

// --- 监听 props 变化，初始化数据 ---
watch([() => props.initMetaForm, () => props.initSelectedTags],
    ([newMeta, newTags]) => {
      // 初始化表单数据
      metaForm.value = { ...newMeta };
      // 初始化选中标签
      selectedTagIds.value = [...newTags];

      // 自动选中一级分类
      if (newTags.length > 0) {
        const firstTag = getTagById(newTags[0]);
        let firstLevelId: number | null = null;

        if (firstTag.parentId === 0) {
          firstLevelId = firstTag.id;
        } else {
          let parentTag = getTagById(firstTag.parentId);
          while (parentTag.parentId !== 0 && parentTag.id) {
            parentTag = getTagById(parentTag.parentId);
            if (parentTag.parentId === 0) {
              firstLevelId = parentTag.id;
              break;
            }
          }
        }
        selectedFirstLevelTagId.value = firstLevelId;
      }
    },
    { immediate: true }
);

// --- 方法定义 ---
// 获取标签信息
const getTagById = (id: number) => {
  return tagMap.value.get(id) || {
    categoryName: '未知',
    id: -1,
    parentId: 0,
    orderNum: 0
  };
};

// 加载标签数据
const loadTags = async () => {
  try {
    const res = await getAllCategorys();
    const data = res.data?.data || res.data;
    if (Array.isArray(data)) {
      tags.value = data as CategoryVO[];
    }
  } catch (error) {
    console.error('标签加载失败:', error);
    ElMessage.error('标签加载失败');
  }
};

// 一级标签点击
const handleFirstLevelTagClick = (tagId: number) => {
  if (selectedFirstLevelTagId.value === tagId) {
    selectedFirstLevelTagId.value = null;
    // 清空该分类下的子标签选择
    const childTags = tagChildrenMap.value.get(tagId) || [];
    const grandChildTags: number[] = [];
    childTags.forEach(child => {
      grandChildTags.push(...(tagChildrenMap.value.get(child.id) || []).map(t => t.id));
    });
    const needRemove = [...childTags.map(t => t.id), ...grandChildTags];
    selectedTagIds.value = selectedTagIds.value.filter(id => !needRemove.includes(id));
    metaForm.value.tagIds = [...selectedTagIds.value];
  } else {
    selectedFirstLevelTagId.value = tagId;
  }
};

// 标签禁用判断
const isTagDisabled = (tag: CategoryVO) => {
  if (selectedTagIds.value.includes(tag.id)) return false;

  const children = tagChildrenMap.value.get(tag.id);
  if (children && children.length > 0) {
    if (children.some(child => selectedTagIds.value.includes(child.id))) {
      return true;
    }
  }

  if (tag.parentId && selectedTagIds.value.includes(tag.parentId)) {
    return true;
  }

  return false;
};

// 标签点击
const handleTagClick = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId);
  if (index > -1) {
    selectedTagIds.value.splice(index, 1);
  } else {
    if (selectedTagIds.value.length >= 10) {
      ElMessage.warning('最多只能选择10个标签');
      return;
    }
    selectedTagIds.value.push(tagId);
  }
  metaForm.value.tagIds = [...selectedTagIds.value];

  // 自动选中一级分类
  const tag = getTagById(tagId);
  let firstLevelId: number | null = null;

  if (tag.parentId === 0) {
    firstLevelId = tag.id;
  } else {
    let parentTag = getTagById(tag.parentId);
    while (parentTag.parentId !== 0 && parentTag.id) {
      parentTag = getTagById(parentTag.parentId);
      if (parentTag.parentId === 0) {
        firstLevelId = parentTag.id;
        break;
      }
    }
  }

  if (firstLevelId && selectedFirstLevelTagId.value !== firstLevelId) {
    selectedFirstLevelTagId.value = firstLevelId;
  }
};

// 移除标签
const handleTagRemove = (tagId: number) => {
  selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId);
  metaForm.value.tagIds = [...selectedTagIds.value];

  // 更新一级分类选中状态
  const tag = getTagById(tagId);
  let firstLevelId: number | null = null;

  if (tag.parentId === 0) {
    firstLevelId = tag.id;
  } else {
    let parentTag = getTagById(tag.parentId);
    while (parentTag.parentId !== 0 && parentTag.id) {
      parentTag = getTagById(parentTag.parentId);
      if (parentTag.parentId === 0) {
        firstLevelId = parentTag.id;
        break;
      }
    }
  }

  if (firstLevelId) {
    const firstLevelTag = getTagById(firstLevelId);
    const childTags = tagChildrenMap.value.get(firstLevelTag.id) || [];
    const grandChildTags: number[] = [];
    childTags.forEach(child => {
      grandChildTags.push(...(tagChildrenMap.value.get(child.id) || []).map(t => t.id));
    });
    const allChildIds = [...childTags.map(t => t.id), ...grandChildTags];

    const hasSelected = allChildIds.some(id => selectedTagIds.value.includes(id));
    if (!hasSelected && selectedFirstLevelTagId.value === firstLevelId) {
      selectedFirstLevelTagId.value = null;
    }
  }
};

// 清空所有标签
const clearAllTags = () => {
  selectedTagIds.value = [];
  selectedFirstLevelTagId.value = null;
  metaForm.value.tagIds = [];
};

// 封面上传前处理
const beforeCoverUpload = (file: File) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片格式的文件');
    return false;
  }
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error(`${file.name} 大小超过5MB，请压缩后上传`);
    return null;
  }

  // 1. 释放旧的预览 URL（避免内存泄漏）
  if (metaForm.value.cover) {
    URL.revokeObjectURL(metaForm.value.cover);
  }

  // 2. 保存原始文件对象（核心！用于后续上传）
  coverFile.value = file;

  // 3. 生成预览 URL（仅用于页面展示）
  metaForm.value.cover = URL.createObjectURL(file);

  return false; // 阻止默认上传行为
};



// 确认发布
const handleConfirm = () => {
  // 基础校验
  if (!props.articleTitle?.trim()) {
    ElMessage.warning('请输入文章标题');
    return;
  }

  if (selectedTagIds.value.length === 0) {
    ElMessage.warning('请至少选择一个标签');
    return;
  }

  // 构造提交数据
  const submitData = {
    title: props.articleTitle.trim(),
    content: props.articleContent.trim(),
    summary: metaForm.value.summary.trim() || props.articleContent.slice(0, 100).trim(),
    coverFile: coverFile.value,  // 原始文件对象（父组件用这个上传）
    tagIds: selectedTagIds.value
  };

  // 触发确认事件
  emit('confirm', submitData);
};



// 关闭弹窗
const handleClose = () => {
  emit('close');
  // 释放封面URL
  if (metaForm.value.cover) {
    URL.revokeObjectURL(metaForm.value.cover);
  }
};

// --- 生命周期 ---
// 加载标签数据
loadTags();

// 组件卸载时清理资源
onUnmounted(() => {
  if (metaForm.value.cover) {
    URL.revokeObjectURL(metaForm.value.cover);
  }
});
</script>

<style lang="scss" scoped>
.publish-form {
  .form-row {
    display: flex;
    justify-content: space-between;
  }

  // 新增：标签选择外层容器，垂直排列各个区块
  .tag-select-wrapper {
    display: flex;
    flex-direction: column;
    gap: 16px; // 区块之间的间距
    width: 100%;
  }

  // 一级标签容器
  .first-tag-wrapper {
    width: 100%;
  }

  // 已选择标签容器
  .selected-tags-wrapper {
    width: 100%;
  }

  .cover-uploader {
    :deep(.el-upload) {
      width: 100%;
      border: 1px dashed #dcdfe6;
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      &:hover { border-color: var(--el-color-primary); }
    }

    .uploader-content {
      width: 100%;
      height: 180px;
      background: #fafafa;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      font-size: 13px;
      .upload-icon {
        font-size: 32px;
        margin-bottom: 8px;
        color: #c0c4cc;
      }
    }

    .cover-img {
      width: 100%;
      height: 180px;
      object-fit: cover;
    }
  }

  /* 标签选择样式 */
  .tags-row {
    margin-bottom: 0; // 移除默认margin，改用外层gap控制
    display: flex;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px; // 标签之间的间距
  }

  .custom-check-tag {
    background-color: transparent !important;
    color: #606266 !important;
    border: 1px solid transparent !important;
  }

  .custom-check-tag:hover {
    color: var(--el-color-primary) !important;
  }

  .tag-content-container {
    padding: 16px;
    background: #f7f8fa;
    border-radius: 8px;
    margin-bottom: 0; // 移除默认margin，改用外层gap控制
    margin-left: 0;
    position: relative;
    width: 100%; // 确保宽度100%
  }

  .tag-content-container::before {
    content: '';
    position: absolute;
    top: -8px;
    left: 20px;
    border-width: 0 8px 8px;
    border-style: solid;
    border-color: transparent transparent #f7f8fa;
  }

  .sub-tags-row {
    margin-bottom: 8px;
    align-items: center;
    gap: 8px;
  }

  .sub-tags-row:last-child {
    margin-bottom: 0;
  }

  .second-level-tag {
    font-weight: 500;
  }

  .tag-separator {
    color: #909399;
    margin: 0 8px 0 0; // 调整margin，避免和flex gap冲突
    font-weight: bold;
    align-self: center; // 垂直居中
  }

  .third-tag-group {
    display: inline-flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .no-sub-tags {
    color: #909399;
    font-size: 13px;
    padding: 10px 0;
    text-align: center;
  }

  .selected-area {
    margin-top: 0; // 移除默认margin，改用外层gap控制
    align-items: center;
    min-height: 32px;
    width: 100%;
  }

  .selected-tags-label {
    color: #606266;
    margin-right: 8px; // 调整间距
    font-size: 14px;
    font-weight: bold;
    flex-shrink: 0; // 防止标签被挤压
  }

  .selected-tags-container {
    display: inline-flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    flex: 1; // 占满剩余空间
  }

  .no-selected-tips {
    color: #999;
    font-size: 14px;
  }

  .clear-btn {
    margin-left: 12px;
    flex-shrink: 0; // 防止按钮被挤压
  }
}

.dialog-footer {
  text-align: right;
}
</style>