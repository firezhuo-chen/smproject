<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNoticeList, addNotice, markNoticeAsRead, markAllNoticesAsRead } from '@/api/business'
import { getStudentStatusInfoList } from '@/api/student'
import type { Notice, StudentStatusInfo } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const noticeList = ref<Notice[]>([])
const studentList = ref<StudentStatusInfo[]>([])
const selectedNotice = ref<Notice | null>(null)
const detailVisible = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const searchKeyword = ref('')
const filterType = ref('')
const filterRead = ref('')
const activeTab = ref('received')

const noticeTypes = ['注册通知', '奖学金通知', '处分通知', '学籍变动通知', '离校通知', '系统通知', '其他']
const readOptions = [{ label: '未读', value: 'unread' }, { label: '已读', value: 'read' }]

// 生成通知编号
const generateNoticeId = () => {
  const date = new Date()
  const dateStr = date.toISOString().replace(/[-:T]/g, '').substring(0, 14)
  return `N${dateStr}`
}

const defaultForm = (): Partial<Notice> => ({
  noticeId: '',
  title: '',
  content: '',
  noticeType: '系统通知',
  targetUser: '',
  targetType: 'student',
  publishUser: userStore.userInfo?.userName || '辅导员',
  publishUserId: userStore.userId,
  publishTime: new Date().toISOString(),
  priority: '普通'
})

const form = ref<Partial<Notice>>(defaultForm())

const rules = ref<FormRules>({
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  targetUser: [{ required: true, message: '请选择目标学生', trigger: 'change' }]
})

// 我负责的学生
const myStudents = computed(() => {
  return studentList.value.filter(s => s.advisorId === userStore.userId)
})

// 收到的通知（发给我的）
const receivedNotices = computed(() => {
  let result = noticeList.value.filter(n => 
    n.targetUser === userStore.userId && n.targetType === 'advisor'
  )
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(n =>
      n.title?.toLowerCase().includes(keyword) ||
      n.content?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(n => n.noticeType === filterType.value)
  }
  if (filterRead.value === 'unread') {
    result = result.filter(n => !n.isRead)
  } else if (filterRead.value === 'read') {
    result = result.filter(n => n.isRead)
  }
  return result
})

