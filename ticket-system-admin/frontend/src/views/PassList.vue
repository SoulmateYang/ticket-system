<template>
  <div class="pass-list">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">年/月卡管理</h1>
        <p class="page-subtitle">ANNUAL & MONTHLY PASS</p>
      </div>
      <button class="create-btn" @click="showCreateDialog = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14m-7-7h14"/></svg>
        <span>创建年/月卡</span>
      </button>
    </div>

    <div class="search-panel">
      <div class="search-field"><label>访客姓名</label><input v-model="searchForm.visitorName" type="text" placeholder="输入姓名搜索" class="search-input" /></div>
      <div class="search-field"><label>手机号</label><input v-model="searchForm.phone" type="text" placeholder="输入手机号" class="search-input" /></div>
      <div class="search-field"><label>卡种</label>
        <select v-model="searchForm.type" class="search-select">
          <option value="">全部</option>
          <option value="YEAR_PASS">年卡</option>
          <option value="MONTH_PASS">月卡</option>
        </select>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>查询</button>
        <button class="btn-reset" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th><th>访客姓名</th><th>手机号</th><th>身份证号</th><th>卡种</th><th>状态</th><th>有效期限</th><th>人脸模板</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(pass, index) in passes" :key="pass.id" class="table-row" :style="{ animationDelay: `${index * 0.05}s` }" @mouseenter="highlightRow($event)" @mouseleave="unhighlightRow($event)">
            <td class="cell-id">{{ pass.id }}</td>
            <td class="cell-name">{{ pass.visitorName }}</td>
            <td class="cell-phone">{{ pass.phone }}</td>
            <td class="cell-idcard">{{ pass.visitorId }}</td>
            <td><span class="type-badge" :class="pass.type">{{ pass.type === 'YEAR_PASS' ? '年卡' : '月卡' }}</span></td>
            <td><span class="status-badge" :class="pass.status">{{ getStatusName(pass.status) }}</span></td>
            <td class="cell-date"><span>{{ pass.validFrom?.slice(0, 10) || '-' }}</span><span class="date-sep">至</span><span>{{ pass.validTo?.slice(0, 10) || '-' }}</span></td>
            <td><span v-if="pass.faceTemplate" class="face-tag face-yes">已录入</span><span v-else class="face-tag face-no">未录入</span></td>
            <td class="cell-actions">
              <button v-if="pass.status === 'PENDING'" class="action-btn action-success" @click="handleActivate(pass)">激活</button>
              <button v-if="pass.status === 'ACTIVE'" class="action-btn action-warning" @click="handleSuspend(pass)">挂失</button>
              <button v-if="pass.status === 'SUSPENDED'" class="action-btn action-success" @click="handleActivate(pass)">恢复</button>
              <button v-if="pass.status !== 'CANCELLED'" class="action-btn action-danger" @click="handleCancel(pass)">注销</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span class="pagination-info">共 {{ pagination.total }} 条</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; fetchPasses()">上一页</button>
          <span class="page-indicator">{{ pagination.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pagination.page >= totalPages" @click="pagination.page++; fetchPasses()">下一页</button>
        </div>
      </div>
    </div>

    <div class="dialog-overlay" v-if="showCreateDialog" @click.self="showCreateDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h2>创建年/月卡</h2>
          <button class="dialog-close" @click="showCreateDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body">
          <div class="form-field"><label>访客姓名</label><input v-model="createForm.visitorName" type="text" placeholder="请输入访客姓名" /></div>
          <div class="form-field"><label>手机号</label><input v-model="createForm.phone" type="text" placeholder="请输入手机号" /></div>
          <div class="form-field"><label>身份证号</label><input v-model="createForm.visitorId" type="text" placeholder="请输入身份证号" /></div>
          <div class="form-field"><label>卡种类型</label>
            <select v-model="createForm.type" class="form-select">
              <option value="YEAR_PASS">年卡</option>
              <option value="MONTH_PASS">月卡</option>
            </select>
          </div>
          <div class="form-field"><label>有效起始日</label><input v-model="createForm.validFrom" type="date" /></div>
          <div class="form-field"><label>有效截止日</label><input v-model="createForm.validTo" type="date" /></div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showCreateDialog = false">取消</button>
          <button class="btn-submit" @click="handleCreate" :disabled="submitting">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTicketStore } from '../stores/ticket'

const ticketStore = useTicketStore()
const passes = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const submitting = ref(false)

const searchForm = reactive({ visitorName: '', phone: '', type: '' })
const createForm = reactive({ visitorName: '', phone: '', visitorId: '', type: 'YEAR_PASS', validFrom: '', validTo: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.size)))

