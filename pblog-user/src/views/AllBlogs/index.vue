<template>
  <section class="allBlogs-search-header">

    <!-- 搜索框区域 -->
    <div class="search-input-container">
      <el-input
          v-model="keyword"
          placeholder="搜索感兴趣的文章..."
          class="custom-search-input"
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
      >
        <template #prefix>
          <el-icon class="el-input__icon"><search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="search-filter">
      <div class="search-conditions">

        <!-- 排序方式 -->
        <div class="tags-row">
          <span class="category-label">排序：</span>
          <el-check-tag
              v-for="(label, index) in sortOptions"
              :key="index"
              :checked="activeSortField === index"
              type="primary"
              :class="{'custom-check-tag': activeSortField !== index}"
              @change="handleSortFieldClick(index)"
          >
            {{ label }}
          </el-check-tag>
        </div>

        <!-- 一级标签 (分类) -->
        <div class="tags-row">
          <span class="category-label">分类：</span>
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

        <!-- 二级/三级标签容器 -->
        <div v-if="selectedFirstLevelTagId" class="tag-content-container">
          <div v-for="secondTag in secondLevelTags" :key="secondTag.id" class="tags-row sub-tags-row">
            <el-check-tag
                :checked="selectedTagIds.includes(secondTag.id)"
                type="primary"
                @change="handleTagClick(secondTag.id)"
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
                  @change="handleTagClick(thirdTag.id)"
                  :disabled="isTagDisabled(thirdTag)"
              >
                {{ thirdTag.categoryName }}
              </el-check-tag>
            </div>
          </div>
          <div v-if="secondLevelTags.length === 0" class="no-sub-tags">暂无子标签</div>
        </div>

        <!-- 已选择标签展示区 -->
        <div class="tags-row selected-area">
          <span class="selected-tags-label">已选择:</span>
          <div class="selected-tags-container">
            <el-tag
                v-for="tagId in selectedTagIds"
                :key="tagId"
                closable
                @close="handleTagRemove(tagId)"
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

    <div class="mt-4">
      <div class="divider"></div>
    </div>
  </section>

  <!-- 内容区域：添加 loading -->
  <section class="allBlogs-search-content" v-loading="loading" element-loading-text="正在寻找精彩内容...">

    <!-- 空状态 -->
    <el-empty
        v-if="articles.length === 0 && !loading"
        description="暂无相关文章"
    />

    <!-- 文章列表 -->
    <div class="article-grid" v-else>
      <ArticlesItem
          v-for="article in articles"
          :key="article.id"
          :article="article"
      />
    </div>

    <!-- 分页组件 (修改点) -->
    <!-- layout 中移除了 sizes，page-size 固定绑定 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAllCategorys } from '@/apis/category.js'
import { pageQueryArticles } from '@/apis/article/article.js'
import { ElMessage } from 'element-plus'
import ArticlesItem from "@/views/Home/components/ArticlesItem.vue";

// ================= 状态定义 ==================
const tags = ref([])
const selectedFirstLevelTagId = ref(null)
const selectedTagIds = ref([])

// 搜索与排序
const keyword = ref('')
const activeSortField = ref(0)
const sortOptions = ['综合排序', '最多浏览', '最新发布', '最多评论', '最多点赞']

// 分页与数据 (修改点)
const currentPage = ref(1)
const pageSize = ref(20) // 固定为20，不再提供修改入口
const total = ref(0)
const articles = ref([])
const loading = ref(false)

// ================= 计算属性 (标签逻辑 - 保持不变) ==================
const tagMap = computed(() => {
  const map = new Map()
  tags.value.forEach(tag => map.set(tag.id, tag))
  return map
})
const tagChildrenMap = computed(() => {
  const map = new Map()
  tags.value.forEach(tag => {
    const pid = tag.parentId || 0
    if (!map.has(pid)) map.set(pid, [])
    map.get(pid).push(tag)
  })
  map.forEach(list => list.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0)))
  return map
})
const firstLevelTags = computed(() => tagChildrenMap.value.get(0) || [])
const secondLevelTags = computed(() => {
  if (!selectedFirstLevelTagId.value) return []
  return tagChildrenMap.value.get(selectedFirstLevelTagId.value) || []
})
const thirdLevelTagsMap = computed(() => {
  const result = {}
  secondLevelTags.value.forEach(secondTag => {
    const children = tagChildrenMap.value.get(secondTag.id)
    if (children && children.length > 0) {
      result[secondTag.id] = children
    }
  })
  return result
})

// ================= 事件处理 ==================
const getTagById = (id) => tagMap.value.get(id) || { categoryName: '未知' }

const handleSearch = () => {
  currentPage.value = 1
  fetchArticles()
}

const handleSortFieldClick = (idx) => {
  if (activeSortField.value === idx) return
  activeSortField.value = idx
  currentPage.value = 1
  fetchArticles()
}

const handleFirstLevelTagClick = (tagId) => {
  selectedFirstLevelTagId.value = selectedFirstLevelTagId.value === tagId ? null : tagId
}

