<template>
  <section class="series-manage">
    <header class="manage-header">
      <div><p>CONTENT SERIES</p><h2>合集管理</h2><span>把文章整理成连续的阅读路径</span></div>
      <button type="button" class="primary-button" @click="openCreate">＋ 新建合集</button>
    </header>

    <div class="manage-toolbar">
      <el-input v-model="keyword" placeholder="搜索合集" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      <button type="button" @click="handleSearch">搜索</button>
    </div>

    <div v-loading="loading" class="series-grid">
      <article v-for="(item, index) in seriesList" :key="item.id" class="series-card" :class="`tone-${index % 3}`">
        <div class="card-art"><span></span><span></span><strong>{{ item.articleCount || 0 }}</strong></div>
        <div class="card-content">
          <small>SERIES / {{ String(index + 1).padStart(2, '0') }}</small>
          <h3>{{ item.seriesName }}</h3>
          <p>{{ item.description || '暂无简介' }}</p>
          <div class="card-actions">
            <button type="button" @click="openEdit(item.id)">编辑</button>
            <button type="button" class="danger" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </article>
    </div>

    <div v-if="!loading && seriesList.length === 0" class="empty-manage">
      <span></span><h3>还没有合集</h3><p>新建一个合集，把相关文章串联起来。</p>
    </div>

    <el-pagination v-if="total > query.pageSize" v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="prev, pager, next" @current-change="fetchSeries" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑合集' : '新建合集'" width="760px" class="series-dialog" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="合集名称" prop="seriesName">
            <el-input v-model="form.seriesName" maxlength="100" show-word-limit placeholder="例如：Vue 3 从入门到实践" />
          </el-form-item>
          <el-form-item label="合集简介" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="说明这个合集能帮助读者解决什么问题" />
          </el-form-item>
        </div>

        <div class="article-picker">
          <div class="picker-heading"><div><strong>选择文章</strong><span>已选择 {{ form.articleIds.length }} 篇</span></div><el-input v-model="articleKeyword" placeholder="筛选文章" clearable /></div>
          <div class="picker-columns">
            <div class="candidate-list">
              <label v-for="article in filteredCandidates" :key="article.id" class="candidate-item">
                <el-checkbox v-model="form.articleIds" :value="article.id" />
                <span><strong>{{ article.title }}</strong><small>{{ statusLabel(article.status) }}</small></span>
              </label>
              <p v-if="filteredCandidates.length === 0" class="picker-empty">没有可选文章</p>
            </div>
            <div class="selected-list">
              <div v-for="(article, index) in selectedArticles" :key="article.id" class="selected-item">
                <b>{{ index + 1 }}</b><span>{{ article.title }}</span>
                <div><button type="button" :disabled="index === 0" @click="moveArticle(index, -1)">↑</button><button type="button" :disabled="index === selectedArticles.length - 1" @click="moveArticle(index, 1)">↓</button></div>
              </div>
              <p v-if="selectedArticles.length === 0" class="picker-empty">勾选文章后可调整阅读顺序</p>
            </div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <button type="button" class="secondary-button" @click="dialogVisible = false">取消</button>
        <button type="button" class="primary-button" :disabled="saving" @click="submitForm">{{ saving ? '保存中...' : '保存合集' }}</button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createSeries, deleteSeries, getMySeriesDetail, getSeriesCandidateArticles, pageQueryMySeries, updateSeries } from '@/apis/series.js'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const total = ref(0)
const seriesList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const candidates = ref([])
const articleKeyword = ref('')
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 8 })
const form = reactive({ seriesName: '', description: '', coverFileId: null, articleIds: [] })
const rules = { seriesName: [{ required: true, message: '请输入合集名称', trigger: 'blur' }, { max: 100, message: '不能超过100个字符', trigger: 'blur' }], description: [{ max: 500, message: '不能超过500个字符', trigger: 'blur' }] }

const candidateMap = computed(() => new Map(candidates.value.map(item => [item.id, item])))
const filteredCandidates = computed(() => {
  const value = articleKeyword.value.trim().toLowerCase()
  return value ? candidates.value.filter(item => item.title?.toLowerCase().includes(value)) : candidates.value
})
const selectedArticles = computed(() => form.articleIds.map(id => candidateMap.value.get(id)).filter(Boolean))

const fetchSeries = async () => {
  loading.value = true
  try {
    const response = await pageQueryMySeries({ ...query, keyword: keyword.value.trim() || null, sortDir: 'desc' })
    const data = response.data?.data || response.data || {}
    seriesList.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) { console.error(error); ElMessage.error('合集列表加载失败') }
  finally { loading.value = false }
}

