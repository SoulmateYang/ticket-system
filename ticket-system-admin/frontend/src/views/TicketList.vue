<template>
  <div class="ticket-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">票据列表</h1>
        <p class="page-subtitle">TICKET MANAGEMENT</p>
      </div>
      <button class="create-btn" @click="showCreateDialog = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 5v14m-7-7h14"/>
        </svg>
        <span>创建单票</span>
      </button>
    </div>

    <!-- Search Panel -->
    <div class="search-panel">
      <div class="search-field">
        <label>票码</label>
        <input v-model="searchForm.ticketCode" type="text" placeholder="输入票码搜索" class="search-input" />
      </div>
      <div class="search-field">
        <label>状态</label>
        <select v-model="searchForm.status" class="search-select">
          <option value="">全部状态</option>
          <option value="ACTIVE">有效</option>
          <option value="USED">已使用</option>
          <option value="EXPIRED">已过期</option>
          <option value="CANCELLED">已取消</option>
        </select>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          查询
        </button>
        <button class="btn-reset" @click="resetSearch">重置</button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>票码</th>
            <th>票种</th>
            <th>访客ID</th>
            <th>状态</th>
            <th>有效期限</th>
            <th>使用次数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(ticket, index) in tickets" :key="ticket.id" class="table-row" :style="{ animationDelay: `${index * 0.05}s` }" @mouseenter="highlightRow($event)" @mouseleave="unhighlightRow($event)">
            <td class="cell-id">{{ ticket.id }}</td>
            <td class="cell-code">{{ ticket.ticketCode }}</td>
            <td><span class="type-badge" :class="ticket.type">{{ getTypeName(ticket.type) }}</span></td>
            <td class="cell-visitor">{{ ticket.visitorId }}</td>
            <td><span class="status-badge" :class="ticket.status">{{ getStatusName(ticket.status) }}</span></td>
            <td class="cell-date"><span>{{ ticket.validFrom?.slice(0, 10) || '-' }}</span><span class="date-sep">至</span><span>{{ ticket.validTo?.slice(0, 10) || '-' }}</span></td>
            <td class="cell-entries"><span class="entries-count">{{ ticket.usedEntries || 0 }}</span><span class="entries-sep">/</span><span class="entries-max">{{ ticket.maxEntries || '-' }}</span></td>
            <td class="cell-actions">
              <button class="action-link" @click="viewDetail(ticket)">详情</button>
              <button v-if="ticket.status === 'ACTIVE'" class="action-link action-danger" @click="handleCancel(ticket)">取消</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span class="pagination-info">共 {{ pagination.total }} 条</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; fetchTickets()">上一页</button>
          <span class="page-indicator">{{ pagination.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pagination.page >= totalPages" @click="pagination.page++; fetchTickets()">下一页</button>
        </div>
      </div>
    </div>

    <!-- Create Dialog -->
    <div class="dialog-overlay" v-if="showCreateDialog" @click.self="showCreateDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h2>创建单票</h2>
          <button class="dialog-close" @click="showCreateDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body">
          <div class="form-field"><label>演出名称</label><input v-model="createForm.performanceName" type="text" placeholder="请输入演出名称" /></div>
          <div class="form-field"><label>数量</label><input v-model.number="createForm.quantity" type="number" min="1" max="100" /></div>
          <div class="form-field"><label>OTA渠道</label>
            <select v-model="createForm.channel" class="form-select">
              <option value="">无</option>
              <option value="MEITUAN">美团</option>
              <option value="DOUYIN">抖音</option>
              <option value="CTRIP">携程</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showCreateDialog = false">取消</button>
          <button class="btn-submit" @click="handleCreate" :disabled="submitting">创建</button>
        </div>
      </div>
    </div>

    <!-- Detail Dialog -->
    <div class="dialog-overlay" v-if="showDetailDialog" @click.self="showDetailDialog = false">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h2>票据详情</h2>
          <button class="dialog-close" @click="showDetailDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body" v-if="currentTicket">
          <div class="detail-grid">
            <div class="detail-item"><span class="detail-label">ID</span><span class="detail-value">{{ currentTicket.id }}</span></div>
            <div class="detail-item"><span class="detail-label">票码</span><span class="detail-value code">{{ currentTicket.ticketCode }}</span></div>
            <div class="detail-item"><span class="detail-label">票种</span><span class="detail-value">{{ getTypeName(currentTicket.type) }}</span></div>
            <div class="detail-item"><span class="detail-label">状态</span><span class="detail-value"><span class="status-badge" :class="currentTicket.status">{{ getStatusName(currentTicket.status) }}</span></span></div>
            <div class="detail-item"><span class="detail-label">访客ID</span><span class="detail-value">{{ currentTicket.visitorId || '-' }}</span></div>
            <div class="detail-item"><span class="detail-label">已用/最大次数</span><span class="detail-value">{{ currentTicket.usedEntries || 0 }} / {{ currentTicket.maxEntries || '-' }}</span></div>
            <div class="detail-item"><span class="detail-label">有效开始</span><span class="detail-value">{{ currentTicket.validFrom || '-' }}</span></div>
            <div class="detail-item"><span class="detail-label">有效结束</span><span class="detail-value">{{ currentTicket.validTo || '-' }}</span></div>
            <div class="detail-item full"><span class="detail-label">创建时间</span><span class="detail-value">{{ currentTicket.createdAt || '-' }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTicketStore } from '../stores/ticket'

const ticketStore = useTicketStore()
const tickets = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const submitting = ref(false)
const currentTicket = ref(null)

const searchForm = reactive({ ticketCode: '', status: '' })
const createForm = reactive({ performanceName: '', quantity: 1, channel: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.size)))

const getTypeName = (type) => ({ YEAR_PASS: '年卡', MONTH_PASS: '月卡', SINGLE_USE: '单次票', OTA_TICKET: 'OTA票' }[type] || type)
const getStatusName = (status) => ({ ACTIVE: '有效', USED: '已使用', EXPIRED: '已过期', CANCELLED: '已取消' }[status] || status)
const highlightRow = (e) => e.currentTarget.classList.add('row-highlighted')
const unhighlightRow = (e) => e.currentTarget.classList.remove('row-highlighted')

const handleSearch = () => { pagination.page = 1; fetchTickets() }
const resetSearch = () => { searchForm.ticketCode = ''; searchForm.status = ''; handleSearch() }

const fetchTickets = async () => {
  loading.value = true
  try { await ticketStore.fetchTickets({ page: pagination.page - 1, size: pagination.size }); tickets.value = ticketStore.tickets; pagination.total = ticketStore.tickets.length || 50 }
  catch { ElMessage.error('获取票据列表失败') } finally { loading.value = false }
}

const handleCreate = async () => {
  if (!createForm.performanceName || createForm.performanceName.trim() === '') {
    ElMessage.error('演出名称不能为空')
    return
  }
  if (!createForm.quantity || createForm.quantity <= 0) {
    ElMessage.error('数量必须大于0')
    return
  }
  submitting.value = true
  try { await ticketStore.createSingleTickets(createForm); ElMessage.success('创建成功'); showCreateDialog.value = false; fetchTickets() }
  catch (e) { ElMessage.error(e.response?.data?.message || '创建失败') } finally { submitting.value = false }
}

const viewDetail = (ticket) => { currentTicket.value = ticket; showDetailDialog.value = true }
const handleCancel = async (ticket) => { ElMessage.info('取消功能开发中') }

onMounted(() => { fetchTickets() })
</script>

<style scoped>

.ticket-list { min-height: 100vh; width: 100%; font-family: 'Noto Serif SC', serif; background: var(--bg-main); display: flex; flex-direction: column; flex: 1; }

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
.search-input::placeholder { color: var(--text-muted); }
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
.cell-code { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent); }
.type-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: var(--bg-main); }
.type-badge.YEAR_PASS { background: var(--danger); }
.type-badge.MONTH_PASS { background: var(--success); }
.type-badge.SINGLE_USE { background: var(--warning); }
.status-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.status-badge.ACTIVE { background: var(--success); color: var(--bg-main); }
.status-badge.USED { background: var(--warning); color: var(--bg-main); }
.status-badge.EXPIRED { background: var(--text-muted); color: var(--text-primary); }
.status-badge.CANCELLED { background: var(--danger); color: white; }
.cell-date { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }
.date-sep { margin: 0 6px; color: var(--text-muted); }
.cell-entries { font-family: 'JetBrains Mono', monospace; }
.entries-count { font-weight: 700; color: var(--text-primary); }
.entries-sep { color: var(--text-muted); margin: 0 2px; }
.entries-max { color: var(--text-secondary); }
.cell-actions { display: flex; gap: 12px; }
.action-link { background: none; border: none; font-size: 13px; color: var(--accent); cursor: pointer; transition: color 0.2s ease; text-decoration: underline; text-underline-offset: 3px; }
.action-link:hover { color: var(--btn-primary-hover); }
.action-danger:hover { color: var(--danger); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 20px 48px; border-top: 1px solid var(--border); background: var(--bg-secondary); flex-shrink: 0; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.pagination-controls { display: flex; align-items: center; gap: 16px; }
.page-btn { padding: 8px 16px; background: var(--btn-secondary); border: 1px solid var(--border); border-radius: 6px; font-size: 13px; color: var(--text-primary); cursor: pointer; transition: all 0.2s ease; }
.page-btn:hover:not(:disabled) { background: var(--accent); color: var(--bg-main); border-color: var(--accent); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-indicator { font-size: 13px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item.full { grid-column: 1 / -1; }
.detail-label { font-size: 11px; color: var(--text-secondary); letter-spacing: 0.1em; }
.detail-value { font-size: 14px; color: var(--text-primary); }
.detail-value.code { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent); }
</style>