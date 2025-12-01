// 通用响应类型
export interface Result<T> {
  code: number
  message: string
  data: T
}

// 用户相关类型
export interface UserStudent {
  userId: string
  userName: string
  passwd?: string
  phone: string
  email: string
  status: string
  lastLoginTime: string
  createTime: string
}

export interface UserAdvisor {
  userId: string
  userName: string
  passwd?: string
  phone: string
  email: string
  status: string
  lastLoginTime: string
  createTime: string
}

// 辅导员类型别名
export type Advisor = UserAdvisor

export interface UserAdmin {
  userId: string
  userName: string
  passwd?: string
  role?: string  // 管理员角色
  phone: string
  email: string
  status: string
  lastLoginTime: string
  createTime: string
}

// 登录请求/响应
export interface LoginRequest {
  userId: string
  passwd: string
  userType: 'student' | 'advisor' | 'admin'
}

export interface LoginResponse {
  token: string
  userInfo: UserStudent | UserAdvisor | UserAdmin
  userType: 'student' | 'advisor' | 'admin'
}

// 学生基本信息
export interface StudentBasicInfo {
  stuId: string
  name: string
  gender: '男' | '女'
  idCard: string
  birthDate: string
  nationality: string
  nation: string
  nativePlace: string
  politicalStatus: '群众' | '共青团员' | '中共党员' | '中共预备党员'
  phone: string
}

// 学生学籍信息
export interface StudentStatusInfo {
  stuId: string
  department: string
  major: string
  className: string
  academicStatus: '在读' | '毕业' | '退学' | '休学' | '转学' | '保留学籍'
  admissionDate: string
  graduationDate: string
  warningLevel: '无' | '一级' | '二级' | '三级'
  registerStatus: '已注册' | '未注册'
  advisorId: string
  advisor: string
}

// 奖励信息
export interface Award {
  awardId: string
  stuId: string
  awardType: string
  awardLevel: string
  awardName: string
  awardAmount: number
  issueOrg: string
  applyDate: string
  awardDate: string
  advisorStatus: '待审批' | '已通过' | '未通过'
  advisorOpinion?: string
  adminStatus: '待审批' | '已通过' | '未通过'
  adminOpinion?: string
  awardStatus: '审批中' | '已通过' | '未通过'
}

// 处分信息
export interface Punishment {
  punishmentId: string
  stuId: string
  punishmentType: '警告' | '严重警告' | '记过' | '留校察看' | '开除学籍'
  punishmentReason: string
  issueOrg: string
  applyDate: string
  punishmentDate: string
  applicantId?: string    // 申请人ID
  applicantRole?: string  // 申请人角色
  adminStatus: '待审批' | '已通过' | '未通过'
  adminOpinion?: string
  punishmentStatus: '审批中' | '已生效' | '申诉中' | '已撤销'
}

// 申诉信息
export interface Appeal {
  appealId: string
  punishmentId: string
  stuId: string
  appealReason: string
  appealDate: string
  advisorStatus: '待审理' | '已通过' | '未通过'
  advisorOpinion?: string
  adminStatus: '待审理' | '已通过' | '未通过'
  adminOpinion?: string
  appealStatus: '受理中' | '已通过' | '未通过'
}

// 学籍变动
export interface StatusChange {
  changeId: string
  stuId: string
  changeType: '转专业' | '交换' | '退学' | '休学' | '复学' | '转学'
  changeReason: string
  currentSchool: string
  currentCollege: string
  currentMajor: string
  targetSchool: string
  targetCollege: string
  targetMajor: string
  applyDate: string
  startDate: string
  endDate: string
  advisorId: string
  advisorStatus: '待审核' | '已通过' | '未通过'
  advisorOpinion: string
  adminId: string
  adminStatus: '待审核' | '已通过' | '未通过'
  adminOpinion: string
  applyStatus: '待审核' | '已通过' | '未通过'
}

// 离校手续
export interface LeaveSchool {
  leaveId: string
  stuId: string
  leaveType: '毕业' | '退学' | '休学' | '转学' | '交换'
  leaveReason: string
  applyDate: string
  leaveDate: string
  dormitoryReviewerId: string
  dormitoryStatus: '待审核' | '已通过' | '未通过'
  dormitoryOpinion?: string
  libraryReviewerId: string
  libraryStatus: '待审核' | '已通过' | '未通过'
  libraryOpinion?: string
  financeReviewerId: string
  financeStatus: '待审核' | '已通过' | '未通过'
  financeOpinion?: string
  adminId: string
  adminStatus: '待审核' | '已通过' | '未通过'
  adminOpinion?: string
  overallStatus: '审核中' | '已通过' | '未通过'
}

// 系统通知
export interface Notice {
  noticeId: string
  title: string
  content: string
  noticeType: string
  targetUser: string
  targetType: 'student' | 'advisor'
  publishUser: string
  publishUserId: string
  publishTime: string
  priority: '普通' | '重要'
  isRead: boolean
  readTime: string | null
}

// 操作日志（包含登录日志）
export interface Log {
  logId: string
  userId: string
  userName: string
  userType?: string           // 用户类型
  operation: string
  operationDetail: string
  targetType?: string         // 操作对象类型
  targetId?: string           // 操作对象ID
  oldValue?: string           // 变更前的值
  newValue?: string           // 变更后的值
  ipAddress?: string          // IP地址
  userAgent?: string          // 浏览器/设备信息
  requestUrl?: string         // 请求URL
  requestMethod?: string      // 请求方法
  operationTime: string
  result: boolean
  errorMessage?: string       // 错误信息
}

// 附件
export interface Attachment {
  attachmentId: string
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  relatedId: string
  relatedType: 'award' | 'punishment' | 'statusChange' | 'leaveSchool'
  uploadUserId: string
  uploadTime: string
}

// 分页响应类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 分页查询参数
export interface PageParams {
  pageNum?: number
  pageSize?: number
}
