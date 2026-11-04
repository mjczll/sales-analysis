import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 15000,
  withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 只有当请求体不是FormData时才设置Content-Type为application/json
    if (config.method === 'post' && !(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json'
    }

    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    // token 失效（401）时清除登录态并跳转登录页
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('menus')
      ElMessage.error('登录已过期，请重新登录')
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        router.push('/login')
      }
      return Promise.reject(error)
    }

    // 根据不同的错误类型显示不同的错误消息
    let errorMsg = '网络错误'
    if (error.response) {
      // 服务器返回错误
      errorMsg = error.response.data?.message || error.response.statusText || '服务器错误'
    } else if (error.request) {
      // 请求已发出但未收到响应
      errorMsg = '服务器无响应，请检查网络连接'
    } else {
      // 请求配置出错
      errorMsg = error.message || '请求错误'
    }

    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default request
