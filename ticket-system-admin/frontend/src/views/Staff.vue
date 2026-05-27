<template>
  <div class="staff-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">员工管理</h1>
        <p class="page-subtitle">STAFF MANAGEMENT</p>
      </div>
      <button class="create-btn" @click="showCreateDialog = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14m-7-7h14"/></svg>
        <span>添加员工</span>
      </button>
    </div>

    <div class="search-panel">
      <div class="search-field"><label>姓名</label><input v-model="searchForm.name" type="text" placeholder="输入姓名搜索" class="search-input" /></div>
      <div class="search-field"><label>手机号</label><input v-model="searchForm.phone" type="text" placeholder="输入手机号" class="search-input" /></div>
      <div class="search-field"><label>角色</label>
        <select v-model="searchForm.role" class="search-select">
          <option value="">全部</option>
          <option value="ADMIN">管理员</option>
          <option value="TICKETER">验票员</option>
          <option value="FINANCE">财务</option>
          <option value="OTA">OTA运营</option>
        </select>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>查询</button>
        <button class="btn-reset" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else-if="staffList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        <p>暂无员工数据</p>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th><th>工号</th><th>姓名</th><th>手机号</th><th>角色</th><th>状态</th><th>创建时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(staff, index) in staffList" :key="staff.id" class="table-row" :style="{ animationDelay: `${index * 0.05}s` }" @mouseenter="highlightRow($event)" @mouseleave="unhighlightRow($event)">
            <td class="cell-id">{{ staff.id }}</td>
            <td class="cell-mono">{{ staff.employeeNo }}</td>
            <td class="cell-name">{{ staff.name }}</td>
            <td class="cell-mono">{{ staff.phone }}</td>
            <td><span class="role-badge" :class="staff.role">{{ getRoleName(staff.role) }}</span></td>
            <td><span class="status-badge" :class="staff.status">{{ staff.status === 'ACTIVE' ? '正常' : '停用' }}</span></td>
            <td class="cell-time">{{ staff.createdAt?.slice(0, 10) || '-' }}</td>
            <td class="cell-actions">
              <button v-if="staff.status === 'ACTIVE'" class="action-btn action-danger" @click="handleSuspend(staff)">停用</button>
              <button v-else class="action-btn action-success" @click="handleActivate(staff)">启用</button>
              <button class="action-btn action-danger" @click="handleDelete(staff)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && staffList.length > 0" class="pagination">
        <span class="pagination-info">共 {{ pagination.total }} 条</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; fetchStaff()">上一页</button>
          <span class="page-indicator">{{ pagination.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="pagination.page >= totalPages" @click="pagination.page++; fetchStaff()">下一页</button>
        </div>
      </div>
    </div>

    <!-- Create Dialog -->
    <div class="dialog-overlay" v-if="showCreateDialog" @click.self="showCreateDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h2>添加员工</h2>
          <button class="dialog-close" @click="showCreateDialog = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body">
          <div class="form-field"><label>工号</label><input v-model="createForm.employeeNo" type="text" placeholder="如：EMP001" /></div>
          <div class="form-field"><label>姓名</label><input v-model="createForm.name" type="text" placeholder="请输入姓名" /></div>
          <div class="form-field"><label>手机号</label><input v-model="createForm.phone" type="tel" placeholder="请输入手机号" /></div>
          <div class="form-field"><label>初始密码</label><input v-model="createForm.password" type="password" placeholder="请输入密码（6-32位）" /></div>
          <div class="form-field"><label>角色</label>
            <select v-model="createForm.role" class="form-select">
              <option value="ADMIN">管理员</option>
              <option value="TICKETER">验票员</option>
              <option value="FINANCE">财务</option>
              <option value="OTA">OTA运营</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showCreateDialog = false">取消</button>
          <button class="btn-submit" @click="handleCreate" :disabled="submitting">创建</button>
        </div>
      </div>
    </div>

    <!-- Confirm Dialog -->
    <div class="dialog-overlay" v-if="confirmDialog.show" @click.self="confirmDialog.show = false">
      <div class="dialog dialog-sm">
        <div class="dialog-header">
          <h2>{{ confirmDialog.title }}</h2>
          <button class="dialog-close" @click="confirmDialog.show = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
        </div>
        <div class="dialog-body">
          <p class="confirm-message">{{ confirmDialog.message }}</p>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="confirmDialog.show = false">取消</button>
          <button class="btn-submit" :class="confirmDialog.type === 'danger' ? 'btn-danger' : ''" @click="confirmDialog.onConfirm">{{ confirmDialog.confirmText }}</button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container" v-if="toast.show">
      <div class="toast" :class="toast.type">
        <span class="toast-message">{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { staffApi } from '../api'

const staffList = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const submitting = ref(false)

const searchForm = reactive({ name: '', phone: '', role: '' })
const createForm = reactive({ employeeNo: '', name: '', phone: '', password: '', role: 'ADMIN' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.size)))

const toast = reactive({ show: false, message: '', type: 'info', timer: null })
const confirmDialog = reactive({ show: false, title: '', message: '', type: 'info', confirmText: '确定', onConfirm: () => {} })

const showToast = (message, type = 'info') => {
  if (toast.timer) clearTimeout(toast.timer)
  toast.message = message
  toast.type = type
  toast.show = true
  toast.timer = setTimeout(() => { toast.show = false }, 3000)
}

const showConfirm = (title, message, type, confirmText, onConfirm) => {
  confirmDialog.title = title
  confirmDialog.message = message
  confirmDialog.type = type
  confirmDialog.confirmText = confirmText
  confirmDialog.onConfirm = () => { confirmDialog.show = false; onConfirm() }
  confirmDialog.show = true
}

const getRoleName = (role) => ({ ADMIN: '管理员', TICKETER: '验票员', FINANCE: '财务', OTA: 'OTA运营' }[role] || role)
const highlightRow = (e) => e.currentTarget.classList.add('row-highlighted')
const unhighlightRow = (e) => e.currentTarget.classList.remove('row-highlighted')

const handleSearch = () => { pagination.page = 1; fetchStaff() }
const resetSearch = () => { searchForm.name = ''; searchForm.phone = ''; searchForm.role = ''; handleSearch() }

const fetchStaff = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page - 1, size: pagination.size, ...searchForm }
    const response = await staffApi.getAll(params)
    staffList.value = response.data.content || response.data || []
    pagination.total = response.data.totalElements || staffList.value.length
  } catch {
    showToast('获取员工列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createForm.employeeNo || !createForm.name || !createForm.phone || !createForm.password) {
    showToast('请填写完整信息', 'warning')
    return
  }
  if (createForm.password.length < 6 || createForm.password.length > 32) {
    showToast('密码长度必须为6-32位', 'warning')
    return
  }
  submitting.value = true
  try {
    await staffApi.create(createForm)
    showToast('创建成功', 'success')
    showCreateDialog.value = false
    Object.assign(createForm, { employeeNo: '', name: '', phone: '', password: '', role: 'ADMIN' })
    fetchStaff()
  } catch (e) {
    showToast(e.response?.data?.message || '创建失败', 'error')
  } finally {
    submitting.value = false
  }
}

