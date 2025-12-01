import { ElMessageBox, type ElMessageBoxOptions } from 'element-plus'

/**
 * 通用确认对话框工具
 */

// 确认类型配置
interface ConfirmConfig {
  title: string
  message: string
  confirmText: string
  cancelText: string
  type: 'warning' | 'error' | 'info' | 'success'
  icon?: string
}

// 预设的确认配置
const presets: Record<string, ConfirmConfig> = {
  delete: {
    title: '删除确认',
    message: '此操作将永久删除该数据，是否继续？',
    confirmText: '确认删除',
    cancelText: '取消',
    type: 'error'
  },
  batchDelete: {
    title: '批量删除确认',
    message: '此操作将永久删除选中的所有数据，是否继续？',
    confirmText: '确认删除',
    cancelText: '取消',
    type: 'error'
  },
  revoke: {
    title: '撤回确认',
    message: '确定要撤回该申请吗？撤回后需要重新提交。',
    confirmText: '确认撤回',
    cancelText: '取消',
    type: 'warning'
  },
  approve: {
    title: '审批确认',
    message: '确定通过该申请吗？',
    confirmText: '确认通过',
    cancelText: '取消',
    type: 'info'
  },
  reject: {
    title: '拒绝确认',
    message: '确定拒绝该申请吗？',
    confirmText: '确认拒绝',
    cancelText: '取消',
    type: 'warning'
  },
  logout: {
    title: '退出确认',
    message: '确定要退出登录吗？',
    confirmText: '确认退出',
    cancelText: '取消',
    type: 'warning'
  },
  reset: {
    title: '重置确认',
    message: '确定要重置吗？所有未保存的更改将丢失。',
    confirmText: '确认重置',
    cancelText: '取消',
    type: 'warning'
  },
  submit: {
    title: '提交确认',
    message: '确定要提交吗？提交后将无法修改。',
    confirmText: '确认提交',
    cancelText: '取消',
    type: 'info'
  }
}

/**
 * 显示确认对话框
 * @param type 预设类型或自定义配置
 * @param customMessage 自定义消息（可选，会覆盖预设消息）
 * @returns Promise<boolean> 用户确认返回 true，取消返回 false
 */
export async function confirm(
  type: keyof typeof presets | ConfirmConfig,
  customMessage?: string
): Promise<boolean> {
  const config = typeof type === 'string' ? { ...presets[type] } : type
  
  if (customMessage) {
    config.message = customMessage
  }

  try {
    await ElMessageBox.confirm(config.message, config.title, {
      confirmButtonText: config.confirmText,
      cancelButtonText: config.cancelText,
      type: config.type,
      draggable: true,
      closeOnClickModal: false
    } as ElMessageBoxOptions)
    return true
  } catch {
    return false
  }
}

/**
 * 删除确认
 * @param itemName 要删除的项目名称（可选）
 */
export async function confirmDelete(itemName?: string): Promise<boolean> {
  const message = itemName 
    ? `确定要删除「${itemName}」吗？此操作不可恢复。`
    : '确定要删除该数据吗？此操作不可恢复。'
  return confirm('delete', message)
}

/**
 * 批量删除确认
 * @param count 要删除的数量
 */
export async function confirmBatchDelete(count: number): Promise<boolean> {
  return confirm('batchDelete', `确定要删除选中的 ${count} 条数据吗？此操作不可恢复。`)
}

/**
 * 撤回确认
 * @param itemName 要撤回的项目名称（可选）
 */
export async function confirmRevoke(itemName?: string): Promise<boolean> {
  const message = itemName 
    ? `确定要撤回「${itemName}」吗？撤回后需要重新提交。`
    : '确定要撤回该申请吗？撤回后需要重新提交。'
  return confirm('revoke', message)
}

/**
 * 审批通过确认
 * @param itemName 项目名称（可选）
 */
export async function confirmApprove(itemName?: string): Promise<boolean> {
  const message = itemName 
    ? `确定通过「${itemName}」的申请吗？`
    : '确定通过该申请吗？'
  return confirm('approve', message)
}

/**
 * 审批拒绝确认
 * @param itemName 项目名称（可选）
 */
export async function confirmReject(itemName?: string): Promise<boolean> {
  const message = itemName 
    ? `确定拒绝「${itemName}」的申请吗？`
    : '确定拒绝该申请吗？'
  return confirm('reject', message)
}

/**
 * 退出登录确认
 */
export async function confirmLogout(): Promise<boolean> {
  return confirm('logout')
}

/**
 * 自定义危险操作确认
 * @param title 标题
 * @param message 消息
 * @param confirmText 确认按钮文字
 */
export async function confirmDanger(
  title: string,
  message: string,
  confirmText: string = '确认'
): Promise<boolean> {
  return confirm({
    title,
    message,
    confirmText,
    cancelText: '取消',
    type: 'error'
  })
}

/**
 * 自定义警告操作确认
 * @param title 标题
 * @param message 消息
 * @param confirmText 确认按钮文字
 */
export async function confirmWarning(
  title: string,
  message: string,
  confirmText: string = '确认'
): Promise<boolean> {
  return confirm({
    title,
    message,
    confirmText,
    cancelText: '取消',
    type: 'warning'
  })
}

export default {
  confirm,
  confirmDelete,
  confirmBatchDelete,
  confirmRevoke,
  confirmApprove,
  confirmReject,
  confirmLogout,
  confirmDanger,
  confirmWarning
}
