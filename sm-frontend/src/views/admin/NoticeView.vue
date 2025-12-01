<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNoticePage, addNotice, deleteNotice } from '@/api/business'
import { getStudentBasicInfoList, getAdvisorList } from '@/api/student'
import type { Notice, StudentBasicInfo, Advisor } from '@/api/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const noticeList = ref<Notice[]>([])
const studentList = ref<StudentBasicInfo[]>([])
const advisorList = ref<Advisor[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const searchKeyword = ref('')
const filterType = ref('')
const targetType = ref<'student' | 'advisor'>('student')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const noticeTypes = ['注册通知', '奖学金通知', '处分通知', '学籍变动通知', '离校通知', '系统通知', '其他']

// 搜索过滤
const filteredNotices = computed(() => {
  let result = noticeList.value
  if (searchKeyword.value) {
    result = result.filter(n =>
      n.title?.includes(searchKeyword.value) ||
      n.targetUser?.includes(searchKeyword.value)
    )
  }
  if (filterType.value) {
    result = result.filter(n => n.noticeType === filterType.value)
  }
  return result
})

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
  noticeType: '',
  targetUser: '',
  targetType: 'student',
  publishUser: userStore.userInfo?.userName || '管理员',
  publishUserId: userStore.userId,
  publishTime: new Date().toISOString(),
  priority: '普通'
})

const form = ref<Partial<Notice>>(defaultForm())

const rules = ref<FormRules>({
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  targetUser: [{ required: true, message: '请选择目标用户', trigger: 'change' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const [noticeRes, studentRes, advisorRes] = await Promise.all([
      getNoticePage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        noticeType: filterType.value || undefined
      }),
      getStudentBasicInfoList(),
      getAdvisorList()
    ])
    noticeList.value = noticeRes.data?.records || []
    total.value = noticeRes.data?.total || 0
    studentList.value = studentRes.data || []
    advisorList.value = advisorRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

// 分页变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// 监听筛选条件变化
watch(filterType, () => {
  currentPage.value = 1
  fetchData()
})

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
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
    ElMessage.success('通知发布成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
  }
}

const handleDelete = async (row: Notice) => {
  try {
    await ElMessageBox.confirm('确定要删除该通知吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteNotice(row.noticeId)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 用户取消
  }
}

// 获取用户姓名
const getUserName = (userId: string, type: string) => {
  if (type === 'student') {
    const student = studentList.value.find(s => s.stuId === userId)
    return student ? student.name : userId
  } else {
    const advisor = advisorList.value.find(a => a.userId === userId)
    return advisor ? advisor.userName : userId
  }
}

// 目标类型改变时清空目标用户
watch(targetType, () => {
  form.value.targetUser = ''
  form.value.targetType = targetType.value
})

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
            <el-select v-model="filterType" placeholder="通知类型" clearable style="width: 130px; margin-right: 12px;">
              <el-option v-for="t in noticeTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索标题/目标用户" clearable style="width: 180px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              发布通知
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredNotices" v-loading="loading" stripe>
        <el-table-column prop="noticeId" label="通知编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.noticeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.targetType === 'student' ? 'primary' : 'warning'" size="small">
              {{ row.targetType === 'student' ? '学生' : '辅导员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetUser" label="目标用户" width="150">
          <template #default="{ row }">
            {{ row.targetUser }} ({{ getUserName(row.targetUser, row.targetType) }})
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.priority === '重要' ? 'danger' : 'info'" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishUser" label="发布人" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="150">
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
        <el-table-column prop="readTime" label="阅读时间" width="150">
          <template #default="{ row }">
            {{ row.readTime ? formatTime(row.readTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 发布通知对话框 -->
    <el-dialog v-model="dialogVisible" title="发布通知" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知编号">
          <el-input v-model="form.noticeId" disabled />
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择通知类型" style="width: 100%">
            <el-option v-for="type in noticeTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-radio-group v-model="targetType">
            <el-radio value="student">学生</el-radio>
            <el-radio value="advisor">辅导员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标用户" prop="targetUser">
          <el-select v-model="form.targetUser" :placeholder="targetType === 'student' ? '请选择目标学生' : '请选择目标辅导员'" filterable style="width: 100%">
            <template v-if="targetType === 'student'">
              <el-option
                v-for="student in studentList"
                :key="student.stuId"
                :label="`${student.stuId} - ${student.name}`"
                :value="student.stuId"
              />
            </template>
            <template v-else>
              <el-option
                v-for="advisor in advisorList"
                :key="advisor.userId"
                :label="`${advisor.userId} - ${advisor.userName}`"
                :value="advisor.userId"
              />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="普通">普通</el-radio>
            <el-radio value="重要">重要</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请输入通知内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">发布</el-button>
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
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