const handleSuspend = (staff) => {
  showConfirm('提示', `确定要停用员工 ${staff.name} 吗？`, 'warning', '确定', async () => {
    try {
      await staffApi.suspend(staff.id)
      showToast('已停用', 'success')
      fetchStaff()
    } catch {
      showToast('操作失败', 'error')
    }
  })
}

const handleActivate = (staff) => {
  showConfirm('提示', `确定要启用员工 ${staff.name} 吗？`, 'info', '确定', async () => {
    try {
      await staffApi.activate(staff.id)
      showToast('已启用', 'success')
      fetchStaff()
    } catch {
      showToast('操作失败', 'error')
    }
  })
}

const handleDelete = (staff) => {
  showConfirm('警告', `确定要删除员工 ${staff.name} 吗？此操作不可恢复！`, 'danger', '删除', async () => {
    try {
      await staffApi.delete(staff.id)
      showToast('已删除', 'success')
      fetchStaff()
    } catch {
      showToast('删除失败', 'error')
    }
  })
}

onMounted(() => { fetchStaff() })
</script>

<style scoped>
.staff-management { min-height: 100vh; width: 100%; font-family: 'Noto Serif SC', serif; background: var(--bg-main); display: flex; flex-direction: column; flex: 1; }

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
.search-input:focus, .search-select:focus { outline: none; border-color: var(--accent); }
.search-actions { display: flex; align-items: flex-end; gap: 12px; margin-left: auto; }

