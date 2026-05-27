<template>
  <el-container class="admin-layout">
    <!-- Sidebar - Dark Tech Theme -->
    <el-aside width="260px" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <circle cx="24" cy="24" r="8" stroke="currentColor" stroke-width="2.5"/>
            <circle cx="24" cy="24" r="3" fill="currentColor"/>
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">景区票务中台</span>
          <span class="logo-subtitle">SCENIC TICKET SYSTEM</span>
        </div>
      </div>

      <nav class="nav-menu">
        <router-link
          v-for="(item, index) in visibleMenuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :style="{ animationDelay: `${index * 0.05}s` }"
        >
          <div class="nav-indicator"></div>
          <div class="nav-icon" v-html="item.icon"></div>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="system-status">
          <div class="status-indicator"></div>
          <span>系统运行正常</span>
        </div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <!-- Header -->
      <el-header class="header">
        <div class="breadcrumb">
          <span class="breadcrumb-item">{{ route.meta.title }}</span>
        </div>
        <div class="header-actions">
          <div class="current-user" v-if="currentStaff">
            <span class="user-name">{{ currentStaff.name }}</span>
            <span class="user-role">{{ getRoleName(currentStaff.role) }}</span>
          </div>
          <button class="header-btn" @click="handleLogout" title="退出登录">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
              <polyline points="16,17 21,12 16,7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
          <button class="header-btn" @click="refreshData" title="刷新">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4v5h5M20 20v-5h-5"/>
              <path d="M20.49 9A9 9 0 005.64 5.64L4 4m16 16l-5.64-5.64A9 9 0 013.51 15"/>
            </svg>
          </button>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { authApi } from '../api'

const route = useRoute()
const currentStaff = computed(() => authApi.getStaff())

const menuItems = [
  {
    path: '/dashboard',
    label: '首页仪表盘',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>'
  },
  {
    path: '/staff',
    label: '员工管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>',
    roles: ['ADMIN']
  },
  {
    path: '/tickets',
    label: '票据列表',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"/></svg>'
  },
  {
    path: '/tickets/passes',
    label: '年/月卡管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>'
  },
  {
    path: '/ota/orders',
    label: 'OTA订单',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/></svg>'
  },
  {
    path: '/logs/entries',
    label: '入园记录',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>'
  },
  {
    path: '/settings',
    label: '系统设置',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg>'
  }
]

const visibleMenuItems = computed(() => {
  const staff = currentStaff.value
  if (!staff) return menuItems.filter(item => !item.roles)
  return menuItems.filter(item => {
    if (!item.roles) return true
    return item.roles.includes(staff.role)
  })
})

const isActive = (path) => route.path === path

const getRoleName = (role) => ({ ADMIN: '管理员', TICKETER: '验票员', FINANCE: '财务', OTA: 'OTA运营' }[role] || role)

const refreshData = () => window.location.reload()

const handleLogout = () => {
  authApi.clear()
  window.location.href = '/login'
}
</script>

<style scoped>
/* Reset & Full Screen */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

body {
  background: var(--bg-main);
}

#app {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.admin-layout {
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
  font-family: 'Noto Serif SC', 'Source Han Serif SC', serif;
  background: var(--bg-main);
}

/* Sidebar - Navigation - Pure Black with Neon Border */
.sidebar {
  background: var(--bg-nav);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid var(--border);
  box-shadow: 4px 0 30px rgba(0, 245, 255, 0.05);
}

/* Scanline overlay effect */
.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(0, 245, 255, 0.01) 2px,
    rgba(0, 245, 255, 0.01) 4px
  );
  pointer-events: none;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 28px 24px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.logo-icon {
  width: 48px;
  height: 48px;
  color: var(--accent);
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.15em;
}

.logo-subtitle {
  font-size: 9px;
  color: var(--text-secondary);
  letter-spacing: 0.3em;
  margin-top: 4px;
  font-family: 'Arial', sans-serif;
}

/* Navigation */
.nav-menu {
  flex: 1;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 24px;
  color: var(--text-secondary);
  text-decoration: none;
  position: relative;
  transition: all 0.25s ease;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.nav-item.active {
  color: var(--text-primary);
  background: var(--bg-selected);
  border-left-color: var(--accent);
}

.nav-item.active .nav-indicator {
  opacity: 1;
  transform: scale(1);
}

.nav-indicator {
  position: absolute;
  left: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
  opacity: 0;
  transform: scale(0);
  transition: all 0.25s ease;
}

.nav-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.05em;
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 20px 24px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--neon-cyan);
  box-shadow: 0 0 10px var(--neon-cyan), 0 0 20px var(--neon-cyan-glow);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px var(--neon-cyan), 0 0 20px var(--neon-cyan-glow); }
  50% { opacity: 0.6; box-shadow: 0 0 5px var(--neon-cyan), 0 0 10px var(--neon-cyan-dim); }
}

/* Main Container */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
  background: var(--bg-main);
}

/* Header */
.header {
  background: var(--bg-secondary);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  height: 64px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb-item {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.1em;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.current-user {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 11px;
  color: var(--text-secondary);
}

.header-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.25s ease;
}

.header-btn:hover {
  background: var(--bg-hover);
  color: var(--accent);
  border-color: var(--accent);
}

.header-btn svg {
  width: 18px;
  height: 18px;
}

/* Main Content */
.main-content {
  background: var(--bg-main);
  padding: 0;
  overflow-y: auto;
  flex: 1;
}
</style>