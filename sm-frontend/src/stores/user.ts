import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { UserStudent, UserAdvisor, UserAdmin, LoginRequest } from '@/api/types'
import { login as loginApi } from '@/api/user'

type UserInfo = UserStudent | UserAdvisor | UserAdmin | null
type UserType = 'student' | 'advisor' | 'admin' | null

// 管理员角色类型
type AdminRole = '宿管管理员' | '图书馆管理员' | '财务处管理员' | '教务管理员' | null

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo>(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const userType = ref<UserType>(localStorage.getItem('userType') as UserType)
  const adminRole = ref<AdminRole>(localStorage.getItem('adminRole') as AdminRole)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const userName = computed(() => userInfo.value?.userName || '')
  const userId = computed(() => userInfo.value?.userId || '')
  const isEducationAdmin = computed(() => adminRole.value === '教务管理员')

  // 登录方法（调用后端登录接口）
  const login = async (loginData: LoginRequest) => {
    const res = await loginApi(loginData)
    
    if (!res.data) {
      throw new Error('登录失败')
    }

    const user = res.data as any
    
    // 保存 JWT Token 和用户信息
    token.value = user.token
    userInfo.value = user
    userType.value = user.userType
    adminRole.value = user.role || null  // 保存管理员角色
    
    localStorage.setItem('token', user.token)
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('userType', user.userType)
    if (user.role) {
      localStorage.setItem('adminRole', user.role)
    }
    
    return user
  }

  // 登出方法
  const logout = () => {
    token.value = null
    userInfo.value = null
    userType.value = null
    adminRole.value = null
    
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userType')
    localStorage.removeItem('adminRole')
  }

  // 更新用户信息
  const updateUserInfo = (info: UserInfo) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  }

  return {
    token,
    userInfo,
    userType,
    adminRole,
    isLoggedIn,
    userName,
    userId,
    isEducationAdmin,
    login,
    logout,
    updateUserInfo
  }
})