const getStatusName = (status) => ({ PENDING: '待激活', ACTIVE: '有效', SUSPENDED: '已挂失', CANCELLED: '已注销' }[status] || status)
const highlightRow = (e) => e.currentTarget.classList.add('row-highlighted')
const unhighlightRow = (e) => e.currentTarget.classList.remove('row-highlighted')

const handleSearch = () => { pagination.page = 1; fetchPasses() }
const resetSearch = () => { searchForm.visitorName = ''; searchForm.phone = ''; searchForm.type = ''; handleSearch() }

const fetchPasses = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page - 1, size: pagination.size }
    if (searchForm.visitorName) params.visitorName = searchForm.visitorName
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.type) params.type = searchForm.type
    await ticketStore.fetchPasses(params)
    passes.value = ticketStore.passes
    pagination.total = ticketStore.passesTotal
  }
  catch { ElMessage.error('获取年/月卡列表失败') } finally { loading.value = false }
}

const handleCreate = async () => {
  if (!createForm.visitorName || !createForm.visitorId || !createForm.validFrom || !createForm.validTo) {
    ElMessage.error('请填写完整信息')
    return
  }
  if (new Date(createForm.validTo) < new Date(createForm.validFrom)) {
    ElMessage.error('截止日不能早于起始日')
    return
  }
  const payload = { ...createForm, validFrom: createForm.validFrom + 'T00:00:00', validTo: createForm.validTo + 'T23:59:59' }
  submitting.value = true
  try { await ticketStore.createPass(payload); ElMessage.success('创建成功'); showCreateDialog.value = false; fetchPasses() }
  catch (e) { ElMessage.error(e.response?.data?.message || e.response?.data?.error || '创建失败') } finally { submitting.value = false }
}

