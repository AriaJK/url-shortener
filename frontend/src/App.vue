<script setup>
import { ref } from 'vue'
import { shortenUrl } from './api/url'

const inputUrl = ref('')
const result = ref('')
const history = ref([]) // 历史记录
const loading = ref(false)
const errorMsg = ref('')

function isValidUrl(url) {
  // 简单URL正则校验
  const pattern = /^(https?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w\-._~:/?#[\]@!$&'()*+,;=]*)?$/i
  return pattern.test(url)
}

const onVisit = (idx) => {
  history.value[idx].count++
  window.open(history.value[idx].shortUrl, '_blank')
}

const handleClick = async () => {
  errorMsg.value = ''
  result.value = ''
  if (!inputUrl.value) {
    errorMsg.value = '请输入URL'
    return
  }
  if (!isValidUrl(inputUrl.value)) {
    errorMsg.value = '请输入有效的URL（如 https://example.com）'
    return
  }
  try {
    loading.value = true
    const res = await shortenUrl(inputUrl.value)
    result.value = res.data.shortUrl
    // 添加到历史记录
    history.value.unshift({
      fullUrl: inputUrl.value,
      shortUrl: res.data.shortUrl,
      count: 0 // 访问次数
    })
    window.alert('短链接生成成功！')
  } catch (e) {
    console.error(e)
    if (e?.response?.data?.message) {
      errorMsg.value = e.response.data.message
    } else {
      errorMsg.value = '请求失败，可能是无效URL或服务器错误'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="padding: 40px; text-align: center;">
    <h1>短链接系统</h1>

    <!-- 输入框 -->
    <input
      v-model="inputUrl"
      :disabled="loading"
      placeholder="请输入长链接，例如 https://google.com"
      style="width: 300px; padding: 8px; margin-right: 10px;"
      @keydown.enter="handleClick"
    />

    <!-- 按钮 -->
    <button @click="handleClick" :disabled="loading" style="min-width: 100px;">
      <span v-if="loading">
        <svg style="width:18px;height:18px;vertical-align:middle;margin-right:5px;animation:spin 1s linear infinite;" viewBox="0 0 24 24">
          <path fill="currentColor" d="M12,4V1L8,5l4,4V6c3.31,0,6,2.69,6,6c0,3.31-2.69,6-6,6c-3.31,0-6-2.69-6-6H4c0,4.42,3.58,8,8,8s8-3.58,8-8S16.42,4,12,4z" />
        </svg>
        生成中...
      </span>
      <span v-else>生成短链接</span>
    </button>

    <!-- 错误提示 -->
    <div v-if="errorMsg" style="color: red; margin-top: 15px;">{{ errorMsg }}</div>

    <!-- 结果 -->
    <div v-if="result" style="margin-top: 20px;">
      <p>短链接：</p>
      <a :href="result" target="_blank">{{ result }}</a>
    </div>

    <!-- 历史记录 -->
    <div v-if="history.length" style="margin: 30px auto 0; max-width: 500px;">
      <h2 style="font-size: 20px; margin-bottom: 10px;">历史记录</h2>
      <div v-for="(item, idx) in history" :key="item.shortUrl" class="history-card">
        <div style="word-break: break-all;">
          <span style="color: #888;">原始链接：</span>{{ item.fullUrl }}
        </div>
        <div>
          <span style="color: #888;">短链接：</span>
          <a :href="item.shortUrl" target="_blank" @click.prevent="onVisit(idx)">{{ item.shortUrl }}</a>
        </div>
        <div style="color: #b469a0; font-size: 13px; margin-top: 2px;">访问次数：{{ item.count }}</div>
      </div>
    </div>
  </div>
</template>

<style>
body {
  background: #ffe4ef;
}
.history-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(180,105,160,0.08);
  padding: 16px 18px;
  margin-bottom: 14px;
  text-align: left;
  border: 1px solid #f3c6de;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>