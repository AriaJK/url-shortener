import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080'
})

// 调后端生成短链接口
export function shortenUrl(fullUrl) {
  return request.post('/shorten', {
    fullUrl: fullUrl
  })
}