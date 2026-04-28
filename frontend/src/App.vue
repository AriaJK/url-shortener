<script setup>
import { ref } from 'vue'
import { shortenUrl } from './api/url'

const inputUrl = ref('')
const result = ref('')
const loading = ref(false)

const handleClick = async () => {
  if (!inputUrl.value) {
    alert('请输入URL')
    return
  }

  try {
    loading.value = true
    const res = await shortenUrl(inputUrl.value)
    result.value = res.data.shortUrl
  } catch (e) {
    console.error(e)
    alert('请求失败')
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
      placeholder="请输入长链接，例如 https://google.com"
      style="width: 300px; padding: 8px; margin-right: 10px;"
    />

    <!-- 按钮 -->
    <button @click="handleClick">
      {{ loading ? '生成中...' : '生成短链接' }}
    </button>

    <!-- 结果 -->
    <div v-if="result" style="margin-top: 20px;">
      <p>短链接：</p>
      <a :href="result" target="_blank">{{ result }}</a>
    </div>
  </div>
</template>