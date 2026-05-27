<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <circle cx="24" cy="24" r="8" stroke="currentColor" stroke-width="2.5"/>
            <circle cx="24" cy="24" r="3" fill="currentColor"/>
          </svg>
        </div>
        <h1>景区票务中台</h1>
        <p>SCENIC TICKET SYSTEM</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-field">
          <label>手机号</label>
          <input
            v-model="form.phone"
            type="tel"
            placeholder="请输入手机号"
            autocomplete="tel"
          />
        </div>

        <div class="form-field">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>

        <div class="error-message" v-if="error">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ error }}
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="login-footer">
        <p>默认账号: ADMIN001 / admin123</p>
      </div>
    </div>

    <!-- Background decoration -->
    <div class="bg-grid"></div>
    <div class="bg-glow"></div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  phone: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.phone || !form.password) {
    error.value = '请输入手机号和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await authApi.login({
      phone: form.phone,
      password: form.password
    })

    const { token, staff } = response.data
    authApi.setToken(token)
    authApi.setStaff(staff)

    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-main);
  position: relative;
  overflow: hidden;
  font-family: 'Noto Serif SC', serif;
}

.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(27, 77, 137, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(27, 77, 137, 0.04) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(27, 77, 137, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.login-container {
  width: 420px;
  max-width: 90vw;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 48px;
  position: relative;
  z-index: 1;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.04), 0 12px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  color: var(--primary);
  margin: 0 auto 20px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.15em;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 10px;
  color: var(--text-secondary);
  letter-spacing: 0.3em;
  font-family: 'Arial', sans-serif;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field label {
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 0.1em;
}

.form-field input {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-main);
  color: var(--text-primary);
  transition: border-color 0.3s ease;
}

.form-field input::placeholder {
  color: var(--text-muted);
}

.form-field input:focus {
  outline: none;
  border-color: var(--primary);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 45, 106, 0.1);
  border: 1px solid var(--danger);
  border-radius: 6px;
  font-size: 13px;
  color: var(--danger);
}

.error-message svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.login-btn {
  padding: 14px;
  background: var(--primary);
  color: #FFFFFF;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.2em;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
}

.login-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-glow);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top-color: var(--bg-main);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.login-footer p {
  font-size: 11px;
  color: var(--text-muted);
  font-family: 'JetBrains Mono', monospace;
}
</style>