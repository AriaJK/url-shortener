import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',
})

// 注册
export function register(username, password) {
  return request.post('/api/auth/register', { username, password })
}

// 登录
export function login(username, password) {
  return request.post('/api/auth/login', { username, password })
}

// 获取我的短链接列表
export function getMyUrls(token) {
  return request.get('/api/url/my', {
    headers: { Authorization: `Bearer ${token}` },
  })
}

// 创建短链接（支持自定义后缀）
export function createShortUrl(fullUrl, token) {
  return request.post('/api/url/create', null, {
    params: { fullUrl },
    headers: { Authorization: `Bearer ${token}` },
  })
}

// 删除短链接
export function deleteUrl(id, token) {
  return request.delete(`/api/url/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
}

// 编辑短链接
export function editUrl(id, newFullUrl, token) {
  return request.put(`/api/url/${id}`, null, {
    params: { newFullUrl },
    headers: { Authorization: `Bearer ${token}` },
  })
}
