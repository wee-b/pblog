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
/* 搜索框容器 */
.search-input-container {
  display: flex;
  justify-content: center;
  padding: 30px 96px 0;
}

.custom-search-input {
  width: 100%;
  max-width: 600px;
}

/* 现有样式保持不变，略作调整 */
.allBlogs-search-header {
  background: #fff;
  padding-bottom: 0;
}

.search-conditions {
  margin-top: 20px;
  padding: 0 96px;
}

.tags-row {
  margin-bottom: 14px;
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
}

.category-label {
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
  line-height: 24px;
  flex-shrink: 0;
  font-weight: bold;
}

.el-check-tag {
  margin-right: 12px;
  margin-bottom: 8px;
  font-weight: normal;
  transition: all 0.3s;
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
  padding: 20px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 20px;
  margin-left: 54px;
  position: relative;
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
}
.sub-tags-row:last-child {
  margin-bottom: 0;
}

.second-level-tag {
  font-weight: 500;
}

.tag-separator {
  color: #909399;
  margin: 0 8px 8px 0;
  font-weight: bold;
}

.third-tag-group {
  display: inline-flex;
  flex-wrap: wrap;
}

.no-sub-tags {
  color: #909399;
  font-size: 13px;
  padding: 10px 0;
}

.selected-area {
  margin-top: 20px;
  align-items: center;
  min-height: 32px; /* 防止没有选中时高度坍塌太严重 */
}

.selected-tags-label {
  color: #606266;
  margin-right: 12px;
  font-size: 14px;
  font-weight: bold;
}

.selected-tags-container {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.no-selected-tips {
  color: #999;
  font-size: 14px;
}

.clear-btn {
  margin-left: 12px;
}

.divider {
  height: 1px;
  background-color: #ebeef5;
  width: 100%;
  margin-top: 20px;
}

.allBlogs-search-content {
  padding: 0 96px;
  min-height: 400px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px 0;
  align-items: stretch;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 40px;
}
</style>