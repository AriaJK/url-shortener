<script setup>
import { ref, onMounted } from 'vue'
import {
  register,
  login,
  getMyUrls,
  createShortUrl,
  deleteUrl,
  editUrl,
  getUrlStats,
} from './api/user'

const inputUrl = ref('')
const result = ref('')
const history = ref([])
const loading = ref(false)
const errorMsg = ref('')
const username = ref('')
const password = ref('')
const token = ref(localStorage.getItem('jwt_token') || '')
const apiOrigin = (import.meta.env.VITE_API_BASE || 'http://localhost:8080').replace(/\/$/, '')
const isLogin = ref(!!token.value)
const page = ref(isLogin.value ? 'main' : 'login') // login/register/main
const confirmPassword = ref('')
const captcha = ref('')
const captchaInput = ref('')
const statsById = ref({})

function isValidUrl(url) {
  const pattern = /^(https?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w\-._~:/?#[\]@!$&'()*+,;=]*)?$/i
  return pattern.test(url)
}

const normalizeShortUrl = (shortUrl) => {
  if (!shortUrl) return ''
  if (shortUrl.startsWith('http://') || shortUrl.startsWith('https://')) {
    return shortUrl
  }
  return `${apiOrigin}/${shortUrl.replace(/^\//, '')}`
}

const buildLineChart = (daily) => {
  const width = 240
  const height = 80
  if (!daily || daily.length === 0) {
    return { width, height, path: '', max: 0 }
  }
  const max = Math.max(...daily.map(item => item.count), 1)
  const step = daily.length > 1 ? width / (daily.length - 1) : 0
  const path = daily
    .map((item, index) => {
      const x = index * step
      const y = height - (item.count / max) * height
      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`
    })
    .join(' ')
  return { width, height, path, max }
}

const buildPieChart = (regions) => {
  const total = regions.reduce((sum, item) => sum + item.count, 0)
  if (!total) {
    return { style: '', legend: [] }
  }
  const colors = ['#ff9f9f', '#ffd479', '#8ed1fc', '#b2f7ef', '#c7ceea', '#f3c4fb']
  let current = 0
  const segments = regions.map((item, index) => {
    const start = current
    const end = current + (item.count / total) * 100
    current = end
    const color = colors[index % colors.length]
    return { color, start, end, label: item.region, count: item.count }
  })
  const style = `conic-gradient(${segments
    .map(seg => `${seg.color} ${seg.start}% ${seg.end}%`)
    .join(', ')})`
  const legend = segments.map(seg => ({
    color: seg.color,
    label: seg.label,
    count: seg.count,
    percent: Math.round((seg.count / total) * 100),
  }))
  return { style, legend }
}

const formatRegion = (region) => {
  if (region === 'local') return '局域网'
  if (region === 'public') return '公网'
  if (!region || region === 'unknown') return '未知'
  return region
}

const fetchMyUrls = async () => {
  if (!token.value) return
  try {
    loading.value = true
    const res = await getMyUrls(token.value)
    history.value = res.data.map(item => ({
      ...item,
      displayShortUrl: normalizeShortUrl(item.shortUrl),
      count: 0,
    }))
  } catch (e) {
    errorMsg.value = '获取短链失败，请重新登录'
  } finally {
    loading.value = false
  }
}

function generateCaptcha() {
  captcha.value = String(Math.floor(1000 + Math.random() * 9000));
}

onMounted(() => {
  if (isLogin.value) fetchMyUrls()
  generateCaptcha();
})

const onVisit = (idx) => {
  history.value[idx].count++
  window.open(history.value[idx].displayShortUrl, '_blank')
}

const handleCreate = async () => {
  errorMsg.value = ''
  result.value = ''
  if (!inputUrl.value) {
    errorMsg.value = '请输入URL'
    return
  }
  if (!isValidUrl(inputUrl.value)) {
    errorMsg.value = '请输入有效的URL'
    return
  }
  try {
    loading.value = true
    const res = await createShortUrl(inputUrl.value, token.value)
    result.value = normalizeShortUrl(res.data.shortUrl)
    await fetchMyUrls()
    window.alert('短链接生成成功！')
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || '请求失败，可能是无效URL或服务器错误'
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteUrl(id, token.value)
    await fetchMyUrls()
  } catch (e) {
    errorMsg.value = '删除失败'
  }
}

const handleEdit = async (id) => {
  const newFullUrl = prompt('请输入新的原始链接')
  if (!newFullUrl) return
  try {
    await editUrl(id, newFullUrl, token.value)
    await fetchMyUrls()
  } catch (e) {
    errorMsg.value = '编辑失败'
  }
}

const toggleStats = async (id) => {
  const current = statsById.value[id]
  if (current && current.open) {
    statsById.value[id] = { ...current, open: false }
    return
  }
  if (current && current.data) {
    statsById.value[id] = { ...current, open: true }
    return
  }
  statsById.value[id] = { open: true, loading: true, error: '', data: null }
  try {
    const res = await getUrlStats(id, token.value, 7)
    const data = res.data || {}
    const line = buildLineChart(data.daily || [])
    const pie = buildPieChart(
      (data.regions || []).map(item => ({
        ...item,
        region: formatRegion(item.region),
      }))
    )
    statsById.value[id] = { open: true, loading: false, error: '', data, line, pie }
  } catch (e) {
    statsById.value[id] = { open: true, loading: false, error: '获取统计失败', data: null }
  }
}

const handleRegister = async () => {
  if (!username.value || !password.value || !confirmPassword.value) {
    errorMsg.value = '请输入用户名和两次密码'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次密码输入不一致'
    return
  }
  if (!captchaInput.value || captchaInput.value !== captcha.value) {
    errorMsg.value = '验证码错误'
    generateCaptcha();
    return
  }
  try {
    await register(username.value, password.value)
    errorMsg.value = ''
    page.value = 'login'
    username.value = ''
    password.value = ''
    confirmPassword.value = ''
    captchaInput.value = ''
    generateCaptcha();
    window.alert('注册成功，请登录')
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || '注册失败'
    generateCaptcha();
  }
}

const handleLogin = async () => {
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  try {
    const res = await login(username.value, password.value)
    token.value = res.data.token
    localStorage.setItem('jwt_token', token.value)
    isLogin.value = true
    page.value = 'main'
    errorMsg.value = ''
    await fetchMyUrls()
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || '登录失败'
  }
}

const handleLogout = () => {
  token.value = ''
  isLogin.value = false
  localStorage.removeItem('jwt_token')
  history.value = []
  page.value = 'login'
}
</script>

<template>
  <div class="container">
    <h1 class="main-title">短链接系统</h1>

    <div v-if="page === 'login'" class="card card-center card-spaced">
      <h2>登录</h2>
      <div class="form-group">
        <input v-model="username" placeholder="用户名" class="input" />
        <input v-model="password" type="password" placeholder="密码" class="input" />
        <div class="captcha-row">
          <input v-model="captchaInput" maxlength="4" placeholder="验证码" class="input captcha-input" />
          <span class="captcha-img" @click="generateCaptcha">{{ captcha }}</span>
        </div>
      </div>
      <button @click="handleLogin" class="btn primary btn-block">登录</button>
      <button @click="() => { page = 'register'; errorMsg = '' }" class="btn btn-block btn-secondary">注册</button>
      <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
    </div>

    <div v-else-if="page === 'register'" class="card card-center card-spaced">
      <h2>注册</h2>
      <div class="form-group">
        <input v-model="username" placeholder="用户名" class="input" />
        <input v-model="password" type="password" placeholder="密码" class="input" />
        <input v-model="confirmPassword" type="password" placeholder="确认密码" class="input" />
        <div class="captcha-row">
          <input v-model="captchaInput" maxlength="4" placeholder="验证码" class="input captcha-input" />
          <span class="captcha-img" @click="generateCaptcha">{{ captcha }}</span>
        </div>
      </div>
      <button @click="handleRegister" class="btn primary btn-block">注册</button>
      <button @click="() => { page = 'login'; errorMsg = '' }" class="btn btn-block btn-secondary">返回登录</button>
      <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
    </div>

    <div v-else-if="page === 'main'">
      <div class="top-bar">
        <span style="font-size: 16px; color: #b469a0;">欢迎，{{ username }}</span>
        <button @click="handleLogout" class="btn">退出登录</button>
      </div>
      <div class="card">
        <input
          v-model="inputUrl"
          :disabled="loading"
          placeholder="请输入长链接，例如 https://google.com"
          class="input"
          style="width: 320px; margin-bottom: 10px;"
        />
        <button @click="handleCreate" :disabled="loading" class="btn primary" style="min-width: 120px;">
          <span v-if="loading">生成中...</span>
          <span v-else>生成短链接</span>
        </button>
        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <div v-if="result" class="result">
          <p>短链接：</p>
          <a :href="result" target="_blank">{{ result }}</a>
        </div>
      </div>
      <div v-if="history.length" class="card" style="margin-top: 30px;">
        <h2 style="font-size: 20px; margin-bottom: 10px;">我的短链接</h2>
        <div v-for="(item, idx) in history" :key="item.id" class="history-card">
          <div style="word-break: break-all;">
            <span style="color: #888;">原始链接：</span>{{ item.fullUrl }}
          </div>
          <div>
            <span style="color: #888;">短链接：</span>
            <a :href="item.displayShortUrl" target="_blank" @click.prevent="onVisit(idx)">{{ item.displayShortUrl }}</a>
          </div>
          <div style="color: #b469a0; font-size: 13px; margin-top: 2px;">访问次数：{{ item.count }}</div>
          <button @click="() => handleEdit(item.id)" class="btn">编辑</button>
          <button @click="() => handleDelete(item.id)" class="btn">删除</button>
          <button @click="() => toggleStats(item.id)" class="btn">统计</button>
          <div v-if="statsById[item.id]?.open" class="stats-panel">
            <div v-if="statsById[item.id]?.loading" class="stats-loading">统计加载中...</div>
            <div v-else-if="statsById[item.id]?.error" class="error">{{ statsById[item.id].error }}</div>
            <div v-else class="stats-content">
              <div class="stats-summary">
                <div class="stats-total">总点击：{{ statsById[item.id]?.data?.total || 0 }}</div>
                <div class="stats-sub">近7天趋势</div>
                <svg :width="statsById[item.id].line.width" :height="statsById[item.id].line.height" class="line-chart">
                  <path :d="statsById[item.id].line.path" fill="none" stroke="#b469a0" stroke-width="2" />
                </svg>
              </div>
              <div class="stats-charts">
                <div class="pie-chart" :style="{ background: statsById[item.id].pie.style || '#f1f1f1' }"></div>
                <div class="pie-legend">
                  <div v-for="segment in statsById[item.id].pie.legend" :key="segment.label" class="pie-item">
                    <span class="pie-dot" :style="{ background: segment.color }"></span>
                    <span>{{ segment.label }} {{ segment.percent }}% ({{ segment.count }})</span>
                  </div>
                  <div v-if="!statsById[item.id].pie.legend.length" class="stats-muted">暂无地域数据</div>
                </div>
              </div>
              <div class="stats-recent">
                <div class="stats-sub">最近访问</div>
                <div v-if="!statsById[item.id]?.data?.recent?.length" class="stats-muted">暂无访问记录</div>
                <div v-for="(row, rIdx) in statsById[item.id]?.data?.recent || []" :key="rIdx" class="stats-row">
                  <span>{{ row.clickedAt }}</span>
                  <span>{{ row.ip || 'unknown' }}</span>
                  <span>{{ formatRegion(row.region) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.container {
  min-height: 100vh;
  width: 100vw;
  background: #ffe4ef;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.card-center {
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.card-spaced {
  margin-top: 40px;
  margin-bottom: 40px;
  padding-top: 24px !important;
  padding-bottom: 24px !important;
}
.captcha-row {
  display: flex;
  align-items: center;
  margin-bottom: 0;
  gap: 10px;
}
.captcha-input {
  width: 120px;
}
.captcha-img {
  display: inline-block;
  background: #f3c6de;
  color: #b469a0;
  font-weight: bold;
  font-size: 18px;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  border: 1px solid #e0b7d1;
  transition: background 0.2s;
}
.captcha-img:hover {
  background: #e0b7d1;
}
.main-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 32px;
  letter-spacing: 4px;
}
.card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(180,105,160,0.10);
  padding: 28px 32px 24px 32px;
  margin: 0 auto 18px auto;
  max-width: 420px;
  text-align: left;
  border: 1px solid #f3c6de;
}
.input {
  display: block;
  width: 90%;
  margin: 0 auto 14px auto;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #e0b7d1;
  font-size: 16px;
}
.btn {
  padding: 7px 18px;
  border-radius: 6px;
  border: none;
  background: #f3c6de;
  color: #333;
  font-size: 15px;
  margin-right: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn.primary {
  background: #b469a0;
  color: #fff;
}
.btn:hover {
  background: #e0b7d1;
}
.btn.primary:hover {
  background: #a05a8c;
}
.btn-block {
  width: 100%;
  margin-right: 0;
  margin-bottom: 10px;
}
.btn-secondary {
  background: #f3c6de;
  color: #333;
}
.btn-block + .btn-block {
  margin-top: 6px;
}
.error {
  color: #e74c3c;
  margin-top: 10px;
  font-size: 15px;
}
.result {
  margin-top: 18px;
  font-size: 16px;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 420px;
  margin: 0 auto 18px auto;
}
.history-card {
  background: #f9f6fb;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(180,105,160,0.06);
  padding: 14px 16px;
  margin-bottom: 12px;
  text-align: left;
  border: 1px solid #e0b7d1;
}
.stats-panel {
  margin-top: 8px;
  padding: 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #f3c6de;
}
.stats-loading {
  color: #b469a0;
  font-size: 13px;
}
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stats-summary {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stats-total {
  font-size: 16px;
  font-weight: bold;
  color: #b469a0;
}
.stats-sub {
  font-size: 13px;
  color: #888;
}
.line-chart {
  background: #f8f2f6;
  border-radius: 6px;
  padding: 4px;
}
.stats-charts {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.pie-chart {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 1px solid #f3c6de;
}
.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #666;
}
.pie-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pie-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.stats-recent {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #666;
}
.stats-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
.stats-muted {
  color: #aaa;
  font-size: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
  align-items: center;
}
</style>