const handleActivate = async (pass) => { try { await ticketStore.activatePass(pass.id); ElMessage.success('激活成功'); fetchPasses() } catch (e) { ElMessage.error(e.response?.data?.message || '激活失败') } }
const handleSuspend = async (pass) => { try { await ticketStore.suspendPass(pass.id); ElMessage.success('挂失成功'); fetchPasses() } catch (e) { ElMessage.error(e.response?.data?.message || '挂失失败') } }
const handleCancel = async (pass) => { try { await ElMessageBox.confirm('确定要注销此年/月卡吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }); await ticketStore.cancelPass(pass.id); ElMessage.success('注销成功'); fetchPasses() } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '注销失败') } }

onMounted(() => { fetchPasses() })
</script>

<style scoped>
.pass-list { min-height: 100vh; width: 100%; font-family: 'Noto Serif SC', serif; background: var(--bg-main); display: flex; flex-direction: column; flex: 1; }

.page-header { display: flex; justify-content: space-between; align-items: flex-end; padding: 32px 48px 24px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.page-title { font-size: 28px; font-weight: 700; color: var(--text-primary); margin: 0; letter-spacing: 0.1em; }
.page-subtitle { font-size: 11px; color: var(--text-secondary); letter-spacing: 0.3em; margin: 4px 0 0 0; font-family: 'Arial', sans-serif; }

.create-btn { display: flex; align-items: center; gap: 8px; padding: 12px 20px; background: var(--accent); color: var(--bg-main); border: none; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; font-weight: 600; }
.create-btn:hover { background: var(--btn-primary-hover); transform: translateY(-2px); }
.create-btn svg { width: 18px; height: 18px; }

.search-panel { display: flex; gap: 20px; padding: 24px 48px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.search-field { display: flex; flex-direction: column; gap: 8px; }
.search-field label { font-size: 12px; color: var(--text-secondary); letter-spacing: 0.1em; }
.search-input, .search-select { padding: 10px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; background: var(--bg-card); color: var(--text-primary); min-width: 180px; transition: border-color 0.3s ease; }
.search-input::placeholder, .search-select::placeholder { color: var(--text-muted); }
.search-input:focus, .search-select:focus { outline: none; border-color: var(--accent); }
.search-actions { display: flex; align-items: flex-end; gap: 12px; margin-left: auto; }

.btn-search, .btn-reset { padding: 10px 20px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; }
.btn-search { background: var(--accent); color: var(--bg-main); border: none; display: flex; align-items: center; gap: 8px; font-weight: 600; }
.btn-search:hover { background: var(--btn-primary-hover); }
.btn-search svg { width: 16px; height: 16px; }
.btn-reset { background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); }
.btn-reset:hover { background: var(--bg-hover); border-color: var(--accent); }

.table-container { background: var(--bg-card); border-bottom: 1px solid var(--border); overflow: hidden; flex: 1; display: flex; flex-direction: column; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { padding: 16px 48px; text-align: left; font-size: 12px; font-weight: 600; color: var(--text-secondary); letter-spacing: 0.1em; background: var(--bg-secondary); border-bottom: 1px solid var(--border); }
.data-table td { padding: 16px 48px; border-bottom: 1px solid var(--border); font-size: 14px; color: var(--text-primary); }
.table-row { transition: background 0.2s ease; }
.table-row:hover { background: var(--bg-hover); }
.row-highlighted { background: var(--bg-hover); }

.cell-id { font-family: 'JetBrains Mono', monospace; color: var(--text-secondary); }
.cell-name { font-weight: 600; color: var(--text-primary); }
.cell-phone, .cell-idcard { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }

.type-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: var(--bg-main); }
.type-badge.YEAR_PASS { background: var(--danger); }
.type-badge.MONTH_PASS { background: var(--success); }

.status-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.status-badge.PENDING { background: var(--warning); color: var(--bg-main); }
.status-badge.ACTIVE { background: var(--success); color: var(--bg-main); }
.status-badge.SUSPENDED { background: var(--danger); color: white; }
.status-badge.CANCELLED { background: var(--text-muted); color: var(--text-primary); }

.cell-date { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }
.date-sep { margin: 0 6px; color: var(--text-muted); }

.face-tag { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.face-yes { background: var(--success); color: var(--bg-main); }
.face-no { background: var(--btn-secondary); color: var(--text-secondary); }

.cell-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.action-btn { padding: 6px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s ease; }
.action-success { background: var(--success); color: var(--bg-main); }
.action-warning { background: var(--warning); color: var(--bg-main); }
.action-danger { background: var(--danger); color: white; }
.action-btn:hover { transform: translateY(-1px); opacity: 0.9; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 20px 48px; border-top: 1px solid var(--border); background: var(--bg-secondary); flex-shrink: 0; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.pagination-controls { display: flex; align-items: center; gap: 16px; }
.page-btn { padding: 8px 16px; background: var(--btn-secondary); border: 1px solid var(--border); border-radius: 6px; font-size: 13px; color: var(--text-primary); cursor: pointer; transition: all 0.2s ease; }
.page-btn:hover:not(:disabled) { background: var(--accent); color: var(--bg-main); border-color: var(--accent); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-indicator { font-size: 13px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }

</style>