const isTagDisabled = (tag) => {
  if (selectedTagIds.value.includes(tag.id)) return false
  const children = tagChildrenMap.value.get(tag.id)
  if (children && children.length > 0) {
    if (children.some(child => selectedTagIds.value.includes(child.id))) return true
  }
  if (tag.parentId && selectedTagIds.value.includes(tag.parentId)) return true
  return false
}

const handleTagClick = (tagId) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    if (selectedTagIds.value.length >= 10) {
      ElMessage.warning('最多只能选择10个标签')
      return
    }
    selectedTagIds.value.push(tagId)
  }
  currentPage.value = 1
  fetchArticles()
}

const handleTagRemove = (tagId) => {
  selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId)
  fetchArticles()
}

const clearAllTags = () => {
  selectedTagIds.value = []
  fetchArticles()
}

// ================= 核心：数据请求 (修改点) ==================

const handleCurrentChange = (val) => {
  currentPage.value = val
  window.scrollTo({ top: 0, behavior: 'smooth' })
  fetchArticles()
}

const getSortParams = () => {
  switch (activeSortField.value) {
    case 1: return { sortField: 'viewCount', sortDir: 'desc' }
    case 2: return { sortField: 'publishedAt', sortDir: 'desc' }
    case 3: return { sortField: 'commentCount', sortDir: 'desc' }
    case 4: return { sortField: 'likeCount', sortDir: 'desc' }
    case 0: default: return { sortField: null, sortDir: 'desc' }
  }
}

/**
 * 辅助函数：延迟执行
 * @param {number} ms 毫秒
 */
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const fetchArticles = async () => {
  loading.value = true

  const { sortField, sortDir } = getSortParams()
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value, // 传递固定值 20
    categoryIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : null,
    keyword: keyword.value.trim() || null,
    sortField: sortField,
    sortDir: sortDir,
  }

  try {
    // 修改点 1：使用 Promise.all 并行执行 API 请求和 1秒 定时器
    // 只有当两者都完成时，才会继续往下执行
    // 结果数组中：result[0] 是 API 响应，result[1] 是 delay 的返回值(undefined)
    const [res] = await Promise.all([
      pageQueryArticles(params),
      delay(1000) // 强制等待至少 1 秒
    ])

    // 修改点 3：处理 total
    if (res.data) {
      const pageData = res.data.data || res.data
      articles.value = pageData.records || pageData.list || []

      // 后端返回的总条数，组件会自动根据 pageSize=20 计算页数
      // 例如 total=41, pageSize=20 -> 组件显示 3 页
      total.value = parseInt(pageData.total || 0)
    }
  } catch (error) {
    console.error('Fetch articles error:', error)
    ElMessage.error('加载失败，请重试')
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取标签数据
const getTags = async () => {
  try {
    const res = await getAllCategorys()
    const data = res.data?.data || res.data
    if (Array.isArray(data)) {
      tags.value = data
    }
  } catch (error) {
    ElMessage.error('标签加载失败')
  }
}

onMounted(() => {
  getTags()
  fetchArticles()
})
</script>

<style scoped>
/* ============================================
   AllBlogs — Geometric Theme Styles
   ============================================ */

/* --- Search Header --- */
.allBlogs-search-header {
  background: var(--color-bg-card);
  padding-bottom: 0;
  position: relative;
  overflow: hidden;
}

/* Subtle geometric corner decoration */
.allBlogs-search-header::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, var(--geo-coral-light) 0%, transparent 60%);
  border-radius: 50%;
  opacity: 0.3;
  pointer-events: none;
}

.allBlogs-search-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 60px;
  width: 0;
  height: 0;
  border-left: 18px solid transparent;
  border-right: 18px solid transparent;
  border-bottom: 24px solid var(--geo-gold-light);
  opacity: 0.35;
  pointer-events: none;
}

/* --- Search Input --- */
.search-input-container {
  display: flex;
  justify-content: center;
  padding: 30px 96px 0;
  position: relative;
  z-index: 1;
}

.custom-search-input {
  width: 100%;
  max-width: 600px;
}

:deep(.custom-search-input .el-input__wrapper) {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-geo);
  transition: box-shadow 0.3s ease;
}

:deep(.custom-search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--geo-coral-light), var(--shadow-md);
}

:deep(.custom-search-input .el-input-group__append) {
  background: linear-gradient(135deg, var(--geo-coral), var(--geo-coral-dark));
  color: #fff;
  border: none;
  border-radius: 0 var(--radius-xl) var(--radius-xl) 0;
  box-shadow: none;
  font-weight: 500;
}

:deep(.custom-search-input .el-input-group__append:hover) {
  background: linear-gradient(135deg, var(--geo-coral-dark), var(--geo-coral));
}

/* --- Search Filter / Conditions --- */
.search-conditions {
  margin-top: 20px;
  padding: 0 96px;
  position: relative;
  z-index: 1;
}

.tags-row {
  margin-bottom: 14px;
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
}

