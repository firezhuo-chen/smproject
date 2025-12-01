import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api', // 通过 vite 代理转发到后端
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 是否正在跳转登录页（防止重复弹窗）
let isRedirecting = false

// 清除登录状态并跳转登录页
const handleLogout = (message: string = '登录已过期，请重新登录') => {
  if (isRedirecting) return
  isRedirecting = true
  
  // 清除所有登录相关的本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userType')
  localStorage.removeItem('adminRole')
  
  ElMessageBox.alert(message, '提示', {
    confirmButtonText: '重新登录',
    type: 'warning',
    callback: () => {
      isRedirecting = false
      router.replace('/login')
    }
  })
}

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 检查是否有新 Token（无感刷新）
    const newToken = response.headers['x-new-token']
    if (newToken) {
      localStorage.setItem('token', newToken)
      console.log('Token 已自动刷新')
    }
    
    const res = response.data
    // 根据后端 Result 结构判断
    if (res.code !== 200) {
      // 401: 未登录或 token 过期
      if (res.code === 401) {
        handleLogout(res.message || '登录已过期，请重新登录')
        return Promise.reject(new Error(res.message || '登录已过期'))
      }
      
      // 检查消息内容是否包含登录过期相关信息
      const expiredKeywords = ['登录已过期', '请重新登录', 'token', 'Token', '未登录', '身份验证']
      const isExpired = expiredKeywords.some(keyword => res.message?.includes(keyword))
      if (isExpired) {
        handleLogout(res.message)
        return Promise.reject(new Error(res.message))
      }
      
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    // HTTP 状态码 401
    if (error.response?.status === 401) {
      handleLogout('登录已过期，请重新登录')
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}

export default service