const loadCandidates = async () => {
  const response = await getSeriesCandidateArticles()
  candidates.value = response.data?.data || response.data || []
}

const resetForm = () => {
  editingId.value = null
  articleKeyword.value = ''
  Object.assign(form, { seriesName: '', description: '', coverFileId: null, articleIds: [] })
}

const openCreate = async () => {
  resetForm(); dialogVisible.value = true
  try { await loadCandidates() } catch (error) { console.error(error); ElMessage.error('文章列表加载失败') }
}

const openEdit = async (id) => {
  resetForm(); editingId.value = id; dialogVisible.value = true; saving.value = true
  try {
    const [detailResponse] = await Promise.all([getMySeriesDetail(id), loadCandidates()])
    const data = detailResponse.data?.data || detailResponse.data || {}
    Object.assign(form, { seriesName: data.seriesName || '', description: data.description || '', coverFileId: data.coverFileId || null, articleIds: (data.articles || []).map(item => item.id) })
  } catch (error) { console.error(error); ElMessage.error('合集信息加载失败'); dialogVisible.value = false }
  finally { saving.value = false }
}

const moveArticle = (index, offset) => {
  const target = index + offset
  if (target < 0 || target >= form.articleIds.length) return
  const next = [...form.articleIds]
  ;[next[index], next[target]] = [next[target], next[index]]
  form.articleIds = next
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      const payload = { seriesName: form.seriesName.trim(), description: form.description.trim(), coverFileId: form.coverFileId, articleIds: [...form.articleIds] }
      if (editingId.value) await updateSeries(editingId.value, payload)
      else await createSeries(payload)
      ElMessage.success(editingId.value ? '合集已更新' : '合集已创建')
      dialogVisible.value = false
      fetchSeries()
    } catch (error) { console.error(error); ElMessage.error(error.response?.data?.message || error.message || '保存失败') }
    finally { saving.value = false }
  })
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除合集「${item.seriesName}」吗？文章本身不会被删除。`, '删除合集', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await deleteSeries(item.id)
    ElMessage.success('合集已删除')
    if (seriesList.value.length === 1 && query.pageNum > 1) query.pageNum--
    fetchSeries()
  } catch (error) { if (error !== 'cancel' && error !== 'close') { console.error(error); ElMessage.error('删除失败') } }
}

const handleSearch = () => { query.pageNum = 1; fetchSeries() }
const statusLabel = status => ({ '0': '草稿', '1': '已发布', '2': '待审核' }[status] || '未知状态')
onMounted(fetchSeries)
</script>

<style scoped>
.series-manage { color: var(--geo-navy); }.manage-header { display: flex; margin-bottom: 28px; align-items: flex-end; justify-content: space-between; gap: 20px; }.manage-header p { margin: 0 0 6px; color: var(--geo-coral-dark); font-size: 10px; font-weight: 900; letter-spacing: .15em; }.manage-header h2 { margin: 0; font-size: 30px; }.manage-header span { display: block; margin-top: 7px; color: var(--color-text-muted); font-size: 13px; }
.primary-button,.secondary-button,.manage-toolbar button,.card-actions button { min-height: 40px; padding: 0 15px; color: var(--geo-navy); font-weight: 850; background: var(--geo-gold); border: 2px solid var(--geo-navy); box-shadow: 4px 4px 0 var(--geo-navy); cursor: pointer; }.secondary-button { margin-right: 12px; background: #fff; box-shadow: 4px 4px 0 var(--geo-coral); }.primary-button:disabled { opacity: .5; cursor: not-allowed; }
.manage-toolbar { display: flex; max-width: 430px; margin-bottom: 28px; }.manage-toolbar :deep(.el-input__wrapper) { min-height: 42px; border: 2px solid var(--geo-navy); border-radius: 0; box-shadow: none; }.manage-toolbar button { margin-left: -2px; box-shadow: 4px 4px 0 var(--geo-coral); }
.series-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); min-height: 140px; gap: 22px; }.series-card { display: grid; grid-template-columns: 130px 1fr; overflow: hidden; border: 2px solid var(--geo-navy); box-shadow: 6px 6px 0 var(--geo-navy); }.card-art { position: relative; min-height: 190px; overflow: hidden; background: var(--geo-gold-light); border-right: 2px solid var(--geo-navy); }.tone-1 .card-art { background: var(--geo-coral-light); }.tone-2 .card-art { background: var(--geo-sky-light); }.card-art span { position: absolute; border: 2px solid var(--geo-navy); }.card-art span:first-child { top: 26px; left: 20px; width: 54px; height: 54px; background: var(--geo-coral); transform: rotate(12deg); }.card-art span:nth-child(2) { right: 14px; bottom: 30px; width: 48px; height: 48px; background: var(--geo-sky); border-radius: 50%; }.card-art strong { position: absolute; bottom: 8px; left: 9px; font-size: 38px; }.card-content { padding: 20px; }.card-content small { color: var(--geo-coral-dark); font-weight: 900; letter-spacing: .12em; }.card-content h3 { margin: 7px 0; font-size: 20px; }.card-content > p { min-height: 40px; margin: 0; color: var(--color-text-secondary); font-size: 12px; line-height: 1.6; }.card-actions { display: flex; margin-top: 16px; gap: 9px; }.card-actions button { min-height: 34px; padding: 0 11px; font-size: 12px; box-shadow: 3px 3px 0 var(--geo-navy); }.card-actions .danger { background: var(--geo-coral-light); }
.empty-manage { padding: 70px 20px; text-align: center; border: 2px dashed var(--geo-navy); }.empty-manage > span { display: block; width: 52px; height: 52px; margin: 0 auto 18px; background: var(--geo-gold); border: 2px solid var(--geo-navy); box-shadow: 5px 5px 0 var(--geo-navy); transform: rotate(9deg); }.empty-manage h3 { margin: 0 0 8px; }.empty-manage p { color: var(--color-text-muted); }
.series-manage :deep(.el-pagination) { justify-content: center; margin-top: 34px; }.form-grid { display: grid; gap: 4px; }.article-picker { margin-top: 8px; border: 2px solid var(--geo-navy); }.picker-heading { display: flex; padding: 12px; align-items: center; justify-content: space-between; gap: 16px; background: var(--geo-gold-light); border-bottom: 2px solid var(--geo-navy); }.picker-heading > div { display: flex; flex-direction: column; }.picker-heading span { color: var(--color-text-muted); font-size: 11px; }.picker-heading .el-input { width: 220px; }.picker-heading :deep(.el-input__wrapper) { border: 2px solid var(--geo-navy); border-radius: 0; box-shadow: none; }.picker-columns { display: grid; grid-template-columns: 1fr 1fr; min-height: 240px; max-height: 340px; }.candidate-list,.selected-list { padding: 10px; overflow-y: auto; }.candidate-list { border-right: 2px solid var(--geo-navy); }.candidate-item { display: flex; padding: 10px 8px; gap: 8px; align-items: flex-start; cursor: pointer; border-bottom: 1px solid rgba(26,26,46,.12); }.candidate-item > span { display: flex; min-width: 0; flex-direction: column; }.candidate-item strong { overflow: hidden; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }.candidate-item small { color: var(--color-text-muted); }.selected-item { display: grid; grid-template-columns: 25px 1fr auto; padding: 8px; gap: 8px; align-items: center; border-bottom: 1px solid rgba(26,26,46,.12); }.selected-item b { display: grid; width: 24px; height: 24px; place-items: center; background: var(--geo-gold); border: 1px solid var(--geo-navy); }.selected-item span { overflow: hidden; font-size: 12px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }.selected-item button { width: 26px; height: 26px; margin-left: 3px; background: #fff; border: 1px solid var(--geo-navy); cursor: pointer; }.selected-item button:disabled { opacity: .3; }.picker-empty { padding: 40px 10px; color: var(--color-text-muted); font-size: 12px; text-align: center; }
@media (max-width:900px) { .series-grid { grid-template-columns: 1fr; } }
</style>

<style>
.series-dialog { border: 3px solid var(--geo-navy)!important; border-radius: 0!important; box-shadow: 9px 9px 0 var(--geo-navy)!important; }.series-dialog .el-dialog__header { padding: 20px 24px; background: var(--geo-gold-light); border-bottom: 2px solid var(--geo-navy); }.series-dialog .el-dialog__title { color: var(--geo-navy); font-weight: 900; }.series-dialog .el-dialog__body { padding: 22px 24px; }.series-dialog .el-dialog__footer { padding: 16px 24px 22px; }.series-dialog .el-input__wrapper,.series-dialog .el-textarea__inner { border: 2px solid var(--geo-navy); border-radius: 0; box-shadow: none; }.series-dialog .el-form-item__label { color: var(--geo-navy); font-weight: 800; }
@media (max-width:640px) { .series-dialog { width: calc(100% - 24px)!important; }.series-dialog .picker-heading { align-items: stretch; flex-direction: column; }.series-dialog .picker-heading .el-input { width: 100%; }.series-dialog .picker-columns { grid-template-columns: 1fr; max-height: 440px; }.series-dialog .candidate-list { max-height: 220px; border-right: 0; border-bottom: 2px solid var(--geo-navy); }.series-dialog .selected-list { max-height: 220px; } }
</style>
