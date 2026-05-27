<template>
  <div class="settings">
    <el-card>
      <template #header>
        <span>系统设置</span>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="basicConfig.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="景区名称">
              <el-input v-model="basicConfig.scenicName" placeholder="请输入景区名称" />
            </el-form-item>
            <el-form-item label="开放时间">
              <el-time-picker
                v-model="basicConfig.openTime"
                placeholder="选择开放时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
            <el-form-item label="关闭时间">
              <el-time-picker
                v-model="basicConfig.closeTime"
                placeholder="选择关闭时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="OTA配置" name="ota">
          <el-form :model="otaConfig" label-width="120px">
            <el-divider>美团</el-divider>
            <el-form-item label="App ID">
              <el-input v-model="otaConfig.meituan.appId" placeholder="请输入美团App ID" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="otaConfig.meituan.appSecret" placeholder="请输入美团App Secret" show-password />
            </el-form-item>
            <el-form-item label="同步间隔">
              <el-input-number v-model="otaConfig.meituan.syncInterval" :min="1" :max="60" />
              <span class="form-tip">分钟</span>
            </el-form-item>

            <el-divider>抖音</el-divider>
            <el-form-item label="App ID">
              <el-input v-model="otaConfig.douyin.appId" placeholder="请输入抖音App ID" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="otaConfig.douyin.appSecret" placeholder="请输入抖音App Secret" show-password />
            </el-form-item>
            <el-form-item label="同步间隔">
              <el-input-number v-model="otaConfig.douyin.syncInterval" :min="1" :max="60" />
              <span class="form-tip">分钟</span>
            </el-form-item>

            <el-divider>携程</el-divider>
            <el-form-item label="API Key">
              <el-input v-model="otaConfig.ctrip.apiKey" placeholder="请输入携程API Key" />
            </el-form-item>
            <el-form-item label="同步间隔">
              <el-input-number v-model="otaConfig.ctrip.syncInterval" :min="1" :max="60" />
              <span class="form-tip">分钟</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveOtaConfig">保存配置</el-button>
              <el-button @click="testOtaConnection">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="核验设置" name="verify">
          <el-form :model="verifyConfig" label-width="120px">
            <el-form-item label="启用扫码核验">
              <el-switch v-model="verifyConfig.enableScan" />
            </el-form-item>
            <el-form-item label="启用人脸核验">
              <el-switch v-model="verifyConfig.enableFace" />
            </el-form-item>
            <el-form-item label="人脸相似度阈值">
              <el-slider
                v-model="verifyConfig.faceThreshold"
                :min="0.5"
                :max="1"
                :step="0.01"
                show-stops
              />
              <span>{{ (verifyConfig.faceThreshold * 100).toFixed(0) }}%</span>
            </el-form-item>
            <el-form-item label="重复核验间隔">
              <el-input-number v-model="verifyConfig.verifyInterval" :min="0" :max="300" />
              <span class="form-tip">秒（0表示不限制）</span>
            </el-form-item>
            <el-form-item label="核验成功音效">
              <el-select v-model="verifyConfig.successSound" placeholder="请选择音效">
                <el-option label="提示音1" value="success1" />
                <el-option label="提示音2" value="success2" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item label="核验失败音效">
              <el-select v-model="verifyConfig.failSound" placeholder="请选择音效">
                <el-option label="失败音1" value="fail1" />
                <el-option label="失败音2" value="fail2" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveVerifyConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="系统维护" name="maintenance">
          <el-form label-width="120px">
            <el-form-item label="数据备份">
              <el-button @click="backupData">手动备份</el-button>
              <el-button @click="autoBackup">设置自动备份</el-button>
            </el-form-item>
            <el-form-item label="日志导出">
              <el-button @click="exportLogs">导出日志</el-button>
            </el-form-item>
            <el-form-item label="清空缓存">
              <el-button type="danger" @click="clearCache">清空缓存</el-button>
            </el-form-item>
            <el-form-item label="系统信息">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
                <el-descriptions-item label="数据库版本">MySQL 8.0</el-descriptions-item>
                <el-descriptions-item label="Java版本">17</el-descriptions-item>
                <el-descriptions-item label="Spring Boot">3.2.0</el-descriptions-item>
              </el-descriptions>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicConfig = reactive({
  systemName: '景区票务中台系统',
  scenicName: '某景区',
  openTime: '09:00',
  closeTime: '17:00'
})

const otaConfig = reactive({
  meituan: {
    appId: '',
    appSecret: '',
    syncInterval: 5
  },
  douyin: {
    appId: '',
    appSecret: '',
    syncInterval: 5
  },
  ctrip: {
    apiKey: '',
    syncInterval: 5
  }
})

const verifyConfig = reactive({
  enableScan: true,
  enableFace: true,
  faceThreshold: 0.85,
  verifyInterval: 5,
  successSound: 'success1',
  failSound: 'fail1'
})

const saveBasicConfig = () => {
  ElMessage.success('基础配置保存成功')
}

const saveOtaConfig = () => {
  ElMessage.success('OTA配置保存成功')
}

const testOtaConnection = () => {
  ElMessage.info('正在测试OTA连接...')
  setTimeout(() => {
    ElMessage.success('连接测试成功')
  }, 1000)
}

const saveVerifyConfig = () => {
  ElMessage.success('核验设置保存成功')
}

const backupData = () => {
  ElMessage.info('正在备份数据...')
  setTimeout(() => {
    ElMessage.success('数据备份成功')
  }, 1500)
}

const autoBackup = () => {
  ElMessage.info('自动备份设置已保存')
}

const exportLogs = () => {
  ElMessage.info('正在导出日志...')
  setTimeout(() => {
    ElMessage.success('日志导出成功')
  }, 1000)
}

const clearCache = () => {
  ElMessage.success('缓存已清空')
}
</script>

<style scoped>
.form-tip {
  margin-left: 10px;
  color: #909399;
}
</style>
