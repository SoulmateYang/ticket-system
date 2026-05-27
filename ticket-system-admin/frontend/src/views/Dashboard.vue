<template>
  <div class="dashboard">
    <!-- Header Section -->
    <header class="dashboard-header">
      <div class="header-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <path d="M4 14l20 10m0 0l20-10m-20 10v20" stroke="currentColor" stroke-width="2"/>
            <circle cx="24" cy="24" r="6" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="brand-text">
          <h1>景区票务中台</h1>
          <p>SCENIC TICKET SYSTEM</p>
        </div>
      </div>
      <div class="header-time">
        <span class="time-label">实时数据</span>
        <span class="time-value">{{ currentTime }}</span>
      </div>
    </header>

    <!-- Stats Section -->
    <section class="stats-section">
      <div class="stat-card" @mouseenter="animateStat($event)" @mouseleave="resetStat($event)">
        <div class="stat-icon-wrapper">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"/>
            </svg>
          </div>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.totalTickets }}</span>
          <span class="stat-label">总票数</span>
          <div class="stat-trend up">
            <svg viewBox="0 0 12 12" fill="currentColor"><path d="M6 2l4 5H2l4-5z"/></svg>
            <span>+12.5%</span>
          </div>
        </div>
      </div>

      <div class="stat-card" @mouseenter="animateStat($event)" @mouseleave="resetStat($event)">
        <div class="stat-icon-wrapper">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.activePasses }}</span>
          <span class="stat-label">有效年/月卡</span>
          <div class="stat-trend up">
            <svg viewBox="0 0 12 12" fill="currentColor"><path d="M6 2l4 5H2l4-5z"/></svg>
            <span>+8.3%</span>
          </div>
        </div>
      </div>

      <div class="stat-card" @mouseenter="animateStat($event)" @mouseleave="resetStat($event)">
        <div class="stat-icon-wrapper">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
            </svg>
          </div>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pendingOrders }}</span>
          <span class="stat-label">待处理OTA订单</span>
          <div class="stat-trend down">
            <svg viewBox="0 0 12 12" fill="currentColor"><path d="M6 10L2 5h8l-4 5z"/></svg>
            <span>-3.2%</span>
          </div>
        </div>
      </div>

      <div class="stat-card" @mouseenter="animateStat($event)" @mouseleave="resetStat($event)">
        <div class="stat-icon-wrapper">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M9 11a4 4 0 100-8 4 4 0 000 8zM23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
            </svg>
          </div>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.todayEntries }}</span>
          <span class="stat-label">今日入园人数</span>
          <div class="stat-trend up">
            <svg viewBox="0 0 12 12" fill="currentColor"><path d="M6 2l4 5H2l4-5z"/></svg>
            <span>+15.7%</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Content Grid -->
    <section class="content-grid">
      <!-- Quick Actions Panel -->
      <div class="panel quick-actions-panel">
        <div class="panel-header">
          <h2>快捷操作</h2>
        </div>
        <div class="quick-actions">
          <button class="action-btn action-btn-primary" @click="$router.push('/tickets/passes')">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 5v14m-7-7h14"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">创建年/月卡</span>
              <span class="action-desc">录入新会员信息</span>
            </div>
            <div class="action-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14m-7-7l7 7-7 7"/>
              </svg>
            </div>
          </button>

          <button class="action-btn action-btn-success" @click="$router.push('/ota/orders')">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4v16h16M8 12l4 4 4-4"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">OTA订单同步</span>
              <span class="action-desc">同步各大平台订单</span>
            </div>
            <div class="action-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14m-7-7l7 7-7 7"/>
              </svg>
            </div>
          </button>

          <button class="action-btn action-btn-warning" @click="$router.push('/logs/entries')">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">查看入园记录</span>
              <span class="action-desc">核验记录与统计</span>
            </div>
            <div class="action-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14m-7-7l7 7-7 7"/>
              </svg>
            </div>
          </button>
        </div>
      </div>

      <!-- Recent Entries Panel -->
      <div class="panel entries-panel">
        <div class="panel-header">
          <h2>最近入园</h2>
        </div>
        <div class="entries-list">
          <div
            v-for="(entry, index) in recentEntries"
            :key="entry.id"
            class="entry-item"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @mouseenter="highlightEntry($event)"
            @mouseleave="unhighlightEntry($event)"
          >
            <div class="entry-indicator" :class="entry.status"></div>
            <div class="entry-info">
              <span class="entry-name">{{ entry.visitorName }}</span>
              <span class="entry-code">{{ entry.ticketCode }}</span>
            </div>
            <div class="entry-time">{{ entry.entryTime }}</div>
            <div class="entry-status">
              <span class="status-badge" :class="entry.status">
                {{ entry.status === 'SUCCESS' ? '成功' : '失败' }}
              </span>
            </div>
          </div>
        </div>
        <div class="panel-footer">
          <span class="footer-text">实时更新</span>
          <div class="live-dot"></div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="dashboard-footer">
      <div class="footer-left">
        <span class="footer-version">v1.0.0</span>
        <span class="footer-separator">|</span>
        <span class="footer-copyright">景区票务中台管理系统</span>
      </div>
      <div class="footer-right">
        <span class="footer-status">
          <span class="status-dot"></span>
          系统运行正常
        </span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const currentTime = ref('')
let timeInterval = null

const stats = ref({
  totalTickets: 1256,
  activePasses: 342,
  pendingOrders: 5,
  todayEntries: 89
})

