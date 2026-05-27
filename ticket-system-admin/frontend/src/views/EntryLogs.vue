<template>
  <div class="entry-logs">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">入园记录</h1>
        <p class="page-subtitle">ENTRY LOG MANAGEMENT</p>
      </div>
      <div class="header-actions">
        <button class="btn-refresh" @click="fetchLogs">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4v5h5M20 20v-5h-5"/><path d="M20.49 9A9 9 0 005.64 5.64L4 4m16 16l-5.64-5.64A9 9 0 013.51 15"/></svg>
          刷新
        </button>
      </div>
    </div>

    <div class="search-panel">
      <div class="search-field"><label>开始日期</label>
        <input v-model="searchForm.startDate" type="date" class="search-input" />
      </div>
      <div class="search-field"><label>结束日期</label>
        <input v-model="searchForm.endDate" type="date" class="search-input" />
      </div>
      <div class="search-field"><label>票种类型</label>
        <select v-model="searchForm.ticketType" class="search-select">
          <option value="">全部</option>
          <option value="YEAR_PASS">年卡</option>
          <option value="MONTH_PASS">月卡</option>
          <option value="SINGLE_USE">单次票</option>
        </select>
      </div>
      <div class="search-field"><label>核验方式</label>
        <select v-model="searchForm.verifyMethod" class="search-select">
          <option value="">全部</option>
          <option value="SCAN">扫码</option>
          <option value="FACE">人脸</option>
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
            <th>ID</th>
            <th>票码</th>
            <th>票种类型</th>
            <th>访客ID</th>
            <th>访客姓名</th>
            <th>渠道</th>
            <th>入园时间</th>
            <th>出园时间</th>
            <th>状态</th>
            <th>核验方式</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(log, index) in logs" :key="log.id" class="table-row" :style="{ animationDelay: `${index * 0.05}s` }" @mouseenter="highlightRow($event)" @mouseleave="unhighlightRow($event)">
            <td class="cell-id">{{ log.id }}</td>
            <td class="cell-code">{{ log.ticketCode }}</td>
            <td><span class="type-badge" :class="log.ticketType">{{ getTicketTypeName(log.ticketType) }}</span></td>
            <td class="cell-mono">{{ log.visitorId }}</td>
            <td class="cell-name">{{ log.visitorName }}</td>
            <td><span class="channel-tag">{{ log.channel || '窗口' }}</span></td>
            <td class="cell-time">{{ log.entryTime?.slice(0, 16) || '-' }}</td>
            <td class="cell-time">{{ log.exitTime?.slice(0, 16) || '-' }}</td>
            <td><span class="status-badge" :class="log.status">{{ log.status === 'SUCCESS' ? '成功' : '失败' }}</span></td>
            <td><span class="verify-tag" :class="log.verifyMethod">{{ log.verifyMethod === 'SCAN' ? '扫码' : log.verifyMethod === 'FACE' ? '人脸' : '其他' }}</span></td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span class="pagination-info">共 {{ pagination.total }} 条</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; fetchLogs()">上一页</button>
          <span class="page-indicator">{{ pagination.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pagination.page >= totalPages" @click="pagination.page++; fetchLogs()">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const logs = ref([])
const loading = ref(false)

const searchForm = reactive({ startDate: '', endDate: '', ticketType: '', verifyMethod: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.size)))

const getTicketTypeName = (type) => ({ YEAR_PASS: '年卡', MONTH_PASS: '月卡', SINGLE_USE: '单次票' }[type] || type)
const highlightRow = (e) => e.currentTarget.classList.add('row-highlighted')
const unhighlightRow = (e) => e.currentTarget.classList.remove('row-highlighted')

const handleSearch = () => { pagination.page = 1; fetchLogs() }
const resetSearch = () => { searchForm.startDate = ''; searchForm.endDate = ''; searchForm.ticketType = ''; searchForm.verifyMethod = ''; handleSearch() }

const fetchLogs = async () => {
  loading.value = true
  try {
    logs.value = [
      { id: 1, ticketCode: 'TK-20260526001', ticketType: 'SINGLE_USE', visitorId: 'V001', visitorName: '张三', channel: 'MEITUAN', entryTime: '2026-05-26 09:30:00', exitTime: '2026-05-26 17:00:00', status: 'SUCCESS', verifyMethod: 'SCAN' },
      { id: 2, ticketCode: 'TK-20260526002', ticketType: 'YEAR_PASS', visitorId: 'V002', visitorName: '李四', channel: null, entryTime: '2026-05-26 09:45:00', exitTime: null, status: 'SUCCESS', verifyMethod: 'FACE' },
      { id: 3, ticketCode: 'TK-20260526003', ticketType: 'MONTH_PASS', visitorId: 'V003', visitorName: '王五', channel: 'DOUYIN', entryTime: '2026-05-26 10:00:00', exitTime: null, status: 'SUCCESS', verifyMethod: 'SCAN' }
    ]
    pagination.total = logs.value.length
  } catch (e) {
    ElMessage.error('获取入园记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchLogs() })
</script>

<style scoped>
.entry-logs { min-height: 100vh; width: 100%; font-family: 'Noto Serif SC', serif; background: var(--bg-main); display: flex; flex-direction: column; flex: 1; }

.page-header { display: flex; justify-content: space-between; align-items: flex-end; padding: 32px 48px 24px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.page-title { font-size: 28px; font-weight: 700; color: var(--text-primary); margin: 0; letter-spacing: 0.1em; }
.page-subtitle { font-size: 11px; color: var(--text-secondary); letter-spacing: 0.3em; margin: 4px 0 0 0; font-family: 'Arial', sans-serif; }
.header-actions { display: flex; gap: 12px; }

.btn-refresh { display: flex; align-items: center; gap: 8px; padding: 12px 20px; background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; font-weight: 600; }
.btn-refresh:hover { background: var(--bg-hover); border-color: var(--accent); color: var(--accent); }
.btn-refresh svg { width: 18px; height: 18px; }

.search-panel { display: flex; gap: 20px; padding: 24px 48px; background: var(--bg-secondary); border-bottom: 1px solid var(--border); flex-shrink: 0; }
.search-field { display: flex; flex-direction: column; gap: 8px; }
.search-field label { font-size: 12px; color: var(--text-secondary); letter-spacing: 0.1em; }
.search-input, .search-select { padding: 10px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; background: var(--bg-card); color: var(--text-primary); min-width: 160px; transition: border-color 0.3s ease; }
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
.data-table th { padding: 16px 48px; text-align: left; font-size: 12px; font-weight: 600; color: var(--text-secondary); letter-spacing: 0.1em; background: var(--bg-secondary); border-bottom: 1px solid var(--border); white-space: nowrap; }
.data-table td { padding: 16px 48px; border-bottom: 1px solid var(--border); font-size: 14px; color: var(--text-primary); }
.table-row { transition: background 0.2s ease; }
.table-row:hover { background: var(--bg-hover); }
.row-highlighted { background: var(--bg-hover); }

.cell-id { font-family: 'JetBrains Mono', monospace; color: var(--text-secondary); }
.cell-code { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent); }
.cell-mono { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }
.cell-name { font-weight: 600; }
.cell-time { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }

.type-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.type-badge.YEAR_PASS { background: var(--danger); color: white; }
.type-badge.MONTH_PASS { background: var(--success); color: var(--bg-main); }
.type-badge.SINGLE_USE { background: var(--warning); color: var(--bg-main); }

.channel-tag { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; background: var(--btn-secondary); color: var(--text-secondary); }

.status-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.status-badge.SUCCESS { background: var(--success); color: var(--bg-main); }
.status-badge.FAILURE { background: var(--danger); color: white; }

.verify-tag { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.verify-tag.SCAN { background: var(--accent); color: var(--bg-main); }
.verify-tag.FACE { background: var(--warning); color: var(--bg-main); }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 20px 48px; border-top: 1px solid var(--border); background: var(--bg-secondary); flex-shrink: 0; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.pagination-controls { display: flex; align-items: center; gap: 16px; }
.page-btn { padding: 8px 16px; background: var(--btn-secondary); border: 1px solid var(--border); border-radius: 6px; font-size: 13px; color: var(--text-primary); cursor: pointer; transition: all 0.2s ease; }
.page-btn:hover:not(:disabled) { background: var(--accent); color: var(--bg-main); border-color: var(--accent); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-indicator { font-size: 13px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }
</style>