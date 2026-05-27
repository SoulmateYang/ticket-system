<template>
  <div class="ota-orders">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">OTA订单管理</h1>
        <p class="page-subtitle">OTA ORDER MANAGEMENT</p>
      </div>
      <div class="header-actions">
        <button class="btn-sync" @click="showSyncDialog = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4v5h5M20 20v-5h-5"/><path d="M20.49 9A9 9 0 005.64 5.64L4 4m16 16l-5.64-5.64A9 9 0 013.51 15"/></svg>
          <span>同步订单</span>
        </button>
        <button class="btn-refresh" @click="fetchOrders">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4v5h5M20 20v-5h-5"/><path d="M20.49 9A9 9 0 005.64 5.64L4 4m16 16l-5.64-5.64A9 9 0 013.51 15"/></svg>
        </button>
      </div>
    </div>

    <div class="search-panel">
      <div class="search-field"><label>OTA渠道</label>
        <select v-model="searchForm.channel" class="search-select">
          <option value="">全部渠道</option>
          <option value="MEITUAN">美团</option>
          <option value="DOUYIN">抖音</option>
          <option value="CTRIP">携程</option>
          <option value="WINDOW">窗口</option>
          <option value="MINIAPP">小程序</option>
        </select>
      </div>
      <div class="search-field"><label>同步状态</label>
        <select v-model="searchForm.syncStatus" class="search-select">
          <option value="">全部状态</option>
          <option value="PENDING">待处理</option>
          <option value="SYNCED">已同步</option>
          <option value="COMPLETED">已完成</option>
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
            <th>ID</th><th>外部订单号</th><th>OTA渠道</th><th>演出名称</th><th>数量</th><th>金额</th><th>买家信息</th><th>同步状态</th><th>创建时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in orders" :key="order.id" class="table-row" :style="{ animationDelay: `${index * 0.05}s` }" @mouseenter="highlightRow($event)" @mouseleave="unhighlightRow($event)">
            <td class="cell-id">{{ order.id }}</td>
            <td class="cell-code">{{ order.externalOrderId }}</td>
            <td><span class="channel-badge" :class="order.otaChannel">{{ getChannelName(order.otaChannel) }}</span></td>
            <td class="cell-performance">{{ order.performanceName }}</td>
            <td class="cell-num">{{ order.quantity }}</td>
            <td class="cell-amount">¥{{ order.amount?.toFixed(2) }}</td>
            <td class="cell-buyer">{{ order.buyerName }}<br/><span class="buyer-phone">{{ order.buyerPhone }}</span></td>
            <td><span class="sync-badge" :class="order.syncStatus">{{ getSyncStatusName(order.syncStatus) }}</span></td>
            <td class="cell-time">{{ order.createdAt?.slice(0, 16) || '-' }}</td>
            <td class="cell-actions">
              <button v-if="order.syncStatus === 'PENDING'" class="action-btn action-primary" @click="handleGenerate(order)">生成票务</button>
              <button v-else-if="order.syncStatus === 'SYNCED'" class="action-btn action-disabled" disabled>已生成</button>
              <button class="action-btn action-info" @click="viewDetail(order)">详情</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span class="pagination-info">共 {{ pagination.total }} 条</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; fetchOrders()">上一页</button>
          <span class="page-indicator">{{ pagination.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pagination.page >= totalPages" @click="pagination.page++; fetchOrders()">下一页</button>
        </div>
      </div>
    </div>

    <div class="dialog-overlay" v-if="showSyncDialog" @click.self="showSyncDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h2>同步OTA订单</h2>
          <button class="dialog-close" @click="showSyncDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body">
          <div class="form-field"><label>OTA渠道</label><select v-model="syncForm.channel" class="form-select"><option value="MEITUAN">美团</option><option value="DOUYIN">抖音</option><option value="CTRIP">携程</option></select></div>
          <div class="form-field"><label>外部订单号</label><input v-model="syncForm.externalOrderId" type="text" placeholder="请输入外部订单号" /></div>
          <div class="form-field"><label>演出名称</label><input v-model="syncForm.performanceName" type="text" placeholder="请输入演出名称" /></div>
          <div class="form-field"><label>数量</label><input v-model.number="syncForm.quantity" type="number" min="1" max="100" /></div>
          <div class="form-field"><label>金额</label><input v-model.number="syncForm.amount" type="number" min="0" step="0.01" /></div>
          <div class="form-field"><label>买家姓名</label><input v-model="syncForm.buyerName" type="text" placeholder="请输入买家姓名" /></div>
          <div class="form-field"><label>买家手机</label><input v-model="syncForm.buyerPhone" type="text" placeholder="请输入买家手机" /></div>
          <div class="form-field"><label>买家身份证</label><input v-model="syncForm.buyerIdCard" type="text" placeholder="请输入买家身份证" /></div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showSyncDialog = false">取消</button>
          <button class="btn-submit" @click="handleSync" :disabled="submitting">同步</button>
        </div>
      </div>
    </div>

    <div class="dialog-overlay" v-if="showDetailDialog" @click.self="showDetailDialog = false">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h2>订单详情</h2>
          <button class="dialog-close" @click="showDetailDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body" v-if="currentOrder">
          <div class="detail-grid">
            <div class="detail-item"><span class="detail-label">ID</span><span class="detail-value">{{ currentOrder.id }}</span></div>
            <div class="detail-item"><span class="detail-label">外部订单号</span><span class="detail-value code">{{ currentOrder.externalOrderId }}</span></div>
            <div class="detail-item"><span class="detail-label">OTA渠道</span><span class="detail-value"><span class="channel-badge" :class="currentOrder.otaChannel">{{ getChannelName(currentOrder.otaChannel) }}</span></span></div>
            <div class="detail-item"><span class="detail-label">演出名称</span><span class="detail-value">{{ currentOrder.performanceName }}</span></div>
            <div class="detail-item"><span class="detail-label">数量</span><span class="detail-value">{{ currentOrder.quantity }}</span></div>
            <div class="detail-item"><span class="detail-label">金额</span><span class="detail-value amount">¥{{ currentOrder.amount?.toFixed(2) }}</span></div>
            <div class="detail-item"><span class="detail-label">买家姓名</span><span class="detail-value">{{ currentOrder.buyerName }}</span></div>
            <div class="detail-item"><span class="detail-label">买家手机</span><span class="detail-value">{{ currentOrder.buyerPhone }}</span></div>
            <div class="detail-item"><span class="detail-label">买家身份证</span><span class="detail-value">{{ currentOrder.buyerIdCard }}</span></div>
            <div class="detail-item"><span class="detail-label">同步状态</span><span class="detail-value"><span class="sync-badge" :class="currentOrder.syncStatus">{{ getSyncStatusName(currentOrder.syncStatus) }}</span></span></div>
            <div class="detail-item"><span class="detail-label">订单状态</span><span class="detail-value">{{ currentOrder.status }}</span></div>
            <div class="detail-item"><span class="detail-label">同步时间</span><span class="detail-value">{{ currentOrder.syncTime }}</span></div>
            <div class="detail-item full"><span class="detail-label">创建时间</span><span class="detail-value">{{ currentOrder.createdAt }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useOtaStore } from '../stores/ota'

const otaStore = useOtaStore()
const orders = ref([])
const loading = ref(false)
const showSyncDialog = ref(false)
const showDetailDialog = ref(false)
const submitting = ref(false)
const currentOrder = ref(null)

const searchForm = reactive({ channel: '', syncStatus: '' })
const syncForm = reactive({ channel: 'MEITUAN', externalOrderId: '', performanceName: '', quantity: 1, amount: 0, buyerName: '', buyerPhone: '', buyerIdCard: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.size)))

const getChannelName = (channel) => ({ MEITUAN: '美团', DOUYIN: '抖音', CTRIP: '携程', WINDOW: '窗口', MINIAPP: '小程序' }[channel] || channel)
const getSyncStatusName = (status) => ({ PENDING: '待处理', SYNCED: '已同步', COMPLETED: '已完成' }[status] || status)
const highlightRow = (e) => e.currentTarget.classList.add('row-highlighted')
const unhighlightRow = (e) => e.currentTarget.classList.remove('row-highlighted')

const handleSearch = () => { pagination.page = 1; fetchOrders() }
const resetSearch = () => { searchForm.channel = ''; searchForm.syncStatus = ''; handleSearch() }

const fetchOrders = async () => {
  loading.value = true
  try { await otaStore.fetchPendingOrders(); orders.value = otaStore.orders; pagination.total = otaStore.orders.length || 50 }
  catch { ElMessage.error('获取OTA订单列表失败') } finally { loading.value = false }
}

const handleSync = async () => {
  submitting.value = true
  try { await otaStore.syncOrder(syncForm); ElMessage.success('同步成功'); showSyncDialog.value = false; fetchOrders() }
  catch { ElMessage.error('同步失败') } finally { submitting.value = false }
}

const handleGenerate = async (order) => { try { await otaStore.generateTickets(order.id); ElMessage.success('生成票务成功'); fetchOrders() } catch { ElMessage.error('生成票务失败') } }
const viewDetail = (order) => { currentOrder.value = order; showDetailDialog.value = true }

onMounted(() => { fetchOrders() })
</script>

<style scoped>
.ota-orders { min-height: 100vh; width: 100%; font-family: 'Noto Serif SC', serif; background: var(--bg-main); display: flex; flex-direction: column; flex: 1; }

.page-header { display: flex; justify-content: space-between; align-items: flex-end; padding: 32px 48px 24px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.page-title { font-size: 28px; font-weight: 700; color: var(--text-primary); margin: 0; letter-spacing: 0.1em; }
.page-subtitle { font-size: 11px; color: var(--text-secondary); letter-spacing: 0.3em; margin: 4px 0 0 0; font-family: 'Arial', sans-serif; }
.header-actions { display: flex; gap: 12px; }

.btn-sync, .btn-refresh { display: flex; align-items: center; gap: 8px; padding: 12px 20px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; border: none; font-weight: 600; }
.btn-sync { background: var(--success); color: var(--bg-main); }
.btn-sync:hover { background: var(--btn-primary-hover); transform: translateY(-2px); }
.btn-sync svg, .btn-refresh svg { width: 18px; height: 18px; }
.btn-refresh { background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); }
.btn-refresh:hover { background: var(--bg-hover); border-color: var(--accent); color: var(--accent); }

.search-panel { display: flex; gap: 20px; padding: 24px 48px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.search-field { display: flex; flex-direction: column; gap: 8px; }
.search-field label { font-size: 12px; color: var(--text-secondary); letter-spacing: 0.1em; }
.search-select { padding: 10px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; background: var(--bg-card); color: var(--text-primary); min-width: 180px; transition: border-color 0.3s ease; }
.search-select:focus { outline: none; border-color: var(--accent); }
.search-actions { display: flex; align-items: flex-end; gap: 12px; margin-left: auto; }

.btn-search, .btn-reset { padding: 10px 20px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; }
.btn-search { background: var(--accent); color: var(--bg-main); border: none; display: flex; align-items: center; gap: 8px; font-weight: 600; }
.btn-search:hover { background: var(--btn-primary-hover); }
.btn-search svg { width: 16px; height: 16px; }
.btn-reset { background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); }
.btn-reset:hover { background: var(--bg-hover); border-color: var(--accent); }

.table-container { background: var(--bg-card); border-bottom: 1px solid var(--border); overflow: hidden; flex: 1; display: flex; flex-direction: column; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { padding: 16px 48px; text-align: left; font-size: 12px; font-weight: 600; color: var(--text-secondary); letter-spacing: 0.1em; background: var(--bg-secondary); border-bottom: 1px solid var(--border); white-space: nowrap; }
.data-table td { padding: 16px 48px; border-bottom: 1px solid var(--border); font-size: 14px; color: var(--text-primary); }
.table-row { transition: background 0.2s ease; }
.table-row:hover { background: var(--bg-hover); }
.row-highlighted { background: var(--bg-hover); }

.cell-id { font-family: 'JetBrains Mono', monospace; color: var(--text-secondary); font-size: 13px; }
.cell-code { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent); }
.channel-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; color: white; }
.channel-badge.MEITUAN { background: #FF6B00; }
.channel-badge.DOUYIN { background: #2D2D2D; }
.channel-badge.CTRIP { background: #FFB800; color: #333; }
.channel-badge.WINDOW { background: var(--btn-secondary); }
.channel-badge.MINIAPP { background: var(--success); }
.cell-performance { font-weight: 500; }
.cell-num { font-family: 'JetBrains Mono', monospace; text-align: center; }
.cell-amount { font-family: 'JetBrains Mono', monospace; font-weight: 700; color: var(--accent); }
.cell-buyer { font-size: 13px; }
.buyer-phone { font-size: 12px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }
.sync-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.sync-badge.PENDING { background: var(--warning); color: var(--bg-main); }
.sync-badge.SYNCED { background: var(--btn-secondary); color: var(--text-primary); }
.sync-badge.COMPLETED { background: var(--success); color: var(--bg-main); }
.cell-time { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: var(--text-secondary); }
.cell-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.action-btn { padding: 6px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s ease; }
.action-primary { background: var(--accent); color: var(--bg-main); }
.action-disabled { background: var(--btn-secondary); color: var(--text-muted); cursor: not-allowed; }
.action-info { background: var(--btn-secondary); color: var(--text-primary); }
.action-btn:hover:not(:disabled) { transform: translateY(-1px); opacity: 0.9; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 20px 48px; border-top: 1px solid var(--border); background: var(--bg-secondary); flex-shrink: 0; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.pagination-controls { display: flex; align-items: center; gap: 16px; }
.page-btn { padding: 8px 16px; background: var(--btn-secondary); border: 1px solid var(--border); border-radius: 6px; font-size: 13px; color: var(--text-primary); cursor: pointer; transition: all 0.2s ease; }
.page-btn:hover:not(:disabled) { background: var(--accent); color: var(--bg-main); border-color: var(--accent); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-indicator { font-size: 13px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }

.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 15, 30, 0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; animation: fade-in 0.2s ease; }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }
.dialog { background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; width: 460px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5); animation: dialog-enter 0.3s ease; }
.dialog-wide { width: 580px; }
@keyframes dialog-enter { from { opacity: 0; transform: translateY(-20px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 24px; border-bottom: 1px solid var(--border); }
.dialog-header h2 { font-size: 18px; font-weight: 600; color: var(--text-primary); margin: 0; letter-spacing: 0.1em; }
.dialog-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: var(--text-secondary); cursor: pointer; transition: color 0.2s ease; }
.dialog-close:hover { color: var(--accent); }
.dialog-close svg { width: 20px; height: 20px; }
.dialog-body { padding: 24px; max-height: 60vh; overflow-y: auto; }
.form-field { margin-bottom: 18px; }
.form-field:last-child { margin-bottom: 0; }
.form-field label { display: block; font-size: 12px; color: var(--text-secondary); letter-spacing: 0.1em; margin-bottom: 8px; }
.form-field input, .form-select { width: 100%; padding: 12px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; background: var(--bg-main); color: var(--text-primary); transition: border-color 0.3s ease; }
.form-field input:focus, .form-select:focus { outline: none; border-color: var(--accent); }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 20px 24px; border-top: 1px solid var(--border); background: var(--bg-secondary); }
.btn-cancel, .btn-submit { padding: 10px 24px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; }
.btn-cancel { background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); }
.btn-cancel:hover { background: var(--bg-hover); }
.btn-submit { background: var(--accent); color: var(--bg-main); border: none; font-weight: 600; }
.btn-submit:hover:not(:disabled) { background: var(--btn-primary-hover); }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item.full { grid-column: 1 / -1; }
.detail-label { font-size: 11px; color: var(--text-secondary); letter-spacing: 0.1em; }
.detail-value { font-size: 14px; color: var(--text-primary); }
.detail-value.code { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent); }
.detail-value.amount { font-family: 'JetBrains Mono', monospace; font-weight: 700; color: var(--accent); font-size: 16px; }
</style>