// 发送的通知（我发的）
const sentNotices = computed(() => {
  let result = noticeList.value.filter(n => n.publishUserId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(n =>
      n.title?.toLowerCase().includes(keyword) ||
      n.targetUser?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(n => n.noticeType === filterType.value)
  }
  return result
})

// 未读通知数量
const unreadCount = computed(() => {
  return noticeList.value.filter(n => 
    n.targetUser === userStore.userId && n.targetType === 'advisor' && !n.isRead
  ).length
})

const fetchData = async () => {
  loading.value = true
  try {
    const [noticeRes, studentRes] = await Promise.all([
      getNoticeList(),
      getStudentStatusInfoList()
    ])
    noticeList.value = noticeRes.data || []
    studentList.value = studentRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

const showDetail = async (notice: Notice) => {
  selectedNotice.value = notice
  detailVisible.value = true
  
  // 如果是收到的通知且未读，标记为已读
  if (notice.targetUser === userStore.userId && !notice.isRead) {
    try {
      await markNoticeAsRead(notice.noticeId)
      notice.isRead = true
      notice.readTime = new Date().toISOString()
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

// 全部标记为已读
const handleMarkAllRead = async () => {
  try {
    await markAllNoticesAsRead(userStore.userId)
    noticeList.value.forEach(n => {
      if (n.targetUser === userStore.userId && n.targetType === 'advisor' && !n.isRead) {
        n.isRead = true
        n.readTime = new Date().toISOString()
      }
    })
  } catch (error) {
    console.error('批量标记已读失败', error)
  }
}

const handleAdd = () => {
  form.value = defaultForm()
  form.value.noticeId = generateNoticeId()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addNotice(form.value as Notice)
    ElMessage.success('通知发送成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '发送失败')
  }
}

// 获取学生姓名
const getStudentName = (stuId: string) => {
  const student = studentList.value.find(s => s.stuId === stuId)
  return student ? `${student.stuId}` : stuId
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="notice-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="通知类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in noticeTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-if="activeTab === 'received'" v-model="filterRead" placeholder="读取状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="r in readOptions" :key="r.value" :label="r.label" :value="r.value" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索" clearable style="width: 140px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              发送通知
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane name="received">
          <template #label>
            <span>收到的通知</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 8px;" />
          </template>
          
          <div style="margin-bottom: 12px;" v-if="unreadCount > 0">
            <el-button type="primary" size="small" link @click="handleMarkAllRead">
              全部标记已读
            </el-button>
          </div>
          
          <el-empty v-if="receivedNotices.length === 0 && !loading" description="暂无收到的通知" />
          <div v-else class="notice-list" v-loading="loading">
            <div
              v-for="notice in receivedNotices"
              :key="notice.noticeId"
              class="notice-item"
              :class="{ 'unread': !notice.isRead }"
              @click="showDetail(notice)"
            >
              <div class="notice-header">
                <div class="notice-title">
                  <el-badge v-if="!notice.isRead" is-dot style="margin-right: 8px;" />
                  <el-tag v-if="notice.priority === '重要'" type="danger" size="small" style="margin-right: 8px;">重要</el-tag>
                  <span :style="{ fontWeight: notice.isRead ? 'normal' : 'bold' }">{{ notice.title }}</span>
                </div>
                <span class="notice-time">{{ formatTime(notice.publishTime) }}</span>
              </div>
              <div class="notice-content">
                {{ notice.content.substring(0, 100) }}{{ notice.content.length > 100 ? '...' : '' }}
              </div>
              <div class="notice-footer">
                <el-tag size="small" type="info">{{ notice.noticeType }}</el-tag>
                <span class="notice-publisher">发布人：{{ notice.publishUser }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="发送的通知" name="sent">
          <el-empty v-if="sentNotices.length === 0 && !loading" description="暂无发送的通知" />
          <el-table v-else :data="sentNotices" v-loading="loading" stripe>
            <el-table-column prop="noticeId" label="通知编号" width="160" />
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="noticeType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.noticeType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetUser" label="目标学生" width="120">
              <template #default="{ row }">
                {{ getStudentName(row.targetUser) }}
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发送时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.publishTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="isRead" label="阅读状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'success' : 'danger'" size="small">
                  {{ row.isRead ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <template v-if="selectedNotice">
        <div class="detail-header">
          <h3>
            <el-tag v-if="selectedNotice.priority === '重要'" type="danger" size="small" style="margin-right: 8px;">重要</el-tag>
            {{ selectedNotice.title }}
          </h3>
          <div class="detail-meta">
            <span><el-icon><Clock /></el-icon> {{ formatTime(selectedNotice.publishTime) }}</span>
            <span><el-icon><User /></el-icon> {{ selectedNotice.publishUser }}</span>
            <el-tag size="small" type="info">{{ selectedNotice.noticeType }}</el-tag>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">{{ selectedNotice.content }}</div>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发送通知对话框 -->
    <el-dialog v-model="dialogVisible" title="发送通知" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择通知类型" style="width: 100%">
            <el-option v-for="type in noticeTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标学生" prop="targetUser">
          <el-select v-model="form.targetUser" placeholder="请选择目标学生（仅限您负责的学生）" filterable style="width: 100%">
            <el-option
              v-for="student in myStudents"
              :key="student.stuId"
              :label="`${student.stuId} - ${student.major} - ${student.className}`"
              :value="student.stuId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="普通">普通</el-radio>
            <el-radio value="重要">重要</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.notice-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
  }
  .header-actions {
    display: flex;
    align-items: center;
  }
}

.notice-list {
  .notice-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
    }
    
    &.unread {
      background-color: #f0f9ff;
      border-color: #b3d8ff;
    }
  }
  
  .notice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .notice-title {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
  }
  
  .notice-time {
    font-size: 12px;
    color: #909399;
  }
  
  .notice-content {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 8px;
  }
  
  .notice-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;
  }
}

.detail-header {
  h3 {
    margin: 0 0 12px 0;
    font-size: 18px;
    color: #303133;
  }
  
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 13px;
    color: #909399;
    
    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.detail-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