.category-label {
  font-size: 14px;
  color: var(--color-text-primary);
  margin-right: 12px;
  line-height: 24px;
  flex-shrink: 0;
  font-weight: 700;
}

/* --- Tag Chips --- */
.el-check-tag {
  margin-right: 12px;
  margin-bottom: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: var(--radius-md);
}

.custom-check-tag {
  background-color: transparent !important;
  color: var(--color-text-secondary) !important;
  border: 1px solid transparent !important;
}

.custom-check-tag:hover {
  color: var(--geo-coral) !important;
  background-color: var(--geo-coral-light) !important;
}

/* Checked tags use coral theme */
:deep(.el-check-tag.is-checked) {
  background-color: var(--geo-coral) !important;
  color: #fff !important;
  border-color: var(--geo-coral) !important;
}

/* --- Sub-tag Content Container --- */
.tag-content-container {
  padding: 20px;
  background: var(--color-bg-soft);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  margin-left: 54px;
  position: relative;
  border-left: 3px solid var(--geo-sky);
}

.tag-content-container::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 20px;
  border-width: 0 8px 8px;
  border-style: solid;
  border-color: transparent transparent var(--color-bg-soft);
}

/* Small geometric accent inside tag container */
.tag-content-container::after {
  content: '';
  position: absolute;
  top: 10px;
  right: 14px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 10px solid var(--geo-gold-light);
  opacity: 0.6;
  pointer-events: none;
}

.sub-tags-row {
  margin-bottom: 8px;
  align-items: center;
}
.sub-tags-row:last-child {
  margin-bottom: 0;
}

.second-level-tag {
  font-weight: 500;
}

.tag-separator {
  color: var(--geo-gray-mid);
  margin: 0 8px 8px 0;
  font-weight: bold;
}

.third-tag-group {
  display: inline-flex;
  flex-wrap: wrap;
}

.no-sub-tags {
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 10px 0;
}

/* --- Selected Tags Area --- */
.selected-area {
  margin-top: 20px;
  align-items: center;
  min-height: 32px;
}

.selected-tags-label {
  color: var(--color-text-primary);
  margin-right: 12px;
  font-size: 14px;
  font-weight: 700;
}

.selected-tags-container {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* Selected tag pills use sky accent */
:deep(.selected-area .el-tag) {
  background-color: var(--geo-sky-light);
  color: var(--geo-sky-dark);
  border-color: var(--geo-sky);
  border-radius: var(--radius-md);
  font-weight: 500;
}

:deep(.selected-area .el-tag .el-tag__close) {
  color: var(--geo-sky-dark);
}

:deep(.selected-area .el-tag .el-tag__close:hover) {
  background-color: var(--geo-sky);
  color: #fff;
}

.no-selected-tips {
  color: var(--color-text-muted);
  font-size: 14px;
}

.clear-btn {
  margin-left: 12px;
  color: var(--geo-coral) !important;
  font-weight: 500;
}

.clear-btn:hover {
  color: var(--geo-coral-dark) !important;
}

/* --- Divider (gradient line) --- */
.divider {
  height: 3px;
  width: 100%;
  margin-top: 20px;
  background: linear-gradient(
    90deg,
    var(--geo-coral) 0%,
    var(--geo-gold) 33%,
    var(--geo-sky) 66%,
    var(--geo-navy) 100%
  );
  border-radius: 2px;
  opacity: 0.6;
}

/* --- Content Area --- */
.allBlogs-search-content {
  padding: 0 96px;
  min-height: 400px;
  background: var(--color-bg-soft);
}

/* Loading text color */
:deep(.el-loading-spinner .el-loading-text) {
  color: var(--geo-coral);
}

:deep(.el-loading-spinner .path) {
  stroke: var(--geo-coral);
}

/* --- Article Grid --- */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding: 24px 0;
  align-items: stretch;
}

/* --- Pagination --- */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 40px;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: var(--color-text-primary);
  --el-pagination-button-color: var(--geo-coral);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--geo-coral);
  color: #fff;
  border-radius: var(--radius-md);
}

:deep(.el-pagination button:hover),
:deep(.el-pagination .el-pager li:hover) {
  color: var(--geo-coral);
}

/* --- Empty State --- */
:deep(.el-empty__description p) {
  color: var(--color-text-muted);
}

/* --- Responsive --- */
@media (max-width: 992px) {
  .search-input-container {
    padding: 24px 40px 0;
  }
  .search-conditions {
    padding: 0 40px;
  }
  .allBlogs-search-content {
    padding: 0 40px;
  }
}

@media (max-width: 768px) {
  .search-input-container {
    padding: 20px 16px 0;
  }
  .search-conditions {
    padding: 0 16px;
  }
  .allBlogs-search-content {
    padding: 0 16px;
  }
  .article-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .tag-content-container {
    margin-left: 0;
  }
}

@media (max-width: 576px) {
  .search-input-container {
    padding: 16px 12px 0;
  }
  .search-conditions {
    padding: 0 12px;
  }
  .allBlogs-search-content {
    padding: 0 12px;
  }
}
</style>