.btn-search, .btn-reset { padding: 10px 20px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: all 0.3s ease; }
.btn-search { background: var(--accent); color: var(--bg-main); border: none; display: flex; align-items: center; gap: 8px; font-weight: 600; }
.btn-search:hover { background: var(--btn-primary-hover); }
.btn-search svg { width: 16px; height: 16px; }
.btn-reset { background: var(--btn-secondary); color: var(--text-primary); border: 1px solid var(--border); }
.btn-reset:hover { background: var(--bg-hover); border-color: var(--accent); }

.table-container { background: var(--bg-card); border-bottom: 1px solid var(--border); overflow: hidden; flex: 1; display: flex; flex-direction: column; }

.loading-state, .empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 48px; color: var(--text-muted); gap: 16px; }
.loading-spinner { width: 32px; height: 32px; border: 3px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-icon { width: 48px; height: 48px; opacity: 0.5; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { padding: 16px 48px; text-align: left; font-size: 12px; font-weight: 600; color: var(--text-secondary); letter-spacing: 0.1em; background: var(--bg-secondary); border-bottom: 1px solid var(--border); }
.data-table td { padding: 16px 48px; border-bottom: 1px solid var(--border); font-size: 14px; color: var(--text-primary); }
.table-row { transition: background 0.2s ease; }
.table-row:hover { background: var(--bg-hover); }
.row-highlighted { background: var(--bg-hover); }

.cell-id { font-family: 'JetBrains Mono', monospace; color: var(--text-secondary); }
.cell-mono { font-family: 'JetBrains Mono', monospace; font-size: 13px; }
.cell-name { font-weight: 600; }
.cell-time { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: var(--text-secondary); }

.role-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.role-badge.ADMIN { background: var(--danger); color: white; }
.role-badge.TICKETER { background: var(--accent); color: var(--bg-main); }
.role-badge.FINANCE { background: var(--warning); color: var(--bg-main); }
.role-badge.OTA { background: var(--success); color: var(--bg-main); }

.status-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.status-badge.ACTIVE { background: var(--success); color: var(--bg-main); }
.status-badge.SUSPENDED { background: var(--danger); color: white; }

.cell-actions { display: flex; gap: 8px; }
.action-btn { padding: 6px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s ease; }
.action-success { background: var(--success); color: var(--bg-main); }
.action-danger { background: var(--danger); color: white; }
.action-btn:hover { transform: translateY(-1px); opacity: 0.9; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 20px 48px; border-top: 1px solid var(--border); background: var(--bg-secondary); flex-shrink: 0; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.pagination-controls { display: flex; align-items: center; gap: 16px; }
.page-btn { padding: 8px 16px; background: var(--btn-secondary); border: 1px solid var(--border); border-radius: 6px; font-size: 13px; color: var(--text-primary); cursor: pointer; transition: all 0.2s ease; }
.page-btn:hover:not(:disabled) { background: var(--accent); color: var(--bg-main); border-color: var(--accent); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-indicator { font-size: 13px; color: var(--text-secondary); font-family: 'JetBrains Mono', monospace; }

.confirm-message { font-size: 14px; color: var(--text-primary); line-height: 1.6; margin: 0; }
.dialog-sm { width: 360px; }
.btn-danger { background: var(--danger); color: white; }
.btn-danger:hover { opacity: 0.9; }

/* Toast */
.toast-container { position: fixed; top: 24px; right: 24px; z-index: 2000; display: flex; flex-direction: column; gap: 8px; }
.toast { padding: 12px 20px; border-radius: 6px; font-size: 14px; animation: toast-in 0.3s ease; min-width: 200px; }
@keyframes toast-in { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
.toast.info { background: var(--bg-card); border: 1px solid var(--accent); color: var(--text-primary); }
.toast.success { background: rgba(0, 255, 136, 0.15); border: 1px solid var(--success); color: var(--success); }
.toast.warning { background: rgba(255, 149, 0, 0.15); border: 1px solid var(--warning); color: var(--warning); }
.toast.error { background: rgba(255, 45, 106, 0.15); border: 1px solid var(--danger); color: var(--danger); }
.toast-message { font-weight: 500; }
</style>
