import { request } from '@/utils/request'
import type { Result, StudentBasicInfo, StudentStatusInfo, PageResult, Advisor } from './types'

// 学生基本信息
export const getStudentBasicInfoList = (): Promise<Result<StudentBasicInfo[]>> => {
  return request.get('/studentBasicInfo')
}

export const getStudentBasicInfoById = (id: string): Promise<Result<StudentBasicInfo>> => {
  return request.get(`/studentBasicInfo/${id}`)
}

// 学生基本信息分页查询
export const getStudentBasicInfoPage = (params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
}): Promise<Result<PageResult<StudentBasicInfo>>> => {
  return request.get('/studentBasicInfo/page', { params })
}

export const addStudentBasicInfo = (data: StudentBasicInfo): Promise<Result<boolean>> => {
  return request.post('/studentBasicInfo', data)
}

export const updateStudentBasicInfo = (data: StudentBasicInfo): Promise<Result<boolean>> => {
  return request.put('/studentBasicInfo', data)
}

export const deleteStudentBasicInfo = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/studentBasicInfo/${id}`)
}

// 学生学籍信息
export const getStudentStatusInfoList = (): Promise<Result<StudentStatusInfo[]>> => {
  return request.get('/studentStatusInfo')
}

export const getStudentStatusInfoById = (id: string): Promise<Result<StudentStatusInfo>> => {
  return request.get(`/studentStatusInfo/${id}`)
}

// 学籍信息分页查询
export const getStudentStatusInfoPage = (params: {
  pageNum?: number
  pageSize?: number
  advisorId?: string
  academicStatus?: string
  department?: string
}): Promise<Result<PageResult<StudentStatusInfo>>> => {
  return request.get('/studentStatusInfo/page', { params })
}

export const addStudentStatusInfo = (data: StudentStatusInfo): Promise<Result<boolean>> => {
  return request.post('/studentStatusInfo', data)
}

export const updateStudentStatusInfo = (data: StudentStatusInfo): Promise<Result<boolean>> => {
  return request.put('/studentStatusInfo', data)
}

export const deleteStudentStatusInfo = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/studentStatusInfo/${id}`)
}

// 辅导员信息
export const getAdvisorList = (): Promise<Result<Advisor[]>> => {
  return request.get('/userAdvisor')
}