const recentEntries = ref([
  { id: 1, ticketCode: 'TK-20260526001', visitorName: '张三', entryTime: '09:30', status: 'SUCCESS' },
  { id: 2, ticketCode: 'TK-20260526002', visitorName: '李四', entryTime: '09:45', status: 'SUCCESS' },
  { id: 3, ticketCode: 'TK-20260526003', visitorName: '王五', entryTime: '10:00', status: 'SUCCESS' },
  { id: 4, ticketCode: 'TK-20260526004', visitorName: '赵六', entryTime: '10:15', status: 'SUCCESS' },
  { id: 5, ticketCode: 'TK-20260526005', visitorName: '钱七', entryTime: '10:30', status: 'FAILED' }
])

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const animateStat = (e) => e.currentTarget.classList.add('stat-animated')
const resetStat = (e) => e.currentTarget.classList.remove('stat-animated')
const highlightEntry = (e) => e.currentTarget.classList.add('entry-highlighted')
const unhighlightEntry = (e) => e.currentTarget.classList.remove('entry-highlighted')

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
})
</script>

<style scoped>
/* Dashboard Layout */
.dashboard {
  min-height: 100vh;
  width: 100%;
  background: var(--bg-main);
  color: var(--text-primary);
  position: relative;
  overflow: hidden;
  font-family: 'Noto Serif SC', 'Source Han Serif SC', serif;
  display: flex;
  flex-direction: column;
}

/* Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 48px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 20px;
}

.brand-icon {
  width: 56px;
  height: 56px;
  color: var(--primary);
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-text h1 {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.15em;
  margin: 0;
  color: var(--text-primary);
}

.brand-text p {
  font-size: 11px;
  letter-spacing: 0.3em;
  color: var(--text-secondary);
  margin: 4px 0 0 0;
  font-family: 'Arial', sans-serif;
}

.header-time {
  text-align: right;
}

.time-label {
  display: block;
  font-size: 11px;
  color: var(--text-secondary);
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}

.time-value {
  font-size: 16px;
  color: var(--text-primary);
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.05em;
}

/* Stats Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 32px 48px;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 28px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: flex-start;
  gap: 20px;
  animation: stat-card-enter 0.5s ease-out backwards;
}

.stat-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 16px var(--primary-glow);
  transform: translateY(-4px);
}

.stat-animated { transform: scale(1.02); }

.stat-icon-wrapper { flex-shrink: 0; }

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--primary);
  color: #FFFFFF;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon { transform: rotate(-5deg) scale(1.1); }

.stat-icon svg {
  width: 26px;
  height: 26px;
}

.stat-content { flex: 1; min-width: 0; }

.stat-value {
  display: block;
  font-size: 38px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: var(--text-secondary);
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-family: 'Arial', sans-serif;
}

.stat-trend svg { width: 10px; height: 10px; }
.stat-trend.up { color: var(--success); }
.stat-trend.down { color: var(--danger); }

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 0 48px 32px;
  position: relative;
  z-index: 10;
  flex: 1;
  min-height: 0;
}

.panel {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  animation: panel-enter 0.5s ease-out backwards;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.panel-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.1em;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  background: var(--bg-nav);
  border: 1px solid var(--border);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.action-btn-primary::before { background: var(--primary); }
.action-btn-success::before { background: var(--success); }
.action-btn-warning::before { background: var(--warning); }

.action-btn:hover { border-color: var(--primary); transform: translateX(6px); }
.action-btn:hover::before { transform: scaleY(1); }

.action-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
  transition: transform 0.3s ease;
  background: var(--primary);
  color: #FFFFFF;
}

.action-btn-primary .action-icon { background: var(--primary); color: #FFFFFF; }
.action-btn-success .action-icon { background: var(--success); color: #FFFFFF; }
.action-btn-warning .action-icon { background: var(--warning); color: #FFFFFF; }

.action-btn:hover .action-icon { transform: rotate(-5deg) scale(1.05); }

.action-icon svg { width: 24px; height: 24px; }

.action-text { flex: 1; }

.action-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.action-desc {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
}

.action-arrow {
  width: 24px;
  height: 24px;
  color: var(--text-secondary);
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.action-btn:hover .action-arrow { opacity: 1; transform: translateX(0); }
.action-arrow svg { width: 100%; height: 100%; }

/* Entries List */
.entries-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  overflow-y: auto;
}

.entry-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  background: var(--bg-nav);
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.entry-item:hover {
  border-color: var(--primary);
  transform: translateX(4px);
  background: var(--bg-hover);
}

.entry-highlighted {
  background: var(--bg-hover);
  border-color: var(--primary);
}

.entry-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--success);
  flex-shrink: 0;
}

.entry-indicator.FAILED { background: var(--danger); }

.entry-info { flex: 1; min-width: 0; }

.entry-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.entry-code {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'JetBrains Mono', monospace;
}

.entry-time {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'JetBrains Mono', monospace;
  flex-shrink: 0;
}

.status-badge {
  padding: 5px 14px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.status-badge.SUCCESS { background: var(--primary); color: #FFFFFF; }
.status-badge.FAILED { background: var(--danger); color: white; }

.panel-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.footer-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  animation: live-pulse 2s ease-in-out infinite;
}

@keyframes live-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* Footer */
.dashboard-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 48px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-version, .footer-separator, .footer-copyright {
  font-size: 12px;
  color: var(--text-secondary);
}

.footer-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
}

/* Animation Keyframes */
@keyframes stat-card-enter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes panel-enter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>