import { request } from '@/utils/request'
import type { Result, Award, Punishment, Appeal, StatusChange, LeaveSchool, Notice, Log, PageResult } from './types'

// 奖励相关
export const getAwardList = (): Promise<Result<Award[]>> => {
  return request.get('/award')
}

export const getAwardById = (id: string): Promise<Result<Award>> => {
  return request.get(`/award/${id}`)
}

export const addAward = (data: Award): Promise<Result<boolean>> => {
  return request.post('/award', data)
}

export const updateAward = (data: Award): Promise<Result<boolean>> => {
  return request.put('/award', data)
}

export const deleteAward = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/award/${id}`)
}

// 奖励分页查询
export const getAwardPage = (params: {
  pageNum?: number
  pageSize?: number
  stuId?: string
  awardStatus?: string
}): Promise<Result<PageResult<Award>>> => {
  return request.get('/award/page', { params })
}

// 处分相关
export const getPunishmentList = (): Promise<Result<Punishment[]>> => {
  return request.get('/punishment')
}

export const getPunishmentById = (id: string): Promise<Result<Punishment>> => {
  return request.get(`/punishment/${id}`)
}

export const addPunishment = (data: Punishment): Promise<Result<boolean>> => {
  return request.post('/punishment', data)
}

export const updatePunishment = (data: Punishment): Promise<Result<boolean>> => {
  return request.put('/punishment', data)
}

export const deletePunishment = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/punishment/${id}`)
}

// 处分分页查询
export const getPunishmentPage = (params: {
  pageNum?: number
  pageSize?: number
  stuId?: string
  punishmentStatus?: string
}): Promise<Result<PageResult<Punishment>>> => {
  return request.get('/punishment/page', { params })
}

// 申诉相关
export const getAppealList = (): Promise<Result<Appeal[]>> => {
  return request.get('/appeal')
}

export const getAppealById = (id: string): Promise<Result<Appeal>> => {
  return request.get(`/appeal/${id}`)
}

export const addAppeal = (data: Appeal): Promise<Result<boolean>> => {
  return request.post('/appeal', data)
}

export const updateAppeal = (data: Appeal): Promise<Result<boolean>> => {
  return request.put('/appeal', data)
}

export const deleteAppeal = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/appeal/${id}`)
}

// 申诉分页查询
export const getAppealPage = (params: {
  pageNum?: number
  pageSize?: number
  stuId?: string
  appealStatus?: string
}): Promise<Result<PageResult<Appeal>>> => {
  return request.get('/appeal/page', { params })
}

// 学籍变动相关
export const getStatusChangeList = (): Promise<Result<StatusChange[]>> => {
  return request.get('/statusChange')
}

export const getStatusChangeById = (id: string): Promise<Result<StatusChange>> => {
  return request.get(`/statusChange/${id}`)
}

export const addStatusChange = (data: StatusChange): Promise<Result<boolean>> => {
  return request.post('/statusChange', data)
}

export const updateStatusChange = (data: StatusChange): Promise<Result<boolean>> => {
  return request.put('/statusChange', data)
}

export const deleteStatusChange = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/statusChange/${id}`)
}

// 学籍变动分页查询
export const getStatusChangePage = (params: {
  pageNum?: number
  pageSize?: number
  stuId?: string
  applyStatus?: string
}): Promise<Result<PageResult<StatusChange>>> => {
  return request.get('/statusChange/page', { params })
}

// 离校手续相关
export const getLeaveSchoolList = (): Promise<Result<LeaveSchool[]>> => {
  return request.get('/leaveSchool')
}

export const getLeaveSchoolById = (id: string): Promise<Result<LeaveSchool>> => {
  return request.get(`/leaveSchool/${id}`)
}

export const addLeaveSchool = (data: LeaveSchool): Promise<Result<boolean>> => {
  return request.post('/leaveSchool', data)
}

export const updateLeaveSchool = (data: LeaveSchool): Promise<Result<boolean>> => {
  return request.put('/leaveSchool', data)
}

export const deleteLeaveSchool = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/leaveSchool/${id}`)
}

// 离校手续分页查询
export const getLeaveSchoolPage = (params: {
  pageNum?: number
  pageSize?: number
  stuId?: string
  overallStatus?: string
}): Promise<Result<PageResult<LeaveSchool>>> => {
  return request.get('/leaveSchool/page', { params })
}

// 通知相关
export const getNoticeList = (): Promise<Result<Notice[]>> => {
  return request.get('/notice')
}

export const getNoticeById = (id: string): Promise<Result<Notice>> => {
  return request.get(`/notice/${id}`)
}

export const addNotice = (data: Notice): Promise<Result<boolean>> => {
  return request.post('/notice', data)
}

export const updateNotice = (data: Notice): Promise<Result<boolean>> => {
  return request.put('/notice', data)
}

export const deleteNotice = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/notice/${id}`)
}

// 标记通知为已读
export const markNoticeAsRead = (id: string): Promise<Result<boolean>> => {
  return request.put(`/notice/${id}/read`)
}

// 批量标记通知为已读
export const markAllNoticesAsRead = (userId: string): Promise<Result<boolean>> => {
  return request.put('/notice/read-all', null, { params: { userId } })
}

// 获取未读通知数量
export const getUnreadNoticeCount = (userId: string): Promise<Result<number>> => {
  return request.get(`/notice/unread-count/${userId}`)
}

// 通知分页查询
export const getNoticePage = (params: {
  pageNum?: number
  pageSize?: number
  targetUser?: string
  noticeType?: string
}): Promise<Result<PageResult<Notice>>> => {
  return request.get('/notice/page', { params })
}

// 日志相关
export const getLogList = (): Promise<Result<Log[]>> => {
  return request.get('/log')
}

export const getLogById = (id: string): Promise<Result<Log>> => {
  return request.get(`/log/${id}`)
}

// 日志分页查询
export const getLogPage = (params: {
  pageNum?: number
  pageSize?: number
  userId?: string
  operation?: string
}): Promise<Result<PageResult<Log>>> => {
  return request.get('/log/page', { params })
}

// 数据导出相关
export const exportStudents = () => {
  const token = localStorage.getItem('token')
  window.open(`/api/export/students?token=${token}`, '_blank')
}

export const exportStatus = () => {
  const token = localStorage.getItem('token')
  window.open(`/api/export/status?token=${token}`, '_blank')
}

export const exportAwards = (stuId?: string) => {
  const token = localStorage.getItem('token')
  const params = stuId ? `?stuId=${stuId}&token=${token}` : `?token=${token}`
  window.open(`/api/export/awards${params}`, '_blank')
}

export const exportPunishments = (stuId?: string) => {
  const token = localStorage.getItem('token')
  const params = stuId ? `?stuId=${stuId}&token=${token}` : `?token=${token}`
  window.open(`/api/export/punishments${params}`, '_blank')
}

// 附件相关
import type { Attachment } from './types'

// 获取附件列表
export const getAttachmentList = (relatedId: string, relatedType: string): Promise<Result<Attachment[]>> => {
  return request.get('/attachment/list', { params: { relatedId, relatedType } })
}

// 上传附件
export const uploadAttachment = (formData: FormData): Promise<Result<Attachment>> => {
  return request.post('/attachment/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除附件
export const deleteAttachment = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/attachment/${id}`)
}

// 获取附件下载URL
export const getAttachmentDownloadUrl = (id: string): string => {
  return `/api/attachment/download/${id}`
}
