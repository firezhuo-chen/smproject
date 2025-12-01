import { request } from '@/utils/request'
import type { Result, LoginRequest, LoginResponse, UserStudent, UserAdvisor, UserAdmin } from './types'

// 用户登录（临时实现，后端需要添加登录接口）
export const login = (data: LoginRequest): Promise<Result<LoginResponse>> => {
  return request.post('/login', data)
}

// 学生用户相关
export const getUserStudentList = (): Promise<Result<UserStudent[]>> => {
  return request.get('/userStudent')
}

export const getUserStudentById = (id: string): Promise<Result<UserStudent>> => {
  return request.get(`/userStudent/${id}`)
}

export const addUserStudent = (data: UserStudent): Promise<Result<boolean>> => {
  return request.post('/userStudent', data)
}

export const updateUserStudent = (data: UserStudent): Promise<Result<boolean>> => {
  return request.put('/userStudent', data)
}

export const deleteUserStudent = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/userStudent/${id}`)
}

// 辅导员用户相关
export const getUserAdvisorList = (): Promise<Result<UserAdvisor[]>> => {
  return request.get('/userAdvisor')
}

export const getUserAdvisorById = (id: string): Promise<Result<UserAdvisor>> => {
  return request.get(`/userAdvisor/${id}`)
}

// 管理员用户相关
export const getUserAdminList = (): Promise<Result<UserAdmin[]>> => {
  return request.get('/userAdmin')
}

export const getUserAdminById = (id: string): Promise<Result<UserAdmin>> => {
  return request.get(`/userAdmin/${id}`)
}
